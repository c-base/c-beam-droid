package org.c_base.c_beam.activity;

import java.io.IOException;

import org.c_base.c_beam.R;
import org.c_base.c_beam.ccorder.DrawOnTop;
import org.c_base.c_beam.ccorder.Scanbar;
import org.c_base.c_beam.ccorder.TouchSurfaceView;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ToggleButton;

@SuppressLint("NewApi")
public class CcorderActivity extends C_beamActivity implements Callback {
	private Camera camera;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	//	private TouchSurfaceView mGLSurfaceView;

	private View scanbar;
	private View grid;

	private View mVictimContainer;
	private ToggleButton toggleButtonScanner;
	private ToggleButton toggleButtonGrid;
	private Button buttonPhotons;

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
		toggleButtonScanner = (ToggleButton) findViewById(R.id.hideme1);
		toggleButtonScanner.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ToggleButton b = (ToggleButton) v;
				if (b.isChecked()) {
					int height = getResources().getDisplayMetrics().heightPixels - getSupportActionBar().getHeight() - 240;
					TranslateAnimation transAnimation= new TranslateAnimation(0, 0, 0, height);
					transAnimation.setRepeatMode(2);
					transAnimation.setRepeatCount(-1);
					transAnimation.setDuration(1000);
					scanbar.startAnimation(transAnimation);
				} else {
					scanbar.clearAnimation();
					scanbar.setVisibility(View.GONE);
				}
			}
		});
		toggleButtonGrid = (ToggleButton) findViewById(R.id.hideme2);
		toggleButtonGrid.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ToggleButton b = (ToggleButton) v;
				if (b.isChecked()) {
					grid.setVisibility(View.VISIBLE);
				} else {
					grid.setVisibility(View.INVISIBLE);
				}
			}
		});
		
		buttonPhotons = (Button) findViewById(R.id.hideme3);
		buttonPhotons.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ledon();
				
			}
		});

		Button visibleButton = (Button) findViewById(R.id.vis);
		visibleButton.setOnClickListener(mVisibleListener);
		Button invisibleButton = (Button) findViewById(R.id.invis);
		invisibleButton.setOnClickListener(mInvisibleListener);

		grid = findViewById(R.id.grid); 
		parent = (ViewGroup) grid.getParent();
		index = parent.indexOfChild(grid);
		parent.removeView(grid);
		grid = new DrawOnTop(this);
		grid.setVisibility(View.INVISIBLE);
		parent.addView(grid, index);

		scanbar = findViewById(R.id.scanbar); 
		parent = (ViewGroup) scanbar.getParent();
		index = parent.indexOfChild(scanbar);
		parent.removeView(scanbar);
		scanbar = new Scanbar(this);
		scanbar.setVisibility(View.INVISIBLE);
		parent.addView(scanbar, index);

		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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
	
	void ledon() {
	    
	    Parameters params = camera.getParameters();
	    params.setFlashMode(Parameters.FLASH_MODE_ON);
	    camera.setParameters(params);
	    
	    camera.startPreview();
	}

	void ledoff() {
//	    camera.stopPreview();
//	    camera.release();
	}

	OnClickListener mVisibleListener = new OnClickListener() {
		public void onClick(View v) {
			toggleButtonScanner.setVisibility(View.VISIBLE);
			toggleButtonGrid.setVisibility(View.VISIBLE);
			buttonPhotons.setVisibility(View.VISIBLE);
			mVictimContainer.setVisibility(View.VISIBLE);
		}
	};

	OnClickListener mInvisibleListener = new OnClickListener() {
		public void onClick(View v) {
//			toggleButtonScanner.setVisibility(View.INVISIBLE);
//			toggleButtonGrid.setVisibility(View.INVISIBLE);
//			buttonPhotons.setVisibility(View.INVISIBLE);
			mVictimContainer.setVisibility(View.INVISIBLE);
		}
	};
}

