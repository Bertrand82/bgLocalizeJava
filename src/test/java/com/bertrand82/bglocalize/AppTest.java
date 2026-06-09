package com.bertrand82.bglocalize;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.opencv.core.Core;

import nu.pattern.OpenCV;

/**
 * Unit test for OpenCV dependencies.
 */
public class AppTest {

    @Test
    public void shouldLoadOpenCvDependencies() {
        assertDoesNotThrow(OpenCV::loadLocally);
        assertEquals("4.9.0", Core.VERSION);
        assertFalse(Core.getVersionString().isBlank());
    }
}
