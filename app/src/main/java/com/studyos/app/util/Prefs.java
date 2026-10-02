package com.studyos.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/** App settings stored in SharedPreferences. */
public final class Prefs {
    private final SharedPreferences sp;

    public Prefs(Context c) {
        sp = c.getApplicationContext().getSharedPreferences("studyos_prefs", Context.MODE_PRIVATE);
    }

    public int dailyGoalMin() { return sp.getInt("daily_goal_min", 360); }
    public void setDailyGoalMin(int v) { sp.edit().putInt("daily_goal_min", v).apply(); }

    public int focusMin() { return sp.getInt("focus_min", 25); }
    public void setFocusMin(int v) { sp.edit().putInt("focus_min", v).apply(); }

    public int breakMin() { return sp.getInt("break_min", 5); }
    public void setBreakMin(int v) { sp.edit().putInt("break_min", v).apply(); }

    /** 0 = follow system, 1 = light, 2 = dark. */
    public int theme() { return sp.getInt("theme", 0); }
    public void setTheme(int v) { sp.edit().putInt("theme", v).apply(); }

    public boolean notifications() { return sp.getBoolean("notifications", true); }
    public void setNotifications(boolean v) { sp.edit().putBoolean("notifications", v).apply(); }

    public int reminderHour() { return sp.getInt("reminder_hour", 20); }
    public void setReminderHour(int v) { sp.edit().putInt("reminder_hour", v).apply(); }

    public boolean seeded() { return sp.getBoolean("seeded", false); }
    public void setSeeded() { sp.edit().putBoolean("seeded", true).apply(); }

    public int nightMode() {
        switch (theme()) {
            case 1: return AppCompatDelegate.MODE_NIGHT_NO;
            case 2: return AppCompatDelegate.MODE_NIGHT_YES;
            default: return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
}
