package com.studyos.app.notify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.studyos.app.data.Goal;
import com.studyos.app.data.Task;
import com.studyos.app.util.Prefs;
import com.studyos.app.util.TimeUtil;

import java.util.Calendar;

/** Schedules local reminders with inexact alarms (no special permission needed). */
public final class Reminders {
    static final String EXTRA_KIND = "kind";
    static final String EXTRA_TITLE = "title";
    static final String EXTRA_TEXT = "text";
    static final String EXTRA_ID = "nid";
    static final String EXTRA_CHANNEL = "channel";
    static final String KIND_DAILY = "daily";

    private static final int NID_FOCUS = 2001;
    private static final int RC_DAILY = 3;
    private static final int RC_FOCUS = 4;
    private static final int RC_TASK = 1_000_000;
    private static final int RC_GOAL = 2_000_000;
    private static final int DEFAULT_START_MINUTE = 9 * 60;

    private Reminders() {}

    private static PendingIntent pending(Context c, int code, String kind, String channel, int nid,
                                         String title, String text) {
        Intent i = new Intent(c, ReminderReceiver.class);
        i.putExtra(EXTRA_KIND, kind);
        i.putExtra(EXTRA_CHANNEL, channel);
        i.putExtra(EXTRA_ID, nid);
        i.putExtra(EXTRA_TITLE, title);
        i.putExtra(EXTRA_TEXT, text);
        return PendingIntent.getBroadcast(c, code, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void set(Context c, PendingIntent pi, long whenMs) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        if (am != null) am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenMs, pi);
    }

    private static void cancel(Context c, PendingIntent pi) {
        AlarmManager am = c.getSystemService(AlarmManager.class);
        if (am != null) am.cancel(pi);
    }

    // ---- tasks ----
    public static void scheduleTask(Context c, Task t) {
        int code = RC_TASK + (int) (t.id % 900_000);
        PendingIntent pi = pending(c, code, "task", Notifier.CH_REMINDERS, code, "Task reminder", t.title);
        long trigger = 0;
        if (t.status != Task.DONE && t.reminderMin >= 0 && t.dueDate > 0) {
            int start = t.startMinute >= 0 ? t.startMinute : DEFAULT_START_MINUTE;
            trigger = t.dueDate + (start - t.reminderMin) * 60_000L;
        }
        if (trigger > System.currentTimeMillis()) set(c, pi, trigger);
        else cancel(c, pi);
    }

    public static void cancelTask(Context c, long taskId) {
        int code = RC_TASK + (int) (taskId % 900_000);
        cancel(c, pending(c, code, "task", Notifier.CH_REMINDERS, code, "", ""));
    }

    // ---- goals ----
    public static void scheduleGoal(Context c, Goal g) {
        int code = RC_GOAL + (int) (g.id % 900_000);
        PendingIntent pi = pending(c, code, "goal", Notifier.CH_REMINDERS, code, "Goal deadline today", g.title);
        long trigger = g.deadline > 0 ? g.deadline + DEFAULT_START_MINUTE * 60_000L : 0;
        if (trigger > System.currentTimeMillis()) set(c, pi, trigger);
        else cancel(c, pi);
    }

    public static void cancelGoal(Context c, long goalId) {
        int code = RC_GOAL + (int) (goalId % 900_000);
        cancel(c, pending(c, code, "goal", Notifier.CH_REMINDERS, code, "", ""));
    }

    // ---- daily study / habit reminder ----
    public static void scheduleDaily(Context c) {
        Prefs prefs = new Prefs(c);
        PendingIntent pi = pending(c, RC_DAILY, KIND_DAILY, Notifier.CH_REMINDERS, RC_DAILY,
                "Time to study", "Open Study OS: start a focus session and check your habits.");
        if (!prefs.notifications()) {
            cancel(c, pi);
            return;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(TimeUtil.startOfDay(System.currentTimeMillis()));
        cal.set(Calendar.HOUR_OF_DAY, prefs.reminderHour());
        if (cal.getTimeInMillis() <= System.currentTimeMillis() + 60_000L) cal.add(Calendar.DAY_OF_YEAR, 1);
        set(c, pi, cal.getTimeInMillis());
    }

    // ---- focus timer end ----
    public static void scheduleFocusEnd(Context c, long whenMs, boolean focusPhase) {
        String title = focusPhase ? "Focus session finished" : "Break is over";
        String text = focusPhase ? "Great work! Take a short break." : "Ready for the next focus session?";
        set(c, pending(c, RC_FOCUS, "focus", Notifier.CH_FOCUS, NID_FOCUS, title, text), whenMs);
    }

    public static void cancelFocusEnd(Context c) {
        cancel(c, pending(c, RC_FOCUS, "focus", Notifier.CH_FOCUS, NID_FOCUS, "", ""));
    }

    public static int focusNotificationId() {
        return NID_FOCUS;
    }
}
