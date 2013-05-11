package org.c_base.c_beam.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import org.c_base.c_beam.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;


public class CubeRenderer implements GLSurfaceView.Renderer {  
	private Square square;
	private Context context;

	public CubeRenderer(boolean useTranslucentBackground, Context context) { 
		mTranslucentBackground = useTranslucentBackground;   
		mCube = new Cube();
		this.square = new Square();
		this.context = context;
	}  


	public void onDrawFrame(GL10 gl) {  
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);  
		gl.glMatrixMode(GL10.GL_MODELVIEW);     
		gl.glLoadIdentity();    
		gl.glTranslatef(0, 0, -2.0f);  
		gl.glRotatef(mAngle,        0, 1, 0);  
		gl.glRotatef(mAngle*0.25f,  1, 0, 0);     
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY); 
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);    
		mCube.draw(gl);


//		gl.glRotatef(mAngle*2.0f, 0, 1, 1);    
//		gl.glTranslatef(0.5f, 0.5f, 0.5f);     
//		mCube.draw(gl);     
		mAngle += 1.2f;

		// clear Screen and Depth Buffer
		//		square.draw(gl);			
	}  
	public void onSurfaceChanged(GL10 gl, int width, int height) {     
		gl.glViewport(0, 0, width, height);    
		float ratio = (float) width / height;     
		gl.glMatrixMode(GL10.GL_PROJECTION);       
		gl.glLoadIdentity();     
		gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);

		if(height == 0) { 						//Prevent A Divide By Zero By
			height = 1; 						//Making Height Equal One
		}

		gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
		gl.glLoadIdentity(); 					//Reset The Projection Matrix

		//Calculate The Aspect Ratio Of The Window
		GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
		gl.glLoadIdentity(); 					//Reset The Modelview Matrix
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

		// Load the texture for the square
		//				square.loadGLTexture(gl, this.context);
		//				
		//				gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping ( NEW )
		//				gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
		//				gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 	//Black Background
		//				gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
		//				gl.glEnable(GL10.GL_DEPTH_TEST); 			//Enables Depth Testing
		//				gl.glDepthFunc(GL10.GL_LEQUAL); 			//The Type Of Depth Testing To Do

		//Really Nice Perspective Calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); 
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
			0f,    0f,  0f,  0.0f, 
			1f,    0f,  0f,  0.0f,    
			1f,	   1f,  0f,  0.0f,   
			0f,    1f,  0f,  0.0f,      
			0f,    0f,  1f,  0.0f,       
			1f,    0f,  1f,  0.0f,      
			1f,    1f,  1f,  0.0f,        
			0f,    1f,  1f,  0.0f,        };    

	//	0f,    0f,  0f,  0.5f, 
	//	1f,    0f,  0f,  0.1f,    
	//	1f,	   1f,  0f,  0.5f,   
	//	0f,    1f,  0f,  0.1f,      
	//	0f,    0f,  1f,  0.1f,       
	//	1f,    0f,  1f,  0.2f,      
	//	1f,    1f,  1f,  0.1f,        
	//	0f,    1f,  1f,  0.1f,        };    

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
		gl.glDrawElements(gl.GL_TRIANGLES, 36, gl.GL_UNSIGNED_BYTE, mIndexBuffer);    
	} 

	private IntBuffer   mVertexBuffer;  
	private FloatBuffer   mColorBuffer;   
	private ByteBuffer  mIndexBuffer;

}


/**
 * @author impaler
 *
 */
class Square {

	private FloatBuffer vertexBuffer;	// buffer holding the vertices
	private float vertices[] = {
			-1.0f, -1.0f,  0.0f,		// V1 - bottom left
			-1.0f,  1.0f,  0.0f,		// V2 - top left
			1.0f, -1.0f,  0.0f,		// V3 - bottom right
			1.0f,  1.0f,  0.0f			// V4 - top right
	};

	private FloatBuffer textureBuffer;	// buffer holding the texture coordinates
	private float texture[] = {    		
			// Mapping coordinates for the vertices
			0.0f, 1.0f,		// top left		(V2)
			0.0f, 0.0f,		// bottom left	(V1)
			1.0f, 1.0f,		// top right	(V4)
			1.0f, 0.0f		// bottom right	(V3)
	};

	/** The texture pointer */
	private int[] textures = new int[1];

	public Square() {
		// a float has 4 bytes so we allocate for each coordinate 4 bytes
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());

		// allocates the memory from the byte buffer
		vertexBuffer = byteBuffer.asFloatBuffer();

		// fill the vertexBuffer with the vertices
		vertexBuffer.put(vertices);

		// set the cursor position to the beginning of the buffer
		vertexBuffer.position(0);

		byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuffer.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);
	}

	/**
	 * Load the texture for the square
	 * @param gl
	 * @param context
	 */
	public void loadGLTexture(GL10 gl, Context context) {
		// loading texture
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.ic_launcher);

		// generate one texture pointer
		gl.glGenTextures(1, textures, 0);
		// ...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		// create nearest filtered texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		//Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

		// Use Android GLUtils to specify a two-dimensional texture image from our bitmap 
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

		// Clean up
		bitmap.recycle();
	}


	/** The draw method for the square with the GL context */
	public void draw(GL10 gl) {
		// bind the previously generated texture
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		// Point to our buffers
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		// Set the face rotation
		gl.glFrontFace(GL10.GL_CW);

		// Point to our vertex buffer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

		// Draw the vertices as triangle strip
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
}
