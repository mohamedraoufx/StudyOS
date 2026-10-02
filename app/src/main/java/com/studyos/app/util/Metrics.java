package com.studyos.app.util;

import com.studyos.app.data.Goal;
import com.studyos.app.data.HabitLog;
import com.studyos.app.data.Milestone;
import com.studyos.app.data.Project;
import com.studyos.app.data.Subject;
import com.studyos.app.data.Task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Derived progress numbers shared by several screens. */
public final class Metrics {
    private Metrics() {}

    public static int pct(long part, long total) {
        if (total <= 0) return 0;
        return (int) Math.min(100, part * 100 / total);
    }

    /** Milestone based progress; falls back to linked tasks when there are no milestones. */
    public static int goalProgress(Goal g, List<Milestone> milestones, List<Task> tasks) {
        int total = 0, done = 0;
        for (Milestone m : milestones) {
            if (m.goalId == g.id) {
                total++;
                if (m.done) done++;
            }
        }
        if (total > 0) return pct(done, total);
        int tt = 0, td = 0;
        for (Task t : tasks) {
            if (t.goalId != null && t.goalId.longValue() == g.id) {
                tt++;
                if (t.status == Task.DONE) td++;
            }
        }
        return pct(td, tt);
    }

    /** {done, total} of the tasks linked to a project. */
    public static int[] projectTasks(Project p, List<Task> tasks) {
        int total = 0, done = 0;
        for (Task t : tasks) {
            if (t.projectId != null && t.projectId.longValue() == p.id) {
                total++;
                if (t.status == Task.DONE) done++;
            }
        }
        return new int[]{done, total};
    }

    /** Average level of the skills of a language. */
    public static int languageLevel(List<Subject> subjects, String language) {
        int sum = 0, n = 0;
        for (Subject s : subjects) {
            if (Subject.SKILL.equals(s.category) && language.equals(s.groupName)) {
                sum += s.level;
                n++;
            }
        }
        return n == 0 ? 0 : sum / n;
    }

    public static Set<Long> habitPeriods(List<HabitLog> logs, long habitId, boolean weekly) {
        Set<Long> out = new HashSet<>();
        for (HabitLog l : logs) {
            if (l.habitId == habitId) out.add(weekly ? StatsCalculator.weekIndex(l.day) : l.day);
        }
        return out;
    }

    /** Completion of the last 30 days (daily) or last 4 weeks (weekly), 0..100. */
    public static int habitCompletion(Set<Long> periods, long today, boolean weekly) {
        int span = weekly ? 4 : 30;
        long current = weekly ? StatsCalculator.weekIndex(today) : today;
        int hit = 0;
        for (int i = 0; i < span; i++) {
            if (periods.contains(current - i)) hit++;
        }
        return pct(hit, span);
    }
}
