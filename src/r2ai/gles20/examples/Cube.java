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

import java.nio.IntBuffer;

import r2ai.gles20.examples.CubeBuffers;
import r2ai.gles20.examples.GLESUtil;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

/**
 * @author r2ai
 *
 */
public class Cube {
	
	private int mVertexShaderHandle;
	private int mFragmentShaderHandle;
	private int mProgramHandle;
	
	float[] mModelMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];
	
	private String mVertexShaderSrc, mFragmentShaderSrc;

	private int mAColor;
	private int mAPosition;
	private int mANormal;
	private int mATextureCoord;
	private int mUMVPMatrix;
	private int mUTexture;
	
	public Cube() {}
	
	public void init(CubeBuffers cubeBuffers, String vertexShader, String fragmentShader) {
		
		mVertexShaderSrc = vertexShader;
		mFragmentShaderSrc = fragmentShader;
		
		mVertexShaderHandle = GLESUtil.loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderSrc);

		mFragmentShaderHandle = GLESUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderSrc);

		mProgramHandle = GLESUtil.createProgram(mVertexShaderHandle, mFragmentShaderHandle);
		IntBuffer params = IntBuffer.allocate(1);
		GLES20.glGetProgramiv(mProgramHandle, GLES20.GL_ACTIVE_UNIFORMS, params);
		Log.d("CubeMapActivity","uniform count " + params.get(0));
		
		Matrix.setIdentityM(mModelMatrix,0);
		Matrix.setIdentityM(mMVPMatrix,0);
		
		mAPosition = GLES20.glGetAttribLocation(mProgramHandle, "a_position");
		mAColor = GLES20.glGetAttribLocation(mProgramHandle, "a_color");
		mANormal = GLES20.glGetAttribLocation(mProgramHandle, "a_normal");
		mATextureCoord = GLES20.glGetAttribLocation(mProgramHandle, "a_texture_coord");
		
		GLES20.glEnableVertexAttribArray(mAPosition);
		GLES20.glEnableVertexAttribArray(mAColor);
		GLES20.glEnableVertexAttribArray(mANormal);
		GLES20.glEnableVertexAttribArray(mATextureCoord);
		
		mUMVPMatrix = GLES20.glGetUniformLocation(mProgramHandle, "u_mvp_matrix");
		
		mUTexture = GLES20.glGetUniformLocation(mProgramHandle, "s_texture");
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeBuffers.mBuffers.get(0));
		GLES20.glVertexAttribPointer(mAPosition, 3, GLES20.GL_FLOAT, false, 0, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeBuffers.mBuffers.get(1));
		GLES20.glVertexAttribPointer(mAColor, 4, GLES20.GL_FLOAT, false, 0, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeBuffers.mBuffers.get(2));
		GLES20.glVertexAttribPointer(mANormal, 3, GLES20.GL_FLOAT, false, 0, 0);
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, cubeBuffers.mBuffers.get(3));
		GLES20.glVertexAttribPointer(mATextureCoord, 3, GLES20.GL_FLOAT, false, 0, 0);
		
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, cubeBuffers.mTextures.get(0));
		
		GLES20.glUniform1i(mUTexture, 0);

	}
	
	public void draw(float[] viewProjectionMatrix) {

		GLES20.glUseProgram(mProgramHandle);
		
	    Matrix.multiplyMM(mMVPMatrix, 0, viewProjectionMatrix, 0, mModelMatrix, 0);
		GLES20.glUniformMatrix4fv(mUMVPMatrix, 1, false, mMVPMatrix, 0);
		
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36/*CubeBuffers.mCubeVertices.length / 3*/);
	
	}
	
}
