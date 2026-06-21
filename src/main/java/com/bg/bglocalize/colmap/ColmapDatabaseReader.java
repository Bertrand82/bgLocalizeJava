package com.bg.bglocalize.colmap;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.OptionalInt;

public final class ColmapDatabaseReader {

    private static final String FIND_IMAGE_ID_BY_NAME =
            "SELECT image_id FROM images WHERE name = ?";

    private final File databaseFile;

    public ColmapDatabaseReader(File databaseFile) {
        this.databaseFile = Objects.requireNonNull(databaseFile, "databaseFile must not be null");
    }

    public OptionalInt findImageIdByName(String name) throws SQLException {
        Objects.requireNonNull(name, "name must not be null");

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
             PreparedStatement statement = connection.prepareStatement(FIND_IMAGE_ID_BY_NAME)) {
            statement.setString(1, name);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return OptionalInt.of(resultSet.getInt("image_id"));
                }
                return OptionalInt.empty();
            }
        }
    }
}
