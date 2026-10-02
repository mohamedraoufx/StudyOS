package com.studyos.app.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.lifecycle.LiveData;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.studyos.app.R;
import com.studyos.app.data.Subtask;
import com.studyos.app.data.Task;
import com.studyos.app.util.LiveUtil;
import com.studyos.app.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TasksFragment extends CrudListFragment<Task> {
    private static final String[] FILTERS = {"All", "To do", "In progress", "Completed", "Due today", "Overdue", "High & urgent"};
    private static final String[] SORTS = {"Due date", "Priority", "Newest", "Title"};

    private String query = "";
    private int filter = 0;
    private int sort = 0;
    private final Map<Long, int[]> subCounts = new HashMap<>();
    private MaterialButton filterBtn;
    private MaterialButton sortBtn;

    @Override protected String title() { return "Tasks"; }
    @Override protected LiveData<List<Task>> source() { return vm.tasks; }
    @Override protected void onAdd() { Forms.task(requireContext(), vm, null); }
    @Override protected void onItem(Task t) { Forms.task(requireContext(), vm, t); }
    @Override protected String emptyText() { return "No tasks match. Tap + to add one."; }

    @Override
    protected void buildHeader(LinearLayout header) {
        TextInputLayout til = new TextInputLayout(requireContext());
        til.setHint("Search tasks");
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

        LinearLayout row = Ui.hbox(requireContext());
        filterBtn = Ui.button(requireContext(), "Filter: " + FILTERS[0], false, v -> pick("Filter", FILTERS, filter, i -> {
            filter = i;
            filterBtn.setText("Filter: " + FILTERS[i]);
            refresh();
        }));
        sortBtn = Ui.button(requireContext(), "Sort: " + SORTS[0], false, v -> pick("Sort by", SORTS, sort, i -> {
            sort = i;
            sortBtn.setText("Sort: " + SORTS[i]);
            refresh();
        }));
        row.addView(filterBtn, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        row.addView(sortBtn, Ui.lp(0, Ui.WRAP, 8, 0, 0, 0));
        ((LinearLayout.LayoutParams) sortBtn.getLayoutParams()).weight = 1f;
        header.addView(row);
    }

    private interface IntPick { void on(int index); }

    private void pick(String title, String[] options, int current, final IntPick cb) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setSingleChoiceItems(options, current, (d, which) -> {
                    cb.on(which);
                    d.dismiss();
                })
                .show();
    }

    @Override
    protected void observeExtra() {
        vm.subtasks.observe(getViewLifecycleOwner(), list -> {
            subCounts.clear();
            for (Subtask s : LiveUtil.nz(list)) {
                int[] c = subCounts.get(s.taskId);
                if (c == null) {
                    c = new int[2];
                    subCounts.put(s.taskId, c);
                }
                c[1]++;
                if (s.done) c[0]++;
            }
            refresh();
        });
    }

    @Override
    protected List<Task> transform(List<Task> in) {
        long today = TimeUtil.startOfDay(System.currentTimeMillis());
        List<Task> out = new ArrayList<>();
        for (Task t : in) {
            if (!query.isEmpty()) {
                String hay = (t.title + " " + t.notes + " " + t.subject + " " + t.category).toLowerCase(Locale.ROOT);
                if (!hay.contains(query)) continue;
            }
            boolean ok;
            switch (filter) {
                case 1: ok = t.status == Task.TODO; break;
                case 2: ok = t.status == Task.IN_PROGRESS; break;
                case 3: ok = t.status == Task.DONE; break;
                case 4: ok = t.dueDate == today && t.status != Task.DONE; break;
                case 5: ok = t.dueDate > 0 && t.dueDate < today && t.status != Task.DONE; break;
                case 6: ok = t.priority >= Task.HIGH && t.status != Task.DONE; break;
                default: ok = true;
            }
            if (ok) out.add(t);
        }
        final int mode = sort;
        Collections.sort(out, new Comparator<Task>() {
            @Override public int compare(Task a, Task b) {
                if (mode == 3) return a.title.compareToIgnoreCase(b.title);
                if (mode == 2) return Long.compare(b.createdAt, a.createdAt);
                int da = a.status == Task.DONE ? 1 : 0, db = b.status == Task.DONE ? 1 : 0;
                if (da != db) return da - db;
                long dueA = a.dueDate == 0 ? Long.MAX_VALUE : a.dueDate;
                long dueB = b.dueDate == 0 ? Long.MAX_VALUE : b.dueDate;
                if (mode == 1) {
                    if (a.priority != b.priority) return b.priority - a.priority;
                    return Long.compare(dueA, dueB);
                }
                if (dueA != dueB) return Long.compare(dueA, dueB);
                return b.priority - a.priority;
            }
        });
        return out;
    }

    @Override
    protected String subtitle(List<Task> shown) {
        int open = 0;
        for (Task t : shown) if (t.status != Task.DONE) open++;
        return open + " open \u2022 " + shown.size() + " shown";
    }

    @Override
    protected void bindRow(RowView row, final Task t) {
        long today = TimeUtil.startOfDay(System.currentTimeMillis());
        String due = "";
        if (t.dueDate > 0) {
            if (t.dueDate == today) due = "Today";
            else if (t.dueDate == today + TimeUtil.DAY) due = "Tomorrow";
            else if (t.dueDate < today && t.status != Task.DONE) due = "Overdue " + TimeUtil.shortDate(t.dueDate);
            else due = TimeUtil.shortDate(t.dueDate);
            if (t.startMinute >= 0) due += " " + TimeUtil.hhmm(t.startMinute);
        }
        int[] sc = subCounts.get(t.id);
        String sub = Ui.join(" \u2022 ", t.subject, t.category, due,
                t.status == Task.IN_PROGRESS ? "In progress" : "",
                sc == null ? "" : "\u2611 " + sc[0] + "/" + sc[1],
                t.repeat != Task.REPEAT_NONE ? "Repeats" : "");
        row.texts(t.title, sub, Forms.PRIORITIES[t.priority]);
        row.checkbox(t.status == Task.DONE, done -> vm.repo().setTaskDone(t, done));
        int color = t.priority == Task.URGENT ? R.color.so_red : t.priority == Task.HIGH ? R.color.so_orange
                : t.priority == Task.MEDIUM ? R.color.so_primary : R.color.so_text2;
        row.metaColor(color);
    }
}
