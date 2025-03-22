package com.ecs160.hw4;

public interface PostVisitor {
    void visit(Post post);
    void visit(Thread thread);
}
