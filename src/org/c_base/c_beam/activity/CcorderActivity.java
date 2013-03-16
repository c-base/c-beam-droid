package org.c_base.c_beam.activity;

import java.io.IOException;

import org.c_base.c_beam.util.CubeRenderer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager.LayoutParams;

public class CcorderActivity extends Activity implements Callback {
	private Camera camera;
	private SurfaceView mSurfaceView;
	SurfaceHolder mSurfaceHolder;
	private TouchSurfaceView mGLSurfaceView;

	ShutterCallback shutter = new ShutterCallback(){
		@Override
		public void onShutter() {
			// TODO Auto-generated method stub
			// No action to be perfomed on the Shutter callback.
		}
	};
	PictureCallback raw = new PictureCallback(){
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			// No action taken on the raw data. Only action taken on jpeg data.
		}
	};

	PictureCallback jpeg = new PictureCallback(){
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		//				WindowManager.LayoutParams.FLAG_FULLSCREEN);


		mGLSurfaceView = new TouchSurfaceView(this); 

		mSurfaceView = new SurfaceView(this);
		addContentView(mSurfaceView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		addContentView(mGLSurfaceView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));

		DrawOnTop mDraw = new DrawOnTop(this);
		addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT|LayoutParams.FLAG_BLUR_BEHIND); 
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Camera.Parameters p = camera.getParameters();
		p.setPreviewSize(arg2, arg3);
		try {
			camera.setPreviewDisplay(arg0);
			camera.setDisplayOrientation(90);
		} catch (IOException e) {
			e.printStackTrace();
		}
		camera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
	}


	class TouchSurfaceView extends GLSurfaceView {  
		private final float TOUCH_SCALE_FACTOR = 180.0f / 320;  
		private final float TRACKBALL_SCALE_FACTOR = 36.0f;   
		public  CubeRenderer cr ;
		private float mPreviousX;   
		private float mPreviousY;

		public TouchSurfaceView(Context context) {       
			super(context); 
			cr  = new CubeRenderer(true);
			this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			this.setRenderer(cr);
			this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);  
			this.getHolder().setFormat(PixelFormat.TRANSPARENT);

		}  

		public boolean onTrackballEvent(MotionEvent e) {     
			cr.mAngleX += e.getX() * TRACKBALL_SCALE_FACTOR;      
			cr.mAngleY += e.getY() * TRACKBALL_SCALE_FACTOR;    
			requestRender();   
			return true;    }   

		@Override
		public boolean onTouchEvent(MotionEvent e) {      
			float x = e.getX();       
			float y = e.getY();     
			switch (e.getAction()) {    
			case MotionEvent.ACTION_MOVE:     
				float dx = x - mPreviousX;         
				float dy = y - mPreviousY;           
				cr.mAngleX += dx * TOUCH_SCALE_FACTOR;   
				cr.mAngleY += dy * TOUCH_SCALE_FACTOR;       
				requestRender();      
			}     
			mPreviousX = x;  
			mPreviousY = y;     
			return true;  
		}
	}

	class DrawOnTop extends View {
		Paint paint = new Paint();
		
		public DrawOnTop(Context context) {
			super(context);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.YELLOW);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawText("Test Text", 20, 20, paint);
			RectF oval;
			oval = new RectF();
			oval.set(100, 100, 200, 200);
			canvas.drawArc(oval, 45, 270, true, paint);
			super.onDraw(canvas);
		}
	}
}