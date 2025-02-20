package com.ecs160.hw2;

import java.util.ArrayList;
import java.util.List;
import com.ecs160.hw2.persistence.Persistable;
import com.ecs160.hw2.persistence.PersistableId;
import com.ecs160.hw2.persistence.PersistableField;
import com.ecs160.hw2.persistence.PersistableListField;
import java.util.List;

@Persistable
public class Post {
    @PersistableId
    private int postId;

    @PersistableField
    private int numWords; // Number of words in the post

    @PersistableField
    private Record record;

    @PersistableListField(className = "com.ecs160.hw2.Post")
    private List<Post> replies;

    // Default Constructor
    public Post(){
        this.numWords = 0;
        this.record = null;
    }

    public Post(int postId, int numWords, Record record) {
        this.postId = postId;
        this.numWords = numWords;
        this.record = record;
        this.replies = new ArrayList<>();
    }

    public Post(int postId, String postContent) {
        this.postId = postId;
        this.record = new Record(postContent);
        this.numWords = postContent.split("\\s+").length;
        this.replies = new ArrayList<>();
    }

    public int getPostId() {
        return postId;
    }

    public int getNumberOfWords() {
        return numWords;
    }

    public Record getRecord() {
        return record;
    }

    public String getText() {
        return record.getText();
    }

    public String getPostContent() {
        return record.getText();
    }

    public List<Post> getReplies() {
        return replies;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public void setNumberOfWords(int numWords) {
        this.numWords = numWords;
    }

    public void setReplies(List<Post> replies) {
        this.replies = replies;
    }
}
