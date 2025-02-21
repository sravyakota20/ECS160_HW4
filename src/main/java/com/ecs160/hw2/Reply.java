package com.ecs160.hw2;

import com.ecs160.hw2.persistence.Persistable;
import com.ecs160.hw2.persistence.PersistableId;
import com.ecs160.hw2.persistence.PersistableField;

@Persistable
public class Reply {
    @PersistableId
    private int postId;

    @PersistableField
    private String content;

    @PersistableField
    private String createdAt;

    @PersistableField
    private int parentPostId; // Stores the ID of the parent post

    // Default constructor (needed for persistence frameworks)
    public Reply() {}

    // Constructor for creating a reply object
    public Reply(int postId, String content, String createdAt, int parentPostId) {
        this.postId = postId;
        this.content = content;
        this.createdAt = createdAt;
        this.parentPostId = parentPostId;
    }

    // Getters
    public int getPostId() { return postId; }
    public String getContent() { return content; }
    public String getCreatedAt() { return createdAt; }
    public int getParentPostId() { return parentPostId; }

    // Setters
    public void setPostId(int postId) { this.postId = postId; }
    public void setContent(String content) { this.content = content; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setParentPostId(int parentPostId) { this.parentPostId = parentPostId; }
}