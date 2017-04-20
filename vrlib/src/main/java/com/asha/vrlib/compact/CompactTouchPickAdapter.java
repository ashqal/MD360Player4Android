package com.asha.vrlib.compact;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.MDVRLibrary.ITouchPickListener2;
import com.asha.vrlib.model.MDHitEvent;

/**
 * Created by hzqiujiadi on 2017/4/20.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class CompactTouchPickAdapter implements ITouchPickListener2 {

    private final MDVRLibrary.ITouchPickListener listener;

    public CompactTouchPickAdapter(MDVRLibrary.ITouchPickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onHotspotHit(MDHitEvent hitEvent) {
        if (listener != null){
            listener.onHotspotHit(hitEvent.getHotspot(), hitEvent.getRay());
        }
    }
}
