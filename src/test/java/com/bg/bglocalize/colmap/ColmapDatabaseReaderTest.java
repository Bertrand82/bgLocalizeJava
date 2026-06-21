package com.bg.bglocalize.colmap;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.sql.SQLException;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

class ColmapDatabaseReaderTest {

    private static final File DATABASE_FILE = new File("data/BG/database.db");

    @Test
    void shouldReadImageIdFromColmapDatabaseByName() throws SQLException {
        ColmapDatabaseReader reader = new ColmapDatabaseReader(DATABASE_FILE);

        OptionalInt imageId = reader.findImageIdByName("IMG_20260618_124549.jpg");

        assertTrue(imageId.isPresent());
        assertEquals(1, imageId.getAsInt());
    }
}
