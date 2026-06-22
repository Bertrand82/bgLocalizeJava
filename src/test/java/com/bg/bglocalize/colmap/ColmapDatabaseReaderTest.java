package com.bg.bglocalize.colmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.sql.SQLException;
import java.util.OptionalLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ColmapDatabaseReaderTest {

    private static final File DATABASE_FILE = new File("data/BG/database.db");

    private ColmapDatabaseReader reader;

    @BeforeEach
    void setUp() {
        reader = new ColmapDatabaseReader(DATABASE_FILE);
    }

    @Test
    void shouldFindImageIdByName() throws SQLException {
        OptionalLong result = reader.findImageIdByName("IMG_20260618_124549.jpg");

        assertTrue(result.isPresent(), "image_id should be found");
        assertEquals(1L, result.getAsLong());
    }

    @Test
    void shouldReturnEmptyWhenNameNotFound() throws SQLException {
        OptionalLong result = reader.findImageIdByName("nonexistent_image.jpg");

        assertTrue(result.isEmpty(), "should return empty for unknown name");
    }

    @Test
    void shouldFindNameByImageId() throws SQLException {
        String name = reader.findNameByImageId(1L);

        assertNotNull(name, "name should be found");
        assertEquals("IMG_20260618_124549.jpg", name);
    }

    @Test
    void shouldReturnNullWhenImageIdNotFound() throws SQLException {
        String name = reader.findNameByImageId(999999L);

        assertNull(name, "should return null for unknown image_id");
    }

    @Test
    void shouldRejectMissingDatabaseFile() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ColmapDatabaseReader(new File("data/BG/missing.db")));

        assertTrue(exception.getMessage().contains("COLMAP database file not found"));
    }
}
