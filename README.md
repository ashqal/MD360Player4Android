# MD360Player4Android
It is a lite library to render 360 degree panorama video for Android.
[![](https://jitpack.io/v/ashqal/MD360Player4Android.svg)](https://jitpack.io/#ashqal/MD360Player4Android)

## Preview
![ScreenShot](https://github.com/ashqal/MD360Player4Android/raw/master/app/demo/preview.jpg)
* [Demo Preview on YouTube](https://youtu.be/_m1f2I3i-pg)

## NOTICE
* OpenGLES 2.0 required
* Android 4.1 (JellyBean API-16) required
* Compatible with all Players which have `setSurface` api.
* This library do nothing but render the image of video frame, so you may deal with the issues about `MediaPlayer` or `IjkMediaPlayer` (e.g. play local file, rtmp, hls) by yourself;
* 这个库只负责视频帧画面的渲染，所有的视频文件播放、控制的工作都交给了`MediaPlayer`或者`IjkMediaPlayer`，你可能需要自己处理使用Player过程中出现的问题(比如播放本地文件、rtmp、hls).

## Last Commit
**`-SNAPSHOT`**
* add some reset function, such as `MDVRLibrary#resetPinch`,`MDVRLibrary#resetTouch`


## Release Note

**1.2.0**
* pinch gesture supported
* changed the way to listen onClick event
* fix the image distortion on the polar of sphere

**1.1.0**
* Bitmap supported. For more info, See [BitmapPlayerActivity](https://github.com/ashqal/MD360Player4Android/tree/master/app/src/main/java/com/asha/md360player4android/BitmapPlayerActivity.java) in demo.
* Add callback if the TYPE_ROTATION_VECTOR is NOT supported.
* Use more divisions in sphere and load `.obj` file in working thread.
* Bug fix: Can not drag after setOnClickListener.
* Switch to [IjkMediaPlayer](https://github.com/Bilibili/ijkplayer) in demo.

**1.0.0**
* Motion Sensor
* Glass Mode(multi-screen)
* Fix a few bugs.
* More easier.
* Worked with MediaPlayer or ijkMediaPlayer.

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
    compile 'com.github.ashqal:MD360Player4Android:1.2.0'
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
        mVRLibrary = MDVRLibrary.with(this)
                    .displayMode(MDVRLibrary.DISPLAY_MODE_NORMAL)
                    .interactiveMode(MDVRLibrary.INTERACTIVE_MODE_MOTION)
                    .video(new MDVRLibrary.IOnSurfaceReadyCallback() {
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

**STEP3** Addition call in `onTouchEvent` `onResume` `onPause` `onDestroy`.
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
}
```

### Click Listener
`Builder#gesture`
```java
@Override
protected MDVRLibrary createVRLibrary() {
    return MDVRLibrary.with(this)
            .....
            .gesture(new MDVRLibrary.IGestureListener() {
                @Override
                public void onClick(MotionEvent e) {
                    Toast.makeText(VideoPlayerActivity.this, "onClick!", Toast.LENGTH_SHORT).show();
                }
            })
            .build(R.id.surface_view1,R.id.surface_view2);
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
            .build(R.id.surface_view1,R.id.surface_view2);
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
            .build(R.id.surface_view1,R.id.surface_view2);
}

```

## Reference
* [HTY360Player(360 VR Player for iOS)](https://github.com/hanton/HTY360Player)
* [NitroAction360(VR player for Android)](https://github.com/Nitro888/NitroAction360)
* [Learn-OpenGLES-Tutorials](https://github.com/learnopengles/Learn-OpenGLES-Tutorials)
* [ANDROID高性能图形处理之二.OPENGL ES](http://tangzm.com/blog/?p=20)
* [Moredoo.com](http://www.moredoo.com/)

## iOS Version
[MD360Player4iOS](https://github.com/ashqal/MD360Player4iOS)
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
