package com.ecs160.hw4;

import java.util.ArrayList;
import java.util.List;

import com.ecs160.hw4.persistence.*;

@Persistable
public class Post implements SocialMediaComponent {
    @PersistableId
    private int postId;

    @PersistableField
    private int numWords; // Number of words in the post

    @PersistableField
    private int likeCount;

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

    public Post(int postId, String postContent, String createdAt, int likeCount) {
        this.postId = postId;
        this.text = postContent;
        this.numWords = postContent.split("\\s+").length;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
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

    @Override
    public int getLikeCount() {
        return likeCount;
    }
    public void setText(String text) {
        this.text = text;
        this.numWords = text.split("\\s+").length;
    }

    public void setReplies(List<Reply> replies) { this.replies = replies; }

    public String toString() {
        return "PostId: " + postId + " Content: " + getPostContent() + " Replies: " + replies.size();
    }

    @Override
    public void printPost() {
        System.out.println("Post: " + text);
    }

    @Override
    public void accept(PostVisitor visitor) {
        visitor.visit(this);
    }
}