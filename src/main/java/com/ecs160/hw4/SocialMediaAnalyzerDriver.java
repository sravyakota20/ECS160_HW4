package com.ecs160.hw4;

import com.ecs160.hw4.persistence.Session;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.io.InputStream;
import java.util.*;

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
        List<SocialMediaComponent> top10posts = getTop10Posts(posts);
        for (int i = 0; i < top10posts.size() && i < 10; i++) {
            SocialMediaComponent comp = top10posts.get(i);
            if (comp instanceof Thread) {
                Thread t = (Thread) comp;
                String hashTag = ht.getOllamaResposne((t.getMainPost().getText()));
                // Wrap the component with a hashtag decorator.
                top10posts.set(i, new HashtagDecorator(comp, hashTag));
                System.out.println("Hashtag for post " + t.getMainPost().getText() + " is " + hashTag);
            }
        }
    }

    private static List<SocialMediaComponent> getTop10Posts(List<SocialMediaComponent> components) {
        // Sort components descending by like count of main post (if composite) or component directly (if leaf)
        Collections.sort(components, new Comparator<SocialMediaComponent>() {
            @Override
            public int compare(SocialMediaComponent o1, SocialMediaComponent o2) {
                int likes1 = getComponentLikeCount(o1);
                int likes2 = getComponentLikeCount(o2);
                return Integer.compare(likes2, likes1);  // descending order
            }
        });

        // Return the first 10, or fewer if there aren't 10
        List<SocialMediaComponent> top10 = new ArrayList<>();
        for (int i = 0; i < Math.min(10, components.size()); i++) {
            top10.add(components.get(i));
        }
        return top10;
    }

    private static int getComponentLikeCount(SocialMediaComponent comp) {
        if (comp instanceof Thread) {
            return ((Thread) comp).getMainPost().getLikeCount();
        } else {
            return comp.getLikeCount();
        }
    }
}
