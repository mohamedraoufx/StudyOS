package com.studyos.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.studyos.app.R;
import com.studyos.app.data.DailyAllocation;
import com.studyos.app.data.StudySession;
import com.studyos.app.data.Task;
import com.studyos.app.util.LiveUtil;
import com.studyos.app.util.Metrics;
import com.studyos.app.util.Prefs;
import com.studyos.app.util.StatsCalculator;
import com.studyos.app.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Dashboard. */
public class HomeFragment extends Fragment {
    private MainViewModel vm;
    private LinearLayout content;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        Context c = requireContext();
        vm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        content = Ui.vbox(c);
        content.setPadding(0, Ui.dp(12), 0, Ui.dp(24));
        LiveUtil.observeAll(getViewLifecycleOwner(), this::render, vm.sessions, vm.tasks, vm.allocations);
        return Ui.scroll(c, content);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (content != null) render();
    }

    private View tile(Context c, String label, String value) {
        MaterialCardView v = Ui.card(c);
        LinearLayout l = Ui.vbox(c);
        l.addView(Ui.text(c, value, 22, true, R.color.so_text));
        l.addView(Ui.text(c, label, 12, false, R.color.so_text2));
        v.addView(l);
        v.setLayoutParams(Ui.lp(0, Ui.WRAP, 6, 6, 6, 6));
        ((LinearLayout.LayoutParams) v.getLayoutParams()).weight = 1f;
        return v;
    }

    private void tileRow(Context c, View a, View b) {
        LinearLayout r = Ui.hbox(c);
        r.addView(a);
        r.addView(b);
        content.addView(r, Ui.lp(Ui.MATCH, Ui.WRAP, 6, 0, 6, 0));
    }

    private RowView taskRow(Task t) {
        RowView row = new RowView(requireContext());
        String when = t.startMinute >= 0 ? TimeUtil.hhmm(t.startMinute) : "";
        String sub = Ui.join(" \u2022 ", t.subject, t.durationMin > 0 ? t.durationMin + " min" : "");
        row.texts(t.title, sub, when);
        final Task task = t;
        row.checkbox(t.status == Task.DONE, done -> vm.repo().setTaskDone(task, done));
        row.setOnClickListener(v -> Forms.task(requireContext(), vm, task));
        return row;
    }

    private void render() {
        if (!isAdded() || content == null) return;
        Context c = requireContext();
        Prefs prefs = new Prefs(c);
        long now = System.currentTimeMillis();
        long today = TimeUtil.startOfDay(now);
        List<StudySession> sessions = LiveUtil.nz(vm.sessions.getValue());
        List<Task> tasks = LiveUtil.nz(vm.tasks.getValue());
        List<DailyAllocation> allocations = LiveUtil.nz(vm.allocations.getValue());

        long todaySec = StatsCalculator.sum(sessions, today, today + TimeUtil.DAY);
        long goalSec = prefs.dailyGoalMin() * 60L;
        int pct = Metrics.pct(todaySec, goalSec);

        content.removeAllViews();
        content.addView(Ui.text(c, TimeUtil.format(now, "EEEE, d MMMM yyyy"), 13, false, R.color.so_text2),
                Ui.lp(Ui.MATCH, Ui.WRAP, 16, 0, 16, 0));
        content.addView(Ui.title(c, "Study OS"), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 0, 16, 4));

        // --- daily goal ---
        MaterialCardView hero = Ui.card(c);
        LinearLayout h = Ui.hbox(c);
        RingView ring = new RingView(c);
        ring.set(pct / 100f, pct + "%", "of goal");
        h.addView(ring, new LinearLayout.LayoutParams(Ui.dp(120), Ui.dp(120)));
        LinearLayout info = Ui.vbox(c);
        info.addView(Ui.text(c, "Studied today", 12, false, R.color.so_text2));
        info.addView(Ui.text(c, TimeUtil.duration(todaySec), 26, true, R.color.so_text));
        info.addView(Ui.text(c, "Goal: " + TimeUtil.duration(goalSec), 14, false, R.color.so_text));
        long remaining = Math.max(0, goalSec - todaySec);
        info.addView(Ui.text(c, remaining == 0 ? "Goal reached \uD83C\uDF89" : "Remaining: " + TimeUtil.duration(remaining),
                14, false, remaining == 0 ? R.color.so_green : R.color.so_text2));
        h.addView(info, Ui.lp(0, Ui.WRAP, 16, 0, 0, 0));
        ((LinearLayout.LayoutParams) info.getLayoutParams()).weight = 1f;
        hero.addView(h);
        content.addView(hero);

        // --- tiles ---
        int doneToday = 0, left = 0;
        for (Task t : tasks) {
            if (t.status == Task.DONE && t.completedAt >= today && t.completedAt < today + TimeUtil.DAY) doneToday++;
            if (t.status != Task.DONE && t.dueDate > 0 && t.dueDate <= today) left++;
        }
        int[] streak = StatsCalculator.streaks(StatsCalculator.studyDays(sessions), TimeUtil.dayNumber(now));
        int focusCount = 0;
        for (StudySession s : sessions) {
            if (StudySession.SRC_POMODORO.equals(s.source) && s.startTime >= today && s.startTime < today + TimeUtil.DAY) focusCount++;
        }
        tileRow(c, tile(c, "Tasks completed today", String.valueOf(doneToday)), tile(c, "Tasks due / overdue", String.valueOf(left)));
        tileRow(c, tile(c, "Study streak (best " + streak[1] + ")", streak[0] + " days"), tile(c, "Focus sessions today", String.valueOf(focusCount)));

        // --- goal breakdown ---
        if (!allocations.isEmpty()) {
            content.addView(Ui.section(c, "Today by subject"), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 14, 16, 0));
            MaterialCardView card = Ui.card(c);
            LinearLayout col = Ui.vbox(c);
            for (DailyAllocation a : allocations) {
                long sec = 0;
                for (StudySession s : sessions) {
                    if (a.name.equals(s.subjectName) && s.startTime >= today && s.startTime < today + TimeUtil.DAY) sec += s.durationSec;
                }
                LinearLayout line = Ui.hbox(c);
                TextView name = Ui.text(c, a.name, 14, true, R.color.so_text);
                line.addView(name, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
                line.addView(Ui.text(c, TimeUtil.duration(sec) + " / " + TimeUtil.duration(a.minutes * 60L), 13, false, R.color.so_text2));
                col.addView(line, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 6, 0, 2));
                col.addView(Ui.progress(c, Metrics.pct(sec, a.minutes * 60L), sec >= a.minutes * 60L ? R.color.so_green : R.color.so_primary));
            }
            card.addView(col);
            content.addView(card);
        }

        // --- schedule ---
        List<Task> todayTasks = new ArrayList<>();
        List<Task> upcoming = new ArrayList<>();
        for (Task t : tasks) {
            if (t.dueDate == today) todayTasks.add(t);
            else if (t.dueDate > today && t.status != Task.DONE) upcoming.add(t);
        }
        Collections.sort(todayTasks, new Comparator<Task>() {
            @Override public int compare(Task a, Task b) {
                int da = a.status == Task.DONE ? 1 : 0, db = b.status == Task.DONE ? 1 : 0;
                if (da != db) return da - db;
                int sa = a.startMinute < 0 ? 9999 : a.startMinute, sb = b.startMinute < 0 ? 9999 : b.startMinute;
                return sa != sb ? sa - sb : b.priority - a.priority;
            }
        });
        Collections.sort(upcoming, (a, b) -> Long.compare(a.dueDate, b.dueDate));
        content.addView(Ui.section(c, "Today's schedule"), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 14, 16, 4));
        if (todayTasks.isEmpty()) {
            content.addView(Ui.text(c, "No tasks for today.", 14, false, R.color.so_text2), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 0, 16, 0));
        }
        for (Task t : todayTasks) content.addView(taskRow(t), Ui.lp(Ui.MATCH, Ui.WRAP, 12, 4, 12, 4));

        content.addView(Ui.section(c, "Upcoming"), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 14, 16, 4));
        if (upcoming.isEmpty()) {
            content.addView(Ui.text(c, "Nothing upcoming.", 14, false, R.color.so_text2), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 0, 16, 0));
        }
        for (int i = 0; i < Math.min(5, upcoming.size()); i++) {
            Task t = upcoming.get(i);
            RowView row = taskRow(t);
            row.meta.setText(TimeUtil.shortDate(t.dueDate));
            row.meta.setVisibility(View.VISIBLE);
            content.addView(row, Ui.lp(Ui.MATCH, Ui.WRAP, 12, 4, 12, 4));
        }

        // --- weekly chart ---
        long[] bounds = new long[8];
        String[] labels = new String[7];
        float[] hours = new float[7];
        for (int i = 0; i < 8; i++) bounds[i] = TimeUtil.addDays(today, i - 6);
        long[] sums = StatsCalculator.bucket(sessions, bounds);
        for (int i = 0; i < 7; i++) {
            hours[i] = sums[i] / 3600f;
            labels[i] = TimeUtil.format(bounds[i], "EEE");
        }
        long weekStart = TimeUtil.startOfWeek(now);
        long weekSec = StatsCalculator.sum(sessions, weekStart, TimeUtil.addDays(weekStart, 7));
        content.addView(Ui.section(c, "This week: " + TimeUtil.duration(weekSec)), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 14, 16, 0));
        MaterialCardView chartCard = Ui.card(c);
        BarChartView chart = new BarChartView(c);
        chart.setData(hours, labels);
        chartCard.addView(chart, new ViewGroup.LayoutParams(Ui.MATCH, Ui.dp(160)));
        content.addView(chartCard);

        // --- monthly progress ---
        long monthStart = TimeUtil.startOfMonth(now);
        long monthSec = StatsCalculator.sum(sessions, monthStart, TimeUtil.addMonths(monthStart, 1));
        long monthGoal = goalSec * TimeUtil.daysInMonth(now);
        MaterialCardView month = Ui.card(c);
        LinearLayout mc = Ui.vbox(c);
        mc.addView(Ui.text(c, "This month", 12, false, R.color.so_text2));
        mc.addView(Ui.text(c, TimeUtil.duration(monthSec) + " of " + TimeUtil.duration(monthGoal), 16, true, R.color.so_text));
        mc.addView(Ui.progress(c, Metrics.pct(monthSec, monthGoal), R.color.so_primary), Ui.lp(Ui.MATCH, Ui.WRAP, 0, 8, 0, 0));
        month.addView(mc);
        content.addView(month);
    }
}
