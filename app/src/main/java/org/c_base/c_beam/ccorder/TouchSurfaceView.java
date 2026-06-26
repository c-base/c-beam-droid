package org.c_base.c_beam.ccorder;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import org.c_base.c_beam.util.CubeRenderer;

public class TouchSurfaceView extends GLSurfaceView {

	private final CubeRenderer mRenderer;
	private float mPreviousX;
	private float mPreviousY;

	public TouchSurfaceView(Context context) {
		super(context);
		mRenderer = new CubeRenderer(true, context);
		setRenderer(mRenderer);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
	}

	@Override
	public boolean onTrackballEvent(MotionEvent e) {
        float TRACKBALL_SCALE_FACTOR = 36.0f;
        mRenderer.mAngleX += e.getX() * TRACKBALL_SCALE_FACTOR;
		mRenderer.mAngleY += e.getY() * TRACKBALL_SCALE_FACTOR;
		requestRender();
		return true;
	}

    @Override
    public boolean performClick() {
        return super.performClick();
    }

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		float x = e.getX();
		float y = e.getY();
        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = x - mPreviousX;
            float dy = y - mPreviousY;
            float TOUCH_SCALE_FACTOR = 180.0f / 320;
            mRenderer.mAngleX += dx * TOUCH_SCALE_FACTOR;
            mRenderer.mAngleY += dy * TOUCH_SCALE_FACTOR;
            requestRender();
        } else if (e.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
        }
		mPreviousX = x;
		mPreviousY = y;
		return true;
	}

}
