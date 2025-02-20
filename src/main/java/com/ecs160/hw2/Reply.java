package com.ecs160.hw2;

public class Reply {
    private int postId; // Replies to the post
    private int numWords;
    private String createdAt;

    public Reply(int postId, int numWords, String createdAt){
        this.postId = postId;
        this.numWords = numWords;
        this.createdAt = createdAt;
    }

    public Reply(){
        this.postId = -1;
        this.numWords = 0;
        this.createdAt = "";
    }

    public int getPostId(){
        return this.postId;
    }

    public int getNumWords(){
        return this.numWords;
    }

    public String getCreatedAt(){
        return this.createdAt;
    }

    public void setPostId(int postId){
        this.postId = postId;
    }

    public void setNumWords(int numWords){
        this.numWords = numWords;
    }

    public void setCreatedAt(String createdAt){
        this.createdAt = createdAt;
    }
}
