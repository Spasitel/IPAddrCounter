package org.spasitel;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UniqueIpCounter {
    private final Logger logger = Logger.getLogger(UniqueIpCounter.class.getName());
    private final AtomicBitArray ipBitArray = new AtomicBitArray(256L * 256 * 256 * 256);
    private final AtomicLong uniqueIps = new AtomicLong();
    private final AtomicLong duplicateIps = new AtomicLong();
    private final AtomicLong totalIps = new AtomicLong();
    private final long[] digitIndexMultiplier = {
            1, 10, 100,
            256, 2560, 25600,
            65536, 655360, 6553600,
            16777216, 167772160, 1677721600
    };
    private final Semaphore semaphore;
    private final int bufferSize;
    private long maxMemoryUsage = 0;

    public UniqueIpCounter(int threads, int bufferSize) {
        this.semaphore = new Semaphore(threads);
        this.bufferSize = bufferSize;
    }

    private static void failExecution() {
        //Not the best solution, impossible to test
        //But necessary for performance
        System.exit(1);
    }

    public Result processFile(Path path) {
        try (ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());
             InputStream in = Files.newInputStream(path);
             BufferedInputStream bis = new BufferedInputStream(in, bufferSize)
        ) {
            //part of previous buffer after last '\n'
            byte[] prevStart = new byte[0];
            while (true) {
                semaphore.acquire();
                byte[] buffer = new byte[bufferSize];
                int read = bis.read(buffer);
                if (read == -1) {
                    if (prevStart.length > 0) {
                        processBatch(prevStart, new byte[0], -1);
                    }
                    break;
                }
                byte[] end = findEnd(buffer, read);
                if (prevStart.length > 0 || read > end.length) {
                    byte[] finalPrevStart = prevStart;
                    executor.execute(() -> processBatch(finalPrevStart, buffer, read - end.length - 1));
                }
                prevStart = end;
                checkMemory();
            }
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.severe("Execution was interrupted");
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading file", e);
            failExecution();
        } catch (InterruptedException e) {
            logger.severe("Execution was interrupted");
            failExecution();
        }
        return new Result(totalIps.get(), uniqueIps.get(), maxMemoryUsage);
    }

    private void processBatch(byte[] start, byte[] buffer, int end) {
        int pos = end + start.length;
        long ipNumber = 0;

        int digitIndex = 0;
        int points = 0;

        if (getByte(start, buffer, pos) == '\n') pos--; //file can end with \n or not

        while (pos >= 0) {
            byte b = getByte(start, buffer, pos);
            switch (b) {
                case '\r' -> {
                    //support windows new line
                }
                case 0, -1, -2 -> {
                    //support UTF-16
                }
                case '\n' -> {
                    processIpNumber(ipNumber);
                    digitIndex = 0;
                    points = 0;
                    ipNumber = 0;
                }
                case '.' -> {
                    points++;
                    digitIndex = 0;
                }
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    int a = b - '0';
                    ipNumber += a * digitIndexMultiplier[points * 3 + digitIndex];
                    digitIndex++;
                }
                default -> {
                    logger.severe("Failed to parse file, unsupported byte " + b);
                    failExecution();
                }
            }

            pos--;
        }
        processIpNumber(ipNumber); //parameter "start" and file does not start with '\n'
        semaphore.release();
    }

    private void processIpNumber(long ipNumber) {
        //repeat only if bit was false, but we failed to set it to true
        while (true) {
            if (!ipBitArray.getBit(ipNumber)) {
                if (ipBitArray.setBitTrue(ipNumber)) {
                    uniqueIps.getAndIncrement();
                    break;
                }
            } else {
                duplicateIps.getAndIncrement();
                break;
            }
        }
        totalIps.getAndIncrement();
    }

    private byte getByte(byte[] start, byte[] buffer, int pos) {
        if (pos >= start.length) return buffer[pos - start.length];
        else return start[pos];
    }


    private byte[] findEnd(byte[] buffer, int read) {
        int pos = read - 1;
        while (pos >= 0 && buffer[pos] != '\n') {
            pos--;
        }
        return Arrays.copyOfRange(buffer,
                pos + 1, //return start without '\n'
                read);
    }


    private void checkMemory() {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        if (usedMemory > maxMemoryUsage) {
            maxMemoryUsage = usedMemory;
        }
    }

}
