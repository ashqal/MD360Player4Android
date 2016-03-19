# MD360Player4Android
It is a lite library to render 360 degree panorama video for Android.

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
    compile 'com.github.ashqal:MD360Player4Android:0.4'
}
```

## USAGE
### Using with GLSurfaceView
**STEP1** Define `GLSurfaceView` in the layout xml.
```java
<android.opengl.GLSurfaceView
   android:id="@+id/surface_view"
   android:layout_width="match_parent"
   android:layout_height="match_parent" />
```

**STEP2** Init the `MDVRLibrary` in the Activity.
```java
public class MDVRLibraryDemoActivity extends MediaPlayerActivity {

    private MDVRLibrary mVRLibrary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_md_render);

        // init VR Library
        initVRLibrary();
    }

    private void initVRLibrary(){
        // new instance
        mVRLibrary = new MDVRLibrary(new MDVRLibrary.IOnSurfaceReadyCallback() {
            @Override
            public void onSurfaceReady(Surface surface) {
                getPlayer().setSurface(surface);
            }
        });

        // init with glSurfaceView id
        mVRLibrary.initWithGLSurfaceViewIds(this,R.id.surface_view);
    }
}
```

**STEP3** Addition call in `onTouchEvent` `onResume` `onPause`.
```java
public class MDVRLibraryDemoActivity extends MediaPlayerActivity {
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mVRLibrary.handleTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVRLibrary.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVRLibrary.onPause(this);
    }
}
```

## Reference
* [HTY360Player(360 VR Player for iOS)](https://github.com/hanton/HTY360Player)
* [NitroAction360(VR player for Android)](https://github.com/Nitro888/NitroAction360)
* [Learn-OpenGLES-Tutorials](https://github.com/learnopengles/Learn-OpenGLES-Tutorials)
* [ANDROID高性能图形处理之二.OPENGL ES](http://tangzm.com/blog/?p=20)