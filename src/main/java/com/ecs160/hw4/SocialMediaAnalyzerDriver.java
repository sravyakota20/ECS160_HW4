package com.ecs160.hw4;

import com.ecs160.hw4.persistence.Session;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public class SocialMediaAnalyzerDriver {
    public static void main(String[] args) {
        // Create a new persistence session
        Session session = new Session();

        ConfigurationManager configManager = ConfigurationManager.getInstance();
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nEnter the json file name : ");
        String inputFileName = scanner.next();
        configManager.setJsonFileName(inputFileName);

        System.out.print("\nEnter analysis option : Weighted = 1, Non-Weighted = 2 : ");
        int analysis = scanner.nextInt();
        if (!(analysis == 1 || analysis == 2)) {
            System.out.println("Invalid entry. Choose either 1 or 2.  Existing...\n");
        }

        configManager.setAnalysisType(analysis);
        // Load posts from input.json via the persistence layer
        System.out.println("Loading posts from ..." + configManager.getJsonFileName());
        List<SocialMediaComponent> posts = JsonParserUtility.getPosts();
        // System.out.println("Posts loaded: " + posts.size());
        /*for (Post p : posts) {
            System.out.println(p);
        }*/
        // Add posts to the session and persist them
        for (SocialMediaComponent post : posts) {
            session.add(post);
        }
        session.persistAll();

        /*
        // Decide how to analyze
        if (ConfigurationManager.NON_WEIGHTED.equalsIgnoreCase(ConfigurationManager.getInstance().getAnalysisType())) {
            runWeightedAnalysis(posts);
        } else {
            runNonWeightedAnalysis(posts);
        }
         */

        // Prompt the user for a post ID to fetch via the persistence layer
        /*Scanner scanner = new Scanner(System.in);
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
        scanner.close();*/

        // Determine max words across all posts (needed for weighted analysis)
        int maxWords = 0;
        for (SocialMediaComponent comp : posts) {
            if (comp instanceof Thread) {
                Thread thread = (Thread) comp;
                int wordsMain = thread.getMainPost().getNumberOfWords();
                if (wordsMain > maxWords) {
                    maxWords = wordsMain;
                }
                for (SocialMediaComponent child : thread.getReplies()) {
                    if (child instanceof Post) {
                        int words = ((Post) child).getNumberOfWords();
                        if (words > maxWords) {
                            maxWords = words;
                        }
                    }
                }
            } else if (comp instanceof Post) {
                int words = ((Post) comp).getNumberOfWords();
                if (words > maxWords) {
                    maxWords = words;
                }
            }
        }

        // Based on configuration, apply the appropriate visitor:
        int analysisType = ConfigurationManager.getInstance().getAnalysisType();
        if (analysisType == 1) {
            WeightedAnalysisVisitor wVisitor = new WeightedAnalysisVisitor(maxWords);
            for (SocialMediaComponent comp : posts) {
                comp.accept(wVisitor);
            }
            System.out.println("Weighted Status\nTotal Main Posts: " + wVisitor.getTotalWeightedPosts());
            System.out.println("Average Weighted Replies per Post: " + wVisitor.getAverageWeightedReplies());
            System.out.println("Total Main Posts: " + wVisitor.getPostCount());
            System.out.println("Total Replies: " + wVisitor.getReplyCount());
        } else {
            NonWeightedAnalysisVisitor nwVisitor = new NonWeightedAnalysisVisitor();
            for (SocialMediaComponent comp : posts) {
                comp.accept(nwVisitor);
            }
            System.out.println("Non-Weithed stats\nTotal Main Posts: " + nwVisitor.getPostCount());
            System.out.println("Total Replies: " + nwVisitor.getTotalReplies());
            System.out.println("Total Likes: " + nwVisitor.getTotalLikes());
            System.out.println("Average Replies per Post: " + nwVisitor.getAverageRepliesPerPost());
        }

        // Call LLAMA for HashTagging
        HashTagger ht = new HashTagger();
        for (SocialMediaComponent comp : posts) {
            if (comp instanceof Thread) {
                Thread t = (Thread) comp;
                String content = t.getMainPost().getText();
                System.out.println(ht.getOllamaResposne(content));
            }
        }
    }

/*
    private static void runWeightedAnalysis(List<SocialMediaComponent> posts) {
        System.out.println("\nRunning WEIGHTED analysis...");
        double totalScore = 0;

        for (SocialMediaComponent post : posts) {
            int postLikes = post.getLikeCount();
            int replyCount = 0;

            if (post instanceof Thread) {
                replyCount = ((Thread) post).getReplies().size();
            }

            double postScore = (postLikes * 2.0) + (replyCount * 1.5);
            totalScore += postScore;

            post.printPost();
            System.out.printf("Weighted Score: %.2f\n\n", postScore);
        }

        System.out.printf("Total Weighted Score for all posts: %.2f\n", totalScore);
    }

    private static void runNonWeightedAnalysis(List<SocialMediaComponent> posts) {
        System.out.println("\nRunning NON-WEIGHTED analysis...");
        int totalPosts = posts.size();
        int totalReplies = 0;
        int totalLikes = 0;

        for (SocialMediaComponent post : posts) {
            post.printPost();

            if (post instanceof Thread) {
                int count = ((Thread) post).getReplies().size();
                totalReplies += count;
                System.out.println("Reply count: " + count + "\n");
            }
        }

        System.out.println("Total Top-Level Posts: " + totalPosts);
        System.out.println("Total Replies: " + totalReplies);
        System.out.println("Total Post + Reply Count: " + (totalPosts + totalReplies));
        System.out.println("Non-Weighted Score (sum of likes): " + totalLikes);
    }
 */
}
