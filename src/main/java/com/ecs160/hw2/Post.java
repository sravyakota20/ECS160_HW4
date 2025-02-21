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
    private Record record;

    @PersistableListField(className = "com.ecs160.hw2.Post")
    @LazyLoad
    private List<Post> replies;

    // Default Constructor
    public Post(){
        this.numWords = 0;
        this.record = null;
        this.createdAt = "";
        this.replies = new ArrayList<>();
    }

    public Post(int postId, String postContent, String createdAt) {
        this.postId = postId;
        this.record = new Record(postContent);
        this.numWords = postContent.split("\\s+").length;
        this.createdAt = createdAt;
        this.replies = new ArrayList<>();
    }

    public int getPostId() { return postId; }

    public int getNumberOfWords() { return numWords; }

    public String getCreatedAt() { return createdAt; }

    public Record getRecord() { return record; }

    public String getPostContent() { return record.getText(); }

    public List<Post> getReplies() { return replies; }

    public void setPostId(int postId) { this.postId = postId; }

    public void setNumberOfWords(int numWords) { this.numWords = numWords; }

    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public void setRecord(Record record) { this.record = record; }

    public void setReplies(List<Post> replies) { this.replies = replies; }
}