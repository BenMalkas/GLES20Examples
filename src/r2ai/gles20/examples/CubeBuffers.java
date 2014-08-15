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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 *
 * Set up all the per vertex data for a cube (vertices positions, normals, colors
 * and texture coordinates for a cube map), plus load the cube map texture.
 * 
 */
public class CubeBuffers {
	
	// 0 positions
	// 1 colors
    // 2 normals
	// 3 texture coords
	IntBuffer mBuffers = IntBuffer.allocate(4);
	
	IntBuffer mTextures = IntBuffer.allocate(1);
	
	float[] mCubeVertices = { 
			0.5F, 0.5F, 0.5F, //front
			-0.5F, 0.5F, 0.5F,
			-0.5F, -0.5F, 0.5F,
			
			0.5F, 0.5F, 0.5F,
			-0.5F, -0.5F, 0.5F,
			0.5F, -0.5F, 0.5F,
			
			0.5F, 0.5F, -0.5F, //right
			0.5F, 0.5F, 0.5F,
			0.5F, -0.5F, 0.5F,
			
			0.5F, 0.5F, -0.5F,
			0.5F, -0.5F, 0.5F,
			0.5F, -0.5F, -0.5F,
			
			-0.5F, 0.5F, -0.5F, //back
			0.5F, 0.5F, -0.5F,
			0.5F, -0.5F, -0.5F,
			
			-0.5F, 0.5F, -0.5F,
			0.5F, -0.5F, -0.5F,
			-0.5F, -0.5F, -0.5F,
			
			-0.5F, 0.5F, 0.5F, //left
			-0.5F, 0.5F, -0.5F,
			-0.5F, -0.5F, -0.5F,
			
			-0.5F, 0.5F, 0.5F,
			-0.5F, -0.5F, -0.5F,
			-0.5F, -0.5F, 0.5F,
			
			0.5F, 0.5F, -0.5F, //top
			-0.5F, 0.5F, -0.5F,
			-0.5F, 0.5F, 0.5F,
			
			0.5F, 0.5F, -0.5F,
			-0.5F, 0.5F, 0.5F,
			0.5F, 0.5F, 0.5F,
			
			0.5F, -0.5F, 0.5F, //bottom
			-0.5F, -0.5F, 0.5F,
			-0.5F, -0.5F, -0.5F,
			
			0.5F, -0.5F, 0.5F,
			-0.5F, -0.5F, -0.5F,
			0.5F, -0.5F, -0.5F,
	};
	
	private float[] mCubeNormals = {
			0F, 0F, 1.0F,
			0F, 0F, 1.0F,
			0F, 0F, 1.0F,
			0F, 0F, 1.0F,
			0F, 0F, 1.0F,
			0F, 0F, 1.0F,
			
			1.0F, 0F, 0F,
			1.0F, 0F, 0F,
			1.0F, 0F, 0F,
			1.0F, 0F, 0F,
			1.0F, 0F, 0F,
			1.0F, 0F, 0F,
			
			0F, 0F, -1.0F,
			0F, 0F, -1.0F,
			0F, 0F, -1.0F,
			0F, 0F, -1.0F,
			0F, 0F, -1.0F,
			0F, 0F, -1.0F,
			
			-1.0F, 0F, 0F,
			-1.0F, 0F, 0F,
			-1.0F, 0F, 0F,
			-1.0F, 0F, 0F,
			-1.0F, 0F, 0F,
			-1.0F, 0F, 0F,
			
			0F, 1.0F, 0F,
			0F, 1.0F, 0F,
			0F, 1.0F, 0F,
			0F, 1.0F, 0F,
			0F, 1.0F, 0F,
			0F, 1.0F, 0F,
			
			0F, -1.0F, 0F,
			0F, -1.0F, 0F,
			0F, -1.0F, 0F,
			0F, -1.0F, 0F,
			0F, -1.0F, 0F,
			0F, -1.0F, 0F,
	};
	
	private float[] mCubeColors = { 
			1.0F, 0.0F, 0.0F, 1.0F, // RED
			1.0F, 0.0F, 0.0F, 1.0F,
			1.0F, 0.0F, 0.0F, 1.0F,
			1.0F, 0.0F, 0.0F, 1.0F,
			1.0F, 0.0F, 0.0F, 1.0F,
			1.0F, 0.0F, 0.0F, 1.0F,
			
			0F, 1.0F, 0F, 1.0F,  // GREEN
			0F, 1.0F, 0F, 1.0F,
			0F, 1.0F, 0F, 1.0F,
			0F, 1.0F, 0F, 1.0F,
			0F, 1.0F, 0F, 1.0F,
			0F, 1.0F, 0F, 1.0F,
			
			0F, 0F, 1.0F, 1.0F,  // BLUE
			0F, 0F, 1.0F, 1.0F,
			0F, 0F, 1.0F, 1.0F,
			0F, 0F, 1.0F, 1.0F,
			0F, 0F, 1.0F, 1.0F,
			0F, 0F, 1.0F, 1.0F,
			
			1.0F, 1.0F, 0F, 1.0F,  // YELLOW
			1.0F, 1.0F, 0F, 1.0F,
			1.0F, 1.0F, 0F, 1.0F,
			1.0F, 1.0F, 0F, 1.0F,
			1.0F, 1.0F, 0F, 1.0F,
			1.0F, 1.0F, 0F, 1.0F,
			
			1.0F, 0F, 1.0F, 1.0F, // PURPLE
			1.0F, 0F, 1.0F, 1.0F,
			1.0F, 0F, 1.0F, 1.0F,
			1.0F, 0F, 1.0F, 1.0F,
			1.0F, 0F, 1.0F, 1.0F,
			1.0F, 0F, 1.0F, 1.0F,
			
			1.0F, 0F, 0F, 1.0F, //R
			1.0F, 1.0F, 0F, 1.0F, //Y
			0F, 0F, 1.0F, 1.0F, //B
			1.0F, 0F, 0F, 1.0F, //R
			0F, 0F, 1.0F, 1.0F, //B
			0F, 1.0F, 0F, 1.0F, //G
	};

	private float[] mCubeTextureCoords = {
			1.0F, 1.0F, 1.0F, //front
			-1.0F, 1.0F, 1.0F,
			-1.0F, -1.0F, 1.0F,
			
			1.0F, 1.0F, 1.0F,
			-1.0F, -1.0F, 1.0F,
			1.0F, -1.0F, 1.0F,
			
			1.0F, 1.0F, -1.0F, //right
			1.0F, 1.0F, 1.0F,
			1.0F, -1.0F, 1.0F,
			
			1.0F, 1.0F, -1.0F,
			1.0F, -1.0F, 1.0F,
			1.0F, -1.0F, -1.0F,
			
			-1.0F, 1.0F, -1.0F, //back
			1.0F, 1.0F, -1.0F,
			1.0F, -1.0F, -1.0F,
			
			-1.0F, 1.0F, -1.0F,
			1.0F, -1.0F, -1.0F,
			-1.0F, -1.0F, -1.0F,
			
			-1.0F, 1.0F, 1.0F, //left
			-1.0F, 1.0F, -1.0F,
			-1.0F, -1.0F, -1.0F,
			
			-1.0F, 1.0F, 1.0F,
			-1.0F, -1.0F, -1.0F,
			-1.0F, -1.0F, 1.0F,
			
			1.0F, 1.0F, -1.0F, //top
			-1.0F, 1.0F, -1.0F,
			-1.0F, 1.0F, 1.0F,
			
			1.0F, 1.0F, -1.0F,
			-1.0F, 1.0F, 1.0F,
			1.0F, 1.0F, 1.0F,
			
			1.0F, -1.0F, 1.0F, //bottom
			-1.0F, -1.0F, 1.0F,
			-1.0F, -1.0F, -1.0F,
			
			1.0F, -1.0F, 1.0F,
			-1.0F, -1.0F, -1.0F,
			1.0F, -1.0F, -1.0F,
		
	};

	public CubeBuffers() {}

	public void init(Context context) {
		
		FloatBuffer bufferVertices;
		FloatBuffer bufferNormals;
		FloatBuffer bufferTextureCoords;
		FloatBuffer bufferColors;
		
		// initialize vertex byte buffer for shape coordinates
        bufferVertices = ByteBuffer.allocateDirect(mCubeVertices.length * 4)
        		.order(ByteOrder.nativeOrder())
        		.asFloatBuffer();
        bufferVertices.put(mCubeVertices);
        bufferVertices.position(0);
        
        // initialize vertex byte buffer for per vertex color
        bufferColors = ByteBuffer.allocateDirect(mCubeColors.length * 4)
        		.order(ByteOrder.nativeOrder())
        		.asFloatBuffer();
        bufferColors.put(mCubeColors);
        bufferColors.position(0);
        
        // initialize vertex byte buffer for per vertex normal
        bufferNormals = ByteBuffer.allocateDirect(mCubeNormals.length * 4)
        		.order(ByteOrder.nativeOrder())
        		.asFloatBuffer();
        bufferNormals.put(mCubeNormals);
        bufferNormals.position(0);
        
		// initialize vertex byte buffer for per vertex texture coords
		bufferTextureCoords = ByteBuffer.allocateDirect(mCubeTextureCoords.length * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		bufferTextureCoords.put(mCubeTextureCoords);
		bufferTextureCoords.position(0);
		
		// Generate the 4 vertex buffers
		GLES20.glGenBuffers(4, mBuffers);

		// load the positions
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers.get(0));
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,bufferVertices.capacity() * 4,
				bufferVertices, GLES20.GL_STATIC_DRAW);

		// load the colors
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers.get(1));
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferColors.capacity() * 4,
				bufferColors, GLES20.GL_STATIC_DRAW);
		
		// load the normals
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers.get(2));
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferNormals.capacity() * 4,
				bufferNormals, GLES20.GL_STATIC_DRAW);
		
		// load the texture coordinates
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBuffers.get(3));
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, bufferTextureCoords.capacity() * 4,
				bufferTextureCoords, GLES20.GL_STATIC_DRAW);
		
		// TEXTURES
		
		// Retrieve a Bitmap containing our texture
		Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.droid);
		
		// Generates one texture buffer and binds to it
		GLES20.glGenTextures(1, mTextures);
		// After binding all texture calls will effect the texture found at mTextures.get(0)
		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, mTextures.get(0));
		
		// Here GLUtils.texImage2D is used since the texture is contained in a Bitmap
		// If the texture was in a Buffer (i.e ByteBuffer) then GLES20.glTexImage2D could be used
		
		// Load the cube face - Positive X
		GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GLES20.GL_RGBA, bm,
				GLES20.GL_UNSIGNED_BYTE, 0);

		// Load the cube face - Negative X
		GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,  0, GLES20.GL_RGBA, bm,
				GLES20.GL_UNSIGNED_BYTE, 0);

		// Load the cube face - Positive Y
		GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GLES20.GL_RGBA, bm,
				GLES20.GL_UNSIGNED_BYTE, 0);

		// Load the cube face - Negative Y
		GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GLES20.GL_RGBA, bm,
				GLES20.GL_UNSIGNED_BYTE, 0);

		// Load the cube face - Positive Z
		GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GLES20.GL_RGBA, bm,
				GLES20.GL_UNSIGNED_BYTE, 0);

		// Load the cube face - Negative Z
		GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GLES20.GL_RGBA, bm,
				GLES20.GL_UNSIGNED_BYTE, 0);
		
		// Generate a mipmap for the 6 sides so 6 mipmaps
		GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);
		
		// Set the filtering mode
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER,
				// For the emulator use linear filtering since trilinear isn't supported (at least not on my machine)
				// on devices GLES20.GL_LINEAR_MIPMAP_LINEAR should be supported.
				// With really simple textures there isn't much difference anyway.
				GLES20.GL_LINEAR/*GLES20.GL_LINEAR_MIPMAP_LINEAR*/); 

		GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER,
				GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_CLAMP_TO_EDGE);
		
		// the pixel data is saved by GLUtils.texImage2D so this is safe
		// http://androidxref.com/source/xref/frameworks/base/core/jni/android/opengl/util.cpp#util_texImage2D for the curious
		bm.recycle();
		
		// Now everything needed is in video ram.
		// At this point all that is really needed are the buffers index stored in mBuffers and mTextures,
		// everything else can be freed to retrieve memory space.
	}
	
}
