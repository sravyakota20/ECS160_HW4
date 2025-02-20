package com.ecs160.hw2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplyTest {
    private Reply reply;

    @BeforeEach
    void setUp() {
        reply = new Reply(1, 10, "2025-01-31");
    }

    @Test
    void testReplyCreation() {
        assertEquals(1, reply.getPostId());
        assertEquals(10, reply.getNumWords());
        assertEquals("2025-01-31", reply.getCreatedAt());
    }

    @Test
    void testReplyModification() {
        reply.setPostId(2);
        reply.setNumWords(20);
        reply.setCreatedAt("2025-02-01");
        assertEquals(2, reply.getPostId());
        assertEquals(20, reply.getNumWords());
        assertEquals("2025-02-01", reply.getCreatedAt());
    }
}
