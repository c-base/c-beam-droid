package org.c_base.c_beam.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.androidplot.xy.XYPlot;
import java.io.IOException;
import java.util.ArrayList;
import org.c_base.c_beam.R;
import org.c_base.c_beam.Settings;
import org.c_base.c_beam.ccorder.DrawOnTop;
import org.c_base.c_beam.ccorder.Scanbar;
import org.c_base.c_beam.ccorder.SensorPlot;
import org.c_base.c_beam.ccorder.TouchSurfaceView;
import org.c_base.c_beam.domain.Ring;

@SuppressLint("NewApi")
public class CcorderActivity extends C_beamActivity implements Callback, SensorEventListener {
    private static final String TAG = "CCorderActivity";
    public static final int SLOWPLOTS_UPDATE_INTERVAL = 100;
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
    private SensorPlot temperaturePlot;

    private SensorEvent lastLightEvent;
    private SensorEvent lastTemperatureEvent;

    private Handler handler = new Handler();

    protected String[] mDrawerItems;
    protected TypedArray mDrawerImages;
    private ListView mDrawerList;

    protected DrawerLayout mDrawerLayout;
    protected ActionBarDrawerToggle mDrawerToggle;

    protected CharSequence mTitle;

    ArrayList<Sensor> sensors = new ArrayList<Sensor>();

    private String[] dimensionArrayXYZ = {"X", "Y", "Z"};
    private String[] dimensionArrayLux = {"/ lux"};
    private String[] dimensionArrayTemp = {"°K"};
    private ToggleButton toggleButtonSensors;
    private ToggleButton toggleButtonScanner;
    private ToggleButton toggleButtonCam;
    private SharedPreferences sharedPref;
    private Runnable updateSlowPlotsCallbacks = new Runnable() {
        @Override
        public void run() {
            updateSlowPlots();
            handler.postDelayed(this, SLOWPLOTS_UPDATE_INTERVAL);
        }
    };

    private void updateSlowPlots() {
        if (lastLightEvent != null) {
            lightPlot.addEvent(lastLightEvent);
        }
        if (lastTemperatureEvent != null) {
            temperaturePlot.addEvent(lastTemperatureEvent);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ccorder);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        setupSounds();
        setupSensors();
        setupGLSurfaceView();
        setupControls();
        setupPlotViews();
        setupGrid();
        setupScanBar();
        setupSurfaceView();
        setupActionBar();
        setupNavigationDrawer();

        showWarningMessage();
    }

    protected void setupNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setBackgroundColor(Color.argb(120, 0, 0, 0));

        mDrawerItems = getResources().getStringArray(R.array.drawer_items_array);
        mDrawerImages = getResources().obtainTypedArray(R.array.drawer_images_array);

        ArrayList<Ring> mRings = new ArrayList<Ring>();
        for (int i = 0; i < mDrawerItems.length; i++) {
            mRings.add(new Ring(mDrawerItems[i], mDrawerImages.getDrawable(i)));
        }

        mDrawerList.setAdapter(new RingAdapter(this, R.layout.drawer_list_item,
                R.id.drawer_list_item_textview, mRings));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                // TODO Auto-generated method stub
                super.onDrawerOpened(drawerView);
                actionBar.setTitle(mTitle);
                sharedPref.edit().putBoolean(Settings.USER_DISCOVERED_NAVDRAWER, true).commit();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        if (!sharedPref.getBoolean(Settings.USER_DISCOVERED_NAVDRAWER, false)) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setHomeButtonEnabled(true);
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
                    TranslateAnimation transAnimation = new TranslateAnimation(0, 0, 0, height);
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
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ledOn();
                        if (zap != null) {
                            zap.seekTo(0);
                            zap.start();
                        }
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
        temperaturePlot = new SensorPlot("C_trahlungsintensität (300GHz - 400THz)", dimensionArrayTemp, (XYPlot) findViewById(R.id.mySimpleXYPlot6));
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

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)) {
            sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
            sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY));
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)) {
            sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE)) {
            sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_LIGHT)) {
            sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
        }
        // it seems there is no PackageManager.FEATURE_SENSOR_MAGNETIC_FIELD
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        sensors.add(mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE));
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
        if (camera != null) {
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
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //camera = Camera.open();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
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
        //Log.d(TAG, "ledOn " + System.currentTimeMillis());
    }

    void ledOff() {
        Parameters params = camera.getParameters();
        params.setFlashMode(Parameters.FLASH_MODE_OFF);
        camera.setParameters(params);
//        if(mSurfaceView.getVisibility() != View.VISIBLE) {
//            camera.release();
//        }
        //Log.d(TAG, "ledOff" + System.currentTimeMillis());
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
                lastLightEvent = event;
            }

            if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){
                lastTemperatureEvent = event;
            }
        }
    }

    protected void onResume() {
        super.onResume();
//        if (toggleButtonScanner.isChecked() || toggleButtonSensors.isChecked())
        registerSensors();
        startTimerForSlowPlots();
        camera = Camera.open();
    }

    private void startTimerForSlowPlots() {
        handler.postDelayed(updateSlowPlotsCallbacks, SLOWPLOTS_UPDATE_INTERVAL);
    }

    private void registerSensors() {
        for (Sensor sensor : sensors) {
            mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    protected void onPause() {
        super.onPause();
        unregisterSensors();
        camera.stopPreview();
        camera.release();
        camera = null;
        handler.removeCallbacks(updateSlowPlotsCallbacks);
    }

    private void unregisterSensors() {
        for (Sensor sensor : sensors) {
            mSensorManager.unregisterListener(this, sensor);
        }
    }

    protected class RingAdapter extends ArrayAdapter {
        private static final String TAG = "UserAdapter";
        private ArrayList<Ring> items;
        private Context context;

        @SuppressWarnings("unchecked")
        public RingAdapter(Context context, int itemLayout, int textViewResourceId, ArrayList<Ring> items) {
            super(context, itemLayout, textViewResourceId, items);
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View listview = super.getView(position, convertView, parent);

            TextView textView = (TextView) listview.findViewById(R.id.drawer_list_item_textview);
            Ring r = items.get(position);

            textView.setText(r.getName());

            ImageView b = (ImageView) listview.findViewById(R.id.drawer_ring_imageView);
            b.setImageDrawable(r.getImage());
            return listview;
        }

    }

    protected class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {

        /*
        // Create a new fragment and specify the planet to show based on position
        Fragment fragment = new PlanetFragment();
        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawer.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawer);
        */
        setTitle(mDrawerItems[position]);

        switch (position) {
            case 0:
                startActivity(ClampActivity.class);
                break;
            case 1:
                startActivity(CarbonActivity.class);
                break;
            case 2:
                startActivity(CcorderActivity.class);
                break;
            case 3:
                startActivity(CreactivActivity.class);
                break;
            case 4:
                startActivity(CultureActivity.class);
                break;
            case 5:
                startActivity(ComActivity.class);
                break;
            case 6:
                startActivity(CoreActivity.class);
                break;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the event for ActionBarDrawerToggle and return true to cancel propagation
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }

            mDrawerToggle.syncState();
            return true;
        }
        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

}

