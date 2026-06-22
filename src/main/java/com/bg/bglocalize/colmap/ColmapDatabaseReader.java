package com.bg.bglocalize.colmap;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.OptionalLong;

/**
 * Reads data from a COLMAP SQLite database (database.db).
 */
public final class ColmapDatabaseReader {

    private final String jdbcUrl;

    /**
     * @param databaseFile path to the COLMAP database.db file
     */
    public ColmapDatabaseReader(File databaseFile) {
        Objects.requireNonNull(databaseFile, "databaseFile must not be null");
        if (!databaseFile.isFile()) {
            throw new IllegalArgumentException("COLMAP database file not found: " + databaseFile.getAbsolutePath());
        }
        this.jdbcUrl = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
    }

    /**
     * Returns the image_id for the given image name, or empty if not found.
     *
     * @param name the image file name (e.g. "IMG_20260618_124549.jpg")
     * @return the image_id, or {@link OptionalLong#empty()} if not found
     * @throws SQLException if a database error occurs
     */
    public OptionalLong findImageIdByName(String name) throws SQLException {
        Objects.requireNonNull(name, "name must not be null");
        String sql = "SELECT image_id FROM images WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return OptionalLong.of(rs.getLong("image_id"));
                }
                return OptionalLong.empty();
            }
        }
    }

    /**
     * Returns the image name for the given image_id, or empty if not found.
     *
     * @param imageId the COLMAP image_id
     * @return the image name, or {@code null} if not found
     * @throws SQLException if a database error occurs
     */
    public String findNameByImageId(long imageId) throws SQLException {
        String sql = "SELECT name FROM images WHERE image_id = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, imageId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
                return null;
            }
        }
    }
}
