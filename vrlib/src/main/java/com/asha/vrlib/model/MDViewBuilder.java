package com.asha.vrlib.model;

import android.view.View;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.plugins.hotspot.MDLayoutParams;

/**
 * Created by hzqiujiadi on 2017/4/12.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDViewBuilder {

    public MDPluginBuilder builderDelegate;

    public View attachedView;

    public MDLayoutParams layoutParams;

    public MDViewBuilder() {
        this.builderDelegate = new MDPluginBuilder();
    }

    public MDViewBuilder provider(View view, int widthInPx, int heightInPx){
        return provider(view, new MDLayoutParams(widthInPx, heightInPx));
    }

    public MDViewBuilder provider(View view, MDLayoutParams layoutParams){
        this.attachedView = view;
        this.layoutParams = layoutParams;
        return this;
    }

    // delegate builder
    public MDViewBuilder title(String title) {
        builderDelegate.title(title);
        return this;
    }

    public MDViewBuilder size(float width, float height) {
        builderDelegate.size(width, height);
        return this;
    }

    public MDViewBuilder position(MDPosition position) {
        builderDelegate.position(position);
        return this;
    }

    public MDViewBuilder listenClick(MDVRLibrary.ITouchPickListener listener) {
        builderDelegate.listenClick(listener);
        return this;
    }

    public MDViewBuilder tag(String tag) {
        builderDelegate.tag(tag);
        return this;
    }

    public static MDViewBuilder create() {
        return new MDViewBuilder();
    }
}
