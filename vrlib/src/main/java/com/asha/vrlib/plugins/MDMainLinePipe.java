package com.asha.vrlib.plugins;

import android.content.Context;
import android.opengl.GLES20;

import com.asha.vrlib.MD360Director;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Created by hzqiujiadi on 16/8/7.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDMainLinePipe extends MDAbsLinePipe {

    private List<MDAbsLinePipe> plugins = new LinkedList<>();

    private Stack<MDAbsLinePipe> linePipes = new Stack<>();

    @Override
    protected void init(Context context) {
        for (MDAbsLinePipe linePipe : plugins){
            linePipe.setup(context);
        }
    }

    @Override
    public void renderer(int index, int width, int height, MD360Director director) {

    }

    @Override
    public void destroy() {

    }

    @Override
    protected boolean removable() {
        return false;
    }

    @Override
    public void takeOver(int width, int height, int size) {
        linePipes.clear();
        for (MDAbsLinePipe plugin : plugins){
            plugin.takeOver(width,height,size);
            linePipes.push(plugin);
        }
    }

    @Override
    public void commit(int mWidth, int mHeight, int size) {
        int width = mWidth / size;
        while (!linePipes.isEmpty()){
            MDAbsLinePipe plugin = linePipes.pop();
            for (int i = 0; i < size; i++){
                GLES20.glViewport(width * i, 0, width, mHeight);
                GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
                GLES20.glScissor(width * i, 0, width, mHeight);
                plugin.commit(mWidth, mHeight, i);
                GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
            }
        }
    }

    public void add(MDAbsLinePipe linePipe) {
        plugins.add(linePipe);
    }
}
