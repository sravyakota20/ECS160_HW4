package com.ecs160.hw4;

import java.util.ArrayList;
import java.util.List;

public class Thread implements SocialMediaComponent {
    private Post mainPost;
    private List<SocialMediaComponent> replies;

    public Thread(Post mainPost) {
        this.mainPost = mainPost;
        this.replies = new ArrayList<>();
    }

    public void add(SocialMediaComponent component) {
        replies.add(component);
    }

    public void remove(SocialMediaComponent component) {
        replies.remove(component);
    }

    @Override
    public int getLikeCount() {
        int total = mainPost.getLikeCount();
        for (SocialMediaComponent comp : replies) {
            total += comp.getLikeCount();
        }
        return total;
    }

    @Override
    public void printPost() {
        System.out.println("Main Post: " + mainPost.getText());
        for (SocialMediaComponent comp : replies) {
            System.out.print("  Reply: ");
            comp.printPost();
        }
    }

    public Post getMainPost() {
        return mainPost;
    }

    public List<SocialMediaComponent> getReplies() {
        return replies;
    }

    @Override
    public void accept(PostVisitor visitor) {
        // Visit the composite itself first
        visitor.visit(this);
        // Then pass the visitor to each child element
        for (SocialMediaComponent child : replies) {
            child.accept(visitor);
        }
    }
}