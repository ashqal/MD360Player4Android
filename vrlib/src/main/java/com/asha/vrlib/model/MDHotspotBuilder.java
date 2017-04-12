package com.asha.vrlib.model;

import android.content.Context;
import android.net.Uri;
import android.util.SparseArray;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.common.MDUtil;

/**
 * Created by hzqiujiadi on 16/8/10.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDHotspotBuilder {

    public MDPluginBuilder builderDelegate;

    public SparseArray<Uri> uriList = new SparseArray<>(6);

    public int[] statusList;

    public int[] checkedStatusList;

    public MDVRLibrary.IImageLoadProvider imageLoadProvider;

    public static MDHotspotBuilder create(MDVRLibrary.IImageLoadProvider imageLoadProvider){
        return new MDHotspotBuilder(imageLoadProvider);
    }

    public MDHotspotBuilder(MDVRLibrary.IImageLoadProvider imageLoadProvider) {
        this.imageLoadProvider = imageLoadProvider;
        this.builderDelegate = new MDPluginBuilder();
    }

    private MDHotspotBuilder status(int normal, int focused, int pressed){
        statusList = new int[]{normal, focused, pressed};
        return this;
    }

    public MDHotspotBuilder status(int normal, int focused){
        return status(normal, focused, focused);
    }

    public MDHotspotBuilder status(int normal){
        return status(normal,normal);
    }

    private MDHotspotBuilder checkedStatus(int normal, int focused, int pressed){
        checkedStatusList = new int[]{normal, focused, pressed};
        return this;
    }

    public MDHotspotBuilder checkedStatus(int normal, int focused){
        return checkedStatus(normal, focused, focused);
    }

    public MDHotspotBuilder checkedStatus(int normal){
        return checkedStatus(normal, normal);
    }

    public MDHotspotBuilder provider(Uri uri){
        provider(0, uri);
        return this;
    }

    public MDHotspotBuilder provider(String url){
        provider(0, url);
        return this;
    }

    public MDHotspotBuilder provider(Context context, int drawableRes){
        provider(0, context, drawableRes);
        return this;
    }

    public MDHotspotBuilder provider(int key, String url){
        provider(key, Uri.parse(url));
        return this;
    }

    public MDHotspotBuilder provider(int key, Context context, int drawableRes){
        provider(key, MDUtil.getDrawableUri(context, drawableRes));
        return this;
    }

    public MDHotspotBuilder provider(int key, Uri uri){
        uriList.append(key, uri);
        return this;
    }

    // delegate

    public MDHotspotBuilder title(String title) {
        builderDelegate.title(title);
        return this;
    }

    public MDHotspotBuilder size(float width, float height) {
        builderDelegate.size(width, height);
        return this;
    }

    public MDHotspotBuilder position(MDPosition position) {
        builderDelegate.position(position);
        return this;
    }

    public MDHotspotBuilder listenClick(MDVRLibrary.ITouchPickListener listener) {
        builderDelegate.listenClick(listener);
        return this;
    }

    public MDHotspotBuilder tag(String tag) {
        builderDelegate.tag(tag);
        return this;
    }
}
