package ru.spasitel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class Main {
    public static final int CROP_SIZE = 100_000_000;
    public static final String IP_ADDRESSES_DUPLICATE_CROP = "F:\\workspace\\ip_addresses_duplicate.crop";
    public static final String IP_ADDRESSES = "F:\\workspace\\ip_addresses";

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        AtomicBitArray set = new AtomicBitArray(256L * 256 * 256 * 256);

        String filePath = IP_ADDRESSES;
        AtomicLong countIp = new AtomicLong();
        AtomicLong countIpDuplicates = new AtomicLong();
        AtomicLong count = new AtomicLong();
//        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//            for (String line; (line = br.readLine()) != null; ) {
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.parallel().forEach(line -> {
                var split = line.split("\\.");
                long result = Long.parseLong(split[0]) * 256 * 256 * 256 +
                        Long.parseLong(split[1]) * 256 * 256 +
                        Long.parseLong(split[2]) * 256 +
                        Long.parseLong(split[3]);
                while (true) {
                    if (!set.getBit(result)) {
                        if (set.setBit(result, true)) {
                            countIp.getAndIncrement();
                            break;
                        } else {
                            System.out.println("Retry");
                        }
                    } else {
                        countIpDuplicates.getAndIncrement();
                        break;
                    }
                }

                count.getAndIncrement();
                if (count.get() % CROP_SIZE == 0) {
                    System.out.println("---------------------------- " + count);
                    System.out.println("count: " + countIp + " duplicates: " + countIpDuplicates);
                    printTime(startTime);
                    checkMemory();
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }

        printTime(startTime);
        System.out.println("Result " + countIp);

    }


    static void printTime(long startTime) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("Time: " + duration + " ms");
    }

    public static void checkMemory() {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();   // Общий объем памяти, выделенный JVM
        long freeMemory = runtime.freeMemory();       // Свободная память
        long usedMemory = totalMemory - freeMemory;     // Используемая память
        long maxMemory = runtime.maxMemory();         // Максимально доступная память

        System.out.println("Total: " + totalMemory / (1024 * 1024) + " MB");
        System.out.println("Free: " + freeMemory / (1024 * 1024) + " MB");
        System.out.println("Used: " + usedMemory / (1024 * 1024) + " MB");
        System.out.println("Max: " + maxMemory / (1024 * 1024) + " MB");
    }
}