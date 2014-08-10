package r2ai.gles20.examples;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * @author r2ai
 *
 */
public class CubeMapActivity extends Activity {

	@SuppressWarnings("unused")
	private static final String TAG = "CubeMapActivity";
	private FrameLayout mLayout;
	private GLSurfaceView mSurfaceView;
	private SimpleRenderer mRenderer;
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
		mRenderer = new SimpleRenderer(this);

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
				Gravity.TOP | Gravity.END));

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

	public class MyGLSurfaceView extends GLSurfaceView {

		Paint paint;

		public MyGLSurfaceView(Context context) {
			super(context);
		    //super.setEGLConfigChooser(new MultisampleConfigChooser());
			setEGLContextClientVersion(2);
			setPreserveEGLContextOnPause(true);
		}

	}

	public class SimpleRenderer implements Renderer {
		
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
	
}
