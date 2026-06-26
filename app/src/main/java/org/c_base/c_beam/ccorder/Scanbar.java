package org.c_base.c_beam.ccorder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import org.c_base.c_beam.R;

public class Scanbar extends View {

    private final Paint paint = new Paint();
    private final Bitmap scanBitmap;

	public Scanbar(Context context) {
		super(context);
        scanBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.scanner);
	}

	@Override
	protected void onDraw(@NonNull Canvas canvas) {
        if (scanBitmap != null) {
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(scanBitmap, getWidth(), 100, true);
            canvas.drawBitmap(scaledBitmap, 0, 0, paint);
        }
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
