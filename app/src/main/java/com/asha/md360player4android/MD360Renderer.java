package com.asha.md360player4android;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.asha.md360player4android.common.TextureHelper;
import com.asha.md360player4android.objects.MDAbsObject3D;
import com.asha.md360player4android.objects.MDSphere3D;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MD360Renderer implements GLSurfaceView.Renderer {

	private static final String TAG = "LessonFourRenderer";
	private static final int sPositionDataSize = 3;
	
	private final Context mContext;
	
	private float[] mModelMatrix = new float[16];
	private float[] mViewMatrix = new float[16];
	private float[] mProjectionMatrix = new float[16];

	private float[] mMVMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];

	private int mTextureDataHandle;

	private MDAbsObject3D mObject3D;
	private MD360Program mProgram;
	
	public MD360Renderer(final Context activityContext){
		mContext = activityContext;
		mObject3D = new MDSphere3D();
		mProgram = new MD360Program();
	}
	

	@Override
	public void onSurfaceCreated(GL10 glUnused, EGLConfig config){
		// Set the background clear color to black.
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		// Use culling to remove back faces.
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		
		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
		initShader();
		initTexture();
		initObject3D();
	}


	private void initShader(){
		mProgram.build(mContext);
	}

	private void initTexture(){
		// Load the texture
		mTextureDataHandle = TextureHelper.loadTexture(mContext, R.drawable.demo);
	}

	private void initObject3D(){

		// load
		mObject3D.loadObj(mContext);

		// set data to OpenGL
		FloatBuffer vertexBuffer = mObject3D.getVerticesBuffer();
		FloatBuffer textureBuffer = mObject3D.getTexCoordinateBuffer();
		vertexBuffer.position(0);
		textureBuffer.position(0);

		int positionHandle = mProgram.getPositionHandle();
		GLES20.glVertexAttribPointer(positionHandle, sPositionDataSize, GLES20.GL_FLOAT, false, 0, vertexBuffer);
		GLES20.glEnableVertexAttribArray(positionHandle);

		int textureCoordinateHandle = mProgram.getTextureCoordinateHandle();
		GLES20.glVertexAttribPointer(textureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
		GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
	}


	private void update1(int width, int height){

		// View Matrix
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 12.0f;
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = 0.0f;
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;
		Matrix.setIdentityM(mViewMatrix, 0);
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

		// Model Matrix
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.setRotateM(mModelMatrix,0,180,0,1,0);

		// Projection Matrix
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -0.5f;
		final float top = 0.5f;
		final float near = 1;
		final float far = 500;
		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);

		// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
		// (which currently contains model * view).
		Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);

		// This multiplies the model view matrix by the projection matrix, and stores the result in the MVP matrix
		// (which now contains model * view * projection).
		Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVMatrix, 0);
	}

	@Override
	public void onSurfaceChanged(GL10 glUnused, int width, int height) 
	{
		// Set the OpenGL viewport to the same size as the surface.
		GLES20.glViewport(0, 0, width, height);

		update1(width,height);

	}

	@Override
	public void onDrawFrame(GL10 glUnused) 
	{
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		// Set our per-vertex lighting program.
		mProgram.use();

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        
        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureDataHandle);
        
        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mProgram.getTextureUniformHandle(), 0);
        
		draw();
	}
	
	private void draw()
	{

		GLES20.glUniformMatrix4fv(mProgram.getMVMatrixHandle(), 1, false, mMVMatrix, 0);

        // Pass in the combined matrix.
        GLES20.glUniformMatrix4fv(mProgram.getMVPMatrixHandle(), 1, false, mMVPMatrix, 0);
        
        // Draw
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mObject3D.getNumIndices());
	}

}
