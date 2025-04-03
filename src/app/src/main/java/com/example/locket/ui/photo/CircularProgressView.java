package com.example.locket.ui.photo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CircularProgressView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private float progress = 0f;

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setStrokeWidth(8f);
        paint.setColor(0xFFFFC107); // Màu vàng cam
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float padding = 10;
        rect.set(padding, padding, getWidth() - padding, getHeight() - padding);
        canvas.drawArc(rect, -90, 360 * progress, false, paint);
    }
}
