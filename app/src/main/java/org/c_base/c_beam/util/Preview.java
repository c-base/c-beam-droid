package org.c_base.c_beam.util;

import java.io.IOException;

import org.c_base.c_beam.activity.CcorderActivity;

import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * This class handles the camera. In particular, the method setPreviewCallback
 * is used to receive camera images. The camera images are not processed in
 * this class but delivered directly to the GLLayer. This class itself does
 * not display the camera images.
 * 
 * @author Niels
 *
 */
public class Preview extends SurfaceView implements SurfaceHolder.Callback {
    Camera mCamera;
    boolean isPreviewRunning = false;
	GLLayer glLayer;
	
    public Preview(CcorderActivity context, GLLayer glLayer) {
        super(context);
        this.glLayer=glLayer;
        
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        SurfaceHolder mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        this.setFocusable(true);
        this.requestFocus();        
    }
    
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
	}

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
    	try {
	    	if (mCamera!=null) {
	    		mCamera.stopPreview();  
	    		isPreviewRunning=false;
	    		mCamera.release();
	    	}
    	} catch (Exception e) {

    	}
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	if (isPreviewRunning) mCamera.stopPreview();

    	Camera.Parameters p = mCamera.getParameters();  
    	p.setPreviewSize(240, 160);
    	mCamera.setParameters(p);
    	
    	try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	mCamera.startPreview();
    	isPreviewRunning = true;
        mCamera.setPreviewCallback(glLayer);
    }
}