package com.studyos.app.ui;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.studyos.app.R;

import java.util.ArrayList;
import java.util.List;

/** Editable list of checkable lines (used for subtasks and milestones). */
public class ChecklistEditor {
    private final Context ctx;
    private final LinearLayout box;
    private final LinearLayout rows;

    public ChecklistEditor(Context c, String label, String addLabel) {
        ctx = c;
        box = Ui.vbox(c);
        box.addView(Ui.text(c, label, 12, false, R.color.so_text2), Ui.lp(Ui.MATCH, Ui.WRAP, 0, 10, 0, 2));
        rows = Ui.vbox(c);
        box.addView(rows);
        box.addView(Ui.button(c, addLabel, false, v -> addRow("", false)), Ui.lp(Ui.WRAP, Ui.WRAP, 0, 4, 0, 0));
    }

    public View view() { return box; }

    public void addRow(String text, boolean done) {
        final LinearLayout r = Ui.hbox(ctx);
        CheckBox cb = new MaterialCheckBox(ctx);
        cb.setChecked(done);
        EditText et = new EditText(ctx);
        et.setText(text);
        et.setHint("Item");
        et.setSingleLine(true);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        TextView remove = Ui.text(ctx, "\u2715", 18, true, R.color.so_text2);
        remove.setPadding(Ui.dp(10), Ui.dp(6), Ui.dp(6), Ui.dp(6));
        remove.setOnClickListener(v -> rows.removeView(r));
        r.addView(cb);
        r.addView(et, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        r.addView(remove);
        rows.addView(r);
    }

    public void setItems(List<String> texts, List<Boolean> dones) {
        for (int i = 0; i < texts.size(); i++) addRow(texts.get(i), dones.get(i));
    }

    /** Non-blank item texts, in order. */
    public List<String> texts() {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < rows.getChildCount(); i++) {
            String s = ((EditText) ((LinearLayout) rows.getChildAt(i)).getChildAt(1)).getText().toString().trim();
            if (!s.isEmpty()) out.add(s);
        }
        return out;
    }

    /** Done flags matching {@link #texts()} (blank lines skipped). */
    public List<Boolean> dones() {
        List<Boolean> out = new ArrayList<>();
        for (int i = 0; i < rows.getChildCount(); i++) {
            LinearLayout r = (LinearLayout) rows.getChildAt(i);
            String s = ((EditText) r.getChildAt(1)).getText().toString().trim();
            if (!s.isEmpty()) out.add(((CheckBox) r.getChildAt(0)).isChecked());
        }
        return out;
    }
}
