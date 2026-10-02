package com.studyos.app.ui;

import androidx.lifecycle.LiveData;

import com.studyos.app.MainActivity;
import com.studyos.app.data.StudySession;
import com.studyos.app.data.Subject;
import com.studyos.app.util.LiveUtil;
import com.studyos.app.util.Metrics;
import com.studyos.app.util.StatsCalculator;
import com.studyos.app.util.TimeUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LanguagesFragment extends CrudListFragment<Subject> {
    @Override protected String title() { return "Languages"; }
    @Override protected LiveData<List<Subject>> source() { return vm.subjects; }
    @Override protected void onAdd() { Forms.subject(requireContext(), vm, null); }
    @Override protected String emptyText() { return "No languages yet. Tap + and choose type \"Language\"."; }

    @Override
    protected void onItem(Subject s) {
        ((MainActivity) requireActivity()).open(LanguageDetailFragment.of(s.name));
    }

    @Override
    protected void onLongItem(Subject s) { Forms.subject(requireContext(), vm, s); }

    @Override
    protected void observeExtra() {
        vm.sessions.observe(getViewLifecycleOwner(), l -> refresh());
    }

    @Override
    protected List<Subject> transform(List<Subject> in) {
        List<Subject> out = new ArrayList<>();
        for (Subject s : in) if (Subject.LANG.equals(s.category)) out.add(s);
        return out;
    }

    @Override
    protected String subtitle(List<Subject> shown) {
        return "Tap a language for skills and stats. Long-press to rename or delete.";
    }

    @Override
    protected void bindRow(RowView row, Subject lang) {
        long today = TimeUtil.startOfDay(System.currentTimeMillis());
        long total = 0, todaySec = 0;
        Set<Long> days = new HashSet<>();
        for (StudySession s : LiveUtil.nz(vm.sessions.getValue())) {
            if (!lang.name.equals(s.subjectName)) continue;
            total += s.durationSec;
            days.add(TimeUtil.dayNumber(s.startTime));
            if (s.startTime >= today && s.startTime < today + TimeUtil.DAY) todaySec += s.durationSec;
        }
        int level = Metrics.languageLevel(LiveUtil.nz(vm.subjects.getValue()), lang.name);
        int[] st = StatsCalculator.streaks(days, TimeUtil.dayNumber(System.currentTimeMillis()));
        row.texts(lang.name, Ui.join(" \u2022 ", "Today " + TimeUtil.duration(todaySec),
                "Total " + TimeUtil.duration(total), "Streak " + st[0]), level + "%").progress(level);
    }
}
