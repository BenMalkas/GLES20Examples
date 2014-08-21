/*
 * Copyright 2014 Benjamin Malkas
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package r2ai.gles20.examples;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView.Renderer;

/**
 * SimpleRenderer implements the OpenGL renderer.
 * The scene is set once in onSurfaceCreated and onDrawFrame is called continuously.
 */
public class SimpleRenderer implements Renderer {
	
	public static interface FpsListener {
		public void setFps(final int d);
	}
	
	private Context mContext;
	
	private long mStartTimeNS;
	private long mFrameCount = 0;
	
	private Cube mOuterCube;
	private Cube mInnerCube;
	
	private String mVertexShaderSrc = 
			"attribute vec4 a_position;"
			+ "attribute vec4 a_color;"
			+ "attribute vec3 a_normal;"
			+ "attribute vec3 a_texture_coord;" 
			+ "varying vec4 v_color;"
			+ "varying vec3 v_normal;"
			+ "varying vec3 v_texture_coord;"
			+ "uniform mat4 u_mvp_matrix;"
			+ "void main()" 
			+ "{" 
			+ "gl_Position = u_mvp_matrix * a_position;"
			+ "v_color = a_color;"
			+ "v_normal = a_normal;"
			+ "v_texture_coord = a_texture_coord;" 
			+ "}";

	private String mFragmentShaderSrc1 = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH \n"
			+ "precision highp float; \n"
			+ "#else \n"
			+ "precision mediump float; \n"
			+ "#endif \n"
			+ "varying vec4 v_color; \n"
			+ "varying vec3 v_normal; \n"
			+ "varying vec3 v_texture_coord; \n"
			+ "uniform samplerCube s_texture; \n"
			+ "void main() \n" + "{ \n"
			+ "vec4 base_color = textureCube(s_texture, v_texture_coord); \n"
			+ "if(base_color.a < 0.5) \n"
			+ "discard; \n"
			+ "vec3 mix_color = base_color.rgb * v_color.rgb; \n"
			+ "gl_FragColor = vec4(mix_color, base_color.a); \n" 
			+ "} \n";
	
	private String mFragmentShaderSrc2 = 
			"#ifdef GL_FRAGMENT_PRECISION_HIGH \n"
			+ "precision highp float; \n"
			+ "#else \n"
			+ "precision mediump float; \n"
			+ "#endif \n"
			+ "varying vec4 v_color; \n"
			+ "varying vec3 v_normal; \n"
			+ "varying vec3 v_texture_coord; \n"
			+ "uniform samplerCube s_texture; \n"
			+ "void main() \n" + "{ \n"
			+ "vec4 base_color = textureCube(s_texture, v_texture_coord); \n"
			+ "if(base_color.a < 0.5) \n"
			+ "discard; \n"
			+ "gl_FragColor = v_color; \n" 
			+ "} \n";
	
	// camera matrix
	private float[] mViewMatrix = new float[16];
	// projection matrix
	private float[] mProjectionMatrix = new float[16];

	public SimpleRenderer(Context context) {
		mContext = context;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.opengl.GLSurfaceView.Renderer#onSurfaceCreated(javax.microedition
	 * .khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
	 */
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the color buffer clear color.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        
        // Enable the depth buffer so that OpenGL take into account if an object occlude
        // another and not just draw objects over each others according to draw functions call order.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LESS);
        
        // Load all vertex data (positions, texture coordinates, normals 
        // and textures) in video ram.
		
		CubeBuffers mCubeBuffers = new CubeBuffers();
        mCubeBuffers.init(mContext);
        
        mOuterCube = new Cube();
        mInnerCube = new Cube();
        
        mInnerCube.init(mCubeBuffers, mVertexShaderSrc, mFragmentShaderSrc1);
        mOuterCube.init(mCubeBuffers, mVertexShaderSrc, mFragmentShaderSrc2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.opengl.GLSurfaceView.Renderer#onSurfaceChanged(javax.microedition
	 * .khronos.opengles.GL10, int, int) 
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		float aspectRatio = (float) width / (float) height;
		// set the projection as a classic perspective projection
		Matrix.perspectiveM(mProjectionMatrix, 0, 90.0f, aspectRatio, 0.1f, 1000f);
	}
	
	// how long the animation has been running
	private long elapsedTime = 0;
	// when it started
	private long startTime = 0;
	// view-projection matrix
	private float[] vp = new float[16];
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.microedition
	 * .khronos.opengles.GL10)
	 */
	@Override
	public void onDrawFrame(GL10 gl) {
		// clear the color and depth buffer since both of them are used
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT
			| GLES20.GL_DEPTH_BUFFER_BIT);

		long now = System.currentTimeMillis();
		
		// startTime and elapsedTime set the start of the animation at 0 rather
		// than using System.currentTimeMillis() directly.
		if(startTime > 0)
			elapsedTime = now - startTime;
		else 
			startTime = now;
		
		Matrix.setLookAtM(mViewMatrix, 0, -1.0f, 1.75f, 1.5f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
		Matrix.multiplyMM(vp, 0, mProjectionMatrix, 0, mViewMatrix, 0);

		// complete rotation every 4 seconds
		final float angle = 0.09F * (float) (elapsedTime % 4000L);
		
		Matrix.setIdentityM(mInnerCube.mModelMatrix, 0);
	    Matrix.setRotateM(mInnerCube.mModelMatrix, 0, angle, 1.0F, 1.0F, 1.0f);
		
	    mInnerCube.draw(vp);
	    
	    Matrix.setIdentityM(mOuterCube.mModelMatrix, 0);
	    // reverse rotation sense
	    Matrix.setRotateM(mOuterCube.mModelMatrix, 0, -angle, 0F, 1.0F, 0f);
	    // set the outer cube to be 4 times bigger so it can contains the inner cube
	    Matrix.scaleM(mOuterCube.mModelMatrix, 0, 4f, 4f, 4f);
	    
	    mOuterCube.draw(vp);

	    // calculate the frame rate per second
		now = System.nanoTime();
	    ++mFrameCount;
        if (mFrameCount % 50 == 0) {
            final double msPerFrame = (now - mStartTimeNS) / 1e6 / mFrameCount;
					((FpsListener) mContext).setFps((int)(1000/msPerFrame));
            
            mFrameCount = 0;
            mStartTimeNS = now;
        }
        
	}

}
