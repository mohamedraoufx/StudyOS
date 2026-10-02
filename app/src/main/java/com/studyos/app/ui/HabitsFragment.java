package com.studyos.app.ui;

import androidx.lifecycle.LiveData;

import com.studyos.app.R;
import com.studyos.app.data.Habit;
import com.studyos.app.util.LiveUtil;
import com.studyos.app.util.Metrics;
import com.studyos.app.util.StatsCalculator;
import com.studyos.app.util.TimeUtil;

import java.util.List;
import java.util.Set;

public class HabitsFragment extends CrudListFragment<Habit> {
    @Override protected String title() { return "Habits"; }
    @Override protected LiveData<List<Habit>> source() { return vm.habits; }
    @Override protected void onAdd() { Forms.habit(requireContext(), vm, null); }
    @Override protected void onItem(Habit h) { Forms.habit(requireContext(), vm, h); }
    @Override protected String emptyText() { return "No habits yet. Tap + to add one."; }

    @Override
    protected void observeExtra() {
        vm.habitLogs.observe(getViewLifecycleOwner(), l -> refresh());
    }

    @Override
    protected String subtitle(List<Habit> shown) {
        return "Tick a habit when done. Tap a row for the 14-day history.";
    }

    @Override
    protected void bindRow(RowView row, final Habit h) {
        long todayNo = TimeUtil.dayNumber(System.currentTimeMillis());
        Set<Long> periods = Metrics.habitPeriods(LiveUtil.nz(vm.habitLogs.getValue()), h.id, h.weekly);
        long current = h.weekly ? StatsCalculator.weekIndex(todayNo) : todayNo;
        boolean doneNow = periods.contains(current);
        int[] st = StatsCalculator.streaks(periods, current);
        int completion = Metrics.habitCompletion(periods, todayNo, h.weekly);
        String unit = h.weekly ? " weeks" : " days";
        String sub = Ui.join(" \u2022 ", h.weekly ? "Weekly" : "Daily", "Streak " + st[0] + unit,
                "Best " + st[1], completion + "% recent");
        row.texts(h.name, sub, "");
        row.checkbox(doneNow, checked -> {
            // Toggle for today (for weekly habits, today counts for this week).
            boolean hasToday = false;
            for (com.studyos.app.data.HabitLog l : LiveUtil.nz(vm.habitLogs.getValue())) {
                if (l.habitId == h.id && l.day == todayNo) hasToday = true;
            }
            if (checked != hasToday) vm.repo().toggleHabit(h.id, todayNo);
        });
        row.progress(completion);
    }
}
