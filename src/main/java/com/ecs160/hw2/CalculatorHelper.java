package com.ecs160.hw2;

import java.util.ArrayList;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;

public class CalculatorHelper {
    private DatabaseManager db;
    private int numOfWordsInLongestPost;
    private int numOfPosts;
    private ArrayList<Integer> numberOfRepliesOfPostN;

    public CalculatorHelper(){
        this.db = new DatabaseManager();
        this.numOfWordsInLongestPost = JsonParserUtility.getNumOfWordsInLongestPost();
        this.numOfPosts = JsonParserUtility.getNumOfPosts();
        this.numberOfRepliesOfPostN = JsonParserUtility.getNumberOfRepliesOfPostN();
    }

    public CalculatorHelper(int posts, int numWordsLongestPost, ArrayList<Integer> numRepliesPostN){
        this.db = new DatabaseManager();
        this.numOfPosts = posts;
        this.numberOfRepliesOfPostN = numRepliesPostN;
        this.numOfWordsInLongestPost = numWordsLongestPost;
    }

    public int getTotalNumberOfPost(){
        return this.numOfPosts;
    }

    public double calculateAverageReplyPerPost(){
        if (numOfPosts == 0) {
            return 0; // Avoid division by zero
        }

        int totalReplies = 0;
        for (int replies : numberOfRepliesOfPostN) {
            totalReplies += replies;
        }

        return (double) totalReplies / (double) numOfPosts; // Integer division for average
    }

    private double weight(int numOfWordsInPost){
        return (1+((double) numOfWordsInPost/ (double) numOfWordsInLongestPost));
    }

    public double weightedTotalPost(int numberOfPost){
        double sum = 0;
        int[] postInfo;
        double weightOfPost;
        for(int i = 1; i <= numberOfPost; i++){
            postInfo = db.getPostById(i);
            weightOfPost = weight(postInfo[1]);
            sum += weightOfPost;
        }
        return sum;
    }

    public double weightedAvgNumReplies(int numberOfPost){
        double sum = 0;
        ArrayList<Integer> replyInfo;
        double weightOfReply;
        for(int i = 1 ; i <= numberOfPost; i++ ){
            replyInfo = db.getRepliesForPost(i);
            for(int numWordsInAReply : replyInfo){
                weightOfReply = weight(numWordsInAReply);
                sum += weightOfReply;
            }
        }
        return sum;
    }

    public String calculateAverageIntervalBetweenComments() {
        ArrayList<Reply> replies = db.getAllReplies();

        // Sort replies by postId and createdAt timestamp
        replies.sort(Comparator.comparing(Reply::getPostId)
                .thenComparing(Reply::getCreatedAt));

        long totalSeconds = 0;
        int totalIntervals = 0;

        Integer lastPostId = null;
        String lastCreatedAt = null;

        for (Reply reply : replies) {
            if (lastPostId != null && lastPostId.equals(reply.getPostId()) && lastCreatedAt != null) {
                // Calculate interval between the current and previous reply
                long intervalSeconds = calculateIntervalInSeconds(lastCreatedAt, reply.getCreatedAt());
                totalSeconds += intervalSeconds;
                totalIntervals++;
            }

            // Update last seen reply details
            lastPostId = reply.getPostId();
            lastCreatedAt = reply.getCreatedAt();
        }

        // Calculate average interval in seconds
        long averageSeconds = (totalIntervals > 0) ? totalSeconds / totalIntervals : 0;

        // Convert average seconds to HH:MM:SS format
        return formatSecondsToHHMMSS(averageSeconds);
    }

    // Convert timestamp difference to seconds
    private long calculateIntervalInSeconds(String createdAt1, String createdAt2) {
        Instant time1 = Instant.parse(createdAt1);
        Instant time2 = Instant.parse(createdAt2);

        // Calculate absolute duration between timestamps
        Duration duration = Duration.between(time1, time2).abs();
        return duration.getSeconds();
    }

    // Convert seconds to HH:MM:SS format
    private String formatSecondsToHHMMSS(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
