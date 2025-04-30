package org.spasitel;

public record Result(
        long totalIps,
        long uniqueIps,
        long maxMemoryUsage
) {
}
