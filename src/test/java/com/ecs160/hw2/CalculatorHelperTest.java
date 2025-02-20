package com.ecs160.hw2;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;

class CalculatorHelperTest {
    private CalculatorHelper calculatorHelper;

    @BeforeEach
    void setUp() {
        ArrayList<Integer> numReplies = new ArrayList<>(Arrays.asList(2, 3, 5));
        calculatorHelper = new CalculatorHelper(3, 10, numReplies);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getTotalNumberOfPost() {
        int totalPosts = calculatorHelper.getTotalNumberOfPost();
        assertEquals(3, totalPosts);
    }

    @Test
    void calculateAverageReplyPerPost() {
        double avgReplies = calculatorHelper.calculateAverageReplyPerPost();
        assertEquals(3.33, avgReplies, 0.01);
    }

    @Test
    void weightedTotalPost() {
        double weightedTotal = calculatorHelper.weightedTotalPost(3);
        assertTrue(weightedTotal > 0);
    }

    @Test
    void weightedAvgNumReplies() {
        double weightedAvgReplies = calculatorHelper.weightedAvgNumReplies(3);
        assertTrue(weightedAvgReplies > 0);
    }

    @Test
    void calculateAverageIntervalBetweenComments() {
        String avgInterval = calculatorHelper.calculateAverageIntervalBetweenComments();
        assertNotNull(avgInterval);
    }
}
