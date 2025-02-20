package com.ecs160.hw2;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseManager {
    private static final Dotenv dotenv = Dotenv.load();

    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");

    // Establish database connection
    Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Insert a new post
    public int addPost(int numWords) {
        String sql = "INSERT INTO posts (\"numWords\") VALUES (?) RETURNING id";
        int postId = -1; // Default value in case of failure

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, numWords);
            stmt.executeUpdate();

            // Retrieve generated post ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    postId = rs.getInt(1); // Get the first column (ID)
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return postId;
    }

    // Insert a reply to a post
    public void addReply(int postId, int numWords, String createdAt) {
        String sql = "INSERT INTO replies (post_id, \"numWords\", \"createdAt\") VALUES (?, ?, ?) RETURNING id";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, numWords);
            stmt.setString(3, createdAt);
            stmt.executeUpdate();
//            System.out.println("Reply added successfully with numWords = " + numWords);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return;
    }

    // get post by id. Return in the format [id, numWords]
    public int[] getPostById(int postId) {
        String sql = "SELECT id, \"numWords\" FROM posts WHERE id = ?";
        int[] postDetails = {-1, -1}; // Default: {-1, -1} if post is not found

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    postDetails[0] = rs.getInt("id");        // Store post ID
                    postDetails[1] = rs.getInt("numWords");  // Store numWords
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return postDetails;
    }

    // Fetch all replies for a given post ID
    public ArrayList<Integer> getRepliesForPost(int postId) {
        ArrayList<Integer> replies = new ArrayList<>();
        String sql = "SELECT \"numWords\" FROM replies WHERE post_id = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    replies.add(rs.getInt("numWords")); // Retrieve word count from DB
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return replies;
    }

    public ArrayList<Reply> getAllReplies() {
        ArrayList<Reply> allReplies = new ArrayList<>();
        String sql = "SELECT id, post_id, \"numWords\", \"createdAt\" FROM replies";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int postId = rs.getInt("post_id");
                int numWords = rs.getInt("numWords");
                String createdAt = rs.getString("createdAt");

                // Create a Reply object and add to the list
                allReplies.add(new Reply(postId, numWords, createdAt));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return allReplies;
    }
}
