package com.ecs160.hw2;

import redis.clients.jedis.Jedis;
import java.util.*;

public class RedisManager {
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private Jedis jedis;

    public RedisManager() {
        this.jedis = new Jedis(REDIS_HOST, REDIS_PORT);
    }

    // Add a post to Redis (with createdAt timestamp)
    public void addPost(int postId, String content, String createdAt) {
        String postKey = "post:" + postId;
        jedis.hset(postKey, "postId", String.valueOf(postId));
        jedis.hset(postKey, "postContent", content);
        jedis.hset(postKey, "createdAt", createdAt);
        jedis.hset(postKey, "replyIds", ""); // Initially empty
    }

    // Add a reply to an existing post (with createdAt timestamp)
    public void addReply(int postId, int replyId, String content, String createdAt) {
        String replyKey = "post:" + replyId;
        jedis.hset(replyKey, "postId", String.valueOf(replyId));
        jedis.hset(replyKey, "postContent", content);
        jedis.hset(replyKey, "createdAt", createdAt);
        jedis.hset(replyKey, "replyIds", ""); // Replies-to-replies ignored

        // Update the parent post's reply list
        String postKey = "post:" + postId;
        String existingReplies = jedis.hget(postKey, "replyIds");
        jedis.hset(postKey, "replyIds", existingReplies.isEmpty() ? String.valueOf(replyId) : existingReplies + "," + replyId);
    }

    // Fetch a post from Redis (including replies)
    public Post getPost(int postId) {
        String postKey = "post:" + postId;
        Map<String, String> postData = jedis.hgetAll(postKey);

        if (postData.isEmpty()) return null; // Post not found

        Post post = new Post(
                Integer.parseInt(postData.get("postId")),
                postData.get("postContent"),
                postData.get("createdAt")
        );

        // Load replies
        String replyIdsStr = postData.get("replyIds");
        List<Post> replies = new ArrayList<>();
        if (replyIdsStr != null && !replyIdsStr.isEmpty()) {
            String[] replyIds = replyIdsStr.split(",");
            for (String replyId : replyIds) {
                replies.add(getPost(Integer.parseInt(replyId)));
            }
        }
        post.setReplies(replies);
        return post;
    }

    // Fetch all post keys from Redis
    public List<String> getAllPostKeys() {
        return new ArrayList<>(jedis.keys("post:*"));
    }

    // Get reply IDs associated with a given post
    public List<String> getReplyIds(String postKey) {
        String replyIdsStr = jedis.hget(postKey, "replyIds");
        return (replyIdsStr == null || replyIdsStr.isEmpty()) ? new ArrayList<>() : List.of(replyIdsStr.split(","));
    }

    public List<Reply> getAllReplies() {
        List<Reply> replies = new ArrayList<>();
        List<String> postKeys = getAllPostKeys(); // Get all post keys from Redis

        for (String postKey : postKeys) {
            int parentPostId = Integer.parseInt(postKey.split(":")[1]); // Extract parent post ID

            List<String> replyIds = getReplyIds(postKey); // Get the reply IDs for this post
            for (String replyId : replyIds) {
                Post replyPost = getPost(Integer.parseInt(replyId)); // Fetch the reply post from Redis

                if (replyPost != null) {
                    replies.add(new Reply(
                            replyPost.getPostId(),
                            replyPost.getPostContent(),
                            replyPost.getCreatedAt(),
                            parentPostId  // ✅ Correct parent post ID
                    ));
                }
            }
        }
        return replies;
    }

    public String getPostContent(int postId) {
        Post post = getPost(postId);  // Fetch post object
        return (post != null) ? post.getPostContent() : "";  // Return content safely
    }

    // Close the Redis connection
    public void close() {
        jedis.close();
    }

    public void clearDatabase() {
        jedis.flushDB();
    }
}
