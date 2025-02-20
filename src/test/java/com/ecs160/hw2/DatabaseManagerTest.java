package com.ecs160.hw2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseManagerTest {
    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() throws SQLException {
        databaseManager = new DatabaseManager();
        initializeDatabase();  // Ensure tables exist before tests
    }

    private void initializeDatabase() throws SQLException {
        String createPostsTableSQL = "CREATE TABLE IF NOT EXISTS posts (" +
                "id SERIAL PRIMARY KEY, " +
                "\"numWords\" INT DEFAULT 0)";

        String createRepliesTableSQL = "CREATE TABLE IF NOT EXISTS replies (" +
                "id SERIAL PRIMARY KEY, " +
                "post_id INT NOT NULL, " +
                "\"numWords\" INT DEFAULT 0, " +
                "\"createdAt\" TEXT, " +
                "FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE)";

        try (Connection conn = databaseManager.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createPostsTableSQL);
            stmt.execute(createRepliesTableSQL);
        }
    }

    @AfterEach
    void tearDown() {
        String truncateDB = "TRUNCATE TABLE posts RESTART IDENTITY CASCADE";
        Connection conn = null;
        try {
            conn = databaseManager.connect();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(truncateDB);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    assertNull(conn);
                }
            }
        }
    }

    @Test
    void testDatabaseManagerConnection() {
        try (Connection connection = databaseManager.connect()) {
            assertNotNull(connection, "Database connection should not be null");
            assertFalse(connection.isClosed(), "Database connection should be open");
        } catch (SQLException e) {
            fail("SQLException was thrown: " + e.getMessage());
        }
    }
}
