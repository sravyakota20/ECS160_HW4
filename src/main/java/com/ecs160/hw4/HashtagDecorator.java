package com.ecs160.hw4;

public class HashtagDecorator implements SocialMediaComponent {
    private SocialMediaComponent component;
    private String hashtag;

    public HashtagDecorator(SocialMediaComponent component, String hashtag) {
        this.component = component;
        this.hashtag = hashtag;
    }

    @Override
    public int getLikeCount() {
        return component.getLikeCount();
    }

    @Override
    public void printPost() {
        component.printPost();
    }

    @Override
    public void accept(PostVisitor visitor) {
        // dumm impl
    }

    public String getHashtag() {
        return hashtag;
    }
}
