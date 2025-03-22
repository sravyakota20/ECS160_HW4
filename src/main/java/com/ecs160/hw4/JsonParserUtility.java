package com.ecs160.hw4;

import com.google.gson.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JsonParserUtility {
    // Existing fields and methods remain unchanged ...

    // New method: Parses input.json from resources and returns top-level posts.
    public static List<SocialMediaComponent> getPosts() {
        RedisManager jedis = new RedisManager();
        jedis.clearDatabase();
        List<SocialMediaComponent> posts = new ArrayList<>();

        String jsFile = ConfigurationManager.getInstance().getJsonFileName();
        InputStream resourceStream = JsonParserUtility.class.getClassLoader().getResourceAsStream(jsFile);
        if (resourceStream == null) {
            System.err.println("File " + jsFile + " not found in resources.");
            return posts;
        }
        InputStreamReader in = new InputStreamReader(resourceStream);

        if (in == null) {
            System.err.println("File " + jsFile + " not found in resources.");
            return posts;
        }

        JsonElement element = JsonParser.parseReader(in);

        if (element != null && element.isJsonObject()) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonArray feedArray = jsonObject.getAsJsonArray("feed");
            for (JsonElement feedElement : feedArray) {
                JsonObject feedObj = feedElement.getAsJsonObject();
                if (feedObj.has("thread")) {
                    JsonObject threadObj = feedObj.getAsJsonObject("thread");
                    if (threadObj.has("post")) {
                        JsonObject postObj = threadObj.getAsJsonObject("post");
                        Post mainPost = createPostFromJson(postObj);
                        Thread thread = new Thread(mainPost);

                        // Process first-level replies, if any
                        if (threadObj.has("replies")) {
                            JsonArray repliesArray = threadObj.getAsJsonArray("replies");
                            for (JsonElement replyElem : repliesArray) {
                                JsonObject replyContainer = replyElem.getAsJsonObject();
                                if (!replyContainer.has("post"))
                                    continue;
                                JsonObject replyObj = replyContainer.getAsJsonObject("post");
                                Post reply = createPostFromJson(replyObj);
                                thread.add(reply);
                            }
                        }

                        posts.add(thread);
                    }
                        /*
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
                        */

                    // Parse replies, if any (ignore nested replies beyond the first level)
                    /*
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

                     */
                }
            }
        }

        return posts;
    }


    /**
     * Parses a reply JSON object into a Reply object.
     */
    /*
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
*/
    // Creates a Post object from the JSON post object.
    private static Post createPostFromJson(JsonObject postObj) {
        // Using a simple strategy for id: if available, we use the hashCode of the URI.
        int id = postObj.has("uri") ? postObj.get("uri").getAsString().hashCode() : 0;

        // The text is nested inside the "record" object under "text"
        String content = "";
        if (postObj.has("record")) {
            JsonObject recordObj = postObj.getAsJsonObject("record");
            if (recordObj.has("text")) {
                content = recordObj.get("text").getAsString();
            }
        }

        int likeCount = postObj.has("likeCount") ? postObj.get("likeCount").getAsInt() : 0;
        String createdAt = postObj.has("record") && postObj.getAsJsonObject("record").has("createdAt")
                ? postObj.getAsJsonObject("record").get("createdAt").getAsString() : "";
        // Extract content from record.text

        // Create a Post using likeCount and content
        return new Post(id, content, createdAt, likeCount);
    }
}