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

import java.lang.ref.WeakReference;

import r2ai.gles20.examples.SimpleRenderer.FpsListener;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * CubeMapActivty is the main activity. It's view is composed of a TextView
 * displaying the current fps and a GLSurfaceView containing an OpenGL renderer.
 * Both views are contained in a FrameLayout.
 */
public class CubeMapActivity extends Activity implements FpsListener {

	@SuppressWarnings("unused")
	private static final String TAG = "CubeMapActivity";
	private FrameLayout mLayout;
	private GLSurfaceView mSurfaceView;
	private SimpleRenderer mRenderer;
	private TextView mFpsView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// mLayout will contain the GLSurfaceView and the TextView
		// mFpsView
		mLayout = new FrameLayout(this);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);

		mLayout.setLayoutParams(params);

		mSurfaceView = new MyGLSurfaceView(this);
		mRenderer = new SimpleRenderer(this);

		mSurfaceView.setRenderer(mRenderer);

		mSurfaceView.setLayoutParams(params);

		mLayout.addView(mSurfaceView);

		mFpsView = new TextView(this);
		mFpsView.setTextColor(Color.WHITE);
		mFpsView.setBackgroundColor(Color.DKGRAY);
		mFpsView.setEms(3);

		// framelayout child views are drawn on top of each other, with the
		// most recently added at the top
		mLayout.addView(mFpsView, new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP
						| Gravity.END));

		setContentView(mLayout);
	}

	/*
	 * GLSurfaceView require to call its onPause/onResume method to manage its
	 * rendering thread and the OpenGL display according to Android doc.
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mSurfaceView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSurfaceView.onResume();
	}
	
	// Just a little static inner class to not allocate a runnable every time the
	// fps is updated.
	public static class MyRunnable implements Runnable {

		WeakReference<CubeMapActivity> activity;
		int fps;
		
		public MyRunnable(CubeMapActivity activity, int fps) {
			this.activity = new WeakReference<CubeMapActivity>(activity);
			this.fps = fps;
		}
		
		@Override
		public void run() {
			activity.get().mFpsView.setText(Integer.toString(fps));
		}
		
	}
	
	private final MyRunnable r = new MyRunnable(this,0);

	public void setFps(final int fps) {
		r.fps = fps;
		runOnUiThread(r);
	}

	public static class MyGLSurfaceView extends GLSurfaceView {

		public MyGLSurfaceView(Context context) {
			super(context);
			// RGBA_8888 color buffer, 16bits depth buffer
			// and 8bits stencil buffer
			super.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
			// request an OpenGL ES 2.0 context
			setEGLContextClientVersion(2);
		}

	}

}
