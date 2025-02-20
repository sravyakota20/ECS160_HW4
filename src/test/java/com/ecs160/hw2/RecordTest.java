package com.ecs160.hw2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordTest {
    private Record record;

    @BeforeEach
    void setUp() {
        record = new Record("Test content");
    }

    @Test
    void testRecordModification() {
        record.setText("Updated content");
        assertEquals("Updated content", record.getText());
    }
}
