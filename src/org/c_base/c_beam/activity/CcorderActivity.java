package org.c_base.c_beam.activity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
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
			// TODO Auto-generated method stub
//			FileOutputStream outStream = null;
//			try{
//				outStream = new FileOutputStream("/sdcard/test.jpg");
//				outStream.write(data);
//				outStream.close();
//			}catch(FileNotFoundException e){
//				Log.d("Camera", e.getMessage());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				Log.d("Camera", e.getMessage());
//			}
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

	private void takePicture() {
		// TODO Auto-generated method stub
		//		camera.takePicture(shutter, raw, jpeg);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		camera = Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		camera.stopPreview();
		camera.release();
	}
}




//public class CcorderActivity extends Activity {
//	private Camera mCamera;
//    private CameraPreview mPreview;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_ccorder);
//
//        // Create an instance of Camera
//        mCamera = getCameraInstance();
//
//        // Create our Preview view and set it as the content of our activity.
//        mPreview = new CameraPreview(this, mCamera);
//        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
//        preview.addView(mPreview);
//    }
//    
//    public static Camera getCameraInstance(){
//        Camera c = null;
//        try {
//            c = Camera.open(); // attempt to get a Camera instance
//        }
//        catch (Exception e){
//            Log.i("FOOOOOOOOO",e.getMessage());// Camera is not available (in use or does not exist)
//        }
//        return c; // returns null if camera is unavailable
//    }
//    
//    /** Check if this device has a camera */
//    private boolean checkCameraHardware(Context context) {
//        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
//            // this device has a camera
//            return true;
//        } else {
//            // no camera on this device
//            return false;
//        }
//    }
//    
//    @Override
//    protected void onPause() {
//        super.onPause();
////        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
//        releaseCamera();              // release the camera immediately on pause event
//    }
//
////    private void releaseMediaRecorder(){
////        if (mMediaRecorder != null) {
////            mMediaRecorder.reset();   // clear recorder configuration
////            mMediaRecorder.release(); // release the recorder object
////            mMediaRecorder = null;
////            mCamera.lock();           // lock camera for later use
////        }
////    }
//
//    private void releaseCamera(){
//        if (mCamera != null){
//            mCamera.release();        // release the camera for other applications
//            mCamera = null;
//        }
//    }
//    
//}

class TouchSurfaceView extends GLSurfaceView {  

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
	private final float TOUCH_SCALE_FACTOR = 180.0f / 320;  
	private final float TRACKBALL_SCALE_FACTOR = 36.0f;   
	public  CubeRenderer cr ;
	private float mPreviousX;   
	private float mPreviousY;


}

class CubeRenderer implements GLSurfaceView.Renderer {  
	public CubeRenderer(boolean useTranslucentBackground) { 
		mTranslucentBackground = useTranslucentBackground;   
		mCube = new Cube();  
	}  


	public void onDrawFrame(GL10 gl) {  
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);  
		gl.glMatrixMode(GL10.GL_MODELVIEW);     
		gl.glLoadIdentity();    
		gl.glTranslatef(0, 0, -5.0f);  
		gl.glRotatef(mAngle,        0, 1, 0);  
		gl.glRotatef(mAngle*0.25f,  1, 0, 0);     
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); 
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);    
		mCube.draw(gl);      
		gl.glRotatef(mAngle*2.0f, 0, 1, 1);    
		gl.glTranslatef(0.5f, 0.5f, 0.5f);     
		mCube.draw(gl);     
		mAngle += 1.2f;  
	}  
	public void onSurfaceChanged(GL10 gl, int width, int height) {     
		gl.glViewport(0, 0, width, height);    
		float ratio = (float) width / height;     
		gl.glMatrixMode(GL10.GL_PROJECTION);       
		gl.glLoadIdentity();     
		gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
	} 

	public void setAngle(float _angle){

	}
	private boolean mTranslucentBackground;  
	private Cube mCube;  
	private float mAngle;
	public  float mAngleX;  
	public float mAngleY;

	@Override
	public void onSurfaceCreated(GL10 gl,
			javax.microedition.khronos.egl.EGLConfig config) {
		// TODO Auto-generated method stub
		gl.glDisable(GL10.GL_DITHER);     
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);   
		if (mTranslucentBackground) {          
			gl.glClearColor(0,0,0,0);      
		} else {        
			gl.glClearColor(1,1,1,1);     
		}       
		gl.glEnable(GL10.GL_CULL_FACE);     
		gl.glShadeModel(GL10.GL_SMOOTH);     
		gl.glEnable(GL10.GL_DEPTH_TEST);
	}

}

class Cube{   
	public Cube()  
	{        int one = 0x10000;    
	int vertices[] = {  
			-one, -one, -one,   
			one, -one, -one,     
			one,  one, -one,      
			-one,  one, -one,           
			-one, -one,  one,         
			one, -one,  one,           
			one,  one,  one,          
			-one,  one,  one,        };  

	float[] colors = {      
			0f,    0f,    0f,  0.5f, 
			1f ,  0f,  0f, 0.1f,    
			1f,1f,0f,0.5f,   
			0f,  1f,    0f,  0.1f,      
			0f,    0f,  1f,  0.1f,       
			1f,    0f,  1f,  0.2f,      
			1f,  1f,  1f,  0.1f,        
			0f,  1f,  1f,  0.1f,        };    

	byte indices[] = {          
			0, 4, 5,    0, 5, 1,    
			1, 5, 6,    1, 6, 2,     
			2, 6, 7,    2, 7, 3,      
			3, 7, 4,    3, 4, 0,      
			4, 7, 6,    4, 6, 5,       
			3, 0, 1,    3, 1, 2        };   


	ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);   
	vbb.order(ByteOrder.nativeOrder());     
	mVertexBuffer = vbb.asIntBuffer();    
	mVertexBuffer.put(vertices);      
	mVertexBuffer.position(0);      
	ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length*4);   
	cbb.order(ByteOrder.nativeOrder());     
	mColorBuffer = cbb.asFloatBuffer();      
	mColorBuffer.put(colors);     
	mColorBuffer.position(0);      
	mIndexBuffer = ByteBuffer.allocateDirect(indices.length);     
	mIndexBuffer.put(indices);     
	mIndexBuffer.position(0);    } 
	public void draw(GL10 gl)    {    
		gl.glFrontFace(gl.GL_CW);     
		gl.glVertexPointer(3, gl.GL_FIXED, 0, mVertexBuffer);  
		gl.glColorPointer(4, gl.GL_FIXED, 0, mColorBuffer);     
		gl.glDrawElements(gl.GL_TRIANGLES, 36, gl.GL_UNSIGNED_BYTE, mIndexBuffer);    } 

	private IntBuffer   mVertexBuffer;  
	private FloatBuffer   mColorBuffer;   
	private ByteBuffer  mIndexBuffer;

}


//
//
//
//import java.io.IOException;
//
//import android.app.Activity;
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.hardware.Camera;
//import android.os.Bundle;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.view.Window;
//import android.view.ViewGroup.LayoutParams;
//
//public class CcorderActivity extends Activity {
//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//
////        requestWindowFeature(Window.FEATURE_NO_TITLE);
//
//        Preview mPreview = new Preview(this);
//        DrawOnTop mDraw = new DrawOnTop(this);
//
//        setContentView(mPreview);
//        addContentView(mDraw, new LayoutParams
//(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//
//    }
//}

class DrawOnTop extends View {

	public DrawOnTop(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.WHITE);
		canvas.drawText("Test Text", 20, 20, paint);

		super.onDraw(canvas);
	}

}

//----------------------------------------------------------------------

//class Preview extends SurfaceView implements SurfaceHolder.Callback {
//    SurfaceHolder mHolder;
//    Camera mCamera;
//
//    Preview(Context context) {
//        super(context);
//
//        // Install a SurfaceHolder.Callback so we get notified when the
//        // underlying surface is created and destroyed.
//        mHolder = getHolder();
//        mHolder.addCallback(this);
//        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//    }
//
//    public void surfaceCreated(SurfaceHolder holder) {
//        // The Surface has been created, acquire the camera and tell it where
//        // to draw.
//        mCamera = Camera.open();
//        try {
//			mCamera.setPreviewDisplay(holder);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
//
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        // Surface will be destroyed when we return, so stop the preview.
//        // Because the CameraDevice object is not a shared resource, it's very
//        // important to release it when the activity is paused.
//        mCamera.stopPreview();
//        mCamera = null;
//    }
//
//    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
//        // Now that the size is known, set up the camera parameters and begin
//        // the preview.
//        Camera.Parameters parameters = mCamera.getParameters();
//        parameters.setPreviewSize(w, h);
//        mCamera.setParameters(parameters);
//        mCamera.startPreview(); 
//    }
//}