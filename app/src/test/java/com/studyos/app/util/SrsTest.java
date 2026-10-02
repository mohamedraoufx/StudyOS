package com.studyos.app.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.studyos.app.data.Vocab;

import org.junit.Test;

public class SrsTest {

    @Test public void againResetsInterval() {
        assertEquals(0, Srs.nextInterval(10, Srs.AGAIN));
    }

    @Test public void goodDoublesInterval() {
        assertEquals(1, Srs.nextInterval(0, Srs.GOOD));
        assertEquals(8, Srs.nextInterval(4, Srs.GOOD));
    }

    @Test public void easyGrowsFaster() {
        assertEquals(4, Srs.nextInterval(0, Srs.EASY));
        assertEquals(18, Srs.nextInterval(6, Srs.EASY));
    }

    @Test public void cardBecomesLearnedAfterThreeWeeks() {
        Vocab v = new Vocab();
        v.intervalDays = 12;
        Srs.apply(v, Srs.GOOD, 1_000L);
        assertEquals(24, v.intervalDays);
        assertTrue(v.learned);
        assertEquals(1_000L + 24 * TimeUtil.DAY, v.reviewDate);
    }

    @Test public void againKeepsCardDueNow() {
        Vocab v = new Vocab();
        v.intervalDays = 30;
        v.learned = true;
        Srs.apply(v, Srs.AGAIN, 5_000L);
        assertEquals(5_000L, v.reviewDate);
        assertFalse(v.learned);
    }
}
