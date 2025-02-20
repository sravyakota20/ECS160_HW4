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

    // Add a post to Redis
    public void addPost(int postId, String content) {
        String postKey = "post:" + postId;
        jedis.hset(postKey, "postId", String.valueOf(postId));
        jedis.hset(postKey, "postContent", content);
        jedis.hset(postKey, "replyIds", ""); // Initially empty
    }

    // Add a reply to an existing post
    public void addReply(int postId, int replyId, String content) {
        String replyKey = "post:" + replyId;
        jedis.hset(replyKey, "postId", String.valueOf(replyId));
        jedis.hset(replyKey, "postContent", content);
        jedis.hset(replyKey, "replyIds", ""); // Replies-to-replies ignored

        // Update the parent post's reply list
        String postKey = "post:" + postId;
        String existingReplies = jedis.hget(postKey, "replyIds");
        jedis.hset(postKey, "replyIds", existingReplies.isEmpty() ? String.valueOf(replyId) : existingReplies + "," + replyId);
    }

    // Fetch a post from Redis
    public Post getPost(int postId) {
        String postKey = "post:" + postId;
        Map<String, String> postData = jedis.hgetAll(postKey);

        if (postData.isEmpty()) return null; // Post not found

        Post post = new Post(
            Integer.parseInt(postData.get("postId")),
            postData.get("postContent")
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

    // Close the Redis connection
    public void close() {
        jedis.close();
    }
}

