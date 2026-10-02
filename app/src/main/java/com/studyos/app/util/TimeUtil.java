package com.studyos.app.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/** Date/time helpers. All formatting uses English so the UI stays consistent. */
public final class TimeUtil {
    public static final long DAY = 86_400_000L;

    private TimeUtil() {}

    public static long startOfDay(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long addDays(long ms, int days) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        c.add(Calendar.DAY_OF_YEAR, days);
        return c.getTimeInMillis();
    }

    public static long addMonths(long ms, int months) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        c.add(Calendar.MONTH, months);
        return c.getTimeInMillis();
    }

    /** Start of the week (Monday 00:00). */
    public static long startOfWeek(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startOfDay(ms));
        int diff = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        c.add(Calendar.DAY_OF_YEAR, -diff);
        return c.getTimeInMillis();
    }

    public static long startOfMonth(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startOfDay(ms));
        c.set(Calendar.DAY_OF_MONTH, 1);
        return c.getTimeInMillis();
    }

    public static long startOfYear(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(startOfDay(ms));
        c.set(Calendar.DAY_OF_YEAR, 1);
        return c.getTimeInMillis();
    }

    public static int daysInMonth(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        return c.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static int minuteOfDay(long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        return c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
    }

    /** Local day number since the epoch (day 0 = 1970-01-01). */
    public static long dayNumber(long ms) {
        return Math.floorDiv(ms + TimeZone.getDefault().getOffset(ms), DAY);
    }

    /** "2h 15m", "2h", "45m". */
    public static String duration(long seconds) {
        long min = Math.max(0, seconds) / 60;
        long h = min / 60;
        long m = min % 60;
        if (h > 0 && m > 0) return h + "h " + m + "m";
        if (h > 0) return h + "h";
        return m + "m";
    }

    /** "mm:ss" for a countdown. */
    public static String clock(long ms) {
        long s = (Math.max(0, ms) + 999) / 1000;
        return String.format(Locale.US, "%02d:%02d", s / 60, s % 60);
    }

    public static String format(long ms, String pattern) {
        return new SimpleDateFormat(pattern, Locale.ENGLISH).format(new Date(ms));
    }

    public static String shortDate(long ms) {
        return format(ms, "d MMM");
    }

    public static String hhmm(int minuteOfDay) {
        return String.format(Locale.US, "%02d:%02d", minuteOfDay / 60, minuteOfDay % 60);
    }
}
