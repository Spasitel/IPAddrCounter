package ru.spasitel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {

    @Test
    void parseIpv4_v2() {
        assertEquals(0, Main.parseIpv4_v2("0.0.0.0"));
        assertEquals(1, Main.parseIpv4_v2("0.0.0.1"));
        assertEquals(256, Main.parseIpv4_v2("0.0.1.0"));
        assertEquals(256 * 256, Main.parseIpv4_v2("0.1.0.0"));
        assertEquals(256 * 256 * 256, Main.parseIpv4_v2("1.0.0.0"));
        assertEquals(17 * 256 * 256 * 256 + 228 * 256 * 256 + 250 * 256 + 34, Main.parseIpv4_v2("17.228.250.34"));
    }
}