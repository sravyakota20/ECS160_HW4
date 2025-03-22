package com.ecs160.hw4;

public class NonWeightedAnalysisVisitor implements PostVisitor {
    private int postCount = 0;
    private int replyCount = 0;
    private int totalReplies = 0; // Sum of actual reply counts (from composite threads)
    private int totalLikes = 0;   // Sum of likes for all posts and replies

    @Override
    public void visit(Post post) {
        // For a reply, count it and add its likes
        replyCount++;
        totalLikes += post.getLikeCount();
    }

    @Override
    public void visit(Thread thread) {
        // For the main post in a thread:
        postCount++;
        totalLikes += thread.getMainPost().getLikeCount();
        // Also count the number of replies that are attached to this thread.
        totalReplies += thread.getReplies().size();
    }

    public int getPostCount() {
        return postCount;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public int getTotalReplies() {
        return totalReplies;
    }

    public int getTotalLikes() {
        return totalLikes;
    }

    public double getAverageRepliesPerPost() {
        return postCount == 0 ? 0 : (double) totalReplies / postCount;
    }
}