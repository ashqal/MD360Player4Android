package com.asha.vrlib.common;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by hzqiujiadi on 16/9/8.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDGLHandler {
    Queue<Runnable> mQueue;

    public MDGLHandler() {
        mQueue = new LinkedBlockingQueue<>();
    }

    public void post(Runnable runnable){
        mQueue.remove(runnable);
        mQueue.add(runnable);
    }

    public void dealMessage(){
        mQueue.peek();
    }
}
