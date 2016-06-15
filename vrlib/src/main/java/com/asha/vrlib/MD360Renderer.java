package com.asha.vrlib;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.asha.vrlib.common.Fps;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.texture.MD360Texture;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.asha.vrlib.common.GLUtil.glCheck;

/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 *
 * @see Builder
 * @see #with(Context)
 */
public class MD360Renderer implements GLSurfaceView.Renderer {

	private static final String TAG = "MD360Renderer";

	private MDAbsObject3D mObject3D;
	private MD360Program mProgram;
	private MD360Texture mTexture;
	private Fps mFps = new Fps();
	private int mWidth;
	private int mHeight;

	// final
	private final Context mContext;
	private final MD360Director mDirector;

	private MD360Renderer(Builder params){
		mContext = params.context;
		mTexture = params.texture;
		mDirector = params.director;
		mObject3D = params.object3D;
		mProgram = new MD360Program(params.contentType);
	}

	public void updateObject3D(MDAbsObject3D object3D){
		this.mObject3D = object3D;
		mObject3D.markChanged();
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config){
		// set the background clear color to black.
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		// use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		
		// enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		// init
		initProgram();
		initTexture();
		initObject3D();
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height){

		this.mWidth = width;
		this.mHeight = height;

		// Update surface
		mTexture.resize(width,height);

	}

	@Override
	public void onDrawFrame(GL10 glUnused){
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		// mFps.step();

		// check obj3d
		if (mObject3D == null) return;

		boolean updated = mTexture.updateTexture();
		if(updated){
			int size = 2;
			int itemWidth = (int) (this.mWidth * 1.0f / size);
			for (int i = 0; i < size; i++){

				// Set the OpenGL viewport to the same size as the surface.
				GLES20.glViewport(itemWidth * i, 0, itemWidth, mHeight);

				// Update Projection
				mDirector.updateProjection(itemWidth, mHeight);

				// Set our per-vertex lighting program.
				mProgram.use();
				glCheck("mProgram use");

				mObject3D.uploadDataToProgramIfNeed(mProgram);

				// Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
				GLES20.glUniform1i(mProgram.getTextureUniformHandle(), 0);
				glCheck("glUniform1i");

				// Pass in the combined matrix.
				mDirector.shot(mProgram);

				mObject3D.draw();
			}

		}


	}

	private void initProgram(){
		mProgram.build(mContext);
	}

	private void initTexture(){
		mTexture.create();
	}

	private void initObject3D() {
		if (mObject3D != null)
			mObject3D.markChanged();
	}

	public static Builder with(Context context) {
		Builder builder = new Builder();
		builder.context = context;
		return builder;
	}

	public static class Builder{
		private Context context;
		private MD360Texture texture;
		private MD360Director director;
		private MDAbsObject3D object3D;
		private int contentType = MDVRLibrary.ContentType.DEFAULT;

		private Builder() {
		}

		public MD360Renderer build(){
			if (director == null) director = MD360Director.builder().build();
			return new MD360Renderer(this);
		}

		public Builder setContentType(int contentType) {
			this.contentType = contentType;
			return this;
		}

		/**
		 * set surface{@link MD360Texture} to this render
		 * @param texture {@link MD360Texture} surface may used by multiple render{@link MD360Renderer}
		 * @return builder
		 */
		public Builder setTexture(MD360Texture texture){
			this.texture = texture;
			return this;
		}

		public Builder setDirector(MD360Director director) {
			this.director = director;
			return this;
		}

		public Builder setObject3D(MDAbsObject3D object3D) {
			this.object3D = object3D;
			return this;
		}
	}
}
