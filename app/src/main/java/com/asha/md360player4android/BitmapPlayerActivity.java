package com.asha.md360player4android;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.plugins.hotspot.IMDHotspot;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import static com.squareup.picasso.MemoryPolicy.NO_CACHE;
import static com.squareup.picasso.MemoryPolicy.NO_STORE;

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class BitmapPlayerActivity extends MD360PlayerActivity {

    private static final String TAG = "BitmapPlayerActivity";

    private Uri nextUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.control_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                busy();
                getVRLibrary().notifyPlayerChanged();
            }
        });
    }

    private Target mTarget;// keep the reference for picasso.

    private void loadImage(final MD360BitmapTexture.Callback callback){
        mTarget = new com.squareup.picasso.Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d(TAG, "loaded image, size:" + bitmap.getWidth() + "," + bitmap.getHeight());
                Log.d(TAG, "onBitmapLoaded: "+bitmap);

                // notify if size changed
                getVRLibrary().onTextureResize(bitmap.getWidth(), bitmap.getHeight());

                // texture
                callback.texture(bitmap);

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Log.d(TAG, "onBitmapFailed: "+errorDrawable);

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Log.d(TAG, "onPrepareLoad: "+placeHolderDrawable);

            }
        };
        Log.d(TAG, "load image with max texture size:" + callback.getMaxTextureSize());
        Picasso.with(getApplicationContext())
                .load("https://previews.123rf.com/images/svetlanasf/svetlanasf1803/svetlanasf180300010/97034756-panorama-with-the-iluminated-palace-of-fine-arts-during-the-blue-hour-at-sunset-in-san-francisco-cal.jpg")
                .resize(callback.getMaxTextureSize(),callback.getMaxTextureSize())
                .onlyScaleDown()
                .centerInside()
                //.memoryPolicy(NO_CACHE, NO_STORE)
                //.networkPolicy(NetworkPolicy.OFFLINE)
                .into(mTarget);
    }

    private Uri currentUri(){
        if (nextUri == null){
            return getUri();
        } else {
            return nextUri;
        }
    }

    @Override
    protected MDVRLibrary createVRLibrary() {
        return MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION_WITH_TOUCH)
                .asBitmap(new MDVRLibrary.IBitmapProvider() {
                    @Override
                    public void onProvideBitmap(MD360BitmapTexture.Callback callback) {
                        loadImage(callback);
                    }
                })
                .listenTouchPick(new MDVRLibrary.ITouchPickListener() {
                    @Override
                    public void onHotspotHit(IMDHotspot hitHotspot, MDRay ray) {
                        Log.d(TAG,"Ray:" + ray + ", hitHotspot:" + hitHotspot);
                    }
                })
                .pinchEnabled(true)
                .projectionFactory(new CustomProjectionFactory())
                .build(findViewById(R.id.gl_view));
    }


}
