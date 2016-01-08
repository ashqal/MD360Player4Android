package com.asha.md360player4android;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzqiujiadi on 16/1/7.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDGLProgram {

    private static final String TAG = "MDGLProgram";
    private int mProgram;
    private int mVShader;
    private int mFShader;
    private List<String> mAttributes = new ArrayList<>();
    public void init(Context context){
        mProgram = GLES20.glCreateProgram();
        String vsh = readFileInRaw(context,R.raw.vshader);
        String fsh = readFileInRaw(context,R.raw.fshader);
        // 加载顶点着色器
        mVShader = compileShader(GLES20.GL_VERTEX_SHADER,vsh);
        // 加载片元着色器
        mFShader = compileShader(GLES20.GL_FRAGMENT_SHADER,fsh);

        GLES20.glAttachShader(mProgram,mVShader);
        GLES20.glAttachShader(mProgram,mFShader);
    }

    public void destroy(){
        mAttributes.clear();
        if(mVShader != 0) GLES20.glDeleteShader(mVShader);
        if(mFShader != 0) GLES20.glDeleteShader(mFShader);
        if(mProgram != 0) GLES20.glDeleteProgram(mProgram);
    }

    public void addAttribute(String attributeName){
        if ( !mAttributes.contains(attributeName) ){
            mAttributes.add(attributeName);
            GLES20.glBindAttribLocation(mProgram, indexOfAttribute(attributeName), attributeName);
        }
    }

    public boolean link(){
        int[] status = new int[1];
        GLES20.glLinkProgram(mProgram);
        GLES20.glGetProgramiv(mProgram,GLES20.GL_LINK_STATUS,status,0);
        Log.e(TAG,"link:" + status[0]);
        if ( status[0] == GLES20.GL_FALSE ) return false;

        if(mVShader != 0) GLES20.glDeleteShader(mVShader);
        if(mFShader != 0) GLES20.glDeleteShader(mFShader);
        mVShader = 0;
        mFShader = 0;

        return true;
    }

    public void use(){
        GLES20.glUseProgram(mProgram);
    }

    public int indexOfAttribute(String attribute){
        return mAttributes.indexOf(attribute);
    }

    public int indexOfUniform(String uniform){
        return GLES20.glGetUniformLocation(mProgram,uniform);
    }

    private static int compileShader(int type, String source){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader,source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS,compiled,0);
        if (compiled[0] != GLES20.GL_TRUE)
        {
            Log.e(TAG, "Could not compile shader " + type + ":");
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    private static String readFileInRaw(Context context, int raw){
        InputStream in = context.getResources().openRawResource(raw);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String tmp;
        try {
            for (tmp = bufferedReader.readLine(); tmp != null; tmp = bufferedReader.readLine() ){
                sb.append(tmp);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
