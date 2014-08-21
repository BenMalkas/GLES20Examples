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

import r2ai.gles20.examples.CubeBuffers;
import r2ai.gles20.examples.GLESUtil;
import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * Cube represents an encapsulation of a 3D OpengGL object, but with no vertex
 * data since everything is handled with buffer array objects in CubeBuffers.
 */
public class Cube {
	
	private int mVertexShaderHandle;
	private int mFragmentShaderHandle;
	private int mProgramHandle;
	
	// matrix for the cube space
	float[] mModelMatrix = new float[16];
	
	// model-view-projection matrix
	private float[] mMVPMatrix = new float[16];
	
	private String mVertexShaderSrc, mFragmentShaderSrc;

	private int mAColor;
	private int mAPosition;
	private int mANormal;
	private int mATextureCoord;
	private int mUMVPMatrix;
	private int mUTexture;
	
	private int mVerticesCount;
	
	public Cube() {}
	
	/**
	 * Creates the OpengL program and retrieve handles to vertex attributes and uniforms.
	 * Uses the buffer indexes created in CubeBuffer to set attributes indexes locations.
	 */
	public void init(CubeBuffers cubeBuffers, String vertexShader, String fragmentShader) {
		
		mVerticesCount = cubeBuffers.mCubeVertices.length / 3;
		
		mVertexShaderSrc = vertexShader;
		mFragmentShaderSrc = fragmentShader;
		
		// compile the vertex shader
		mVertexShaderHandle = GLESUtil.loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderSrc);

		// compile the fragment shader
		mFragmentShaderHandle = GLESUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderSrc);

		// create an OpenGL program, attach both shaders to it and link it
		mProgramHandle = GLESUtil.createProgram(mVertexShaderHandle, mFragmentShaderHandle);
		
		Matrix.setIdentityM(mModelMatrix,0);
		Matrix.setIdentityM(mMVPMatrix,0);
		
		// retrieve vertex attributes location
		mAPosition = GLES20.glGetAttribLocation(mProgramHandle, "a_position");
		mAColor = GLES20.glGetAttribLocation(mProgramHandle, "a_color");
		mANormal = GLES20.glGetAttribLocation(mProgramHandle, "a_normal");
		mATextureCoord = GLES20.glGetAttribLocation(mProgramHandle, "a_texture_coord");
		
		// no constant attributes so enable arrays for all attributes
		GLES20.glEnableVertexAttribArray(mAPosition);
		GLES20.glEnableVertexAttribArray(mAColor);
		GLES20.glEnableVertexAttribArray(mANormal);
		GLES20.glEnableVertexAttribArray(mATextureCoord);
		
		// retrieve uniforms locations
		mUMVPMatrix = GLES20.glGetUniformLocation(mProgramHandle, "u_mvp_matrix");
		
		mUTexture = GLES20.glGetUniformLocation(mProgramHandle, "s_texture");
		
		// now it's time to tell the attributes where the vertex data is,
		// for each attribute first the associated buffer is binded (glBindBuffer) 
		// and then  the attribute location is specified (glVertexAttribPointer)
		// with data format.
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeBuffers.mBuffers.get(0));
		GLES20.glVertexAttribPointer(mAPosition, 3, GLES20.GL_FLOAT, false, 0, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeBuffers.mBuffers.get(1));
		GLES20.glVertexAttribPointer(mAColor, 4, GLES20.GL_FLOAT, false, 0, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeBuffers.mBuffers.get(2));
		GLES20.glVertexAttribPointer(mANormal, 3, GLES20.GL_FLOAT, false, 0, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeBuffers.mBuffers.get(3));
		GLES20.glVertexAttribPointer(mATextureCoord, 3, GLES20.GL_FLOAT, false, 0, 0);
		
		// same thing for the texture
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, cubeBuffers.mTextures.get(0));
		
		// here GL_TEXTURE0 is used so the uniform must be set to 0
		GLES20.glUniform1i(mUTexture, 0);

	}
	
	/**
	 * The draw method called by SimpleRenderer.onDrawFrame.
	 * It has to be fast : no useless operation and no allocation.
	 */
	public void draw(float[] viewProjectionMatrix) {

		// use the mProgramHandle for which everything has been set up in init
		GLES20.glUseProgram(mProgramHandle);
		
		// the only operation that need to be continuously done here
	    Matrix.multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);
		GLES20.glUniformMatrix4fv(mUMVPMatrix, 1, false, mMVPMatrix, 0);
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mVerticesCount);
	
	}
	
}
