package com.studyos.app.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class StatsCalculatorTest {

    private static HashSet<Long> set(Long... values) {
        return new HashSet<>(Arrays.asList(values));
    }

    @Test public void emptySetHasNoStreak() {
        assertArrayEquals(new int[]{0, 0}, StatsCalculator.streaks(Collections.<Long>emptySet(), 100));
    }

    @Test public void currentStreakIncludingToday() {
        assertArrayEquals(new int[]{3, 3}, StatsCalculator.streaks(set(10L, 11L, 12L), 12));
    }

    @Test public void currentStreakSurvivesUntilEndOfToday() {
        assertArrayEquals(new int[]{2, 2}, StatsCalculator.streaks(set(10L, 11L), 12));
    }

    @Test public void brokenStreakIsZeroButLongestIsKept() {
        assertArrayEquals(new int[]{0, 4}, StatsCalculator.streaks(set(1L, 2L, 3L, 4L, 10L), 20));
    }

    @Test public void longestCanBeLongerThanCurrent() {
        assertArrayEquals(new int[]{1, 4}, StatsCalculator.streaks(set(1L, 2L, 3L, 4L, 10L), 10));
    }

    @Test public void weekIndexStartsOnMonday() {
        // 1970-01-05 was a Monday (day 4); 1970-01-04 a Sunday (day 3).
        assertEquals(StatsCalculator.weekIndex(4), StatsCalculator.weekIndex(10));
        assertEquals(StatsCalculator.weekIndex(3) + 1, StatsCalculator.weekIndex(4));
    }
}
