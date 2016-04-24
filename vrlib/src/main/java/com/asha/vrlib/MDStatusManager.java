package com.asha.vrlib;

import android.util.SparseBooleanArray;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
class MDStatusManager {
    public static final int STATUS_INIT  = 0;
    public static final int STATUS_READY = 1;
    private int mStatus = STATUS_INIT;
    private SparseBooleanArray mReadyList = new SparseBooleanArray();
    private int mVisibleSize;

    public void reset(int visibleSize){
        mVisibleSize = visibleSize;
        mStatus = STATUS_INIT;

        // clear ready list
        for (int i = 0; i < mReadyList.size(); i++){
            mReadyList.put(i,false);
        }
    }

    public boolean isReady(){
        if (mVisibleSize == 1) return true;
        else return (mStatus == STATUS_READY);
    }

    synchronized public void setChildReady(int index) {
        // already ready.
        if (mReadyList.get(index)) return;

        // value markChanged.
        mReadyList.put(index,true);
        boolean ready = true;
        for (int i = 0; i < mReadyList.size(); i++){
            ready &= mReadyList.valueAt(i);
        }
        mStatus = ready ? STATUS_READY : STATUS_INIT;
    }

    public Status newChild(){
        int index = mReadyList.size();
        mReadyList.put(index, false);
        return new StatusImpl(index, this);
    }

    private static class StatusImpl extends Status{

        private MDStatusManager manager;

        private StatusImpl(int id, MDStatusManager manager) {
            super(id);
            this.manager = manager;
        }

        @Override
        public boolean isAllReady() {
            return manager.isReady();
        }

        @Override
        public void ready() {
            manager.setChildReady(mId);
        }
    }

    static abstract class Status {
        protected int mId;
        abstract public boolean isAllReady();
        abstract public void ready();
        public Status(int id) {
            mId = id;
        }
    }
}
