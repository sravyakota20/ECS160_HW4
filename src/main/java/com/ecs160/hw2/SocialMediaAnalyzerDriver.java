package com.ecs160.hw2;

import com.ecs160.hw2.persistence.Session;
import java.util.List;
import java.util.Scanner;

public class SocialMediaAnalyzerDriver {
    public static void main(String[] args) {
        // Create a new persistence session
        Session session = new Session();

        // Load posts from input.json via the persistence layer
        System.out.println("Loading posts from input.json...");
        List<Post> posts = JsonParserUtility.getPosts();
        // System.out.println("Posts loaded: " + posts.size());
        /*for (Post p : posts) {
            System.out.println(p);
        }*/
        // Add posts to the session and persist them
        for (Post post : posts) {
            session.add(post);
        }
        session.persistAll();

        // Prompt the user for a post ID to fetch via the persistence layer
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

            // Create a minimal Post object with just the ID (requires a constructor in Post.java)
            Post queryPost = new Post(postId);
            Post loadedPost = (Post) session.load(queryPost);

            if (loadedPost != null) {
                System.out.println("\nPost ID: " + loadedPost);
                System.out.println("> " + loadedPost.getPostContent());
                if (loadedPost.getReplies() != null && !loadedPost.getReplies().isEmpty()) {
                    System.out.println("Replies:");
                    for (Reply reply : loadedPost.getReplies()) {
                        System.out.println("  > --> " + reply.getContent());
                    }
                } else {
                    System.out.println("No replies for this post.");
                }
            } else {
                System.out.println("Post not found.");
            }
        }
        scanner.close();
    }
}
