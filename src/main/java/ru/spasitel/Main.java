package ru.spasitel;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    public static final int CROP_SIZE = 100_000_000;
    public static final String IP_ADDRESSES_DUPLICATE_CROP = "F:\\workspace\\ip_addresses_duplicate.crop";
    public static final String IP_ADDRESSES = "F:\\workspace\\ip_addresses";
    public static final int BATCH_SIZE = 1000;
    static long[] array = {1, 10, 100,
            256, 2560, 25600,
            65536, 655360, 6553600,
            16777216, 167772160, 1677721600};

    public static void main(String[] args) {
        String filePath = Main.IP_ADDRESSES;
        countIpInFile(filePath);
    }

    static void processLine(String line, AtomicBitArray set, AtomicLong countIp, AtomicLong countIpDuplicates, AtomicLong count, long startTime) {
        long result = parseIpv4_v2(line);
        while (true) {
            if (!set.getBit(result)) {
                if (set.setBitTrue(result)) {
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

    static long parseIpv4(String line) {
        var split = line.split("\\.");
        long result = Long.parseLong(split[0]) * 256 * 256 * 256 +
                Long.parseLong(split[1]) * 256 * 256 +
                Long.parseLong(split[2]) * 256 +
                Long.parseLong(split[3]);
        return result;
    }

    static long parseIpv4_v2(String line) {
        long result = 0;
        int index = 0;
        int points = 0;
        for (int i = line.length() - 1; i >= 0; i--) {
            char ch = line.charAt(i);
            if (ch == '.') {
                points++;
                index = 0;
            } else {
                int a = ch - '0';
                result += a * array[points * 3 + index];
                index++;
            }
        }
//        if (result != parseIpv4(line)) {
//            System.out.println(line + " " + result + " " + parseIpv4(line));
//        }
        return result;
    }

    static void printTime(long startTime) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log("Time: " + duration + " ms");
    }

    public static void checkMemory() {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();   // Общий объем памяти, выделенный JVM
        long freeMemory = runtime.freeMemory();       // Свободная память
        long usedMemory = totalMemory - freeMemory;     // Используемая память
        long maxMemory = runtime.maxMemory();         // Максимально доступная память

        log("Total: " + totalMemory / (1024 * 1024) + " MB");
        log("Free: " + freeMemory / (1024 * 1024) + " MB");
        log("Used: " + usedMemory / (1024 * 1024) + " MB");
        log("Max: " + maxMemory / (1024 * 1024) + " MB");
    }


    private static void countIpInFile(String filePath) {
        ExecutorService executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory());

        long startTime = System.currentTimeMillis();
        AtomicBitArray set = new AtomicBitArray(256L * 256 * 256 * 256);

        AtomicLong countIp = new AtomicLong();
        AtomicLong countIpDuplicates = new AtomicLong();
        AtomicLong count = new AtomicLong();

        try (BufferedReader br = Files.newBufferedReader(Path.of(filePath))) {
            List<String> batch = new ArrayList<>(BATCH_SIZE);
            String line;
            while ((line = br.readLine()) != null) {
                batch.add(line);
                if (batch.size() == BATCH_SIZE) {
                    processBatch(executor, batch, set, countIp, countIpDuplicates, count, startTime);
                    batch = new ArrayList<>(BATCH_SIZE);
                }
            }
            if (!batch.isEmpty()) {
                processBatch(executor, batch, set, countIp, countIpDuplicates, count, startTime);
            }

        } catch (IOException e) {
            e.printStackTrace();
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

        log("---------------------------- " + count);
        log("count: " + countIp + " duplicates: " + countIpDuplicates);
        printTime(startTime);
        checkMemory();
    }

    private static void processBatch(ExecutorService executor, List<String> batch, AtomicBitArray set, AtomicLong countIp, AtomicLong countIpDuplicates, AtomicLong count, long startTime) {
        executor.execute(() -> {
                    for (String l : batch)
                        processLine(l, set, countIp, countIpDuplicates, count, startTime);
                }
        );
    }

    static void log(String str) {
        System.out.println(str);
    }
}