package org.c_base.c_beam.activity;

import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Color;
import android.widget.TextView;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import org.c_base.c_beam.R;
import org.c_base.c_beam.ccorder.DrawOnTop;
import org.c_base.c_beam.ccorder.Scanbar;
import org.c_base.c_beam.ccorder.TouchSurfaceView;

import com.actionbarsherlock.view.Menu;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ToggleButton;

@SuppressLint("NewApi")
public class CcorderActivity extends C_beamActivity implements Callback, SensorEventListener {
	private static final String TAG = "CCorderActivity";
	private Camera camera;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	GLSurfaceView glSurfaceView;
	//	private TouchSurfaceView mGLSurfaceView;

	private View scanbar;
	private View grid;

	private View mVictimContainer;
	private ToggleButton toggleButtonScanner;
	private ToggleButton toggleButtonGrid;
	private Button buttonPhotons;
	private ToggleButton toggleButtonFilter;
    private TextView textView1;
    private TextView textView2;
	private MediaPlayer zap;
	private MediaPlayer scan;

	private SensorManager mSensorManager;
	private Sensor mSensor;
    private Sensor mGravitySensor;

    private XYPlot mySimpleXYPlot;
    private SimpleXYSeries seriesMagnetX;
    private SimpleXYSeries seriesMagnetY;
    private SimpleXYSeries seriesMagnetZ;

    ArrayList<Number> series1Numbers = new ArrayList<Number>();
    ArrayList<Number> series2Numbers = new ArrayList<Number>();

    //Number[] series1Numbers = {1, 8, 5, 2, 7, 4};
    //Number[] series2Numbers = {4, 6, 3, 8, 2, 10};

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
			// No action taken on the jpeg data yet.
		}
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		zap = MediaPlayer.create(this, R.raw.zap);
		scan = MediaPlayer.create(this, R.raw.scan);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		setContentView(R.layout.activity_ccorder);

		glSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
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
		buttonPhotons.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch ( event.getAction() ) {
			    case MotionEvent.ACTION_DOWN:
			    	if (zap != null) {
						zap.seekTo(0);
						zap.start();
					}
			    	ledOn();
			    	break;
			    case MotionEvent.ACTION_UP:
			    	ledOff();
			    	break;
			    }
				return false;
			}
		});
		
//		buttonPhotons.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if (zap != null) {
//					zap.seekTo(0);
//					zap.start();
//				}
//				ledflash();
//			}
//		});
		
		toggleButtonFilter = (ToggleButton) findViewById(R.id.hideme4);
		toggleButtonFilter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ToggleButton b = (ToggleButton) v;
				if (b.isChecked()) {
					glSurfaceView.setVisibility(View.VISIBLE);
				} else {
					glSurfaceView.setVisibility(View.INVISIBLE);
				}
			}
		});

		ToggleButton visibleButton = (ToggleButton) findViewById(R.id.vis);
		visibleButton.setOnClickListener(mVisibleListener);

        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);

        plotData();

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

		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle(R.string.c_corder_warning_title);
		b.setMessage(Html.fromHtml(getString(R.string.c_corder_warning_text)));
		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		b.show();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			menu.findItem(R.id.menu_c_corder).setVisible(false);
		}
		
		boolean mIsOnline = c_beam.isInCrewNetwork();
		// Hide some menu items when not connected to the crew network
		menu.findItem(R.id.menu_login).setVisible(mIsOnline);
		menu.findItem(R.id.menu_logout).setVisible(mIsOnline);
		menu.findItem(R.id.menu_map).setVisible(mIsOnline);
		menu.findItem(R.id.menu_c_out).setVisible(mIsOnline);
		menu.findItem(R.id.menu_c_mission).setVisible(mIsOnline);
		menu.findItem(R.id.menu_c_corder).setVisible(false);
		return true;
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
		Log.i(TAG, "starting cam");
		camera.startPreview();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "stopping cam");
		camera.stopPreview();
		camera.release();
	}

	void ledOn() {
		Parameters params = camera.getParameters();
		params.setFlashMode(Parameters.FLASH_MODE_TORCH);
		camera.setParameters(params);
	}
	
	void ledOff() {
		Parameters params = camera.getParameters();
		params.setFlashMode(Parameters.FLASH_MODE_OFF);
		camera.setParameters(params);
	}
	void ledflash() {
		Parameters params = camera.getParameters();
		params.setFlashMode(Parameters.FLASH_MODE_TORCH);
		camera.setParameters(params);
		params.setFlashMode(Parameters.FLASH_MODE_OFF);
		camera.setParameters(params);
	}

	OnClickListener mVisibleListener = new OnClickListener() {
		public void onClick(View v) {
			if (((ToggleButton) v).isChecked()) {
				toggleButtonScanner.setVisibility(View.VISIBLE);
				toggleButtonGrid.setVisibility(View.VISIBLE);
				buttonPhotons.setVisibility(View.VISIBLE);
				toggleButtonFilter.setVisibility(View.VISIBLE);
				mVictimContainer.setVisibility(View.VISIBLE);
			} else {
				toggleButtonScanner.setVisibility(View.INVISIBLE);
				toggleButtonGrid.setVisibility(View.INVISIBLE);
				buttonPhotons.setVisibility(View.INVISIBLE);
				toggleButtonFilter.setVisibility(View.INVISIBLE);
				mVictimContainer.setVisibility(View.INVISIBLE);
			}
		}
	};

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (!toggleButtonScanner.isChecked())
			return;
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            if (Math.abs(event.values[1] + event.values[2]) > 6) {
                scan.seekTo(0);
                scan.start();
            }
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            textView1.setText("magnetfeldX: " + event.values[0] + " uT");
            textView2.setText("magnetfeldY: " + event.values[1] + " uT");

            if (seriesMagnetX.size() > 100) {
                seriesMagnetX.removeFirst();
            }
            seriesMagnetX.addLast(null, event.values[0]);
            if (seriesMagnetY.size() > 100) {
                seriesMagnetY.removeFirst();
            }
            seriesMagnetY.addLast(null, event.values[1]);
            if (seriesMagnetZ.size() > 100) {
                seriesMagnetZ.removeFirst();
            }
            seriesMagnetZ.addLast(null, event.values[2]);

            //plotData();
            mySimpleXYPlot.redraw();


        }
	}

    private void plotData() {
        mySimpleXYPlot.clear();
        // Turn the above arrays into XYSeries':
        seriesMagnetX = new SimpleXYSeries(new ArrayList<Number>(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "magnetfeldX");
        seriesMagnetY = new SimpleXYSeries(new ArrayList<Number>(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "magnetfeldY");
        seriesMagnetZ = new SimpleXYSeries(new ArrayList<Number>(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "magnetfeldZ");

        // Create a formatter to use for drawing a series using LineAndPointRenderer:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.rgb(0, 0, 200), null, Color.rgb(0, 0, 80));
        series1Format.getFillPaint().setAlpha(150);

        // add a new series' to the xyplot:
        mySimpleXYPlot.addSeries(seriesMagnetX, series1Format);

        LineAndPointFormatter series2Format = new LineAndPointFormatter(Color.rgb(0, 0, 200), null, Color.rgb(0, 80, 0));
        series2Format.getFillPaint().setAlpha(150);
        // same as above:
        mySimpleXYPlot.addSeries(seriesMagnetY, series2Format);

        LineAndPointFormatter series3Format = new LineAndPointFormatter(Color.rgb(0, 0, 200), null, Color.rgb(80, 0, 0));
        series3Format.getFillPaint().setAlpha(150);
        mySimpleXYPlot.addSeries(seriesMagnetZ, series3Format);

        // reduce the number of range labels
        mySimpleXYPlot.setTicksPerRangeLabel(3);

    }

    protected void onResume () {
		super.onResume();
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mGravitySensor, SensorManager.SENSOR_DELAY_UI);
	}

	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this, mSensor);
        mSensorManager.unregisterListener(this, mGravitySensor);
	}

}

