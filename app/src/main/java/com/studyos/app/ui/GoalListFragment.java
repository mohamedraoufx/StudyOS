package com.studyos.app.ui;

import androidx.lifecycle.LiveData;

import com.studyos.app.data.Goal;
import com.studyos.app.data.Milestone;
import com.studyos.app.util.LiveUtil;
import com.studyos.app.util.Metrics;
import com.studyos.app.util.TimeUtil;

import java.util.List;

public class GoalListFragment extends CrudListFragment<Goal> {
    @Override protected String title() { return null; }
    @Override protected LiveData<List<Goal>> source() { return vm.goals; }
    @Override protected void onAdd() { Forms.goal(requireContext(), vm, null); }
    @Override protected void onItem(Goal g) { Forms.goal(requireContext(), vm, g); }
    @Override protected String emptyText() { return "No goals yet. Tap + to set one."; }

    @Override
    protected void observeExtra() {
        vm.milestones.observe(getViewLifecycleOwner(), l -> refresh());
        vm.tasks.observe(getViewLifecycleOwner(), l -> refresh());
    }

    @Override
    protected void bindRow(RowView row, Goal g) {
        int total = 0, done = 0;
        for (Milestone m : LiveUtil.nz(vm.milestones.getValue())) {
            if (m.goalId == g.id) {
                total++;
                if (m.done) done++;
            }
        }
        int pct = Metrics.goalProgress(g, LiveUtil.nz(vm.milestones.getValue()), LiveUtil.nz(vm.tasks.getValue()));
        String sub = Ui.join(" \u2022 ", total > 0 ? done + "/" + total + " milestones" : "",
                g.deadline > 0 ? "Due " + TimeUtil.shortDate(g.deadline) : "");
        row.texts(g.title, sub, pct + "%").progress(pct);
    }
}
