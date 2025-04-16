package ru.spasitel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class Utils {

    public static void nio() {
        long startTime = System.currentTimeMillis();

        AtomicLong count = new AtomicLong();
        String filePath = "F:\\workspace\\ip_addresses_duplicate.crop";
//        String filePath = "F:\\workspace\\ip_addresses";
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.forEach(s -> count.addAndGet(s.length()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("count " + count.get());
        Main.printTime(startTime);
        Main.checkMemory();

    }

    public static void copyToCrop() {
        String filePath = "F:\\workspace\\ip_addresses";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath), 16 * 1024)) {
            FileWriter myWriter = new FileWriter("F:\\workspace\\ip_addresses_duplicate.crop");
            long count = 0;
            long duplicates = 0;
            List<String> copy = new LinkedList<>();
            for (String line; (line = br.readLine()) != null; ) {
                Random random = new Random();
                if (random.nextInt(5) == 0) {
                    copy.add(line);
                }
                if (!copy.isEmpty() && random.nextInt(5) == 0) {
                    String str = copy.remove(random.nextInt(copy.size()));
                    myWriter.write(str + "\n");
                    count++;
                    duplicates++;
                }

                myWriter.write(line + "\n");
                count++;
                if (count > Main.CROP_SIZE) {
                    System.out.println("----------------------------");
                    System.out.println("count " + count + " duplicates: " + duplicates);
                    myWriter.close();
                    return;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void naive() {
        long startTime = System.currentTimeMillis();
        Set<Integer> set = new HashSet<>();

        String filePath = "F:\\workspace\\ip_addresses.crop";
        long countIp = 0;
        long countIpDuplicates = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            long count = 0;
            for (String line; (line = br.readLine()) != null; ) {
                var split = line.split("\\.");
                int result = Integer.MIN_VALUE +
                        Integer.parseInt(split[0]) * 256 * 256 * 256 +
                        Integer.parseInt(split[1]) * 256 * 256 +
                        Integer.parseInt(split[2]) * 256 +
                        Integer.parseInt(split[3]);
                if (set.add(result)) {
                    countIp++;
                } else {
                    countIpDuplicates++;
                }

                count++;
                if (count % Main.CROP_SIZE == 0) {
                    System.out.println("----------------------------");
                    System.out.println("count: " + count + " duplicates: " + countIpDuplicates);
                    Main.printTime(startTime);
                    Main.checkMemory();
                    countIp = 0;
                    countIpDuplicates = 0;
                    set.clear();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        Main.printTime(startTime);
        System.out.println("Result " + countIp);

    }

    public static void read() {
        long startTime = System.currentTimeMillis();
        Set<Integer> set = new HashSet<>();

        String filePath = "F:\\workspace\\ip_addresses.crop";
            long count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            for (String line; (line = br.readLine()) != null; ) {
                count++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("----------------------------");
        System.out.println("count: " + count );
        Main.printTime(startTime);
        Main.checkMemory();
    }

    public static void inputStreamBytes() {
        long startTime = System.currentTimeMillis();

        String filePath = Main.IP_ADDRESSES_DUPLICATE_CROP;
        byte[] buffer = new byte[16 * 1024]; // например, 16 КБ
        int bytesRead;
        long count =0;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
            while ((bytesRead = bis.read(buffer)) != -1) {
                count += bytesRead;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("----------------------------");
        System.out.println("count: " + count );
        Main.printTime(startTime);
        Main.checkMemory();
    }

    public static void main(String[] args) {
        inputStreamBytes();
    }
}
