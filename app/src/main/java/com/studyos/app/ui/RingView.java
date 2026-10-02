package com.studyos.app.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.studyos.app.R;

/** Circular progress ring with a big label in the middle. */
public class RingView extends View {
    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arc = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint big = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint small = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private float progress;
    private String bigText = "";
    private String smallText = "";

    public RingView(Context c) {
        super(c);
        track.setStyle(Paint.Style.STROKE);
        arc.setStyle(Paint.Style.STROKE);
        arc.setStrokeCap(Paint.Cap.ROUND);
        track.setColor(Ui.col(c, R.color.so_divider));
        arc.setColor(Ui.col(c, R.color.so_primary));
        big.setColor(Ui.col(c, R.color.so_text));
        big.setTextAlign(Paint.Align.CENTER);
        big.setFakeBoldText(true);
        big.setTextSize(Ui.dp(22));
        small.setColor(Ui.col(c, R.color.so_text2));
        small.setTextAlign(Paint.Align.CENTER);
        small.setTextSize(Ui.dp(12));
    }

    public void set(float progress01, String bigText, String smallText) {
        this.progress = Math.max(0f, Math.min(1f, progress01));
        this.bigText = bigText;
        this.smallText = smallText;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float stroke = Math.min(getWidth(), getHeight()) * 0.1f;
        track.setStrokeWidth(stroke);
        arc.setStrokeWidth(stroke);
        float pad = stroke / 2f + Ui.dp(2);
        oval.set(pad, pad, getWidth() - pad, getHeight() - pad);
        canvas.drawArc(oval, 0, 360, false, track);
        canvas.drawArc(oval, -90, 360f * progress, false, arc);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        canvas.drawText(bigText, cx, cy + (smallText.isEmpty() ? big.getTextSize() / 3f : 0), big);
        if (!smallText.isEmpty()) canvas.drawText(smallText, cx, cy + small.getTextSize() * 1.4f, small);
    }
}
