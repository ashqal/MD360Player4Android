[![CI Status](https://api.travis-ci.org/kaltura/MD360Player4Android.svg?branch=develop)](https://travis-ci.com/github/kaltura/MD360Player4Android)
[![Download](https://img.shields.io/maven-central/v/com.kaltura.playkit/md360player?label=Download)](https://search.maven.org/artifact/com.kaltura.playkit/md360player)
[![License](https://img.shields.io/badge/license-AGPLv3-black.svg)](https://github.com/kaltura/playkit-android/blob/master/LICENSE)
![Android](https://img.shields.io/badge/platform-android-green.svg)

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
* [Working with vlc](https://github.com/ashqal/MD-vlc-sample)

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
    compile 'com.github.ashqal:MD360Player4Android:2.5.0'
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
public class MDVRLibraryDemoActivity extends Activity {

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

**STEP3** Addition call in `onResume` `onPause` `onDestroy` `onConfigurationChanged`.
```java
public class MDVRLibraryDemoActivity extends MediaPlayerActivity {

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

## CHANGELOG
https://github.com/ashqal/MD360Player4Android/wiki/Changelog

## Advanced Usage
https://github.com/ashqal/MD360Player4Android/wiki/Advanced-Usage

## Reference
* [HTY360Player(360 VR Player for iOS)](https://github.com/hanton/HTY360Player)
* [NitroAction360(VR player for Android)](https://github.com/Nitro888/NitroAction360)
* [Learn-OpenGLES-Tutorials](https://github.com/learnopengles/Learn-OpenGLES-Tutorials)
* [Moredoo.com](http://www.moredoo.com/)

## iOS Version
[MD360Player4iOS](https://github.com/ashqal/MD360Player4iOS)

## Q&A常见问题
[Q&A常见问题](https://github.com/ashqal/MD360Player4Android/wiki/Q&A)

## Feedback
* Open a new issue.
* or ashqalcn@gmail.com
* or QQ Group(抱歉，群已满).<br/>
![QQ Group](https://cloud.githubusercontent.com/assets/5126517/16213934/e398b010-3785-11e6-8877-5d88d9dc3f33.jpeg)

## LICENSE
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
