package com.asha.md360player4android;

import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by hzqiujiadi on 16/1/25.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class DemoActivity extends MD360DemoActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init OpenGL
        initOpenGL(R.id.surface_view);

        // init Seek Bar
        initSeekBar();
    }

    private void initSeekBar() {
        SeekBar eyeZSeekBar = (SeekBar) findViewById(R.id.seek_bar_eye_z);
        eyeZSeekBar.setProgress(valueToProgress(mDirector.getEyeZ(),2,50));

        SeekBar angleSeekBar = (SeekBar) findViewById(R.id.seek_bar_angle);
        angleSeekBar.setProgress(valueToProgress(mDirector.getAngle(),0,360));

        SeekBar nearSeekBar = (SeekBar) findViewById(R.id.seek_bar_near);
        nearSeekBar.setProgress(valueToProgress(mDirector.getNear(),1,12));

        eyeZSeekBar.setOnSeekBarChangeListener(new SeekBarOnChangeListenerAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                mDirector.updateCameraDistance(progressToValue(progress,2,50));
                updateText();
            }
        });

        angleSeekBar.setOnSeekBarChangeListener(new SeekBarOnChangeListenerAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                mDirector.updateModelRotate(progressToValue(progress,0,360));
                updateText();
            }
        });

        nearSeekBar.setOnSeekBarChangeListener(new SeekBarOnChangeListenerAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;
                mDirector.updateProjectionNear(progressToValue(progress,1,12));
                updateText();
            }
        });

        updateText();
    }

    private void updateText() {
        TextView tv = (TextView) findViewById(R.id.tv_seek_result);
        tv.setText(mDirector.toString());
    }

    private static float progressToValue(int progress, int min, int max){
        int range = max - min;
        float result = progress * 1.0f * range / 100.0f + min;
        return result;
    }

    private static int valueToProgress(float value, int min, int max){
        return (int) ((value - min) * 100 / (max - min));
    }

    public static abstract class SeekBarOnChangeListenerAdapter implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // nope
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // nope
        }
    }

}
