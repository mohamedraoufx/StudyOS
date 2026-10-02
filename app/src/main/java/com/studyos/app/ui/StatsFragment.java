package com.studyos.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.studyos.app.R;
import com.studyos.app.data.Goal;
import com.studyos.app.data.Habit;
import com.studyos.app.data.StudySession;
import com.studyos.app.data.Subject;
import com.studyos.app.data.Task;
import com.studyos.app.util.LiveUtil;
import com.studyos.app.util.Metrics;
import com.studyos.app.util.StatsCalculator;
import com.studyos.app.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsFragment extends Fragment {
    private MainViewModel vm;
    private LinearLayout content;
    private int period = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        Context c = requireContext();
        vm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        LinearLayout col = Ui.vbox(c);
        col.addView(Ui.title(c, "Statistics"), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 12, 16, 4));
        TabLayout tl = new TabLayout(c);
        tl.setBackgroundColor(Ui.col(c, R.color.so_bg));
        for (String t : new String[]{"Daily", "Weekly", "Monthly", "Yearly"}) tl.addTab(tl.newTab().setText(t));
        tl.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab t) {
                period = t.getPosition();
                render();
            }
            @Override public void onTabUnselected(TabLayout.Tab t) {}
            @Override public void onTabReselected(TabLayout.Tab t) {}
        });
        col.addView(tl, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        content = Ui.vbox(c);
        content.setPadding(0, Ui.dp(8), 0, Ui.dp(24));
        col.addView(Ui.scroll(c, content), new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));
        LiveUtil.observeAll(getViewLifecycleOwner(), this::render, vm.sessions, vm.tasks, vm.habits,
                vm.habitLogs, vm.goals, vm.milestones, vm.subjects);
        return col;
    }

    private void line(LinearLayout parent, String label, String value) {
        Context c = requireContext();
        LinearLayout r = Ui.hbox(c);
        r.addView(Ui.text(c, label, 14, false, R.color.so_text2), new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        r.addView(Ui.text(c, value, 14, true, R.color.so_text));
        parent.addView(r, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 4, 0, 4));
    }

    private void barLine(LinearLayout parent, String label, String value, int pct) {
        Context c = requireContext();
        LinearLayout r = Ui.hbox(c);
        r.addView(Ui.text(c, label, 14, true, R.color.so_text), new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        r.addView(Ui.text(c, value, 13, false, R.color.so_text2));
        parent.addView(r, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 8, 0, 2));
        parent.addView(Ui.progress(c, pct, R.color.so_primary));
    }

    private void render() {
        if (!isAdded() || content == null) return;
        Context c = requireContext();
        content.removeAllViews();
        long now = System.currentTimeMillis();
        long today = TimeUtil.startOfDay(now);
        List<StudySession> sessions = LiveUtil.nz(vm.sessions.getValue());
        List<Task> tasks = LiveUtil.nz(vm.tasks.getValue());
        List<Subject> subjects = LiveUtil.nz(vm.subjects.getValue());

        // ---- totals ----
        long weekStart = TimeUtil.startOfWeek(now);
        long monthStart = TimeUtil.startOfMonth(now);
        long yearStart = TimeUtil.startOfYear(now);
        long all = 0;
        for (StudySession s : sessions) all += s.durationSec;
        MaterialCardView totals = Ui.card(c);
        LinearLayout tl = Ui.vbox(c);
        tl.addView(Ui.section(c, "Study time"));
        line(tl, "Today", TimeUtil.duration(StatsCalculator.sum(sessions, today, today + TimeUtil.DAY)));
        line(tl, "This week", TimeUtil.duration(StatsCalculator.sum(sessions, weekStart, TimeUtil.addDays(weekStart, 7))));
        line(tl, "This month", TimeUtil.duration(StatsCalculator.sum(sessions, monthStart, TimeUtil.addMonths(monthStart, 1))));
        line(tl, "This year", TimeUtil.duration(StatsCalculator.sum(sessions, yearStart, TimeUtil.addMonths(yearStart, 12))));
        line(tl, "All time", TimeUtil.duration(all));
        totals.addView(tl);
        content.addView(totals);

        // ---- chart ----
        int n = period == 0 ? 7 : period == 1 ? 8 : period == 2 ? 12 : 5;
        long[] bounds = new long[n + 1];
        String[] labels = new String[n];
        for (int i = 0; i <= n; i++) {
            int back = i - n + 1;
            if (period == 0) bounds[i] = TimeUtil.addDays(today, back);
            else if (period == 1) bounds[i] = TimeUtil.addDays(weekStart, back * 7);
            else if (period == 2) bounds[i] = TimeUtil.addMonths(monthStart, back);
            else bounds[i] = TimeUtil.addMonths(yearStart, back * 12);
        }
        String pattern = period == 0 ? "EEE" : period == 1 ? "d/M" : period == 2 ? "MMM" : "yyyy";
        for (int i = 0; i < n; i++) labels[i] = TimeUtil.format(bounds[i], pattern);
        long[] sums = StatsCalculator.bucket(sessions, bounds);
        float[] hours = new float[n];
        long chartTotal = 0;
        for (int i = 0; i < n; i++) {
            hours[i] = sums[i] / 3600f;
            chartTotal += sums[i];
        }
        String[] names = {"Last 7 days (hours)", "Last 8 weeks (hours)", "Last 12 months (hours)", "Last 5 years (hours)"};
        content.addView(Ui.section(c, names[period] + " \u2022 " + TimeUtil.duration(chartTotal)), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 14, 16, 0));
        MaterialCardView chartCard = Ui.card(c);
        BarChartView chart = new BarChartView(c);
        chart.setData(hours, labels);
        chartCard.addView(chart, new ViewGroup.LayoutParams(Ui.MATCH, Ui.dp(170)));
        content.addView(chartCard);

        // ---- distribution (last 30 days) ----
        Map<String, Long> bySubject = new HashMap<>();
        long since = TimeUtil.addDays(today, -29);
        long distTotal = 0;
        for (StudySession s : sessions) {
            if (s.startTime < since) continue;
            Long cur = bySubject.get(s.subjectName);
            bySubject.put(s.subjectName, (cur == null ? 0L : cur) + s.durationSec);
            distTotal += s.durationSec;
        }
        List<Map.Entry<String, Long>> entries = new ArrayList<>(bySubject.entrySet());
        Collections.sort(entries, (a, b) -> Long.compare(b.getValue(), a.getValue()));
        content.addView(Ui.section(c, "Subjects (last 30 days)"), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 14, 16, 0));
        MaterialCardView dist = Ui.card(c);
        LinearLayout dl = Ui.vbox(c);
        if (entries.isEmpty()) dl.addView(Ui.text(c, "No study sessions in the last 30 days.", 14, false, R.color.so_text2));
        for (int i = 0; i < Math.min(8, entries.size()); i++) {
            Map.Entry<String, Long> e = entries.get(i);
            barLine(dl, e.getKey(), TimeUtil.duration(e.getValue()), Metrics.pct(e.getValue(), distTotal));
        }
        dist.addView(dl);
        content.addView(dist);

        // ---- streaks, focus, tasks ----
        int[] st = StatsCalculator.streaks(StatsCalculator.studyDays(sessions), TimeUtil.dayNumber(now));
        int focusWeek = 0, focusAll = 0;
        long focusSec = 0;
        for (StudySession s : sessions) {
            if (!StudySession.SRC_POMODORO.equals(s.source)) continue;
            focusAll++;
            focusSec += s.durationSec;
            if (s.startTime >= weekStart) focusWeek++;
        }
        int done = 0, doneWeek = 0;
        for (Task t : tasks) {
            if (t.status == Task.DONE) {
                done++;
                if (t.completedAt >= weekStart) doneWeek++;
            }
        }
        MaterialCardView misc = Ui.card(c);
        LinearLayout ml = Ui.vbox(c);
        ml.addView(Ui.section(c, "Streaks, focus and tasks"));
        line(ml, "Current study streak", st[0] + " days");
        line(ml, "Longest study streak", st[1] + " days");
        line(ml, "Focus sessions (week / all)", focusWeek + " / " + focusAll);
        line(ml, "Focus time (all)", TimeUtil.duration(focusSec));
        line(ml, "Tasks completed (week / all)", doneWeek + " / " + done);
        line(ml, "Task completion rate", Metrics.pct(done, tasks.size()) + "%");
        misc.addView(ml);
        content.addView(misc);

        // ---- habits ----
        List<Habit> habits = LiveUtil.nz(vm.habits.getValue());
        if (!habits.isEmpty()) {
            content.addView(Ui.section(c, "Habit completion (recent)"), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 14, 16, 0));
            MaterialCardView hc = Ui.card(c);
            LinearLayout hl = Ui.vbox(c);
            long todayNo = TimeUtil.dayNumber(now);
            for (Habit h : habits) {
                int pct = Metrics.habitCompletion(Metrics.habitPeriods(LiveUtil.nz(vm.habitLogs.getValue()), h.id, h.weekly), todayNo, h.weekly);
                barLine(hl, h.name, pct + "%", pct);
            }
            hc.addView(hl);
            content.addView(hc);
        }

        // ---- goals ----
        List<Goal> goals = LiveUtil.nz(vm.goals.getValue());
        if (!goals.isEmpty()) {
            content.addView(Ui.section(c, "Goals"), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 14, 16, 0));
            MaterialCardView gc = Ui.card(c);
            LinearLayout gl = Ui.vbox(c);
            for (Goal g : goals) {
                int pct = Metrics.goalProgress(g, LiveUtil.nz(vm.milestones.getValue()), tasks);
                barLine(gl, g.title, pct + "%", pct);
            }
            gc.addView(gl);
            content.addView(gc);
        }

        // ---- languages ----
        List<Subject> langs = new ArrayList<>();
        for (Subject s : subjects) if (Subject.LANG.equals(s.category)) langs.add(s);
        if (!langs.isEmpty()) {
            content.addView(Ui.section(c, "Language levels"), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 14, 16, 0));
            MaterialCardView lc = Ui.card(c);
            LinearLayout ll = Ui.vbox(c);
            for (Subject l : langs) {
                int lv = Metrics.languageLevel(subjects, l.name);
                barLine(ll, l.name, lv + "%", lv);
            }
            lc.addView(ll);
            content.addView(lc);
        }
    }
}
