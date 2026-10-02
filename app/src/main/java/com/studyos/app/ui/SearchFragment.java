package com.studyos.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.studyos.app.MainActivity;
import com.studyos.app.R;
import com.studyos.app.data.Goal;
import com.studyos.app.data.Note;
import com.studyos.app.data.Project;
import com.studyos.app.data.Repository;
import com.studyos.app.data.Subject;
import com.studyos.app.data.Task;
import com.studyos.app.data.Vocab;

/** Global search across tasks, projects, goals, notes, vocabulary and subjects. */
public class SearchFragment extends Fragment {
    private MainViewModel vm;
    private LinearLayout results;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pending;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        final Context c = requireContext();
        vm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        LinearLayout col = Ui.vbox(c);
        col.addView(Ui.title(c, "Search"), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 12, 16, 4));
        TextInputLayout til = new TextInputLayout(c);
        til.setHint("Search everything");
        TextInputEditText et = new TextInputEditText(til.getContext());
        et.setSingleLine(true);
        til.addView(et, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        col.addView(til, Ui.lp(Ui.MATCH, Ui.WRAP, 16, 4, 16, 4));
        results = Ui.vbox(c);
        results.setPadding(0, 0, 0, Ui.dp(24));
        col.addView(Ui.scroll(c, results), new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));

        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int d) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int d) {}
            @Override public void afterTextChanged(Editable s) {
                final String q = s.toString().trim();
                if (pending != null) handler.removeCallbacks(pending);
                pending = () -> run(q);
                handler.postDelayed(pending, 250);
            }
        });
        return col;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pending != null) handler.removeCallbacks(pending);
    }

    private void run(String q) {
        if (q.length() < 2) {
            results.removeAllViews();
            return;
        }
        vm.repo().search(q, this::show);
    }

    private void show(Repository.SearchResult r) {
        if (!isAdded()) return;
        Context c = requireContext();
        results.removeAllViews();
        if (r.isEmpty()) {
            results.addView(Ui.text(c, "No results.", 14, false, R.color.so_text2), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 16, 16, 0));
            return;
        }
        if (!r.tasks.isEmpty()) section("Tasks");
        for (final Task t : r.tasks) add(new RowView(c).texts(t.title, Ui.join(" \u2022 ", t.subject, t.category), ""),
                v -> Forms.task(requireContext(), vm, t));
        if (!r.projects.isEmpty()) section("Projects");
        for (final Project p : r.projects) add(new RowView(c).texts(p.name, p.notes, ""),
                v -> Forms.project(requireContext(), vm, p));
        if (!r.goals.isEmpty()) section("Goals");
        for (final Goal g : r.goals) add(new RowView(c).texts(g.title, g.notes, ""),
                v -> Forms.goal(requireContext(), vm, g));
        if (!r.notes.isEmpty()) section("Notes");
        for (final Note n : r.notes) add(new RowView(c).texts(n.title, n.body, ""),
                v -> Forms.note(requireContext(), vm, n, null, null));
        if (!r.vocab.isEmpty()) section("Vocabulary");
        for (final Vocab w : r.vocab) add(new RowView(c).texts(w.word, w.translation, w.language),
                v -> Forms.vocab(requireContext(), vm, w, null));
        if (!r.subjects.isEmpty()) section("Subjects");
        for (final Subject s : r.subjects) add(new RowView(c).texts(s.name, s.groupName, s.level + "%"),
                v -> ((MainActivity) requireActivity()).selectTab(R.id.nav_study));
    }

    private void section(String name) {
        results.addView(Ui.section(requireContext(), name), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 14, 16, 4));
    }

    private void add(RowView row, View.OnClickListener l) {
        row.setOnClickListener(l);
        results.addView(row, Ui.lp(Ui.MATCH, Ui.WRAP, 12, 4, 12, 4));
    }
}
