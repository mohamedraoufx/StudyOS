package com.studyos.app.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.LinearLayout;

import androidx.lifecycle.LiveData;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.studyos.app.data.Note;
import com.studyos.app.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Notes list. Optional arguments: "type" and "name" limit it to notes linked to one item. */
public class NotesFragment extends CrudListFragment<Note> {
    private String query = "";

    public static NotesFragment forLink(String type, String name) {
        NotesFragment f = new NotesFragment();
        Bundle b = new Bundle();
        b.putString("type", type);
        b.putString("name", name);
        f.setArguments(b);
        return f;
    }

    private String argType() { return getArguments() == null ? null : getArguments().getString("type"); }
    private String argName() { return getArguments() == null ? null : getArguments().getString("name"); }

    @Override protected String title() { return argName() == null ? "Notes" : "Notes: " + argName(); }
    @Override protected LiveData<List<Note>> source() { return vm.notes; }
    @Override protected void onAdd() { Forms.note(requireContext(), vm, null, argType(), argName()); }
    @Override protected void onItem(Note n) { Forms.note(requireContext(), vm, n, null, null); }
    @Override protected String emptyText() { return "No notes yet. Tap + to write one."; }

    @Override
    protected void buildHeader(LinearLayout header) {
        TextInputLayout til = new TextInputLayout(requireContext());
        til.setHint("Search notes");
        TextInputEditText et = new TextInputEditText(til.getContext());
        et.setSingleLine(true);
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                query = s.toString().trim().toLowerCase(Locale.ROOT);
                refresh();
            }
        });
        til.addView(et, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        header.addView(til, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 8, 0, 0));
    }

    @Override
    protected List<Note> transform(List<Note> in) {
        List<Note> out = new ArrayList<>();
        String name = argName();
        for (Note n : in) {
            if (name != null && !name.equals(n.linkName)) continue;
            if (!query.isEmpty()) {
                String hay = (n.title + " " + n.body + " " + n.category + " " + n.linkName).toLowerCase(Locale.ROOT);
                if (!hay.contains(query)) continue;
            }
            out.add(n);
        }
        return out;
    }

    @Override
    protected void bindRow(RowView row, Note n) {
        String link = n.linkName.isEmpty() ? "" : n.linkType + ": " + n.linkName;
        String body = n.body.length() > 90 ? n.body.substring(0, 90) + "\u2026" : n.body;
        row.texts(n.title, Ui.join(" \u2022 ", n.category, link, body), TimeUtil.shortDate(n.updatedAt));
    }
}
