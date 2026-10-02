package com.studyos.app.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.studyos.app.R;
import com.studyos.app.util.TimeUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/** Builds simple form dialogs from code. */
public class Form {
    public interface Saver { boolean save(); }

    /** A date picked with a dialog. value = midnight millis, 0 = none. */
    public static class DateField {
        public long value;
    }

    /** A time of day picked with a dialog. minute = minutes after midnight, -1 = none. */
    public static class TimeField {
        public int minute = -1;
    }

    private final Context ctx;
    private final String title;
    private final LinearLayout root;

    public Form(Context c, String title) {
        this.ctx = c;
        this.title = title;
        root = Ui.vbox(c);
        root.setPadding(Ui.dp(20), Ui.dp(8), Ui.dp(20), Ui.dp(8));
    }

    public LinearLayout root() { return root; }

    public static String str(EditText e) { return e.getText().toString().trim(); }

    public static int num(EditText e, int fallback) {
        try {
            return Integer.parseInt(e.getText().toString().trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public EditText text(String label, String value, boolean multiline, boolean number) {
        TextInputLayout til = new TextInputLayout(ctx);
        til.setHint(label);
        TextInputEditText et = new TextInputEditText(til.getContext());
        et.setText(value);
        if (number) {
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else if (multiline) {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            et.setMinLines(3);
            et.setGravity(Gravity.TOP | Gravity.START);
        } else {
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        }
        til.addView(et, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        root.addView(til, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 6, 0, 2));
        return et;
    }

    public Spinner spinner(String label, List<String> options, int selected) {
        root.addView(Ui.text(ctx, label, 12, false, R.color.so_text2), Ui.lp(Ui.MATCH, Ui.WRAP, 0, 10, 0, 0));
        Spinner s = new Spinner(ctx);
        fill(s, options, selected);
        root.addView(s, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 0, 0, 0));
        return s;
    }

    public static void fill(Spinner s, List<String> options, int selected) {
        ArrayAdapter<String> a = new ArrayAdapter<>(s.getContext(), android.R.layout.simple_spinner_item, options);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(a);
        s.setSelection(Math.max(0, Math.min(selected, options.size() - 1)));
    }

    public CheckBox check(String label, boolean value) {
        CheckBox cb = new MaterialCheckBox(ctx);
        cb.setText(label);
        cb.setChecked(value);
        root.addView(cb, Ui.lp(Ui.WRAP, Ui.WRAP, 0, 6, 0, 0));
        return cb;
    }

    public SeekBar seek(String label, int value) {
        final TextView t = Ui.text(ctx, label + ": " + value + "%", 14, false, R.color.so_text);
        root.addView(t, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 12, 0, 0));
        SeekBar sb = new SeekBar(ctx);
        sb.setMax(100);
        sb.setProgress(value);
        final String l = label;
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean fromUser) {
                t.setText(l + ": " + p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        root.addView(sb, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 0, 0, 0));
        return sb;
    }

    public DateField date(String label, long initial, boolean allowClear) {
        final DateField f = new DateField();
        f.value = initial;
        root.addView(Ui.text(ctx, label, 12, false, R.color.so_text2), Ui.lp(Ui.MATCH, Ui.WRAP, 0, 10, 0, 0));
        LinearLayout row = Ui.hbox(ctx);
        final MaterialButton btn = Ui.button(ctx, "", false, null);
        btn.setText(f.value > 0 ? TimeUtil.format(f.value, "EEE, d MMM yyyy") : "No date");
        btn.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(f.value > 0 ? f.value : System.currentTimeMillis());
            new DatePickerDialog(ctx, (dp, y, m, d) -> {
                Calendar picked = Calendar.getInstance();
                picked.clear();
                picked.set(y, m, d, 0, 0, 0);
                f.value = picked.getTimeInMillis();
                btn.setText(TimeUtil.format(f.value, "EEE, d MMM yyyy"));
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });
        row.addView(btn);
        if (allowClear) {
            row.addView(Ui.button(ctx, "Clear", false, v -> {
                f.value = 0;
                btn.setText("No date");
            }), Ui.lp(Ui.WRAP, Ui.WRAP, 8, 0, 0, 0));
        }
        root.addView(row);
        return f;
    }

    public TimeField time(String label, int initialMinute, boolean allowClear) {
        final TimeField f = new TimeField();
        f.minute = initialMinute;
        root.addView(Ui.text(ctx, label, 12, false, R.color.so_text2), Ui.lp(Ui.MATCH, Ui.WRAP, 0, 10, 0, 0));
        LinearLayout row = Ui.hbox(ctx);
        final MaterialButton btn = Ui.button(ctx, "", false, null);
        btn.setText(f.minute >= 0 ? TimeUtil.hhmm(f.minute) : "Not set");
        btn.setOnClickListener(v -> {
            int base = f.minute >= 0 ? f.minute : 9 * 60;
            new TimePickerDialog(ctx, (tp, h, m) -> {
                f.minute = h * 60 + m;
                btn.setText(TimeUtil.hhmm(f.minute));
            }, base / 60, base % 60, true).show();
        });
        row.addView(btn);
        if (allowClear) {
            row.addView(Ui.button(ctx, "Clear", false, v -> {
                f.minute = -1;
                btn.setText("Not set");
            }), Ui.lp(Ui.WRAP, Ui.WRAP, 8, 0, 0, 0));
        }
        root.addView(row);
        return f;
    }

    public void add(View v) {
        root.addView(v, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 4, 0, 4));
    }

    public void show(String positive, final Saver saver, final Runnable onDelete) {
        ScrollView sv = new ScrollView(ctx);
        sv.addView(root);
        MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(ctx)
                .setTitle(title)
                .setView(sv)
                .setPositiveButton(positive, null)
                .setNegativeButton("Cancel", null);
        if (onDelete != null) b.setNeutralButton("Delete", null);
        final AlertDialog d = b.create();
        d.show();
        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (saver.save()) d.dismiss();
        });
        if (onDelete != null) {
            d.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v ->
                    Ui.confirm(ctx, "Delete this item?", () -> {
                        onDelete.run();
                        d.dismiss();
                    }));
        }
    }

    public static String fmt(String pattern, Object... args) {
        return String.format(Locale.US, pattern, args);
    }
}
