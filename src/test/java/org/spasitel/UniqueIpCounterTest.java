package org.spasitel;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UniqueIpCounterTest {

    @Test
    @Disabled("Only manual testing in this implementation")
    void processFileWrong() throws URISyntaxException {
        testFile("/wrong.txt", 0, 0);
    }

    @Test
    void processFileUtf16() throws URISyntaxException {
        testFile("/twoUTF16.txt", 2, 2);
    }

    @Test
    void processFileEmpty() throws URISyntaxException {
        testFile("/empty.txt", 0, 0);
    }

    @Test
    void processFileOneIp() throws URISyntaxException {
        testFile("/one.txt", 1, 1);
    }

    @Test
    void processFileOneIpNL() throws URISyntaxException {
        testFile("/oneNL.txt", 1, 1);
    }

    @Test
    void processFileTwoSame() throws URISyntaxException {
        testFile("/twoSame.txt", 2, 1);
    }

    @Test
    void processFileTwoDiffer() throws URISyntaxException {
        testFile("/twoDiffer.txt", 2, 2);
    }

    @Test
    void processFileTen() throws URISyntaxException {
        testFile("/ten.txt", 10, 6);
    }

    //Can be done throw @ParameterizedTest, but not sure if it's better
    void testFile(String resource, int total, int unique) throws URISyntaxException {
        URL url = getClass().getResource(resource);
        assertNotNull(url, "File not found");

        Path path = Paths.get(url.toURI());
        Result result = new UniqueIpCounter(100, 1024).processFile(path);
        assertEquals(total, result.totalIps());
        assertEquals(unique, result.uniqueIps());
    }
}