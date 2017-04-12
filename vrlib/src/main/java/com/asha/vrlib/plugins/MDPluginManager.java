package com.asha.vrlib.plugins;

import android.text.TextUtils;

import com.asha.vrlib.plugins.hotspot.IMDHotspot;
import com.asha.vrlib.plugins.hotspot.MDAbsView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by hzqiujiadi on 16/7/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDPluginManager {

    private static final String TAG = "MDPluginManager";

    private List<MDAbsPlugin> mList;

    public MDPluginManager() {
        mList = new CopyOnWriteArrayList<>();
    }

    public void add(MDAbsPlugin plugin){
        mList.add(plugin);
    }

    public List<MDAbsPlugin> getPlugins() {
        return mList;
    }

    public void remove(MDAbsPlugin plugin) {
        if (plugin != null){
            mList.remove(plugin);
        }
    }

    public void removeAll() {
        for (MDAbsPlugin plugin : mList) {
            if (plugin.removable()) {
                mList.remove(plugin);
            }
        }
    }

    public IMDHotspot findHotspotByTag(String tag) {
        for (MDAbsPlugin plugin : mList) {
            if (plugin.removable() && plugin instanceof IMDHotspot) {
                IMDHotspot hotspot = (IMDHotspot) plugin;
                if (TextUtils.equals(tag, hotspot.getTag())){
                    return hotspot;
                }
            }
        }
        return null;
    }

    public MDAbsView findViewByTag(String tag) {
        for (MDAbsPlugin plugin : mList) {
            if (plugin.removable() && plugin instanceof MDAbsView) {
                MDAbsView mdView = (MDAbsView) plugin;
                if (TextUtils.equals(tag, mdView.getTag())){
                    return mdView;
                }
            }
        }
        return null;
    }
}
