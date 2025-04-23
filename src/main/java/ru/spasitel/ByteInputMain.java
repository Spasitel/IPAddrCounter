package ru.spasitel;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class ByteInputMain {
    static AtomicBitArray set = new AtomicBitArray(256L * 256 * 256 * 256);
    static AtomicLong countIp = new AtomicLong();
    static AtomicLong countIpDuplicates = new AtomicLong();
    static AtomicLong count = new AtomicLong();
    private static final Logger logger = Logger.getLogger(ByteInputMain.class.getName());

    public static void fileChannel() {
//        Path path = Path.of(Main.IP_ADDRESSES);
        Path path = Path.of("F:\\workspace\\2ip.txt");
        int bufferSize = 128 * 1024; // 128 КБ
        long startTime = System.currentTimeMillis();


        try (InputStream in = Files.newInputStream(path);
             ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());
             BufferedInputStream bis = new BufferedInputStream(in, bufferSize)
        ) {
            byte[] prevStart = new byte[0]; //after last '\n'
            while (true) {
                byte[] buffer = new byte[bufferSize];
                int read = bis.read(buffer);
                if (read == -1) {
                    if (prevStart.length > 0) {
                        process(prevStart, new byte[0], -1);
                    }
                    break;
                }
                byte[] start = findEnd(buffer, read);
                byte[] finalPrevStart = prevStart;
                executor.execute(
                        () ->
                                process(finalPrevStart, buffer, read - start.length - 1)
                )
                ;
                prevStart = start;
            }
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("---------------------------- " + count);
        logger.info("count: " + countIp + " duplicates: " + countIpDuplicates);
        Main.printTime(startTime);
    }

    private static void process(byte[] start, byte[] buffer, int end) {
        int pos = end + start.length;
        long ipNumber = 0;

        int index = 0;
        int points = 0;

        if (getByte(start, buffer, pos) == '\n')
            pos--; //file can end with \n or not

        while (pos >= 0) {
            byte b = getByte(start, buffer, pos);
            switch (b) {
                case '\r' -> {
                }
                case '\n' -> {
                    countIpNumber(ipNumber);
                    index = 0;
                    points = 0;
                    ipNumber = 0;
                }
                case '.' -> {
                    points++;
                    index = 0;
                }
                default -> {
                    int a = b - '0';
                    ipNumber += a * Main.array[points * 3 + index];
                    index++;
                }
            }

            pos--;
        }
        countIpNumber(ipNumber); //start and file does not start with '\n'
    }

    private static void countIpNumber(long ipNumber) {
        while (true) {
            if (!set.getBit(ipNumber)) {
                if (set.setBitTrue(ipNumber)) {
                    countIp.getAndIncrement();
                    break;
                }
            } else {
                countIpDuplicates.getAndIncrement();
                break;
            }
        }
        count.getAndIncrement();
    }

    private static byte getByte(byte[] start, byte[] buffer, int pos) {
        if (pos >= start.length)
            return buffer[pos - start.length];
        else
            return start[pos];
    }


    private static byte[] findEnd(byte[] buffer, int read) {
        int pos = read - 1;
        while (buffer[pos] != '\n') {
            pos--;
        }
        return Arrays.copyOfRange(buffer,
                pos + 1, //return start without '\n'
                read);
    }

    public static void main(String[] args) {
        fileChannel();

    }
}
