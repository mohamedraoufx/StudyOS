package com.studyos.app.ui;

import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.studyos.app.R;

import java.util.function.Consumer;

/** Reusable list row: optional checkbox, title, subtitle, progress bar and a right-hand label. */
public class RowView extends MaterialCardView {
    public final MaterialCheckBox check;
    public final TextView title;
    public final TextView sub;
    public final TextView meta;
    public final LinearProgressIndicator bar;

    public RowView(Context c) {
        super(c);
        setCardBackgroundColor(Ui.col(c, R.color.so_card));
        setRadius(Ui.dp(14));
        setCardElevation(0);
        setStrokeWidth(Ui.dp(1));
        setStrokeColor(Ui.col(c, R.color.so_divider));
        setClickable(true);
        setFocusable(true);

        LinearLayout row = Ui.hbox(c);
        row.setPadding(Ui.dp(14), Ui.dp(12), Ui.dp(14), Ui.dp(12));

        check = new MaterialCheckBox(c);
        row.addView(check, new LinearLayout.LayoutParams(Ui.WRAP, Ui.WRAP));

        LinearLayout mid = Ui.vbox(c);
        title = Ui.text(c, "", 16, true, R.color.so_text);
        sub = Ui.text(c, "", 13, false, R.color.so_text2);
        bar = Ui.progress(c, 0, R.color.so_primary);
        mid.addView(title);
        mid.addView(sub);
        mid.addView(bar, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 8, 0, 0));
        row.addView(mid, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));

        meta = Ui.text(c, "", 13, true, R.color.so_text2);
        row.addView(meta, Ui.lp(Ui.WRAP, Ui.WRAP, 8, 0, 0, 0));
        addView(row);
        reset();
    }

    /** Clears any state left over from a previously bound item. */
    public void reset() {
        check.setVisibility(View.GONE);
        check.setOnClickListener(null);
        title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        title.setTextColor(Ui.col(getContext(), R.color.so_text));
        sub.setVisibility(View.GONE);
        bar.setVisibility(View.GONE);
        meta.setVisibility(View.GONE);
        meta.setTextColor(Ui.col(getContext(), R.color.so_text2));
    }

    public RowView texts(String t, String s, String m) {
        title.setText(t);
        if (s != null && !s.isEmpty()) {
            sub.setText(s);
            sub.setVisibility(View.VISIBLE);
        }
        if (m != null && !m.isEmpty()) {
            meta.setText(m);
            meta.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public RowView checkbox(boolean checked, final Consumer<Boolean> onToggle) {
        check.setVisibility(View.VISIBLE);
        check.setChecked(checked);
        check.setOnClickListener(v -> onToggle.accept(check.isChecked()));
        if (checked) {
            title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            title.setTextColor(Ui.col(getContext(), R.color.so_text2));
        }
        return this;
    }

    public RowView progress(int percent) {
        if (percent < 0) {
            bar.setVisibility(View.GONE);
        } else {
            bar.setVisibility(View.VISIBLE);
            bar.setProgressCompat(Math.min(100, percent), false);
        }
        return this;
    }

    public RowView metaColor(@ColorRes int color) {
        meta.setTextColor(Ui.col(getContext(), color));
        return this;
    }
}
