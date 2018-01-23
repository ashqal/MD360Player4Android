package com.asha.md360player4android;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.asha.vrlib.MDDirectorCamUpdate;
import com.asha.vrlib.MDGLScreenWrapper;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDHitEvent;
import com.asha.vrlib.model.MDHotspotBuilder;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.model.MDRay;
import com.asha.vrlib.model.MDVideoHotspotBuilder;
import com.asha.vrlib.model.MDViewBuilder;
import com.asha.vrlib.model.position.MDMutablePosition;
import com.asha.vrlib.plugins.MDAbsPlugin;
import com.asha.vrlib.plugins.MDWidgetPlugin;
import com.asha.vrlib.plugins.hotspot.IMDHotspot;
import com.asha.vrlib.plugins.hotspot.MDAbsHotspot;
import com.asha.vrlib.plugins.hotspot.MDAbsView;
import com.asha.vrlib.plugins.hotspot.MDSimpleHotspot;
import com.asha.vrlib.plugins.hotspot.MDVideoHotspot;
import com.asha.vrlib.plugins.hotspot.MDView;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import tv.danmaku.ijk.media.player.IMediaPlayer;

import static android.animation.PropertyValuesHolder.ofFloat;
import static com.squareup.picasso.MemoryPolicy.NO_CACHE;
import static com.squareup.picasso.MemoryPolicy.NO_STORE;

/**
 * using MD360Renderer
 * <p>
 * Created by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class MD360PlayerActivity extends Activity {

    private static final String TAG = "MD360PlayerActivity";

    private static final SparseArray<String> sDisplayMode = new SparseArray<>();
    private static final SparseArray<String> sInteractiveMode = new SparseArray<>();
    private static final SparseArray<String> sProjectionMode = new SparseArray<>();
    private static final SparseArray<String> sAntiDistortion = new SparseArray<>();
    private static final SparseArray<String> sPitchFilter = new SparseArray<>();
    private static final SparseArray<String> sFlingEnabled = new SparseArray<>();

    static {
        sDisplayMode.put(MDVRLibrary.DISPLAY_MODE_NORMAL, "NORMAL");
        sDisplayMode.put(MDVRLibrary.DISPLAY_MODE_GLASS, "GLASS");

        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_MOTION, "MOTION");
        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_TOUCH, "TOUCH");
        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH, "M & T");
        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION, "CARDBOARD M");
        sInteractiveMode.put(MDVRLibrary.INTERACTIVE_MODE_CARDBORAD_MOTION_WITH_TOUCH, "CARDBOARD M&T");

        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_SPHERE, "SPHERE");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME180, "DOME 180");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME230, "DOME 230");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME180_UPPER, "DOME 180 UPPER");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_DOME230_UPPER, "DOME 230 UPPER");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_HORIZONTAL, "STEREO H SPHERE");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE_VERTICAL, "STEREO V SPHERE");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_FIT, "PLANE FIT");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_CROP, "PLANE CROP");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_PLANE_FULL, "PLANE FULL");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_MULTI_FISH_EYE_HORIZONTAL, "MULTI FISH EYE HORIZONTAL");
        sProjectionMode.put(MDVRLibrary.PROJECTION_MODE_MULTI_FISH_EYE_VERTICAL, "MULTI FISH EYE VERTICAL");
        sProjectionMode.put(CustomProjectionFactory.CUSTOM_PROJECTION_FISH_EYE_RADIUS_VERTICAL, "CUSTOM MULTI FISH EYE");

        sAntiDistortion.put(1, "ANTI-ENABLE");
        sAntiDistortion.put(0, "ANTI-DISABLE");

        sPitchFilter.put(1, "FILTER PITCH");
        sPitchFilter.put(0, "FILTER NOP");

        sFlingEnabled.put(1, "FLING ENABLED");
        sFlingEnabled.put(0, "FLING DISABLED");
    }

    private MediaPlayerWrapper mMediaPlayerWrapper = new MediaPlayerWrapper();

    public static void startVideo(Context context, Uri uri) {
        start(context, uri, VideoPlayerActivity.class);
    }

    public static void startBitmap(Context context, Uri uri) {
        start(context, uri, BitmapPlayerActivity.class);
    }

    public static void startCubemap(Context context, Uri uri) {
        start(context, uri, CubemapPlayerActivity.class);
    }

    private static void start(Context context, Uri uri, Class<? extends Activity> clz) {
        Intent i = new Intent(context, clz);
        i.setData(uri);
        context.startActivity(i);
    }

    private MDVRLibrary mVRLibrary;

    // load resource from android drawable and remote url.
    private MDVRLibrary.IImageLoadProvider mImageLoadProvider = new ImageLoadProvider();

    // load resource from android drawable only.
    private MDVRLibrary.IImageLoadProvider mAndroidProvider = new AndroidProvider(this);

    private List<MDAbsPlugin> plugins = new LinkedList<>();

    private MDPosition logoPosition = MDMutablePosition.newInstance().setY(-8.0f).setYaw(-90.0f);
    private MDPosition videoPosition = MDMutablePosition.newInstance().setZ(-8.0f).setYaw(0.0f).setRoll(-90f);

    private MDPosition[] positions = new MDPosition[]{
            MDPosition.newInstance().setZ(-8.0f).setYaw(-45.0f),
            MDPosition.newInstance().setZ(-18.0f).setYaw(15.0f).setAngleX(15),
            MDPosition.newInstance().setZ(-10.0f).setYaw(-10.0f).setAngleX(-15),
            MDPosition.newInstance().setZ(-10.0f).setYaw(30.0f).setAngleX(30),
            MDPosition.newInstance().setZ(-10.0f).setYaw(-30.0f).setAngleX(-30),
            MDPosition.newInstance().setZ(-5.0f).setYaw(30.0f).setAngleX(60),
            MDPosition.newInstance().setZ(-3.0f).setYaw(15.0f).setAngleX(-45),
            MDPosition.newInstance().setZ(-3.0f).setYaw(15.0f).setAngleX(-45).setAngleY(45),
            MDPosition.newInstance().setZ(-3.0f).setYaw(0.0f).setAngleX(90),
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // set content view
        setContentView(R.layout.activity_md_using_surface_view);

        // init VR Library
        mVRLibrary = createVRLibrary();

        final Activity activity = this;

        final List<View> hotspotPoints = new LinkedList<>();
        hotspotPoints.add(findViewById(R.id.hotspot_point1));
        hotspotPoints.add(findViewById(R.id.hotspot_point2));



        mMediaPlayerWrapper.init();
//        mMediaPlayerWrapper.setPreparedListener(new IMediaPlayer.OnPreparedListener() {
//            @Override
//            public void onPrepared(IMediaPlayer mp) {
//                cancelBusy();
//                if (getVRLibrary() != null){
//                    getVRLibrary().notifyPlayerChanged();
//                }
//
//                Log.e(TAG,"onPrepared: ");
//            }
//        });

        mMediaPlayerWrapper.getPlayer().setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                String error = String.format("Play Error what=%d extra=%d",what,extra);
                Toast.makeText(MD360PlayerActivity.this, error, Toast.LENGTH_SHORT).show();
                return true;
            }
        });


        mMediaPlayerWrapper.openRemoteFile("file:///mnt/sdcard/video.mp4");
        mMediaPlayerWrapper.prepare();


        SpinnerHelper.with(this)
                .setData(sDisplayMode)
                .setDefault(mVRLibrary.getDisplayMode())
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        mVRLibrary.switchDisplayMode(MD360PlayerActivity.this, key);
                        int i = 0;
                        int size = key == MDVRLibrary.DISPLAY_MODE_GLASS ? 2 : 1;
                        for (View point : hotspotPoints) {
                            point.setVisibility(i < size ? View.VISIBLE : View.GONE);
                            i++;
                        }
                    }
                })
                .init(R.id.spinner_display);

        SpinnerHelper.with(this)
                .setData(sInteractiveMode)
                .setDefault(mVRLibrary.getInteractiveMode())
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        mVRLibrary.switchInteractiveMode(MD360PlayerActivity.this, key);
                    }
                })
                .init(R.id.spinner_interactive);

        SpinnerHelper.with(this)
                .setData(sProjectionMode)
                .setDefault(mVRLibrary.getProjectionMode())
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        mVRLibrary.switchProjectionMode(MD360PlayerActivity.this, key);
                    }
                })
                .init(R.id.spinner_projection);

        SpinnerHelper.with(this)
                .setData(sAntiDistortion)
                .setDefault(mVRLibrary.isAntiDistortionEnabled() ? 1 : 0)
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        mVRLibrary.setAntiDistortionEnabled(key != 0);
                    }
                })
                .init(R.id.spinner_distortion);

        findViewById(R.id.button_add_video_hotspot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mMediaPlayerWrapper.getPlayer().setLooping(true);
                float ratio = mMediaPlayerWrapper.getPlayer().getVideoWidth()/(float)mMediaPlayerWrapper.getPlayer().getVideoHeight();

                MDVideoHotspotBuilder builder = MDVideoHotspotBuilder.create(new MDVRLibrary.IOnSurfaceReadyCallback() {
                    @Override
                    public void onSurfaceReady(Surface surface) {
                        mMediaPlayerWrapper.setSurface(surface);
                    }
                })
                .size(4f, 4f*ratio)
                .tag("video-hotspot")
                .position(videoPosition);

                MDVideoHotspot plugin = new MDVideoHotspot(builder);

                plugins.add(plugin);
                getVRLibrary().addPlugin(plugin);
                Toast.makeText(MD360PlayerActivity.this, "add video, position:" + videoPosition, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.button_add_plugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int index = (int) (Math.random() * 100) % positions.length;
                MDPosition position = positions[index];
                MDHotspotBuilder builder = MDHotspotBuilder.create(mImageLoadProvider)
                        .size(4f,4f)
                        .provider(0, activity, android.R.drawable.star_off)
                        .provider(1, activity, android.R.drawable.star_on)
                        .provider(10, activity, android.R.drawable.checkbox_off_background)
                        .provider(11, activity, android.R.drawable.checkbox_on_background)
                        .listenClick(new MDVRLibrary.ITouchPickListener() {
                            @Override
                            public void onHotspotHit(IMDHotspot hitHotspot, MDRay ray) {
                                if (hitHotspot instanceof MDWidgetPlugin){
                                    MDWidgetPlugin widgetPlugin = (MDWidgetPlugin) hitHotspot;
                                    widgetPlugin.setChecked(!widgetPlugin.getChecked());
                                }
                            }
                        })
                        .title("star" + index)
                        .position(position)
                        .status(0,1)
                        .checkedStatus(10,11);

                MDWidgetPlugin plugin = new MDWidgetPlugin(builder);

                plugins.add(plugin);
                getVRLibrary().addPlugin(plugin);
                Toast.makeText(MD360PlayerActivity.this, "add plugin position:" + position, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.button_add_plugin_logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDHotspotBuilder builder = MDHotspotBuilder.create(mImageLoadProvider)
                        .size(4f, 4f)
                        .provider(activity, R.drawable.moredoo_logo)
                        .title("logo")
                        .position(logoPosition)
                        .listenClick(new MDVRLibrary.ITouchPickListener() {
                            @Override
                            public void onHotspotHit(IMDHotspot hitHotspot, MDRay ray) {
                                Toast.makeText(MD360PlayerActivity.this, "click logo", Toast.LENGTH_SHORT).show();
                            }
                        });
                MDAbsHotspot hotspot = new MDSimpleHotspot(builder);
                plugins.add(hotspot);
                getVRLibrary().addPlugin(hotspot);
                Toast.makeText(MD360PlayerActivity.this, "add plugin logo", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.button_remove_plugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (plugins.size() > 0) {
                    MDAbsPlugin plugin = plugins.remove(plugins.size() - 1);
                    getVRLibrary().removePlugin(plugin);
                }
            }
        });

        findViewById(R.id.button_remove_plugins).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plugins.clear();
                getVRLibrary().removePlugins();
            }
        });

        findViewById(R.id.button_add_hotspot_front).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDHotspotBuilder builder = MDHotspotBuilder.create(mImageLoadProvider)
                        .size(4f, 4f)
                        .provider(activity, R.drawable.moredoo_logo)
                        .title("front logo")
                        .tag("tag-front")
                        .position(MDPosition.newInstance().setZ(-12.0f).setY(-1.0f));
                MDAbsHotspot hotspot = new MDSimpleHotspot(builder);
                hotspot.rotateToCamera();
                plugins.add(hotspot);
                getVRLibrary().addPlugin(hotspot);
            }
        });

        findViewById(R.id.button_rotate_to_camera_plugin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMDHotspot hotspot = getVRLibrary().findHotspotByTag("tag-front");
                if (hotspot != null) {
                    hotspot.rotateToCamera();
                }
            }
        });

        findViewById(R.id.button_rotate_camera_45_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVRLibrary().setView(getVRLibrary().getViewAth() + 45, 0);
            }
        });

        findViewById(R.id.button_rotate_camera_45_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVRLibrary().setView(getVRLibrary().getViewAth() - 45, 0);
            }
        });

        findViewById(R.id.button_add_md_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = new TextView(activity);
                textView.setBackgroundColor(0x55FFCC11);
                textView.setText("Hello world.");

                MDViewBuilder builder = MDViewBuilder.create()
                        .provider(textView, 400/*view width*/, 100/*view height*/)
                        .size(4, 1)
                        .position(MDPosition.newInstance().setZ(-12.0f))
                        .title("md view")
                        .tag("tag-md-text-view");

                MDAbsView mdView = new MDView(builder);
                plugins.add(mdView);
                getVRLibrary().addPlugin(mdView);
            }
        });

        findViewById(R.id.button_update_md_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDAbsView mdView = getVRLibrary().findViewByTag("tag-md-text-view");
                if (mdView != null) {
                    TextView textView = mdView.castAttachedView(TextView.class);
                    textView.setText("Cheer up!");
                    textView.setBackgroundColor(0x8800FF00);
                    mdView.invalidate();
                }
            }
        });

        findViewById(R.id.button_md_view_hover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = new HoverView(activity);
                view.setBackgroundColor(0x55FFCC11);

                MDViewBuilder builder = MDViewBuilder.create()
                        .provider(view, 300/*view width*/, 200/*view height*/)
                        .size(3, 2)
                        .position(MDPosition.newInstance().setZ(-8.0f))
                        .title("md view")
                        .tag("tag-md-text-view");

                MDAbsView mdView = new MDView(builder);
                mdView.rotateToCamera();
                plugins.add(mdView);
                getVRLibrary().addPlugin(mdView);
            }
        });

        final TextView hotspotText = (TextView) findViewById(R.id.hotspot_text);
        final TextView directorBriefText = (TextView) findViewById(R.id.director_brief_text);
        getVRLibrary().setEyePickChangedListener(new MDVRLibrary.IEyePickListener2() {
            @Override
            public void onHotspotHit(MDHitEvent hitEvent) {
                IMDHotspot hotspot = hitEvent.getHotspot();
                long hitTimestamp = hitEvent.getTimestamp();
                String text = hotspot == null ? "nop" : String.format(Locale.CHINESE, "%s  %fs", hotspot.getTitle(), (System.currentTimeMillis() - hitTimestamp) / 1000.0f);
                hotspotText.setText(text);

                String brief = getVRLibrary().getDirectorBrief().toString();
                directorBriefText.setText(brief);

                if (System.currentTimeMillis() - hitTimestamp > 5000) {
                    getVRLibrary().resetEyePick();
                }
            }
        });

        findViewById(R.id.button_camera_little_planet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDDirectorCamUpdate cameraUpdate = getVRLibrary().updateCamera();
                PropertyValuesHolder near = ofFloat("near", cameraUpdate.getNearScale(), -0.5f);
                PropertyValuesHolder eyeZ = PropertyValuesHolder.ofFloat("eyeZ", cameraUpdate.getEyeZ(), 18f);
                PropertyValuesHolder pitch = PropertyValuesHolder.ofFloat("pitch", cameraUpdate.getPitch(), 90f);
                PropertyValuesHolder yaw = PropertyValuesHolder.ofFloat("yaw", cameraUpdate.getYaw(), 90f);
                PropertyValuesHolder roll = PropertyValuesHolder.ofFloat("roll", cameraUpdate.getRoll(), 0f);
                startCameraAnimation(cameraUpdate, near, eyeZ, pitch, yaw, roll);
            }
        });

        findViewById(R.id.button_camera_to_normal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MDDirectorCamUpdate cameraUpdate = getVRLibrary().updateCamera();
                PropertyValuesHolder near = ofFloat("near", cameraUpdate.getNearScale(), 0f);
                PropertyValuesHolder eyeZ = PropertyValuesHolder.ofFloat("eyeZ", cameraUpdate.getEyeZ(), 0f);
                PropertyValuesHolder pitch = PropertyValuesHolder.ofFloat("pitch", cameraUpdate.getPitch(), 0f);
                PropertyValuesHolder yaw = PropertyValuesHolder.ofFloat("yaw", cameraUpdate.getYaw(), 0f);
                PropertyValuesHolder roll = PropertyValuesHolder.ofFloat("roll", cameraUpdate.getRoll(), 0f);
                startCameraAnimation(cameraUpdate, near, eyeZ, pitch, yaw, roll);
            }
        });

        SpinnerHelper.with(this)
                .setData(sPitchFilter)
                .setDefault(0)
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        MDVRLibrary.IDirectorFilter filter = key == 0 ? null : new MDVRLibrary.DirectorFilterAdatper() {
                            @Override
                            public float onFilterPitch(float input) {
                                if (input > 70) {
                                    return 70;
                                }

                                if (input < -70) {
                                    return -70;
                                }

                                return input;
                            }
                        };

                        getVRLibrary().setDirectorFilter(filter);
                    }
                })
                .init(R.id.spinner_pitch_filter);

        SpinnerHelper.with(this)
                .setData(sFlingEnabled)
                .setDefault(getVRLibrary().isFlingEnabled() ? 1 : 0)
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        getVRLibrary().setFlingEnabled(key == 1);
                    }
                })
                .init(R.id.spinner_fling_enable);
    }


    private ValueAnimator animator;

    private void startCameraAnimation(final MDDirectorCamUpdate cameraUpdate, PropertyValuesHolder... values) {
        if (animator != null) {
            animator.cancel();
        }

        animator = ValueAnimator.ofPropertyValuesHolder(values).setDuration(2000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float near = (float) animation.getAnimatedValue("near");
                float eyeZ = (float) animation.getAnimatedValue("eyeZ");
                float pitch = (float) animation.getAnimatedValue("pitch");
                float yaw = (float) animation.getAnimatedValue("yaw");
                float roll = (float) animation.getAnimatedValue("roll");
                cameraUpdate.setEyeZ(eyeZ).setNearScale(near).setPitch(pitch).setYaw(yaw).setRoll(roll);
            }
        });
        animator.start();
    }

    abstract protected MDVRLibrary createVRLibrary();

    public MDVRLibrary getVRLibrary() {
        return mVRLibrary;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaPlayerWrapper.resume();
        mVRLibrary.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMediaPlayerWrapper.pause();
        mVRLibrary.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayerWrapper.destroy();
        mVRLibrary.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVRLibrary.onOrientationChanged(this);
    }

    protected Uri getUri() {
        Intent i = getIntent();
        if (i == null || i.getData() == null) {
            return null;
        }
        return i.getData();
    }

    public void cancelBusy() {
        findViewById(R.id.progress).setVisibility(View.GONE);
    }

    public void busy() {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
    }

    // android impl
    private class AndroidProvider implements MDVRLibrary.IImageLoadProvider {

        Activity activity;

        public AndroidProvider(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onProvideBitmap(Uri uri, MD360BitmapTexture.Callback callback) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(uri));
                callback.texture(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    // picasso impl
    private class ImageLoadProvider implements MDVRLibrary.IImageLoadProvider {

        private SimpleArrayMap<Uri, Target> targetMap = new SimpleArrayMap<>();

        @Override
        public void onProvideBitmap(final Uri uri, final MD360BitmapTexture.Callback callback) {

            final Target target = new Target() {

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    // texture
                    callback.texture(bitmap);
                    targetMap.remove(uri);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    targetMap.remove(uri);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };
            targetMap.put(uri, target);
            Picasso.with(getApplicationContext()).load(uri).resize(callback.getMaxTextureSize(), callback.getMaxTextureSize()).onlyScaleDown().centerInside().memoryPolicy(NO_CACHE, NO_STORE).into(target);
        }
    }
}