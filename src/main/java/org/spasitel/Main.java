package org.spasitel;

import java.nio.file.Path;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] raw) {
        long startTime = System.currentTimeMillis();

        Args args = parseArgs(raw);

        if (args.help) {
            printHelpAndExit(0);
        }

        Path path = args.file;

        int bufferSize = args.bufferBytes == null ? 128 * 1024 : args.bufferBytes;
        int threads = args.threads == null ? 2 * Runtime.getRuntime().availableProcessors() : args.threads;
        Result result = new UniqueIpCounter(threads, bufferSize).processFile(path);

        if (args.verbosity == 0) {
            logger.info(String.valueOf(result.uniqueIps()));
        } else {
            logger.info("Read total: " + result.totalIps() + " ip addresses");
            logger.info("Found " + result.uniqueIps() + " unique ips");
            if (args.verbosity == 2) {
                printTime(startTime);
                logger.info("Max used memory: " + result.maxMemoryUsage() / (1024 * 1024) + " MB");
            }
        }

    }


    private static void printTime(long startTime) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        logger.info("Time: " + duration + " ms");
    }

    private static Args parseArgs(String[] raw) {
        if (raw.length == 1 && "--help".equals(raw[0])) {
            Args a = new Args();
            a.help = true;
            return a;
        }

        Args a = new Args();
        int i = 0;

        while (i < raw.length && raw[i].startsWith("-")) {
            String opt = raw[i];
            switch (opt) {
                case "-q" -> {
                    a.verbosity = 1;
                    i++;
                }
                case "-qq" -> {
                    a.verbosity = 0;
                    i++;
                }
                case "-b" -> {
                    if (i + 1 >= raw.length) fail("Missing value after -b");
                    a.bufferBytes = toPositiveInt(raw[++i], "-b");
                    i++;
                }
                case "-t" -> {
                    if (i + 1 >= raw.length) fail("Missing value after -t");
                    a.threads = toPositiveInt(raw[++i], "-t");
                    i++;
                }
                default -> fail("Unknown option: " + opt);
            }
        }

        if (i != raw.length - 1) fail("Exactly one file path must follow the options");
        a.file = Path.of(raw[i]);
        return a;
    }

    private static int toPositiveInt(String s, String flag) {
        try {
            int v = Integer.parseInt(s);
            if (v <= 0) throw new NumberFormatException();
            return v;
        } catch (NumberFormatException e) {
            fail(flag + " expects a positive integer, got \"" + s + '"');
            return -1;
        }
    }

    private static void fail(String msg) {
        logger.severe("Error: " + msg);
        printHelpAndExit(1);
    }

    private static void printHelpAndExit(int code) {
        logger.severe("""
                Usage:
                  java -jar app.jar [--help] [-q|-qq] [-b BYTES] FILE
                
                  --help        show this help and exit
                  -q | -qq      reduce output (default: time+memory, -q: print result, -qq: only number of unique ips)
                  -b BYTES      buffer size in bytes (default: 128KB)
                  -t THREADS    limit of virtual threads (default: double number of processors available to JVM)
                  FILE          required path to the input file
                """);
        System.exit(code);
    }

}
