package com.studyos.app.ui;

import androidx.lifecycle.LiveData;

import com.studyos.app.data.StudySession;
import com.studyos.app.util.TimeUtil;

import java.util.List;

public class SessionsFragment extends CrudListFragment<StudySession> {
    @Override protected String title() { return null; }
    @Override protected LiveData<List<StudySession>> source() { return vm.sessions; }
    @Override protected void onAdd() { Forms.session(requireContext(), vm, null); }
    @Override protected void onItem(StudySession s) { Forms.session(requireContext(), vm, s); }
    @Override protected String emptyText() { return "No study sessions yet. Tap + to log one."; }

    @Override
    protected void bindRow(RowView row, StudySession s) {
        String when = TimeUtil.format(s.startTime, "EEE d MMM, HH:mm") + " - " + TimeUtil.format(s.endTime, "HH:mm");
        String src = StudySession.SRC_POMODORO.equals(s.source) ? "Pomodoro" : "";
        row.texts(s.subjectName, com.studyos.app.ui.Ui.join(" \u2022 ", when, src, s.notes), TimeUtil.duration(s.durationSec));
    }
}
