package com.asha.md360player4android;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class BitmapPlayerActivity extends MD360PlayerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cancelBusy();

        View.OnClickListener surfaceClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(BitmapPlayerActivity.this, "clicked!", Toast.LENGTH_SHORT).show();
            }
        };

        findViewById(R.id.surface_view1).setOnClickListener(surfaceClickListener);
        findViewById(R.id.surface_view2).setOnClickListener(surfaceClickListener);
    }

    private Target mTarget;// keep the reference for picasso.

    private void loadImage(Uri uri, final MD360BitmapTexture.Callback callback){
        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                callback.texture(bitmap);
                cancelBusy();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        Picasso.with(getApplicationContext()).load(uri).into(mTarget);
    }

    @Override
    protected MDVRLibrary createVRLibrary() {
        return MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                .bitmap(new MDVRLibrary.IBitmapProvider() {
                    @Override
                    public void onProvideBitmap(final MD360BitmapTexture.Callback callback) {
                        loadImage(getUri(),callback);
                    }
                })
                .build(R.id.surface_view1,R.id.surface_view2);
    }
}
