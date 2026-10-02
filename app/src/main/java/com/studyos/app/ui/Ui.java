package com.studyos.app.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.studyos.app.R;

/** Small helpers for building the (code-defined) UI. */
public final class Ui {
    public static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    public static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    private Ui() {}

    public static int dp(float v) {
        return Math.round(v * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int col(Context c, @ColorRes int id) {
        return ContextCompat.getColor(c, id);
    }

    public static LinearLayout vbox(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static LinearLayout hbox(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    /** Layout params with margins given in dp. */
    public static LinearLayout.LayoutParams lp(int w, int h, float l, float t, float r, float b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(dp(l), dp(t), dp(r), dp(b));
        return p;
    }

    public static TextView text(Context c, CharSequence s, float sp, boolean bold, @ColorRes int color) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(col(c, color));
        if (bold) t.setTypeface(t.getTypeface(), Typeface.BOLD);
        return t;
    }

    public static TextView title(Context c, CharSequence s) {
        return text(c, s, 26, true, R.color.so_text);
    }

    public static TextView section(Context c, CharSequence s) {
        return text(c, s, 16, true, R.color.so_text);
    }

    public static View spacer(Context c, float heightDp) {
        View v = new View(c);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(heightDp)));
        return v;
    }

    public static MaterialButton button(Context c, CharSequence label, boolean filled, View.OnClickListener l) {
        MaterialButton b = filled
                ? new MaterialButton(c)
                : new MaterialButton(c, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
        b.setText(label);
        b.setOnClickListener(l);
        return b;
    }

    /** A rounded card (already margined for use inside a vertical LinearLayout). */
    public static MaterialCardView card(Context c) {
        MaterialCardView v = new MaterialCardView(c);
        v.setCardBackgroundColor(col(c, R.color.so_card));
        v.setRadius(dp(16));
        v.setCardElevation(0);
        v.setStrokeWidth(dp(1));
        v.setStrokeColor(col(c, R.color.so_divider));
        v.setContentPadding(dp(16), dp(14), dp(16), dp(14));
        v.setLayoutParams(lp(MATCH, WRAP, 12, 6, 12, 6));
        return v;
    }

    public static LinearProgressIndicator progress(Context c, int percent, @ColorRes int color) {
        LinearProgressIndicator p = new LinearProgressIndicator(c);
        p.setIndeterminate(false);
        p.setMax(100);
        p.setTrackThickness(dp(6));
        p.setTrackCornerRadius(dp(3));
        p.setIndicatorColor(col(c, color));
        p.setTrackColor(col(c, R.color.so_divider));
        p.setProgressCompat(Math.max(0, Math.min(100, percent)), false);
        return p;
    }

    public static ScrollView scroll(Context c, View content) {
        ScrollView sv = new ScrollView(c);
        sv.setFillViewport(true);
        sv.addView(content, new ViewGroup.LayoutParams(MATCH, WRAP));
        return sv;
    }

    public static void confirm(Context c, String message, final Runnable onYes) {
        new MaterialAlertDialogBuilder(c)
                .setMessage(message)
                .setPositiveButton("Yes", (d, w) -> onYes.run())
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static void toast(Context c, String message) {
        Toast.makeText(c, message, Toast.LENGTH_SHORT).show();
    }

    public static String join(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(sep);
            sb.append(p);
        }
        return sb.toString();
    }
}
