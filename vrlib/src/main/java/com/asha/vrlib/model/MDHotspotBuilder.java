package com.asha.vrlib.model;

import android.util.SparseArray;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.asha.vrlib.texture.MD360Texture;

/**
 * Created by hzqiujiadi on 16/8/10.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDHotspotBuilder {

    public float width = 2;

    public float height = 2;

    public String title;

    public MDVRLibrary.ITouchPickListener clickListener;

    public MDPosition position;

    public SparseArray<MD360Texture> textures = new SparseArray<>(6);

    public int[] statusList;

    public int[] checkedStatusList;

    public static MDHotspotBuilder create(){
        return new MDHotspotBuilder();
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

    public MDHotspotBuilder title(String title){
        this.title = title;
        return this;
    }

    public MDHotspotBuilder size(float width, float height){
        this.width = width;
        this.height = height;
        return this;
    }

    public MDHotspotBuilder provider(MDVRLibrary.IBitmapProvider provider){
        provider(0,provider);
        return this;
    }

    public MDHotspotBuilder provider(int key, MDVRLibrary.IBitmapProvider provider){
        textures.append(key,new MD360BitmapTexture(provider));
        return this;
    }

    public MDHotspotBuilder position(MDPosition position) {
        this.position = position;
        return this;
    }

    public MDHotspotBuilder listenClick(MDVRLibrary.ITouchPickListener listener){
        this.clickListener = listener;
        return this;
    }
}
