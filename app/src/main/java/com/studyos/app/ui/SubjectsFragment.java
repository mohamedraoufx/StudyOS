package com.studyos.app.ui;

import androidx.lifecycle.LiveData;

import com.studyos.app.data.Subject;

import java.util.ArrayList;
import java.util.List;

/** Software engineering roadmap (and other non-language subjects) with levels. */
public class SubjectsFragment extends CrudListFragment<Subject> {
    @Override protected String title() { return null; }
    @Override protected LiveData<List<Subject>> source() { return vm.subjects; }
    @Override protected void onAdd() { Forms.subject(requireContext(), vm, null); }
    @Override protected void onItem(Subject s) { Forms.subject(requireContext(), vm, s); }
    @Override protected String emptyText() { return "No subjects yet. Tap + to add one."; }

    @Override
    protected List<Subject> transform(List<Subject> in) {
        List<Subject> out = new ArrayList<>();
        for (Subject s : in) {
            if (Subject.SE.equals(s.category) || Subject.OTHER.equals(s.category)) out.add(s);
        }
        return out;
    }

    @Override
    protected String subtitle(List<Subject> shown) {
        int sum = 0, n = 0;
        for (Subject s : shown) {
            if (Subject.SE.equals(s.category)) {
                sum += s.level;
                n++;
            }
        }
        return n == 0 ? "" : "Software Engineering roadmap: " + (sum / n) + "% overall. Tap a topic to update its level.";
    }

    @Override
    protected void bindRow(RowView row, Subject s) {
        String sub = Subject.SE.equals(s.category) ? "Software Engineering" : "Other";
        row.texts(s.name, sub, s.level + "%").progress(s.level);
    }
}
