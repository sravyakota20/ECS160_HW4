package com.ecs160.hw2;

import java.util.ArrayList;
import java.util.List;

import com.ecs160.hw2.persistence.*;

@Persistable
public class Post {
    @PersistableId
    private int postId;

    @PersistableField
    private int numWords; // Number of words in the post

    @PersistableField
    private String createdAt; // Timestamp for the post

    @PersistableField
    private String text;

    @PersistableListField(className = "com.ecs160.hw2.Reply")
    @LazyLoad
    private List<Reply> replies;

    // Default Constructor
    public Post(){
        this.numWords = 0;
        this.text = "";
        this.createdAt = "";
        this.replies = new ArrayList<>();
    }

    public Post(int postId) {
        this.postId = postId;
        this.text = "";
        this.replies = new ArrayList<>();
    }

    public Post(int postId, String postContent, String createdAt) {
        this.postId = postId;
        this.text = postContent;
        this.numWords = postContent.split("\\s+").length;
        this.createdAt = createdAt;
        this.replies = new ArrayList<>();
    }

    public int getPostId() { return postId; }

    public int getNumberOfWords() { return numWords; }

    public String getCreatedAt() { return createdAt; }

    public String getText() { return text; }

    public String getPostContent() { return text; }

    public List<Reply> getReplies() { return replies; }

    public void setPostId(int postId) { this.postId = postId; }

    public void setNumberOfWords(int numWords) { this.numWords = numWords; }

    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public void setText(String text) {
        this.text = text;
    }

    public void setReplies(List<Reply> replies) { this.replies = replies; }

    public String toString() {
        return "PostId: " + postId + " Content: " + getPostContent() + " Replies: " + replies.size();
    }
}