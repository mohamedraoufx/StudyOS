package com.studyos.app.util;

import com.studyos.app.data.Vocab;

/** Tiny spaced-repetition scheduler for flashcards. */
public final class Srs {
    public static final int AGAIN = 0, HARD = 1, GOOD = 2, EASY = 3;

    private Srs() {}

    public static int nextInterval(int currentDays, int rating) {
        switch (rating) {
            case AGAIN: return 0;
            case HARD: return Math.max(1, currentDays);
            case GOOD: return currentDays <= 0 ? 1 : currentDays * 2;
            default: return currentDays <= 0 ? 4 : currentDays * 3;
        }
    }

    public static boolean isLearned(int intervalDays) {
        return intervalDays >= 21;
    }

    /** Applies a rating to a card (AGAIN leaves it due immediately). */
    public static void apply(Vocab v, int rating, long now) {
        int next = nextInterval(v.intervalDays, rating);
        v.intervalDays = next;
        v.reviewDate = next == 0 ? now : now + next * TimeUtil.DAY;
        v.learned = isLearned(next);
    }
}
