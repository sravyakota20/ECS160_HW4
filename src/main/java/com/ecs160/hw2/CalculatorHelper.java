package com.ecs160.hw2;

import redis.clients.jedis.Jedis;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;
import java.time.Instant;

public class CalculatorHelper {
    private RedisManager redisManager;
    private int numOfWordsInLongestPost;
    private int numOfPosts;
    private ArrayList<Integer> numberOfRepliesOfPostN;

    public CalculatorHelper() {
        this.redisManager = new RedisManager();
        this.numOfWordsInLongestPost = getNumOfWordsInLongestPost();
        this.numOfPosts = getNumOfPosts();  // RESTORED
        this.numberOfRepliesOfPostN = getNumberOfRepliesOfPostN();
    }

    public CalculatorHelper(int posts, int numWordsLongestPost, ArrayList<Integer> numRepliesPostN) {
        this.redisManager = new RedisManager();
        this.numOfPosts = posts;
        this.numberOfRepliesOfPostN = numRepliesPostN;
        this.numOfWordsInLongestPost = numWordsLongestPost;
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
        double weightOfPost;

        List<String> postKeys = redisManager.getAllPostKeys();
        for (String postKey : postKeys) {
            int postId = Integer.parseInt(postKey.split(":")[1]);  // Extract postId from postKey
            Post post = redisManager.getPost(postId);  // Fetch post object
            if (post != null) {
                String postContent = post.getPostContent();
                if (postContent != null && !postContent.isEmpty()) {
                    int wordCount = postContent.split("\\s+").length;
                    weightOfPost = weight(wordCount);
                    sum += weightOfPost;
                }
            }
        }
        return sum;
    }

    public double weightedAvgNumReplies(int numberOfPost) {
        double sum = 0;
        double weightOfReply;

        List<String> postKeys = redisManager.getAllPostKeys();
        for (String postKey : postKeys) {
            List<String> replies = redisManager.getReplyIds(postKey);
            for (String replyId : replies) {
                int replyPostId = Integer.parseInt(replyId);
                Post replyPost = redisManager.getPost(replyPostId);
                if (replyPost != null) {
                    String replyContent = replyPost.getPostContent();
                    if (replyContent != null && !replyContent.isEmpty()) {
                        int wordCount = replyContent.split("\\s+").length;
                        weightOfReply = weight(wordCount);
                        sum += weightOfReply;
                    }
                }
            }
        }
        return (numberOfPost > 0) ? sum / numberOfPost : 0;
    }

    /**
     * Updated calculation for average interval between comments.
     * For each post with replies, the method considers the post's own createdAt timestamp
     * along with the createdAt timestamps of its replies, sorts them in chronological order,
     * computes the intervals between consecutive timestamps, and then averages these intervals.
     */
    public String calculateAverageIntervalBetweenComments() {
        List<String> postKeys = redisManager.getAllPostKeys();
        long totalSeconds = 0;
        int totalIntervals = 0;

        for (String postKey : postKeys) {
            int postId = Integer.parseInt(postKey.split(":")[1]);
            Post post = redisManager.getPost(postId);
            if (post == null) continue;
            String parentCreatedAt = post.getCreatedAt();
            List<String> replyIds = redisManager.getReplyIds(postKey);
            if (replyIds.isEmpty()) continue;

            List<String> timestamps = new ArrayList<>();
            // Include the parent's timestamp as the first comment
            timestamps.add(parentCreatedAt);
            // Add each reply's timestamp
            for (String replyId : replyIds) {
                Post reply = redisManager.getPost(Integer.parseInt(replyId));
                if (reply != null && reply.getCreatedAt() != null && !reply.getCreatedAt().isEmpty()) {
                    timestamps.add(reply.getCreatedAt());
                }
            }
            if (timestamps.size() < 2) continue;
            // Sort the timestamps chronologically
            timestamps.sort((t1, t2) -> {
                try {
                    Instant i1 = Instant.parse(t1);
                    Instant i2 = Instant.parse(t2);
                    return i1.compareTo(i2);
                } catch (Exception e) {
                    return 0;
                }
            });
            // Compute the interval (in seconds) between consecutive timestamps
            for (int i = 1; i < timestamps.size(); i++) {
                try {
                    long interval = Duration.between(Instant.parse(timestamps.get(i - 1)), Instant.parse(timestamps.get(i))).getSeconds();
                    totalSeconds += interval;
                    totalIntervals++;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        long averageSeconds = (totalIntervals > 0) ? totalSeconds / totalIntervals : 0;
        return formatSecondsToHHMMSS(averageSeconds);
    }

    private String formatSecondsToHHMMSS(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private int getNumOfWordsInLongestPost() {
        int maxLength = 0;
        List<String> postKeys = redisManager.getAllPostKeys();
        for (String postKey : postKeys) {
            int postId = Integer.parseInt(postKey.split(":")[1]);
            Post post = redisManager.getPost(postId);
            if (post != null) {
                String postContent = post.getPostContent();
                if (postContent != null) {
                    int wordCount = postContent.split("\\s+").length;
                    maxLength = Math.max(maxLength, wordCount);
                }
            }
        }
        return maxLength;
    }

    public int getNumOfPosts() {
        return redisManager.getAllPostKeys().size();
    }

    private ArrayList<Integer> getNumberOfRepliesOfPostN() {
        ArrayList<Integer> replyCounts = new ArrayList<>();
        List<String> postKeys = redisManager.getAllPostKeys();
        for (String postKey : postKeys) {
            List<String> replies = redisManager.getReplyIds(postKey);
            replyCounts.add(replies.size());
        }
        return replyCounts;
    }

    public void close() {
        redisManager.close();
    }
}