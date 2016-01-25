package com.asha.vrlib;

import android.content.Context;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDSphere3D;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

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
	private MD360Surface mSurface;

	// final
	private final Context mContext;
	private final MD360Director mDirector;
	private final MediaPlayer mPlayer;

	private MD360Renderer(Builder params){
		mContext = params.context;
		mDirector = params.director;
		mPlayer = params.player;
		mObject3D = new MDSphere3D();
		mProgram = new MD360Program();
		mSurface = new MD360Surface();
	}

	private void initProgram(){
		mProgram.build(mContext);
	}

	private void initTexture(){
		// Load the texture
		// mTextureDataHandle = TextureHelper.loadTexture(mContext, R.drawable.demo);
		mSurface.createSurface();
		mPlayer.setSurface(mSurface.getSurface());
	}

	private void initObject3D(){
		// load
		mObject3D.loadObj(mContext);

		// upload
		mObject3D.uploadDataToProgram(mProgram);
	}

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config){
		// Set the background clear color to black.
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		
		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		mDirector.prepare();
		initProgram();
		initTexture();
		initObject3D();
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height){
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

		// Update surface
		mSurface.resize(width,height);

		// Update Projection
		mDirector.updateProjection(width,height);

	}

	@Override
	public void onDrawFrame(GL10 glUnused){
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		// Set our per-vertex lighting program.
		mProgram.use();

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        
        // Bind the texture to this unit.
        // GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureDataHandle);
		mSurface.onDrawFrame();
        
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mProgram.getTextureUniformHandle(), 0);

		// Pass in the combined matrix.
		mDirector.shot(mProgram);

		// Draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mObject3D.getNumIndices());
	}

	public static Builder with(Context context) {
		Builder builder = new Builder();
		builder.context = context;
		return builder;
	}

	public static class Builder{
		private Context context;
		private MD360Director director;
		private MediaPlayer player;

		public Builder attachPlayer(MediaPlayer player){
			this.player = player;
			return this;
		}

		public Builder attachDirector(MD360Director director){
			this.director = director;
			return this;
		}

		public MD360Renderer build(){
			checkIfNull(player,TAG + " player can't be null!");
			checkIfNull(director,TAG + " director can't be null!");
			return new MD360Renderer(this);
		}

		private static void checkIfNull(Object obj,String msg){
			if ( obj == null )
				throw new IllegalArgumentException(msg);
		}
	}

}
