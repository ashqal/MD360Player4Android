package com.asha.md360player4android;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by hzqiujiadi on 16/1/26.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class DemoActivity extends AppCompatActivity {

    public static final String sPath = "file:///mnt/sdcard/vr/";

    //public static final String sPath = "file:////storage/sdcard1/vr/";

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

        findViewById(R.id.cubemap_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = et.getText().toString();

                MD360PlayerActivity.startCubemap(DemoActivity.this, null);
            }
        });

        findViewById(R.id.recycler_view_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerViewActivity.start(DemoActivity.this);
            }
        });
    }

    private Uri getDrawableUri(@DrawableRes int resId){
        Resources resources = getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId) + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId) );
    }
}
