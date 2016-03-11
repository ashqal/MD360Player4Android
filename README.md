# MD360Player4Android
It is a lite library to render 360 degree panorama video for Android.
[![](https://jitpack.io/v/ashqal/MD360Player4Android.svg)](https://jitpack.io/#ashqal/MD360Player4Android)

## Preview
![ScreenShot](https://github.com/ashqal/MD360Player4Android/raw/master/app/demo/preview.jpg)
* [Gif Preview(5.2M)](https://github.com/ashqal/MD360Player4Android/raw/master/app/demo/DemoPreview.gif)
* [Demo Preview on YouTube](https://youtu.be/_m1f2I3i-pg)

## Demo APK
[MD360PlayerDemo.apk(21.7M, 360 demo video included)](https://github.com/ashqal/MD360Player4Android/raw/master/app/demo/MD360PlayerDemo.apk)

## NOTICE
* OpenGLES 2.0 required
* Android 4.1 (JellyBean API-16) required

## Gradle
```java
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```
```java
dependencies {
    compile 'com.github.ashqal:MD360Player4Android:0.2'
}
```

## USAGE
There are two way to use this library depend on your requirement,
### Using MDGLSurfaceView
**STEP1** define `com.asha.vrlib.MDGLSurfaceView` in the layout xml.
```java
...
   <com.asha.vrlib.MDGLSurfaceView
       android:id="@+id/md_surface_view"
       android:layout_width="match_parent"
       android:layout_height="match_parent" />
...
```

**STEP2** attach the `android.view.Surface` to your MediaPlayer or something else when the surface is ready.
```java
public class MDGLSurfaceViewDemoActivity extends MediaPlayerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_md_gl_surface);

        mGLSurfaceView = (MDGLSurfaceView) findViewById(R.id.md_surface_view);
        mGLSurfaceView.init(new MD360Surface.IOnSurfaceReadyListener() {
            @Override
            public void onSurfaceReady(Surface surface) {
                // MediaPlayer or something else.
                getPlayer().setSurface(surface);
            }
        });
    }
}
```

### More Flexible Way: MD360Renderer
**STEP1** build a `MD360Renderer` and attach the `android.view.Surface`.
```java
public class MD360RenderDemoActivity extends MediaPlayerActivity {

    private MD360Renderer mRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_md_render);

        mRenderer = MD360Renderer.with(this)
                    .defaultSurface(new MD360Surface.IOnSurfaceReadyListener() {
                        @Override
                        public void onSurfaceReady(Surface surface) {
                            // MediaPlayer or something else.
                            getPlayer().setSurface(surface);
                        }
                    })
                    .build();
                
        // init OpenGL
        initOpenGL(R.id.surface_view);
    }
}
```

**STEP2** init OpenGL by yourself, and set the `MD360Renderer` to your `android.opengl.GLSurfaceView`
```java
private void initOpenGL(int glSurfaceViewResId) {
    mGLSurfaceView = (GLSurfaceView) findViewById(glSurfaceViewResId);

    if (GLUtil.supportsEs2(this)) {
        // Request an OpenGL ES 2.0 compatible context.
        mGLSurfaceView.setEGLContextClientVersion(2);

        // Set the renderer to our demo renderer, defined below.
        mGLSurfaceView.setRenderer(mRenderer);
    } else {
        mGLSurfaceView.setVisibility(View.GONE);
        Toast.makeText(MD360RenderDemoActivity.this, "OpenGLES2 not supported.", Toast.LENGTH_SHORT).show();
    }
}
```

## Reference
* [HTY360Player(360 VR Player for iOS)](https://github.com/hanton/HTY360Player)
* [NitroAction360(VR player for Android)](https://github.com/Nitro888/NitroAction360)
* [Learn-OpenGLES-Tutorials](https://github.com/learnopengles/Learn-OpenGLES-Tutorials)
* [ANDROID高性能图形处理之二.OPENGL ES](http://tangzm.com/blog/?p=20)

## LICENSE
```
The MIT License (MIT)

Copyright (c) 2016 Moredoo.com
```
