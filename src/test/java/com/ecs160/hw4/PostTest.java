package com.ecs160.hw4;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostTest {
    private Post post;

    @BeforeEach
    void setUp() {
        post = new Post(101, "Test Content", "2023-07-17T21:13:20.284Z", 10);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testPostCreation() {
        assertEquals(2, post.getNumberOfWords());
        assertEquals("Test Content", post.getText());
    }
}
