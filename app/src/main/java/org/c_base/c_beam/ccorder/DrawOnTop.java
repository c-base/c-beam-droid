package org.c_base.c_beam.ccorder;

import org.c_base.c_beam.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;
import android.view.MotionEvent;
import android.view.View;

public class DrawOnTop extends View {
	Paint paint = new Paint();
	int yPos = 0;

	public DrawOnTop(Context context) {
		super(context);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(2);
	}

	@Override
	protected void onDraw(Canvas canvas) {
//		NinePatchDrawable npd = (NinePatchDrawable)getResources().getDrawable(R.drawable.smlnpatch240dpi);
//		// Set its bound where you need
//		Rect npdBounds = new Rect(
//				1,1,canvas.getWidth(),canvas.getHeight());
//		npd.setBounds(npdBounds);
//
//		// Finally draw on the canvas
//		npd.draw(canvas);

		//		    canvas.save();
		//		    canvas.scale(10, 10, 0, 0);
		//			canvas.drawText("Test Text", 20, 20, paint);
		//			canvas.restore();
		RectF oval;
		oval = new RectF();
		oval.set(100, 100, 200, 200);
		//			canvas.drawArc(oval, 45, 270, true, paint);

		int spacing = 10;
		for(int y=0; y<canvas.getHeight(); y+=spacing) {
			canvas.drawLine(0, y, canvas.getWidth(), y, paint);
		}
		for(int x=0; x<canvas.getWidth(); x+=spacing) {
			canvas.drawLine(x, 0, x, canvas.getHeight(), paint);
		}
		yPos++;
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		return true;
	}
}
