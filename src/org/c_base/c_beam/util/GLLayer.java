package org.c_base.c_beam.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.opengl.GLU;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * This class uses OpenGL ES to render the camera's viewfinder image on the screen.
 * Unfortunately I don't know much about OpenGL (ES). The code is mostly copied from
 * some examples. The only interesting stuff happens in the main loop (the run method)
 * and the onPreviewFrame method.
 */
public class GLLayer extends SurfaceView implements SurfaceHolder.Callback, Runnable, Camera.PreviewCallback {
	final static float camObjCoord[] = new float[] {
		-2.0f, -1.5f,  2.0f,
		 2.0f, -1.5f,  2.0f,
		-2.0f,  1.5f,  2.0f,
		 2.0f,  1.5f,  2.0f	 
	};
	final static float camTexCoords[] = new float[] {
		// Camera preview
		 0.0f, 0.625f,
		 0.9375f, 0.625f,
		 0.0f, 0.0f,
		 0.9375f, 0.0f			 
	};

	protected EGLContext eglContext;
	protected SurfaceHolder sHolder;
	protected Thread mainLoop;
	protected boolean running;
	int width;
	int height;
	
	EGLSurface surface;
	EGLDisplay dpy;
	EGL10 egl;
	GL10 gl;
	
	FloatBuffer cubeBuff;
	FloatBuffer texBuff;
	byte[] glCameraFrame;
	int[] cameraTexture;

	BooleanLock newFrameLock=new BooleanLock(false);	

	public GLLayer(Context c) {
		super(c);
		
		sHolder = getHolder();
		sHolder.addCallback(this);
		sHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
		
		cubeBuff = makeFloatBuffer(camObjCoord);
		texBuff = makeFloatBuffer(camTexCoords);		
	}
	FloatBuffer makeFloatBuffer(float[] arr) {
		ByteBuffer bb = ByteBuffer.allocateDirect(arr.length*4);
		bb.order(ByteOrder.nativeOrder());
		FloatBuffer fb = bb.asFloatBuffer();
		fb.put(arr);
		fb.position(0);
		return fb;
	}
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		synchronized (this) {
			this.width = width;
			this.height = height;
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		mainLoop = new Thread(this);
		mainLoop.start();
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		if (running) {
			running = false;
			this.newFrameLock.setValue(true);
			try {
				mainLoop.join();
			}
			catch (Exception ex) {}
			mainLoop = null;
			
	        egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
	        egl.eglDestroySurface(dpy, surface);
	        egl.eglDestroyContext(dpy, eglContext);
	        egl.eglTerminate(dpy);
		}
	}	
	
	/**
	 * Some initialization of the OpenGL stuff (that I don't
	 * understand...)
	 */
	protected void init() {		
		// Much of this code is from GLSurfaceView in the Google API Demos.
		// I encourage those interested to look there for documentation.
		egl = (EGL10)EGLContext.getEGL();
		dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		
		int[] version = new int[2];
        egl.eglInitialize(dpy, version);
        
        int[] configSpec = {
                EGL10.EGL_RED_SIZE,      5,
                EGL10.EGL_GREEN_SIZE,    6,
                EGL10.EGL_BLUE_SIZE,     5,
                EGL10.EGL_DEPTH_SIZE,    8,
                EGL10.EGL_NONE
        };
        
        EGLConfig[] configs = new EGLConfig[1];
        int[] num_config = new int[1];
        egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config);
        EGLConfig config = configs[0];
		
		eglContext = egl.eglCreateContext(dpy, config, EGL10.EGL_NO_CONTEXT, null);
		
		surface = egl.eglCreateWindowSurface(dpy, config, sHolder, null);
		egl.eglMakeCurrent(dpy, surface, surface, eglContext);
			
		gl = (GL10)eglContext.getGL();
		
		//Load the buffer and stuff		
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepthf(1.0f);
		
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, cubeBuff);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texBuff);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glEnable(GL10.GL_TEXTURE_2D);
			
		//Resize... whatever?
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glViewport(0,0,width,height);
		GLU.gluPerspective(gl, 45.0f, ((float)width)/height, 1f, 100f);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		GLU.gluLookAt(gl, 0, 0, 5.5f, 0, 0, 0, 0, 1, 0);
		gl.glNormal3f(0,0,1);
	}
	
	/**
	 * Generates a texture from the black and white array filled by the onPreviewFrame
	 * method.
	 */
	void bindCameraTexture(GL10 gl) {
		synchronized(this) {
			if (cameraTexture==null)
				cameraTexture=new int[1];
			else
				gl.glDeleteTextures(1, cameraTexture, 0);
			
			gl.glGenTextures(1, cameraTexture, 0);
			int tex = cameraTexture[0];
			gl.glBindTexture(GL10.GL_TEXTURE_2D, tex);
			gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE, 256, 256, 0, GL10.GL_LUMINANCE, GL10.GL_UNSIGNED_BYTE, ByteBuffer.wrap(glCameraFrame));
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		}
	}	
	
	/**
	 * After some initialization the method loops and renders the camera image
	 * whenever a new frame arrived. The black and white byte array is binded to a
	 * texture by the bindCameraTexture method. Afterwards an object can be
	 * rendered with this texture.
	 */
	public void run() {
		init();

		running = true;
		
		while (running) {
			if (!running) return;

			//gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
			
			newFrameLock.waitUntilTrue(1000000);
			newFrameLock.setValue(false);
			bindCameraTexture(gl);
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
			
			//gl.glRotatef(1,0,0,1); //Rotate the camera image
			
			egl.eglSwapBuffers(dpy, surface);
			
            if (egl.eglGetError() == EGL11.EGL_CONTEXT_LOST) {
                Context c = getContext();
                if (c instanceof Activity) {
                    ((Activity)c).finish();
                }
            }
		}
	}

	/**
	 * This method is called if a new image from the camera arrived. The camera
	 * delivers images in a yuv color format. It is converted to a black and white
	 * image with a size of 256x256 pixels (only a fraction of the resulting image
	 * is used). Afterwards Rendering the frame (in the main loop thread) is started by
	 * setting the newFrameLock to true. 
	 */
	public void onPreviewFrame(byte[] yuvs, Camera camera) {
		if (!running) return;
		
		if (glCameraFrame==null)
			glCameraFrame=new byte[1024*1024]; //size of a texture must be a power of 2
		
   		int bwCounter=0;
   		int yuvsCounter=0;
   		for (int y=0;y<160;y++) {
   			System.arraycopy(yuvs, yuvsCounter, glCameraFrame, bwCounter, 1000);
   			yuvsCounter=yuvsCounter+1000;
   			bwCounter=bwCounter+1024;
   		}
   		
		newFrameLock.setValue(true);	
	}
}
