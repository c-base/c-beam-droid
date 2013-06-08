package org.c_base.c_beam.activity;

import java.io.IOException;
import java.util.ArrayList;

import android.widget.LinearLayout;
import android.widget.TextView;
import com.androidplot.xy.XYPlot;
import org.c_base.c_beam.R;
import org.c_base.c_beam.ccorder.DrawOnTop;
import org.c_base.c_beam.ccorder.Scanbar;
import org.c_base.c_beam.ccorder.SensorPlot;
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

	private View scanBar;
	private View grid;
    private View sensorPlotLayout;

	private View mVictimContainer;
    private TextView textView1;
    private TextView textView2;
	private MediaPlayer zap;
	private MediaPlayer scan;
    private MediaPlayer bleeps;

	private SensorManager mSensorManager;

    private SensorPlot magnetPlot;
    private SensorPlot gravityPlot;
    private SensorPlot accelerationPlot;
    private SensorPlot gyroPlot;
    private SensorPlot lightPlot;

    ArrayList<Sensor> sensors = new ArrayList<Sensor>();

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
    private String[] dimensionArrayXYZ = {"X", "Y", "Z"};
    private String[] dimensionArrayLux = {"/ lux"};
    private ToggleButton toggleButtonSensors;
    private ToggleButton toggleButtonScanner;
    private ToggleButton toggleButtonCam;
    private boolean mustRelease = false;
    private boolean previewOn = false;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ccorder);

        setupSounds();
        setupSensors();
		setupGLSurfaceView();
        setupControls();
        setupPlotViews();
        setupGrid();
        setupScanBar();
        setupSurfaceView();

        showWarningMessage();
	}

    private void setupSounds() {
        zap = MediaPlayer.create(this, R.raw.zap);
        scan = MediaPlayer.create(this, R.raw.scan);
        bleeps = MediaPlayer.create(this, R.raw.bleeps);
    }

    private void setupSurfaceView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
    }

    private void setupControls() {
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
                    scanBar.startAnimation(transAnimation);
                } else {
                    scanBar.clearAnimation();
                    scanBar.setVisibility(View.GONE);
                }
            }
        });

        ToggleButton toggleButtonGrid = (ToggleButton) findViewById(R.id.hideme2);
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

        Button buttonPhotons = (Button) findViewById(R.id.hideme3);
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

        ToggleButton toggleButtonFilter = (ToggleButton) findViewById(R.id.hideme4);
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

        toggleButtonSensors = (ToggleButton) findViewById(R.id.hideme5);
        toggleButtonSensors.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ToggleButton b = (ToggleButton) v;
                if (b.isChecked()) {
                    sensorPlotLayout.setVisibility(View.VISIBLE);

                } else {
                    sensorPlotLayout.setVisibility(View.INVISIBLE);
                }
            }
        });

        toggleButtonCam = (ToggleButton) findViewById(R.id.hideme6);
        toggleButtonCam.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                ToggleButton b = (ToggleButton) v;
                if (b.isChecked()) {
                    mSurfaceView.setVisibility(View.VISIBLE);
                } else {
                    mSurfaceView.setVisibility(View.INVISIBLE);
                }
            }
        });

        ToggleButton visibleButton = (ToggleButton) findViewById(R.id.vis);
        visibleButton.setOnClickListener(mVisibleListener);
    }

    private void setupGLSurfaceView() {
        glSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
        ViewGroup parent = (ViewGroup) glSurfaceView.getParent();
        int index = parent.indexOfChild(glSurfaceView);
        parent.removeView(glSurfaceView);
        glSurfaceView = new TouchSurfaceView(this);
        parent.addView(glSurfaceView, index);
    }

    private void setupPlotViews() {
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);

//        LinearLayout sensorPlotLayout = (LinearLayout) findViewById(R.id.sensorPlotLayout);
//        XYPlot plot = new XYPlot(this, "foo");
//        plot.setLayoutParams(LinearLayout);
//        sensorPlotLayout.addView(plot);

        magnetPlot = new SensorPlot("magnetfeld", dimensionArrayXYZ, (XYPlot) findViewById(R.id.mySimpleXYPlot));
        gravityPlot = new SensorPlot("gravitation", dimensionArrayXYZ, (XYPlot) findViewById(R.id.mySimpleXYPlot2));
        accelerationPlot = new SensorPlot("bec_leunigung", dimensionArrayXYZ, (XYPlot) findViewById(R.id.mySimpleXYPlot3));
        gyroPlot = new SensorPlot("rotation", dimensionArrayXYZ, (XYPlot) findViewById(R.id.mySimpleXYPlot4));
        lightPlot = new SensorPlot("photonendichte", dimensionArrayLux, (XYPlot) findViewById(R.id.mySimpleXYPlot5));
    }

    private void setupGrid() {
        ViewGroup parent;
        int index;
        grid = findViewById(R.id.grid);
        parent = (ViewGroup) grid.getParent();
        index = parent.indexOfChild(grid);
        parent.removeView(grid);
        grid = new DrawOnTop(this);
        grid.setVisibility(View.INVISIBLE);
        parent.addView(grid, index);
    }

    private void setupScanBar() {
        ViewGroup parent;
        int index;
        scanBar = findViewById(R.id.scanbar);
        parent = (ViewGroup) scanBar.getParent();
        index = parent.indexOfChild(scanBar);
        parent.removeView(scanBar);
        scanBar = new Scanbar(this);
        scanBar.setVisibility(View.INVISIBLE);
        parent.addView(scanBar, index);
    }

    private void showWarningMessage() {
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

    private void setupSensors() {
        sensorPlotLayout = findViewById(R.id.sensorPlotLayout);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
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
		//camera = Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "stopping cam");
        if (camera != null) {
            camera.stopPreview();
        }
		//camera.release();
        //camera = null;
	}

	void ledOn() {
//        if(mSurfaceView.getVisibility() != View.VISIBLE) {
//            camera = Camera.open();
//        }
		Parameters params = camera.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_TORCH);
		camera.setParameters(params);
        //Log.i(TAG, ""+getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH));
	}
	
	void ledOff() {
        Parameters params = camera.getParameters();
		params.setFlashMode(Parameters.FLASH_MODE_OFF);
		camera.setParameters(params);
//        if(mSurfaceView.getVisibility() != View.VISIBLE) {
//            camera.release();
//        }
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
				mVictimContainer.setVisibility(View.VISIBLE);
			} else {
				mVictimContainer.setVisibility(View.GONE);
			}
		}
	};

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            if (toggleButtonScanner.isChecked()) {
                if (Math.abs(event.values[1] + event.values[2]) > 5) {
                    scan.seekTo(0);
                    scan.start();
                }
                //accelerationPlot.addEvent(event);
            } else if (event.values[2] > 10) {
                bleeps.seekTo(0);
                bleeps.start();
            }
        }
        if (toggleButtonSensors.isChecked()) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
    //            textView1.setText("magnetfeldX: " + event.values[0] + " uT");
    //            textView2.setText("magnetfeldY: " + event.values[1] + " uT");
                magnetPlot.addEvent(event);
            }
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                gravityPlot.addEvent(event);
            }
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerationPlot.addEvent(event);
            }
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gyroPlot.addEvent(event);
            }
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                lightPlot.addEvent(event);
            }
        }
    }

    protected void onResume () {
		super.onResume();
//        if (toggleButtonScanner.isChecked() || toggleButtonSensors.isChecked())
        registerSensors();
        camera = Camera.open();
    }

    private void registerSensors() {
        for (Sensor sensor: sensors) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    protected void onPause() {
		super.onPause();
        unregisterSensors();
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private void unregisterSensors() {
        for (Sensor sensor: sensors) {
            mSensorManager.unregisterListener(this, sensor);
        }
    }

}

