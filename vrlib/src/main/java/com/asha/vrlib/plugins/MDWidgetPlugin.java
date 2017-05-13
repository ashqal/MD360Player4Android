package com.asha.vrlib.plugins;

import com.asha.vrlib.model.MDHitEvent;
import com.asha.vrlib.model.MDHotspotBuilder;

/**
 * Created by hzqiujiadi on 16/8/10.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDWidgetPlugin extends MDHotspotPlugin {

    private static final int STATUS_NORMAL = 0;

    private static final int STATUS_FOCUSED = 1;

    // private static final int STATUS_PRESSED = 2;

    private boolean mChecked;

    private int[] mStatusList;

    private int[] mCheckedStatusList;

    private int mCurrentStatus = STATUS_NORMAL;

    public MDWidgetPlugin(MDHotspotBuilder builder) {
        super(builder);
        this.mStatusList = builder.statusList;
        this.mCheckedStatusList = builder.checkedStatusList;
        if (this.mStatusList == null){
            mStatusList = new int[]{0, 0, 0};
        }
    }

    public void setChecked(boolean checked){
        this.mChecked = checked;
        updateStatus();
    }

    public boolean getChecked(){
        return mChecked;
    }


    @Override
    public void onEyeHitIn(MDHitEvent hitEvent) {
        super.onEyeHitIn(hitEvent);
        mCurrentStatus = STATUS_FOCUSED;
        updateStatus();
    }

    @Override
    public void onEyeHitOut(long timestamp) {
        super.onEyeHitOut(timestamp);
        mCurrentStatus = STATUS_NORMAL;
        updateStatus();
    }

    private void updateStatus(){
        int[] statusList = mChecked ? mCheckedStatusList : mStatusList;
        if (statusList == null){
            statusList = mStatusList;
        }

        if (statusList != null && mCurrentStatus < statusList.length){
            useTexture(statusList[mCurrentStatus]);
        }

    }
}
