package com.asha.vrlib.common;

import android.os.Looper;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by hzqiujiadi on 16/9/8.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDGLHandler {

    private boolean died;

    private Queue<Runnable> mAddQueue = new LinkedBlockingQueue<>();

    private Queue<Runnable> mWorkQueue = new LinkedBlockingQueue<>();

    private final Object addLock = new Object();

    public MDGLHandler() {
    }

    // main thread
    public void post(Runnable runnable){
        // destroyed ?
        if (died){
            return;
        }

        // check the runnable is not null
        if (runnable == null){
            return;
        }

        if (Looper.getMainLooper() == Looper.myLooper()){
            synchronized (addLock){
                mAddQueue.remove(runnable);
                mAddQueue.offer(runnable);
            }
        } else {
            runnable.run();
        }

    }

    // gl thread
    public void dealMessage(){
        synchronized (addLock){
            mWorkQueue.addAll(mAddQueue);
            mAddQueue.clear();
        }

        while (mWorkQueue.size() > 0){
            Runnable runnable = mWorkQueue.poll();
            runnable.run();
        }
    }

    public void markAsDestroy() {
        died = true;
    }
}
