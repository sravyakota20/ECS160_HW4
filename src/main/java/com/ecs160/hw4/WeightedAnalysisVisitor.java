package com.ecs160.hw4;

public class WeightedAnalysisVisitor implements PostVisitor {
    private double totalWeightedPosts = 0;
    private double totalWeightedReplies = 0;
    private int postCount = 0;
    private int replyCount = 0;
    private double maxWords;  // Maximum number of words in the longest post

    // create the visitor object with maxwords value
    public WeightedAnalysisVisitor(double maxWords) {
        this.maxWords = maxWords;
    }

    private double computeWeight(Post post) {
        int words = post.getNumberOfWords();
        return 1.0 + (maxWords > 0 ? ((double) words / maxWords) : 0);
    }

    @Override
    public void visit(Post post) {
        // This method is invoked for a reply (leaf node)
        double weight = computeWeight(post);
        totalWeightedReplies += weight;
        replyCount++;
    }

    @Override
    public void visit(Thread thread) {
        // This method is invoked for the main post in a thread (composite)
        double weight = computeWeight(thread.getMainPost());
        totalWeightedPosts += weight;
        postCount++;
    }

    public double getTotalWeightedPosts() {
        return totalWeightedPosts;
    }

    public double getTotalWeightedReplies() {
        return totalWeightedReplies;
    }

    public int getPostCount() {
        return postCount;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public double getAverageWeightedReplies() {
        return postCount == 0 ? 0 : totalWeightedReplies / postCount;
    }
}


