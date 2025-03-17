package com.ecs160.hw2;

import com.google.gson.*;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JsonParserUtility {
    // Existing fields and methods remain unchanged ...

    // New method: Parses input.json from resources and returns top-level posts.
    public static List<Post> getPosts() {
        RedisManager jedis = new RedisManager();
        jedis.clearDatabase();
        List<Post> posts = new ArrayList<>();
        JsonElement element = JsonParser.parseReader(new InputStreamReader(
                JsonParserUtility.class.getClassLoader().getResourceAsStream("input.json")
        ));
        if (element != null && element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray feedArray = jsonObject.getAsJsonArray("feed");
            for (JsonElement feedElement : feedArray) {
                JsonObject feedObj = feedElement.getAsJsonObject();
                if (feedObj.has("thread")) {
                    JsonObject threadObj = feedObj.getAsJsonObject("thread");
                    if (threadObj.has("post")) {
                        JsonObject postObj = threadObj.getAsJsonObject("post");
                        // Extract fields safely
                        int postId = 0;
                        if (postObj.has("uri") && !postObj.get("uri").isJsonNull()) {
                            String uri = postObj.get("uri").getAsString();
                            postId = extractPostIdFromUri(uri);
                            //System.out.println("URI Post Id: " + postId);
                        } else {
                            System.err.println("Warning: Missing uri for a post: " + postObj);
                            continue;
                        }
                        String text = "";
                        if (postObj.has("record") && !postObj.get("record").isJsonNull()) {
                            JsonObject recordObj = postObj.getAsJsonObject("record");
                            if (recordObj.has("text") && !recordObj.get("text").isJsonNull()) {
                                text = recordObj.get("text").getAsString();
                            }
                        }
                        String createdAt = "";
                        if (postObj.has("record") && !postObj.get("record").isJsonNull()) {
                            JsonObject recordObj = postObj.getAsJsonObject("record");
                            if (recordObj.has("createdAt") && !recordObj.get("createdAt").isJsonNull()) {
                                createdAt = recordObj.get("createdAt").getAsString();
                            }
                        }
                        Post post = new Post(postId, text, createdAt);

                        // Parse replies, if any (ignore nested replies beyond the first level)
                        if (threadObj.has("replies")) {
                            JsonArray repliesArray = threadObj.getAsJsonArray("replies");
                            List<Reply> replies = new ArrayList<>();
                            for (JsonElement replyElement : repliesArray) {
                                if (replyElement.isJsonObject()) {
                                    // Each reply object should have a "post" field
                                    JsonObject replyContainer = replyElement.getAsJsonObject();
                                    if (replyContainer.has("post")) {
                                        JsonObject replyObj = replyContainer.getAsJsonObject("post");
                                        Reply reply = parseReply(replyObj, postId);
                                        replies.add(reply);
                                    }
                                }
                            }
                            System.out.println("Added " + replies.size() + " replies to " + postId);
                            post.setReplies(replies);
                        }
                        posts.add(post);
                    }
                }
            }
        }
        return posts;
    }

    /**
     * Parses a reply JSON object into a Reply object.
     */
    private static Reply parseReply(JsonObject replyObj, int parentPostId) {
        int replyId = 0;
        if (replyObj.has("uri") && !replyObj.get("uri").isJsonNull()) {
            String uri = replyObj.get("uri").getAsString();
            replyId = extractPostIdFromUri(uri);
            //System.out.println("Reply ID: " + replyId);
        }
        String text = "";
        if (replyObj.has("record") && !replyObj.get("record").isJsonNull()) {
            JsonObject recordObj = replyObj.getAsJsonObject("record");
            if (recordObj.has("text") && !recordObj.get("text").isJsonNull()) {
                text = recordObj.get("text").getAsString();
            }
        }
        String createdAt = "";
        if (replyObj.has("record") && !replyObj.get("record").isJsonNull()) {
            JsonObject recordObj = replyObj.getAsJsonObject("record");
            if (recordObj.has("createdAt") && !recordObj.get("createdAt").isJsonNull()) {
                createdAt = recordObj.get("createdAt").getAsString();
            }
        }
        // Create and return the Reply object (assuming Reply has an appropriate constructor)
        return new Reply(replyId, text, createdAt, parentPostId);
    }

    // Helper method to extract postId from a URI.
    private static int extractPostIdFromUri(String uri) {
        if (uri.isEmpty()) return -1;
        String[] parts = uri.split("/");
        // Here we simply use hashCode as in your original code.
        return parts.length > 0 ? Math.abs(parts[parts.length - 1].hashCode()) : -1;
    }
}