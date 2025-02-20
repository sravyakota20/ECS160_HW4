package com.ecs160.hw2;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class JsonParserUtility {
    private static int numOfPosts = 0;
    private static int numOfWordsInLongestPost = 0;
    // Each index in the array is the number of replies corresponding to ith post.
    // For example, numberOfRepliesOfPostN[0] is the number of replies under post 0. numberOfRepliesOfPostN[1] is the number of replies under post 1
    private static ArrayList<Integer> numberOfRepliesOfPostN = new ArrayList<>();

    public static int getNumOfPosts(){
        return numOfPosts;
    }

    public static int getNumOfWordsInLongestPost(){
        return numOfWordsInLongestPost;
    }

    public static ArrayList<Integer> getNumberOfRepliesOfPostN(){
        return numberOfRepliesOfPostN;
    }

    public static void parseJsonFile() {
        // Initialize DatabaseManager object
        DatabaseManager db = new DatabaseManager();

        // Read JSON file
        JsonElement element = JsonParser.parseReader(new InputStreamReader(
                JsonParserUtility.class.getClassLoader().getResourceAsStream("input.json")
        ));

        if (element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray feedArray = jsonObject.getAsJsonArray("feed");

            for (JsonElement feedObject : feedArray) {
                if (feedObject.isJsonObject() && feedObject.getAsJsonObject().has("thread")) {
                    JsonObject threadObject = feedObject.getAsJsonObject().getAsJsonObject("thread");

                    // Get the post logic
                    if (threadObject.has("post")) {
                        Post post = parsePost(threadObject.getAsJsonObject("post"));
                        numOfPosts += 1;
                        //Save post to postgres
                        int numWords = post.getNumberOfWords();
                        if(numWords > numOfWordsInLongestPost){
                            numOfWordsInLongestPost = numWords;
                        }
                        int postId = db.addPost(numWords);
                        if (postId != -1){
                            int numReplies = 0;
                            ArrayList<Reply> replies = new ArrayList<>();
                            if(threadObject.has("replies")){
                                JsonArray repliesArray = threadObject.getAsJsonArray("replies");
                                replies = parseReplies(repliesArray, postId, db);
                                for (Reply reply: replies){
                                    int numWordsInReply = reply.getNumWords();
                                    int postIdForReply = reply.getPostId();
                                    String replyCreatedAt = reply.getCreatedAt();
                                    // Save reply in the database
                                    db.addReply(postIdForReply, numWordsInReply, replyCreatedAt);
                                }
                            }

                        }

                    }


                }
            }
        }

        return;
    }

    private static ArrayList<Reply> parseReplies(JsonArray repliesArray, int parentPostId, DatabaseManager db) {
        int replyCount = 0; // Track number of replies
        ArrayList<Reply> replies = new ArrayList<>();

        int i = 0;
        for (JsonElement replyElement : repliesArray) {
            if (replyElement.isJsonObject()) {
                JsonObject replyObject = replyElement.getAsJsonObject();
                if (replyObject.has("post")) {
                    JsonObject postObject = replyObject.getAsJsonObject("post");
                    replies.add(new Reply());
                    replies.get(i).setPostId(parentPostId);
                    // Extract reply text and count words
                    int numWords = 0;
                    String text = "";
                    String createdAt = "";

                    if (postObject.has("record")) {
                        JsonObject jsonRecord = postObject.getAsJsonObject("record");
                        if (jsonRecord.has("text")) {
                            text = jsonRecord.get("text").getAsString();
                            numWords = countWords(text);
                            replies.get(i).setNumWords(numWords);
                        }
                        if (jsonRecord.has("createdAt")) {
                            createdAt = jsonRecord.get("createdAt").getAsString();
                            replies.get(i).setCreatedAt(createdAt);
                        }
                    }
                    i++;

                    replyCount++; // Increment reply count for this post

                }
            }
        }

        numberOfRepliesOfPostN.add(replyCount);
        return replies;
    }

    private static Post parsePost(JsonObject postObject) {
        int numWords = 0;
        String text = "";

        // Extract post text from "record" and count words
        if (postObject.has("record")) {
            JsonObject jsonRecord = postObject.getAsJsonObject("record");
            if (jsonRecord.has("text")) {
                text = jsonRecord.get("text").getAsString();
                numWords = countWords(text);
            }
        }

        // Create Record and Post objects
        // Record record = new Record(text);

        Post post = new Post(numWords, text);

        return post;
    }

    // Utility method to count words in a string
    private static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length; // Splitting by whitespace
    }
}
