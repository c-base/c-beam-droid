package org.c_base.c_beam.ccorder;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class DrawOnTop extends View {

    private final Paint paint = new Paint();
    private final RectF oval = new RectF();

	public DrawOnTop(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(@NonNull Canvas canvas) {
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(1);
		paint.setColor(Color.GREEN);
		paint.setAlpha(128);

        int spacing = 50;
        int height = getHeight();
        int width = getWidth();

		for (int y = 0; y < height; y += spacing) {
			canvas.drawLine(0, y, width, y, paint);
		}

		for (int x = 0; x < width; x += spacing) {
			canvas.drawLine(x, 0, x, height, paint);
		}

		oval.set(0, 0, width, height);
		canvas.drawOval(oval, paint);

		super.onDraw(canvas);
	}

    @Override
    public boolean performClick() {
        return super.performClick();
    }

	@Override
	public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
        }
		return true;
	}

}
