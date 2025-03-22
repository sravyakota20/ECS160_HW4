package com.ecs160.hw4;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplyTest {
    private Reply reply;

    @BeforeEach
    void setUp() {
        reply = new Reply(21, "This is not funny", "2025-01-31", 1);
    }

    @Test
    void testReplyCreation() {
        assertEquals(21, reply.getPostId());
        assertEquals(1, reply.getParentPostId());
        assertEquals("2025-01-31", reply.getCreatedAt());
    }

    @Test
    void testReplyModification() {
        reply.setPostId(2);
        reply.setContent("I do not agree");
        reply.setCreatedAt("2025-02-01");
        assertEquals(2, reply.getPostId());
        //assertEquals(20, reply.getContent());
        assertEquals("2025-02-01", reply.getCreatedAt());
    }
}
