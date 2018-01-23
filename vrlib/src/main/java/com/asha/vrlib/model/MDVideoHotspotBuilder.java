package com.asha.vrlib.model;

import com.asha.vrlib.MDVRLibrary;

/**
 * Created by hzqiujiadi on 2017/4/12.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDVideoHotspotBuilder {

    public MDPluginBuilder builderDelegate;

    public MDVRLibrary.IOnSurfaceReadyCallback callback;

    public static MDVideoHotspotBuilder create(MDVRLibrary.IOnSurfaceReadyCallback callback){
        return new MDVideoHotspotBuilder(callback);
    }


    public MDVideoHotspotBuilder(MDVRLibrary.IOnSurfaceReadyCallback callback) {
        this.callback = callback;
        this.builderDelegate = new MDPluginBuilder();
    }

    // delegate builder

    public MDVideoHotspotBuilder size(float width, float height) {
        builderDelegate.size(width, height);
        return this;
    }

    public MDVideoHotspotBuilder position(MDPosition position) {
        builderDelegate.position(position);
        return this;
    }

    public MDVideoHotspotBuilder listenClick(MDVRLibrary.ITouchPickListener listener) {
        builderDelegate.listenClick(listener);
        return this;
    }

    public MDVideoHotspotBuilder tag(String tag) {
        builderDelegate.tag(tag);
        return this;
    }
}
