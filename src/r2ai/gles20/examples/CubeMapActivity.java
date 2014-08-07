package r2ai.gles20.examples;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * @author r2ai
 *
 */
public class CubeMapActivity extends Activity {

	private static final String TAG = "CubeMapActivity";
	private FrameLayout mLayout;
	private GLSurfaceView mSurfaceView;
	private TriangleRenderer mRenderer;
	private TextView mFpsView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLayout = new FrameLayout(this);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);

		mLayout.setLayoutParams(params);

		mSurfaceView = new MyGLSurfaceView(this);
		mRenderer = new TriangleRenderer(this);

		mSurfaceView.setRenderer(mRenderer);

		mSurfaceView.setLayoutParams(params);

		// mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		
		mLayout.addView(mSurfaceView);
		
		mFpsView = new TextView(this);
		mFpsView.setTextColor(Color.WHITE);
		mFpsView.setBackgroundColor(Color.DKGRAY);
		
		mLayout.addView(mFpsView, new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT, 
				Gravity.TOP | Gravity.RIGHT));

		setContentView(mLayout);
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mSurfaceView.onPause();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		mSurfaceView.onResume();
	}
	
	public static int loadShader(int shaderType, String source) {
	    int shader = GLES20.glCreateShader(shaderType);
	        if (shader != 0) {
	            GLES20.glShaderSource(shader, source);
	            GLES20.glCompileShader(shader);
	            int[] compiled = new int[1];
	            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
	            if (compiled[0] == 0) {
	                Log.e(TAG, "Could not compile shader " + shaderType + ":");
	                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
	                GLES20.glDeleteShader(shader);
	                shader = 0;
	            }
	        }
	        return shader;
	}
	
	public static int createProgram(int vertexShader, int pixelShader) {
	    int program = GLES20.glCreateProgram();
	    if (program != 0) {
	        GLES20.glAttachShader(program, vertexShader);
	        GLES20.glAttachShader(program, pixelShader);
	        GLES20.glLinkProgram(program);
	        int[] linkStatus = new int[1];
	        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
	        if (linkStatus[0] != GLES20.GL_TRUE) {
	            Log.e(TAG, "Could not link program: ");
	            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
	            GLES20.glDeleteProgram(program);
	            program = 0;
	        }
	    }
	    return program;
	}

	public class MyGLSurfaceView extends GLSurfaceView {

		Paint paint;

		public MyGLSurfaceView(Context context) {
			super(context);
		    //super.setEGLConfigChooser(new MultisampleConfigChooser());
			setEGLContextClientVersion(2);
			setPreserveEGLContextOnPause(true);
		}

	}

	public class TriangleRenderer implements Renderer {
		
		private Context mContext;
		
		private long mStartTimeNS;
		private long mFrameCount = 0;
		
		private CubeBuffers mCubeBuffers;
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
		
		private float[] mViewMatrix = new float[16];
		private float[] mProjectionMatrix = new float[16];

		public TriangleRenderer(Context context) {
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
	        // Set the background frame color
	        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
	        
	        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	        GLES20.glDepthFunc(GLES20.GL_LESS);
	        
	        // GLES20.glEnable(GLES20.GL_CULL_FACE);
	        
	        GLES20.glEnable(GLES20.GL_BLEND);
	        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
	        
	        mCubeBuffers = new CubeBuffers();
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
			// Matrix.frustumM(mProjectionMatrix,  0, -aspectRatio, aspectRatio, -1f, 1f, 1f, 100f);
			Matrix.perspectiveM(mProjectionMatrix, 0, 90.0f, aspectRatio, 0.1f, 1000f);
		}
		
		long runTime = 0;
		long start = 0;
		
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.microedition
		 * .khronos.opengles.GL10)
		 */
		@Override
		public void onDrawFrame(GL10 gl) {
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT 
					| GLES20.GL_DEPTH_BUFFER_BIT);

			long now = System.currentTimeMillis();
			
			if(start > 0)
				runTime = now - start;
			else 
				start = now;

			float[] vp = new float[16];
			Matrix.setLookAtM(mViewMatrix, 0, -1.0f, 1.75f, 1.5f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
			Matrix.multiplyMM(vp, 0, mProjectionMatrix, 0, mViewMatrix, 0);

			// complete rotation every 4 seconds
			final float angle = 0.09F * (float) (runTime % 4000L);
		    Matrix.setRotateM(mInnerCube.mModelMatrix, 0, angle, 1.0F, 1.0F, 1.0f);
			
		    mInnerCube.draw(vp);
		    

		    Matrix.setIdentityM(mOuterCube.mModelMatrix, 0);
		    Matrix.setRotateM(mOuterCube.mModelMatrix, 0, -angle, 0F, 1.0F, 0f);
		    Matrix.scaleM(mOuterCube.mModelMatrix, 0, 4f, 4f, 4f);
		    
		    
		    mOuterCube.draw(vp);

			now = System.nanoTime();
		    ++mFrameCount;
	        if (mFrameCount % 50 == 0) {
	            final double msPerFrame = (now - mStartTimeNS) / 1e6 / mFrameCount;
	            CubeMapActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						mFpsView.setText(
						"fps: " + String.format("%d",(int)(1000/msPerFrame)));
						
					}
				});
	            
	            mFrameCount = 0;
	            mStartTimeNS = now;
	        }
	        
		}

	}
	
	/*
	 * Set up all the per vertex data for a cube (vertices positions, normals, colors
	 * and texture coordinates for a cube map), plus load the cube map texture.
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
			
			mVertexShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderSrc);

			mFragmentShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderSrc);

			mProgramHandle = createProgram(mVertexShaderHandle, mFragmentShaderHandle);
			
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
	
}
