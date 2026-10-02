package com.studyos.app.util;

import com.studyos.app.data.StudySession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Pure statistics helpers (no Android dependencies, unit tested). */
public final class StatsCalculator {
    private StatsCalculator() {}

    /**
     * Streaks over a set of period indexes (days or weeks).
     * @return {current, longest}. The current streak may end at currentPeriod or currentPeriod - 1.
     */
    public static int[] streaks(Set<Long> periods, long currentPeriod) {
        int current = 0;
        long p = periods.contains(currentPeriod) ? currentPeriod : currentPeriod - 1;
        while (periods.contains(p)) {
            current++;
            p--;
        }
        List<Long> sorted = new ArrayList<>(periods);
        Collections.sort(sorted);
        int longest = 0;
        int run = 0;
        Long prev = null;
        for (Long v : sorted) {
            if (prev != null && v == prev + 1) run++;
            else run = 1;
            if (run > longest) longest = run;
            prev = v;
        }
        return new int[]{current, Math.max(longest, current)};
    }

    /** Monday-based week index of a day number. */
    public static long weekIndex(long dayNumber) {
        return Math.floorDiv(dayNumber + 3, 7L);
    }

    public static long sum(List<StudySession> list, long from, long to) {
        long total = 0;
        for (StudySession s : list) {
            if (s.startTime >= from && s.startTime < to) total += s.durationSec;
        }
        return total;
    }

    /** Sums session seconds into consecutive buckets [bounds[i], bounds[i+1]). */
    public static long[] bucket(List<StudySession> list, long[] bounds) {
        long[] out = new long[Math.max(0, bounds.length - 1)];
        for (StudySession s : list) {
            for (int i = 0; i < out.length; i++) {
                if (s.startTime >= bounds[i] && s.startTime < bounds[i + 1]) {
                    out[i] += s.durationSec;
                    break;
                }
            }
        }
        return out;
    }

    public static Set<Long> studyDays(List<StudySession> list) {
        Set<Long> days = new HashSet<>();
        for (StudySession s : list) days.add(TimeUtil.dayNumber(s.startTime));
        return days;
    }
}
