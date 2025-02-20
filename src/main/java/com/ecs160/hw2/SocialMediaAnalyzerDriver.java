package com.ecs160.hw2;

import java.util.List;
import java.util.Scanner;

public class SocialMediaAnalyzerDriver {
    public static void main(String[] args) {
        RedisManager redisManager = new RedisManager();
        Scanner scanner = new Scanner(System.in);

        // Example: Adding posts and replies
        redisManager.addPost(1, "Hello World!");
        redisManager.addReply(1, 2, "This is a reply to post 1");
        redisManager.addReply(1, 3, "Another reply to post 1");

        // Fetch a post
        System.out.print("Enter post ID to fetch: ");
        int postId = scanner.nextInt();
        Post post = redisManager.getPost(postId);
        
        if (post != null) {
            System.out.println("> " + post.getPostContent());
            List<Post> replies = post.getReplies();
            for (Post reply : replies) {
                System.out.println("> --> " + reply.getPostContent());
            }
        } else {
            System.out.println("Post not found!");
        }
        
        // Close Redis connection
        redisManager.close();
        scanner.close();
    }
}

