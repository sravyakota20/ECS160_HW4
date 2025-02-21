package com.ecs160.hw2;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class JsonParserUtility {
    private static int numOfPosts = 0;
    private static int numOfWordsInLongestPost = 0;
    private static ArrayList<Integer> numberOfRepliesOfPostN = new ArrayList<>();

    public static int getNumOfPosts() { return numOfPosts; }

    public static int getNumOfWordsInLongestPost() { return numOfWordsInLongestPost; }

    public static ArrayList<Integer> getNumberOfRepliesOfPostN() { return numberOfRepliesOfPostN; }

    /**
     * Parses the input.json file and stores posts & replies in Redis.
     */
    public static void parseJsonFile(RedisManager redisManager) {
        JsonElement element = JsonParser.parseReader(new InputStreamReader(
                JsonParserUtility.class.getClassLoader().getResourceAsStream("input.json")
        ));

        redisManager.clearDatabase();
        if (element != null && element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray feedArray = jsonObject.getAsJsonArray("feed");

            for (JsonElement feedObject : feedArray) {
                if (feedObject.isJsonObject() && feedObject.getAsJsonObject().has("thread")) {
                    JsonObject threadObject = feedObject.getAsJsonObject().getAsJsonObject("thread");

                    // Parse the post
                    if (threadObject.has("post")) {
                        Post post = parsePost(threadObject.getAsJsonObject("post"));
                        numOfPosts++;

                        // Track longest post length
                        int numWords = post.getNumberOfWords();
                        if (numWords > numOfWordsInLongestPost) {
                            numOfWordsInLongestPost = numWords;
                        }

                        // Store post in Redis
                        redisManager.addPost(post.getPostId(), post.getPostContent(), post.getCreatedAt());
                        //System.out.println("adding Post " + post.getPostId());
                        if (threadObject.has("replies")) {
                            JsonArray repliesArray = threadObject.getAsJsonArray("replies");
                            // Only process first-level replies; ignore replies-to-replies as required
                            ArrayList<Reply> replies = parseReplies(repliesArray, post.getPostId(), redisManager);

                            for (Reply reply : replies) {
                                //System.out.println("adding replies to " + post.getPostId());
                                redisManager.addReply(post.getPostId(), reply.getPostId(), reply.getContent(), reply.getCreatedAt());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Parses replies and associates them with the given parent post ID in Redis.
     * Note: Nested replies (replies-to-replies) are ignored.
     */
    private static ArrayList<Reply> parseReplies(JsonArray repliesArray, int parentPostId, RedisManager redisManager) {
        int replyCount = 0;
        ArrayList<Reply> replies = new ArrayList<>();

        for (JsonElement replyElement : repliesArray) {
            if (replyElement.isJsonObject()) {
                JsonObject replyObject = replyElement.getAsJsonObject().getAsJsonObject("post");
                Reply reply = parseReply(replyObject, parentPostId);

                // Store reply in Redis
                //redisManager.addReply(parentPostId, reply.getPostId(), reply.getContent(), reply.getCreatedAt());
                replies.add(reply);
                replyCount++;
                // Nested replies are intentionally ignored
            }
        }

        numberOfRepliesOfPostN.add(replyCount);
        return replies;
    }

    /**
     * Parses a post from a JSON object.
     */
    private static Post parsePost(JsonObject postObject) {
        String uri = postObject.has("uri") ? postObject.get("uri").getAsString() : "";
        int postId = extractPostIdFromUri(uri);
        String text = postObject.has("record") ? postObject.getAsJsonObject("record").get("text").getAsString() : "";
        String createdAt = postObject.has("record") ? postObject.getAsJsonObject("record").get("createdAt").getAsString() : "";

        return new Post(postId, text, createdAt);
    }

    /**
     * Parses a reply from a JSON object.
     */
    private static Reply parseReply(JsonObject replyObject, int parentPostId) {
        String uri = replyObject.has("uri") ? replyObject.get("uri").getAsString() : "";
        int replyId = extractPostIdFromUri(uri);
        String text = replyObject.has("record") ? replyObject.getAsJsonObject("record").get("text").getAsString() : "";
        String createdAt = replyObject.has("record") ? replyObject.getAsJsonObject("record").get("createdAt").getAsString() : "";

        return new Reply(replyId, text, createdAt, parentPostId);
    }

    /**
     * Extracts post ID from the `uri` field.
     */
    private static int extractPostIdFromUri(String uri) {
        if (uri.isEmpty()) return -1;
        String[] parts = uri.split("/");
        return parts.length > 0 ? parts[parts.length - 1].hashCode() : -1;  // Use hashCode to convert to int
    }
}
