package com.studyos.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.studyos.app.R;
import com.studyos.app.data.Project;
import com.studyos.app.data.StudySession;
import com.studyos.app.data.Subject;
import com.studyos.app.focus.PomodoroManager;
import com.studyos.app.util.LiveUtil;
import com.studyos.app.util.StatsCalculator;
import com.studyos.app.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/** Pomodoro timer. Finished focus phases are logged automatically. */
public class FocusFragment extends Fragment {
    private MainViewModel vm;
    private PomodoroManager pomo;
    private Spinner subjectSp;
    private Spinner projectSp;
    private RingView ring;
    private TextView modeLabel;
    private TextView todayLabel;
    private MaterialButton mainBtn;
    private List<Subject> subjects = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        Context c = requireContext();
        vm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        pomo = PomodoroManager.get(c);

        LinearLayout col = Ui.vbox(c);
        col.setPadding(Ui.dp(16), Ui.dp(12), Ui.dp(16), Ui.dp(24));

        modeLabel = Ui.text(c, "Focus", 18, true, R.color.so_primary);
        modeLabel.setGravity(android.view.Gravity.CENTER);
        col.addView(modeLabel, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 8, 0, 8));

        ring = new RingView(c);
        col.addView(ring, new LinearLayout.LayoutParams(Ui.dp(240), Ui.dp(240)));
        ((LinearLayout.LayoutParams) ring.getLayoutParams()).gravity = android.view.Gravity.CENTER_HORIZONTAL;

        LinearLayout buttons = Ui.hbox(c);
        buttons.setGravity(android.view.Gravity.CENTER);
        mainBtn = Ui.button(c, "Start", true, v -> {
            PomodoroManager.State s = pomo.state().getValue();
            if (s != null && s.running) pomo.pause();
            else pomo.start();
        });
        buttons.addView(mainBtn);
        buttons.addView(Ui.button(c, "Reset", false, v -> pomo.reset()), Ui.lp(Ui.WRAP, Ui.WRAP, 8, 0, 0, 0));
        buttons.addView(Ui.button(c, "Skip", false, v -> pomo.skip()), Ui.lp(Ui.WRAP, Ui.WRAP, 8, 0, 0, 0));
        col.addView(buttons, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 12, 0, 12));

        col.addView(Ui.text(c, "What are you studying?", 12, false, R.color.so_text2));
        subjectSp = new Spinner(c);
        col.addView(subjectSp, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 0, 0, 8));
        col.addView(Ui.text(c, "Project (optional)", 12, false, R.color.so_text2));
        projectSp = new Spinner(c);
        col.addView(projectSp, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 0, 0, 8));

        AdapterView.OnItemSelectedListener target = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { pushTarget(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };
        subjectSp.setOnItemSelectedListener(target);
        projectSp.setOnItemSelectedListener(target);

        todayLabel = Ui.text(c, "", 14, false, R.color.so_text2);
        todayLabel.setGravity(android.view.Gravity.CENTER);
        col.addView(todayLabel, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 12, 0, 8));
        col.addView(Ui.button(c, "Log a session manually", false, v -> Forms.session(requireContext(), vm, null)),
                Ui.lp(Ui.MATCH, Ui.WRAP, 0, 4, 0, 0));
        col.addView(Ui.text(c, "Tip: keep the app open or in recent apps so the timer can log your session when it ends.",
                12, false, R.color.so_text2), Ui.lp(Ui.MATCH, Ui.WRAP, 0, 12, 0, 0));

        pomo.state().observe(getViewLifecycleOwner(), this::showState);
        vm.subjects.observe(getViewLifecycleOwner(), list -> fillSubjects());
        vm.projects.observe(getViewLifecycleOwner(), list -> fillProjects());
        LiveUtil.observeAll(getViewLifecycleOwner(), this::showToday, vm.sessions);
        return Ui.scroll(c, col);
    }

    private void fillSubjects() {
        String keep = subjectSp.getSelectedItem() == null ? null : (String) subjectSp.getSelectedItem();
        subjects = vm.studySubjects();
        List<String> names = new ArrayList<>();
        int sel = 0;
        for (int i = 0; i < subjects.size(); i++) {
            names.add(subjects.get(i).name);
            if (subjects.get(i).name.equals(keep)) sel = i;
        }
        if (names.isEmpty()) names.add("Study");
        Form.fill(subjectSp, names, sel);
        pushTarget();
    }

    private void fillProjects() {
        projects = vm.projectSnapshot();
        List<String> names = new ArrayList<>();
        names.add("(none)");
        for (Project p : projects) names.add(p.name);
        Form.fill(projectSp, names, 0);
        pushTarget();
    }

    private void pushTarget() {
        if (subjectSp == null || projectSp == null) return;
        int si = subjectSp.getSelectedItemPosition();
        String name = subjects.isEmpty() || si < 0 || si >= subjects.size() ? "Study" : subjects.get(si).name;
        String cat = subjects.isEmpty() || si < 0 || si >= subjects.size() ? "" : subjects.get(si).category;
        int pi = projectSp.getSelectedItemPosition();
        Long pid = pi <= 0 || pi > projects.size() ? null : Long.valueOf(projects.get(pi - 1).id);
        pomo.setTarget(name, cat, pid);
    }

    private void showState(PomodoroManager.State s) {
        if (s == null) return;
        float done = s.totalMs == 0 ? 0 : 1f - (float) s.remainingMs / s.totalMs;
        ring.set(done, TimeUtil.clock(s.remainingMs), s.focus ? "focus" : "break");
        modeLabel.setText(s.focus ? "Focus" : "Break");
        boolean fresh = s.remainingMs >= s.totalMs;
        mainBtn.setText(s.running ? "Pause" : (fresh ? "Start" : "Resume"));
    }

    private void showToday() {
        if (!isAdded() || todayLabel == null) return;
        long today = TimeUtil.startOfDay(System.currentTimeMillis());
        List<StudySession> list = LiveUtil.nz(vm.sessions.getValue());
        int n = 0;
        for (StudySession s : list) {
            if (StudySession.SRC_POMODORO.equals(s.source) && s.startTime >= today && s.startTime < today + TimeUtil.DAY) n++;
        }
        long sec = StatsCalculator.sum(list, today, today + TimeUtil.DAY);
        todayLabel.setText("Today: " + n + " focus sessions \u2022 " + TimeUtil.duration(sec) + " studied");
    }
}
