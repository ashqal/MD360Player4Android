package com.asha.md360player4android;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.asha.md360player4android.common.TextureHelper;
import com.asha.md360player4android.objects.MDAbsObject3D;
import com.asha.md360player4android.objects.MDSphere3D;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MD360Renderer implements GLSurfaceView.Renderer {

	private static final String TAG = "LessonFourRenderer";

	private final Context mContext;

	private int mTextureDataHandle;

	private MDAbsObject3D mObject3D;
	private MD360Program mProgram;
	private final MD360Director mDirector;
	
	public MD360Renderer(final Context activityContext, MD360Director director){
		mContext = activityContext;
		mObject3D = new MDSphere3D();
		mProgram = new MD360Program();
		mDirector = director;
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


	private void initProgram(){
		mProgram.build(mContext);
	}

	private void initTexture(){
		// Load the texture
		mTextureDataHandle = TextureHelper.loadTexture(mContext, R.drawable.demo);
	}

	private void initObject3D(){
		// load
		mObject3D.loadObj(mContext);

		// upload
		mObject3D.uploadDataToProgram(mProgram);
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height){
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

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
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureDataHandle);
        
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mProgram.getTextureUniformHandle(), 0);

		// Pass in the combined matrix.
		mDirector.shot(mProgram);

		// Draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mObject3D.getNumIndices());
	}

}
