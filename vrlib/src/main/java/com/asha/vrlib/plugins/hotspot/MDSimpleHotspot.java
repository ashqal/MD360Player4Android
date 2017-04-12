package com.asha.vrlib.plugins.hotspot;

import android.content.Context;
import android.net.Uri;
import android.util.SparseArray;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDHotspotBuilder;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.asha.vrlib.texture.MD360Texture;

/**
 * Created by hzqiujiadi on 2017/4/12.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class MDSimpleHotspot extends MDAbsHotspot {

    private static final String TAG = "MDSimplePlugin";

    private SparseArray<Uri> uriList;

    private int mPendingTextureKey = 0;

    private int mCurrentTextureKey = 0;

    // plugin
    private MDVRLibrary.IImageLoadProvider provider;

    private MD360Texture texture;

    public MDSimpleHotspot(MDHotspotBuilder builder) {
        super(builder.builderDelegate);
        this.provider = builder.imageLoadProvider;
        this.uriList = builder.uriList;
    }

    @Override
    protected void initInGL(Context context) {
        super.initInGL(context);

        texture = new MD360BitmapTexture(new MDVRLibrary.IBitmapProvider() {
            @Override
            public void onProvideBitmap(MD360BitmapTexture.Callback callback) {
                Uri uri = uriList.get(mCurrentTextureKey);
                if (uri != null){
                    provider.onProvideBitmap(uri, callback);
                }
            }
        });
        texture.create();
    }

    @Override
    public void renderer(int index, int width, int height, MD360Director director) {
        if (texture == null){
            return;
        }

        if (mPendingTextureKey != mCurrentTextureKey){
            mCurrentTextureKey = mPendingTextureKey;
            texture.notifyChanged();
        }

        texture.texture(program);

        if (texture.isReady()){
            super.renderer(index, width, height, director);
        }
    }

    public void useTexture(int key) {
        mPendingTextureKey = key;
    }
}
