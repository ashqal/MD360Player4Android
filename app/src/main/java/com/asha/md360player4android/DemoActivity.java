package com.asha.md360player4android;

import android.content.ContentResolver;
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

/**
 * Created by hzqiujiadi on 16/1/26.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class DemoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        final EditText et = (EditText) findViewById(R.id.edit_text_url);

        SparseArray<String> data = new SparseArray<>();
        data.put(data.size(), getDrawableUri(R.drawable.bitmap360).toString());
        data.put(data.size(), getDrawableUri(R.drawable.texture).toString());
        data.put(data.size(), getDrawableUri(R.drawable.dome_pic).toString());
        data.put(data.size(), getDrawableUri(R.drawable.stereo).toString());

        data.put(data.size(), "file:///mnt/sdcard/vr/28.mp4");
        data.put(data.size(), "file:///mnt/sdcard/vr/haha.mp4");
        data.put(data.size(), "file:///mnt/sdcard/vr/halfdome.mp4");
        data.put(data.size(), "file:///mnt/sdcard/vr/dome.mp4");
        data.put(data.size(), "file:///mnt/sdcard/vr/stereo.mp4");
        data.put(data.size(), "http://10.240.131.39/vr/570624aae1c52.mp4");
        data.put(data.size(), "http://192.168.5.106/vr/570624aae1c52.mp4");


        data.put(data.size(), "file:///mnt/sdcard/vr/AGSK6416.jpg");
        data.put(data.size(), "file:///mnt/sdcard/vr/IJUN2902.jpg");
        data.put(data.size(), "file:///mnt/sdcard/vr/SUYZ2954.jpg");
        data.put(data.size(), "file:///mnt/sdcard/vr/TEJD0097.jpg");
        data.put(data.size(), "file:///mnt/sdcard/vr/WSGV6301.jpg");

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
    }

    private Uri getDrawableUri(@DrawableRes int resId){
        Resources resources = getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId) + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId) );
    }
}
