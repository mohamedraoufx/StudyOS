package com.studyos.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.View;

import com.studyos.app.R;

import java.util.Locale;

/** Simple bar chart. Values are hours; labels sit under the bars. */
public class BarChartView extends View {
    private final Paint bar = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint value = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private float[] values = new float[0];
    private String[] labels = new String[0];

    public BarChartView(Context c) {
        super(c);
        bar.setColor(Ui.col(c, R.color.so_primary));
        grid.setColor(Ui.col(c, R.color.so_divider));
        grid.setStrokeWidth(Ui.dp(1));
        label.setColor(Ui.col(c, R.color.so_text2));
        label.setTextAlign(Paint.Align.CENTER);
        label.setTextSize(sp(10));
        value.setColor(Ui.col(c, R.color.so_text));
        value.setTextAlign(Paint.Align.CENTER);
        value.setTextSize(sp(10));
    }

    private static float sp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, android.content.res.Resources.getSystem().getDisplayMetrics());
    }

    public void setData(float[] hours, String[] labels) {
        this.values = hours;
        this.labels = labels;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int n = values.length;
        if (n == 0) return;
        float max = 0.5f;
        for (float v : values) if (v > max) max = v;
        float left = Ui.dp(4);
        float right = getWidth() - Ui.dp(4);
        float top = Ui.dp(18);
        float bottom = getHeight() - Ui.dp(20);
        canvas.drawLine(left, bottom, right, bottom, grid);
        float slot = (right - left) / n;
        float barW = slot * 0.55f;
        for (int i = 0; i < n; i++) {
            float cx = left + slot * i + slot / 2f;
            float h = (bottom - top) * (values[i] / max);
            rect.set(cx - barW / 2f, bottom - h, cx + barW / 2f, bottom);
            canvas.drawRoundRect(rect, Ui.dp(4), Ui.dp(4), bar);
            if (values[i] > 0) {
                canvas.drawText(String.format(Locale.US, "%.1f", values[i]), cx, bottom - h - Ui.dp(4), value);
            }
            if (i < labels.length) canvas.drawText(labels[i], cx, getHeight() - Ui.dp(5), label);
        }
    }
}
