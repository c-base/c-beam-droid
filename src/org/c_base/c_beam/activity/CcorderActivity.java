package org.c_base.c_beam.activity;

import java.io.IOException;

import org.c_base.c_beam.R;
import org.c_base.c_beam.util.CubeRenderer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.TranslateAnimation;
import android.widget.Button;

@SuppressLint("NewApi")
public class CcorderActivity extends C_beamActivity implements Callback {
	private Camera camera;
	private SurfaceView mSurfaceView;
	SurfaceHolder mSurfaceHolder;
	//	private TouchSurfaceView mGLSurfaceView;


	View mVictimContainer;
	View mVictim1;
	View mVictim2;


	ShutterCallback shutter = new ShutterCallback(){
		@Override
		public void onShutter() {
			// No action to be perfomed on the Shutter callback.
		}
	};
	PictureCallback raw = new PictureCallback(){
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
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

		setContentView(R.layout.activity_ccorder);

		GLSurfaceView glSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
		ViewGroup parent = (ViewGroup) glSurfaceView.getParent();
		int index = parent.indexOfChild(glSurfaceView);
		parent.removeView(glSurfaceView);
		glSurfaceView = new TouchSurfaceView(this);
		parent.addView(glSurfaceView, index);

		// Find the views whose visibility will change
		mVictimContainer = findViewById(R.id.hidecontainer);
		mVictim1 = findViewById(R.id.hideme1);
		mVictim1.setOnClickListener(new HideMeListener(mVictim1));
		mVictim2 = findViewById(R.id.hideme2);
		mVictim2.setOnClickListener(new HideMeListener(mVictim2));

		// Find our buttons
		Button visibleButton = (Button) findViewById(R.id.vis);
		Button invisibleButton = (Button) findViewById(R.id.invis);
		Button goneButton = (Button) findViewById(R.id.gone);

		// Wire each button to a click listener
		visibleButton.setOnClickListener(mVisibleListener);
		invisibleButton.setOnClickListener(mInvisibleListener);
		goneButton.setOnClickListener(mGoneListener);

		//		mSurfaceView = new SurfaceView(this);
		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);

		//		addContentView(mSurfaceView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		//		addContentView(glSurfaceView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
		//
		View grid = findViewById(R.id.grid); 
		parent = (ViewGroup) grid.getParent();
		index = parent.indexOfChild(grid);
		parent.removeView(grid);
		grid = new DrawOnTop(this);
		parent.addView(grid, index);

		//		DrawOnTop mDraw = new DrawOnTop(this);
		//		addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		//		
		View scanbar = findViewById(R.id.scanbar); 
		parent = (ViewGroup) scanbar.getParent();
		index = parent.indexOfChild(scanbar);
		parent.removeView(scanbar);
		scanbar = new Scanbar(this);
		parent.addView(scanbar, index);

		//		Scanbar scanbar = new Scanbar(this);
		//		addContentView(scanbar, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		int height = getResources().getDisplayMetrics().heightPixels - getSupportActionBar().getHeight() - 340;
		TranslateAnimation transAnimation= new TranslateAnimation(0, 0, 0, height);

		transAnimation.setRepeatMode(2);
		transAnimation.setRepeatCount(-1);
		transAnimation.setDuration(1000);
		scanbar.startAnimation(transAnimation);


		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		//		mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT|LayoutParams.FLAG_BLUR_BEHIND); 
		mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
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
			cr  = new CubeRenderer(true, context);
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
		int yPos = 0;

		public DrawOnTop(Context context) {
			super(context);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(2);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			NinePatchDrawable npd = (NinePatchDrawable)getResources().getDrawable(R.drawable.smlnpatch240dpi);
			// Set its bound where you need
			Rect npdBounds = new Rect(
					1,1,canvas.getWidth(),canvas.getHeight());
			npd.setBounds(npdBounds);

			// Finally draw on the canvas
			npd.draw(canvas);

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

	class Scanbar extends View {
		Paint paint = new Paint();
		int yPos = 0;

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
			super.onDraw(canvas);
		}

		@Override
		public boolean onTouchEvent(MotionEvent e) {
			return true;
		}
	}

	OnClickListener mVisibleListener = new OnClickListener() {
		public void onClick(View v) {
			mVictim1.setVisibility(View.VISIBLE);
			mVictim2.setVisibility(View.VISIBLE);
			mVictimContainer.setVisibility(View.VISIBLE);
		}
	};

	OnClickListener mInvisibleListener = new OnClickListener() {
		public void onClick(View v) {
			mVictim1.setVisibility(View.INVISIBLE);
			mVictim2.setVisibility(View.INVISIBLE);
			mVictimContainer.setVisibility(View.INVISIBLE);
		}
	};

	OnClickListener mGoneListener = new OnClickListener() {
		public void onClick(View v) {
			mVictim1.setVisibility(View.GONE);
			mVictim2.setVisibility(View.GONE);
			mVictimContainer.setVisibility(View.GONE);
		}
	};
}

class HideMeListener implements OnClickListener {



	public HideMeListener(View mVictim1) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
System.out.println("fooooo");
	}

}