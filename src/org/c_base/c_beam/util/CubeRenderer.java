package org.c_base.c_beam.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;


public class CubeRenderer implements GLSurfaceView.Renderer {  
	private Square square;
	
	public CubeRenderer(boolean useTranslucentBackground) { 
		mTranslucentBackground = useTranslucentBackground;   
		mCube = new Cube();
		this.square = new Square();
	}  


	public void onDrawFrame(GL10 gl) {  
		Log.i("CubeRenderer", "draw");
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);  
		gl.glMatrixMode(GL10.GL_MODELVIEW);     
		gl.glLoadIdentity();    
		gl.glTranslatef(0, 0, -5.0f);  
		gl.glRotatef(mAngle,        0, 1, 0);  
		gl.glRotatef(mAngle*0.25f,  1, 0, 0);     
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); 
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);    
		mCube.draw(gl);      
//		gl.glRotatef(mAngle*2.0f, 0, 1, 1);    
//		gl.glTranslatef(0.5f, 0.5f, 0.5f);     
//		mCube.draw(gl);     
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

class Square {
	
	private FloatBuffer vertexBuffer;	// buffer holding the vertices
	
	private float vertices[] = {
			-1.0f, -1.0f,  0.0f,		// V1 - bottom left
			-1.0f,  1.0f,  0.0f,		// V2 - top left
			 1.0f, -1.0f,  0.0f,		// V3 - bottom right
			 1.0f,  1.0f,  0.0f			// V4 - top right
	};
	
	public Square() {
		// a float has 4 bytes so we allocate for each coordinate 4 bytes
		ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
		vertexByteBuffer.order(ByteOrder.nativeOrder());
		
		// allocates the memory from the byte buffer
		vertexBuffer = vertexByteBuffer.asFloatBuffer();
		
		// fill the vertexBuffer with the vertices
		vertexBuffer.put(vertices);
		
		// set the cursor position to the beginning of the buffer
		vertexBuffer.position(0);
	}

	/** The draw method for the square with the GL context */
	public void draw(GL10 gl) {
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		// set the colour for the square
		gl.glColor4f(0.0f, 1.0f, 0.0f, 0.5f);
		
		// Point to our vertex buffer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

		// Draw the vertices as triangle strip
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
		
		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}
}
