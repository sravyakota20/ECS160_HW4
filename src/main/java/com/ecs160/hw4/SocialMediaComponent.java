package com.ecs160.hw4;

public interface SocialMediaComponent {
    int getLikeCount();
    void printPost();
    void accept(PostVisitor visitor);
}
