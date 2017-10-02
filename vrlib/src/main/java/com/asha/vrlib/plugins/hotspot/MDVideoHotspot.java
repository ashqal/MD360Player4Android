package com.asha.vrlib.plugins.hotspot;

import android.content.Context;
import android.net.Uri;
import android.util.SparseArray;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDHotspotBuilder;
import com.asha.vrlib.model.MDVideoHotspotBuilder;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.asha.vrlib.texture.MD360Texture;
import com.asha.vrlib.texture.MD360VideoTexture;

/**
 * Created by hzqiujiadi on 2017/4/12.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDVideoHotspot extends MDAbsHotspot {

    private static final String TAG = "MDVideoHotspot";
    private final MDVRLibrary.IOnSurfaceReadyCallback callback;


    // plugin

    private MD360VideoTexture texture;

    public MDVideoHotspot(MDVideoHotspotBuilder builder) {
        super(builder.builderDelegate);

        this.callback= builder.callback;
    }

    @Override
    protected void initInGL(Context context) {
        super.initInGL(context);

        texture = new MD360VideoTexture(callback);
        texture.create();
    }

    @Override
    public void renderer(int index, int width, int height, MD360Director director) {
        if (texture == null){
            return;
        }

            texture.notifyChanged();

        texture.texture(program);

        if (texture.isReady()){
            super.renderer(index, width, height, director);
        }
    }
}
