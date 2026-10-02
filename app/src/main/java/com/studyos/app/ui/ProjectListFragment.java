package com.studyos.app.ui;

import androidx.lifecycle.LiveData;

import com.studyos.app.data.Project;
import com.studyos.app.data.StudySession;
import com.studyos.app.data.Task;
import com.studyos.app.util.LiveUtil;
import com.studyos.app.util.Metrics;
import com.studyos.app.util.TimeUtil;

import java.util.List;

public class ProjectListFragment extends CrudListFragment<Project> {
    @Override protected String title() { return null; }
    @Override protected LiveData<List<Project>> source() { return vm.projects; }
    @Override protected void onAdd() { Forms.project(requireContext(), vm, null); }
    @Override protected void onItem(Project p) { Forms.project(requireContext(), vm, p); }
    @Override protected String emptyText() { return "No projects yet. Tap + to create one."; }

    @Override
    protected void observeExtra() {
        vm.tasks.observe(getViewLifecycleOwner(), l -> refresh());
        vm.sessions.observe(getViewLifecycleOwner(), l -> refresh());
    }

    @Override
    protected void bindRow(RowView row, Project p) {
        List<Task> tasks = LiveUtil.nz(vm.tasks.getValue());
        int[] t = Metrics.projectTasks(p, tasks);
        long sec = 0;
        for (StudySession s : LiveUtil.nz(vm.sessions.getValue())) {
            if (s.projectId != null && s.projectId.longValue() == p.id) sec += s.durationSec;
        }
        String sub = Ui.join(" \u2022 ", t[0] + "/" + t[1] + " tasks", "Time " + TimeUtil.duration(sec),
                p.deadline > 0 ? "Due " + TimeUtil.shortDate(p.deadline) : "");
        row.texts(p.name, sub, Metrics.pct(t[0], t[1]) + "%").progress(Metrics.pct(t[0], t[1]));
    }
}
