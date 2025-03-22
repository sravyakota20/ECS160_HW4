package com.ecs160.hw4;

import com.ecs160.hw4.persistence.Session;
import java.util.ArrayList;
import java.util.List;

public class CalculatorHelper {
    private int numOfWordsInLongestPost;
    private int numOfPosts;
    private ArrayList<Integer> numberOfRepliesOfPostN;
    private Session session;

    public CalculatorHelper(Session session) {
        this.session = session;
        this.numOfWordsInLongestPost = getNumOfWordsInLongestPost();
        this.numOfPosts = getNumOfPosts();
        this.numberOfRepliesOfPostN = getNumberOfRepliesOfPostN();
    }

    public int getTotalNumberOfPost() {
        return numOfPosts;
    }

    public double calculateAverageReplyPerPost() {
        if (numOfPosts == 0) return 0;
        int totalReplies = 0;
        for (int replies : numberOfRepliesOfPostN) {
            totalReplies += replies;
        }
        return (double) totalReplies / numOfPosts;
    }

    private double weight(int numOfWordsInPost) {
        return (1 + ((double) numOfWordsInPost / numOfWordsInLongestPost));
    }

    public double weightedTotalPost(int numberOfPost) {
        double sum = 0;
        List<String> postKeys = session.getAllKeys(); // Use session instead of RedisManager
        for (String key : postKeys) {
            Post post = (Post) session.load(new Post(Integer.parseInt(key)));
            if (post != null) {
                String postContent = post.getPostContent();
                int wordCount = postContent.split("\\s+").length;
                sum += weight(wordCount);
            }
        }
        return sum;
    }

    public double weightedAvgNumReplies(int numberOfPost) {
        double sum = 0;
        List<String> postKeys = session.getAllKeys();
        for (String key : postKeys) {
            Post post = (Post) session.load(new Post(Integer.parseInt(key)));
            if (post != null) {
                List<Reply> replies = post.getReplies();
                for (Reply reply : replies) {
                    String replyContent = reply.getContent();
                    int wordCount = replyContent.split("\\s+").length;
                    sum += weight(wordCount);
                }
            }
        }
        return (numberOfPost > 0) ? sum / numberOfPost : 0;
    }

    private int getNumOfWordsInLongestPost() {
        int maxWords = 0;
        List<String> postKeys = session.getAllKeys();
        for (String key : postKeys) {
            Post post = (Post) session.load(new Post(Integer.parseInt(key)));
            if (post != null) {
                int wordCount = post.getPostContent().split("\\s+").length;
                maxWords = Math.max(maxWords, wordCount);
            }
        }
        return maxWords;
    }

    public int getNumOfPosts() {
        return session.getAllKeys().size();
    }

    private ArrayList<Integer> getNumberOfRepliesOfPostN() {
        ArrayList<Integer> repliesCount = new ArrayList<>();
        List<String> postKeys = session.getAllKeys();
        for (String key : postKeys) {
            Post post = (Post) session.load(new Post(Integer.parseInt(key)));
            if (post != null) {
                repliesCount.add(post.getReplies().size());
            }
        }
        return repliesCount;
    }
}
