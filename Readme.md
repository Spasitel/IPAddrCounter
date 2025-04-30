# IP Address Counter

## Description
A high-performance utility to count the number of **unique IPv4 addresses** in a (potentially huge) text file.  
This project was implemented as a coding exercise based on the specification at [Ecwid’s IP Address Counter assignment](https://github.com/Ecwid/new-job/blob/master/IP-Addr-Counter.md).

## Requirements
- **Java**: JDK 21  
- **Heap memory**:  
  - Minimum: **550 MB**  
  - Recommended: **700 MB+**  
  Actual memory usage depends on buffer size and thread count—see the [Performance](#performance) section.

## Installation

```bash
./gradlew clean jar
```

This will produce a runnable JAR at:
```
build/libs/IPAddrCounter-1.0.jar
```

## Usage

```bash
java -Xmx700m -jar build/libs/IPAddrCounter-1.0.jar [options] FILE
```

```
Usage:
  java -jar IPAddrCounter-1.0.jar [--help] [-q|-qq] [-b BYTES] [-t THREADS] FILE

Options:
  --help            Show this help message and exit.
  -q | -qq          Reduce output verbosity:
                      - Default: print time + memory usage + result
                      - -q: print only the result counts of IPs
                      - -qq: print only the count of unique IPs
  -b BYTES          Buffer size in bytes (default: 128 KB).
  -t THREADS        Maximum number of virtual threads (default: 2 × available processors).
  FILE              Path to the input file (required).
```

## Algorithm
1. **Sequential I/O**  
   The main thread reads the input file in fixed-size byte buffers.  
2. **Parallel Processing**  
   Each buffer is submitted to a pool of Java 21 virtual threads.  
3. **Byte-level Parsing**  
   No intermediate string conversions: each ip address byte sequence is parsed into a 32-bit unsigned integer.
4. **Uniqueness Tracking**  
   A large bitset tracks which IPv4 integers have already been seen.

## Performance
- On my development machine (Intel i7, SSD) with **700 MB** heap and default `-b 128KB`, `-t 2×CPUs`, processing the provided test file took **240 s** (best) to **330 s** (worst) per run.  
- **Recommendations**:  
  - **Heap size**: larger heaps generally improve throughput.  
  - **Buffer size & threads**: there are optimal “sweet spots”—setting either too high or too low may degrade performance.  
  - **Memory footprint** grows roughly proportional to `(number of threads) × (buffer size)`.

## Assumptions & Limitations
- **Character encodings**: UTF-8, UTF-16, CP-* (may not cover all possible encodings).  
- **Output format** and **error handling**: assumes well-formed IPv4 dotted-decimal strings. Invalid addresses (e.g. `127.0.0.999`) are parsed and counted as if valid.  
- These choices were made based on reasonable defaults and can be adjusted—please let me know if any requirements differ.

## Contact
**Developer:** Nikita Minin  
✉️ nikita.minin.dev@gmail.com