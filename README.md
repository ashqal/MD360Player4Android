# MD360Player4Android
It is a lite library to render 360 degree panorama video for Android.
[![](https://jitpack.io/v/ashqal/MD360Player4Android.svg)](https://jitpack.io/#ashqal/MD360Player4Android)

## Preview
![ScreenShot](https://github.com/ashqal/MD360Player4Android/raw/master/app/demo/preview.jpg)
![ScreenShot](https://github.com/ashqal/MD360Player4Android/raw/master/app/demo/preview1.jpg)
![ScreenShot](https://github.com/ashqal/MD360Player4Android/raw/master/app/demo/preview2.jpg)

## NOTICE
* OpenGLES 2.0 required
* Android 4.0.3 (Ice Cream Sandwich  API-15) required
* Compatible with all Players which have `setSurface` api.
* This library do nothing but render the image of video frame, so you may deal with the issues about `MediaPlayer` or `IjkMediaPlayer` (e.g. play local file, rtmp, hls) by yourself;
* 这个库只负责视频帧画面的渲染，所有的视频文件播放、控制的工作都交给了`MediaPlayer`或者`IjkMediaPlayer`，你可能需要自己处理使用Player过程中出现的问题(比如播放本地文件、rtmp、hls).

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
    compile 'com.github.ashqal:MD360Player4Android:2.0.5.beta'
}
```

## CHANGELOG
[Wiki-Changelog](https://github.com/ashqal/MD360Player4Android/wiki/Changelog)

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
        mVRLibrary = MDVRLibrary.with(this)
                    .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                    .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                    .asVideo(new MDVRLibrary.IOnSurfaceReadyCallback() {
                        @Override
                        public void onSurfaceReady(Surface surface) {
                            // IjkMediaPlayer or MediaPlayer
                            getPlayer().setSurface(surface);
                        }
                    })
                    .build(R.id.surface_view);
    }
}
```

**STEP3** Addition call in `onTouchEvent` `onResume` `onPause` `onDestroy` `onConfigurationChanged`.
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVRLibrary.onDestroy();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVRLibrary.onOrientationChanged(this);
    }
}
```

## FEATURE
### Enable Anti Distortion(since 2.0.0)
```java
// init configuation
protected MDVRLibrary createVRLibrary() {
    return MDVRLibrary.with(this)
            ...
            .barrelDistortionConfig(new BarrelDistortionConfig().setDefaultEnabled(true).setScale(0.95f))
            .build(R.id.gl_view);
}
```
```java
// setter
mVRLibrary.setAntiDistortionEnabled(true);
```

### Eye Picker(since 2.0.0)
```java
// setEyePickChangedListener dynamicly.
final TextView hotspotText = (TextView) findViewById(R.id.hotspot_text);
getVRLibrary().setEyePickChangedListener(new MDVRLibrary.IPickListener() {
    @Override
    public void onHotspotHit(IMDHotspot hotspot, long hitTimestamp) {
        String text = hotspot == null ? "nop" : String.format(Locale.CHINESE, "%s  %fs", hotspot.getTitle(), (System.currentTimeMillis() - hitTimestamp) / 1000.0f );
        hotspotText.setText(text);
    }
});
```
```java
// disable the eye picker
getVRLibrary().eyePickEanbled(false);
```

### Hotspot(since 2.0.0)
Hotspot

### Switch projection (since 1.5.0)
```java
// projection mode in MDVRLibrary.java
public static final int PROJECTION_MODE_SPHERE = 201;
public static final int PROJECTION_MODE_DOME180 = 202;
public static final int PROJECTION_MODE_DOME230 = 203;
public static final int PROJECTION_MODE_DOME180_UPPER = 204;
public static final int PROJECTION_MODE_DOME230_UPPER = 205;
public static final int PROJECTION_MODE_STEREO_SPHERE = 206;
public static final int PROJECTION_MODE_PLANE_FIT = 207;
public static final int PROJECTION_MODE_PLANE_CROP = 208;
public static final int PROJECTION_MODE_PLANE_FULL = 209;

// You should call MDVRLibrary#onTextureResize(float width, float height)
// If you are using DOME projection and PLANE projection.

@Override
protected MDVRLibrary createVRLibrary() {
    return MDVRLibrary.with(this)
    		...
            .projectionMode(MDVRLibrary.PROJECTION_MODE_STEREO_SPHERE)
            ...
            .build(R.id.surface_view);
}

// switch in runtime
// MDVRLibrary#switchProjectionMode(Activity activity, int mode)
```

### Motion configuration (since 1.3.0)
* support sensor delay configuration in motion mode.
```java
MDVRLibrary.with(this)
....
.motionDelay(SensorManager.SENSOR_DELAY_GAME)
...
.build(R.id.surface_view);
```
* support sensorCallback
```java
MDVRLibrary.with(this)
....
.sensorCallback(new SensorEventListener() {
    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
})
....
.build(R.id.surface_view);
```

### MD360Director Customize (since 1.2.0)
```java
@Override
protected MDVRLibrary createVRLibrary() {
    return MDVRLibrary.with(this)
            ...
            .directorFactory(new DirectorFactory()) //替换默认MD360DirectorFactory
            ...
            .build(R.id.surface_view);
}

private static class DirectorFactory extends MD360DirectorFactory{
    @Override
    public MD360Director createDirector(int index) {
        switch (index){
            // setAngle: angle to rotate in degrees
            case 1:   return MD360Director.builder().setAngle(20).setEyeX(-2.0f).setLookX(-2.0f).build();
            default:  return MD360Director.builder().setAngle(20).build();
        }
    }
}
```

### Click Listener
`Builder#gesture`
```java
@Override
protected MDVRLibrary createVRLibrary() {
    return MDVRLibrary.with(this)
            .....
            .listenGesture(new MDVRLibrary.IGestureListener() {
                @Override
                public void onClick(MotionEvent e) {
                    //....
                }
            })
            .build(R.id.surface_view);
}

```

### Enable the pinch
`Builder#pinchEnabled`
```java
@Override
protected MDVRLibrary createVRLibrary() {
    return MDVRLibrary.with(this)
            .....
            .pinchEnabled(true) //disable by default
            .build(R.id.surface_view);
}
```

### Feature not support callback
add `ifNotSupport` to builder, e.g. [VideoPlayerActivity#createVRLibrary](https://github.com/ashqal/MD360Player4Android/blob/master/app/src/main/java/com/asha/md360player4android/VideoPlayerActivity.java)

```java
@Override
protected MDVRLibrary createVRLibrary() {
    return MDVRLibrary.with(this)
            .....
            .ifNotSupport(new MDVRLibrary.INotSupportCallback() {
                @Override
                public void onNotSupport(int mode) {
                    String tip = mode == MDVRLibrary.INTERACTIVE_MODE_MOTION
                            ? "onNotSupport:MOTION" : "onNotSupport:" + String.valueOf(mode);
                    Toast.makeText(VideoPlayerActivity.this, tip, Toast.LENGTH_SHORT).show();
                }
            })
            .build(R.id.surface_view);
}

```

## Reference
* [HTY360Player(360 VR Player for iOS)](https://github.com/hanton/HTY360Player)
* [NitroAction360(VR player for Android)](https://github.com/Nitro888/NitroAction360)
* [Learn-OpenGLES-Tutorials](https://github.com/learnopengles/Learn-OpenGLES-Tutorials)
* [Moredoo.com](http://www.moredoo.com/)

## iOS Version
[MD360Player4iOS](https://github.com/ashqal/MD360Player4iOS)

## Feedback
* Open a new issue.
* or ashqalcn@gmail.com
* or QQ Group.<br/>
![QQ Group](https://cloud.githubusercontent.com/assets/5126517/16213934/e398b010-3785-11e6-8877-5d88d9dc3f33.jpeg)

##LICENSE
```
Copyright 2016 Asha

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
