package com.asha.md360player4android;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.leon.lfilepickerlibrary.LFilePicker;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by hzqiujiadi on 16/1/26.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class DemoActivity extends AppCompatActivity {

    public static final String sPath = "file:///mnt/sdcard/vr/";

    //public static final String sPath = "file:////storage/sdcard1/vr/";

    private static final int REQUEST_CODE_CHOOSE = 0;

    private EditText et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        et = (EditText) findViewById(R.id.edit_text_url);

        SparseArray<String> data = new SparseArray<>();

        data.put(data.size(), getDrawableUri(R.drawable.bitmap360).toString());
        data.put(data.size(), getDrawableUri(R.drawable.texture).toString());
        data.put(data.size(), getDrawableUri(R.drawable.dome_pic).toString());
        data.put(data.size(), getDrawableUri(R.drawable.stereo).toString());
        data.put(data.size(), getDrawableUri(R.drawable.multifisheye).toString());
        data.put(data.size(), getDrawableUri(R.drawable.multifisheye2).toString());
        data.put(data.size(), getDrawableUri(R.drawable.fish2sphere180sx2).toString());
        data.put(data.size(), getDrawableUri(R.drawable.fish2sphere180s).toString());

        data.put(data.size(), "rtsp://218.204.223.237:554/live/1/66251FC11353191F/e7ooqwcfbqjoo80j.sdp");
        data.put(data.size(), sPath + "ch0_160701145544.ts");
        data.put(data.size(), sPath + "videos_s_4.mp4");
        data.put(data.size(), sPath + "28.mp4");
        data.put(data.size(), sPath + "haha.mp4");
        data.put(data.size(), sPath + "halfdome.mp4");
        data.put(data.size(), sPath + "dome.mp4");
        data.put(data.size(), sPath + "stereo.mp4");
        data.put(data.size(), sPath + "look25fps3M.mp4");
        data.put(data.size(), "http://10.240.131.39/vr/570624aae1c52.mp4");
        data.put(data.size(), "http://192.168.5.106/vr/570624aae1c52.mp4");
        data.put(data.size(), sPath + "video_31b451b7ca49710719b19d22e19d9e60.mp4");

        data.put(data.size(), "http://cache.utovr.com/201508270528174780.m3u8");
        data.put(data.size(), sPath + "AGSK6416.jpg");
        data.put(data.size(), sPath + "IJUN2902.jpg");
        data.put(data.size(), sPath + "SUYZ2954.jpg");
        data.put(data.size(), sPath + "TEJD0097.jpg");
        data.put(data.size(), sPath + "WSGV6301.jpg");

        SpinnerHelper.with(this)
                .setData(data)
                .setClickHandler(new SpinnerHelper.ClickHandler() {
                    @Override
                    public void onSpinnerClicked(int index, int key, String value) {
                        et.setText(value);
                    }
                })
                .init(R.id.spinner_url);

        findViewById(R.id.video_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = et.getText().toString();
                if (!TextUtils.isEmpty(url)){
                    MD360PlayerActivity.startVideo(DemoActivity.this, Uri.parse(url));
                } else {
                    Toast.makeText(DemoActivity.this, "empty url!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.bitmap_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = et.getText().toString();
                if (!TextUtils.isEmpty(url)){
                    MD360PlayerActivity.startBitmap(DemoActivity.this, Uri.parse(url));
                } else {
                    Toast.makeText(DemoActivity.this, "empty url!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.ijk_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = et.getText().toString();
                if (!TextUtils.isEmpty(url)){
                    IjkPlayerDemoActivity.start(DemoActivity.this, Uri.parse(url));
                } else {
                    Toast.makeText(DemoActivity.this, "empty url!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.choose_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RxPermissions rxPermissions = new RxPermissions(DemoActivity.this);
                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(new Observer<Boolean>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Boolean aBoolean) {
                                if (aBoolean) {
                                    new LFilePicker()
                                            .withActivity(DemoActivity.this)
                                            .withRequestCode(REQUEST_CODE_CHOOSE)
                                            .start();
                                } else {
                                    Toast.makeText(DemoActivity.this, "Permission Denied!", Toast.LENGTH_LONG)
                                            .show();
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        });
    }

    private Uri getDrawableUri(@DrawableRes int resId){
        Resources resources = getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId) + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId) );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<String> list = data.getStringArrayListExtra("paths");
            et.setText(Uri.fromFile(new File(list.get(0))).toString());
        }
    }
}
