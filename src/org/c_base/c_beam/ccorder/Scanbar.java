package org.c_base.c_beam.ccorder;

import org.c_base.c_beam.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

public class Scanbar extends View {
	private Paint paint = new Paint();
	private int yPos = 0;
	private int barHeight = 0;

	public int getBarHeight() {
		return barHeight;
	}

	public void setBarHeight(int barHeight) {
		this.barHeight = barHeight;
	}

	public Scanbar(Context context) {
		super(context);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.YELLOW);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Bitmap foo = BitmapFactory.decodeResource(getResources(),R.drawable.scanner);
		Bitmap img = Bitmap.createScaledBitmap( foo, canvas.getWidth(), 100, true );
		foo.recycle();
		canvas.drawBitmap(img,0,yPos,null);
		barHeight = img.getHeight();
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		return true;
	}

}