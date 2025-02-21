package com.ecs160.hw2;

import java.util.List;
import java.util.Scanner;

public class SocialMediaAnalyzerDriver {
    public static void main(String[] args) {
        RedisManager redisManager = new RedisManager();

        // Parse and store JSON data into Redis
        System.out.println("Loading posts and replies from input.json...");
        JsonParserUtility.parseJsonFile(redisManager);
        System.out.println("All posts and replies successfully stored in Redis!");
        //System.out.println(redisManager.getAllPostKeys());


        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\nEnter post ID to fetch (or -1 to exit): ");
            if (!scanner.hasNextInt()) {
                System.out.println("Please enter a valid integer.");
                scanner.next(); // consume the invalid input
                continue;
            }
            int postId = scanner.nextInt();

            if (postId == -1) {
                System.out.println("Exiting program. Goodbye!");
                break;
            }

            Post post = redisManager.getPost(postId);
            if (post != null) {
                System.out.println("\nPost ID: " + post.getPostId());
                System.out.println("> " + post.getPostContent());

                List<Post> replies = post.getReplies();
                if (replies.isEmpty()) {
                    System.out.println("No replies for this post.");
                } else {
                    System.out.println("Replies:");
                    for (Post reply : replies) {
                        System.out.println("  > --> " + reply.getPostContent());
                    }
                }
            } else {
                System.out.println("Post not found in Redis.");
            }
        }

        scanner.close();
        redisManager.close();
    }
}