package com.asha.md360player4android;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.plugins.hotspot.IMDHotspot;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.asha.vrlib.texture.MD360CubemapTexture;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import androidx.annotation.DrawableRes;

import static com.squareup.picasso.MemoryPolicy.NO_CACHE;
import static com.squareup.picasso.MemoryPolicy.NO_STORE;

/**
 * Created by hzqiujiadi on 16/4/5.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class CubemapPlayerActivity extends MD360PlayerActivity {

    private static final String TAG = "BitmapPlayerActivity";

    private Uri nextUri = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.control_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                busy();
                //nextUri = getDrawableUri(R.drawable.texture);
                getVRLibrary().notifyPlayerChanged();
            }
        });
    }

    private Target mTarget;// keep the reference for picasso.

    private void loadImage(Uri uri, final MD360CubemapTexture.Callback callback) {
        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Log.d(TAG, "loaded image, size:" + bitmap.getWidth() + "," + bitmap.getHeight());

                // notify if size changed
                getVRLibrary().onTextureResize(bitmap.getWidth(), bitmap.getHeight());

                // texture
                callback.texture(bitmap);
                //cancelBusy();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        Log.d(TAG, "load image with max texture size:" + callback.getMaxTextureSize());
        Picasso.with(getApplicationContext())
                .load(uri)
                .resize(callback.getMaxTextureSize(), callback.getMaxTextureSize())
                .onlyScaleDown()
                .centerInside()
                .memoryPolicy(NO_CACHE, NO_STORE)
                .into(mTarget);
    }

    private Uri currentUri() {
        if (nextUri == null) {
            return getUri();
        } else {
            return nextUri;
        }
    }

    @Override
    protected MDVRLibrary createVRLibrary() {
        return MDVRLibrary.with(this)
                .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_TOUCH)
                .projectionMode(MDVRLibrary.PROJECTION_MODE_CUBE) // needed
                .asCubemap(new MDVRLibrary.ICubemapProvider() {
                    @Override
                    public void onProvideCubemap(MD360CubemapTexture.Callback callback, int cubeFace) {
                        Log.d(TAG, "Load face: " + cubeFace);

                        switch (cubeFace) {
                            case MD360CubemapTexture.CUBE_FRONT:
                                nextUri = getDrawableUri(R.drawable.cube_front);
                                break;
                            case MD360CubemapTexture.CUBE_BACK:
                                nextUri = getDrawableUri(R.drawable.cube_back);
                                break;
                            case MD360CubemapTexture.CUBE_TOP:
                                nextUri = getDrawableUri(R.drawable.cube_top);
                                break;
                            case MD360CubemapTexture.CUBE_BOTTOM:
                                nextUri = getDrawableUri(R.drawable.cube_bottom);
                                break;
                            case MD360CubemapTexture.CUBE_LEFT:
                                nextUri = getDrawableUri(R.drawable.cube_left);
                                break;
                            case MD360CubemapTexture.CUBE_RIGHT:
                                nextUri = getDrawableUri(R.drawable.cube_right);
                                break;
                            default:
                                return;
                        }

                        loadImage(currentUri(), callback);
                    }

                    @Override
                    public void onReady() {
                        // This can be used to hide a loading view and show the library view
                        Toast.makeText(CubemapPlayerActivity.this, "CubeMap Ready", Toast.LENGTH_SHORT).show();
                        cancelBusy();
                    }
                })
                .listenTouchPick(new MDVRLibrary.ITouchPickListener() {
                    @Override
                    public void onHotspotHit(IMDHotspot hitHotspot, MDRay ray) {
                        Log.d(TAG, "Ray:" + ray + ", hitHotspot:" + hitHotspot);
                    }
                })
                .pinchEnabled(true)
                .build(findViewById(R.id.gl_view));
    }

    private Uri getDrawableUri(@DrawableRes int resId) {
        Resources resources = getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId) + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId));
    }
}
