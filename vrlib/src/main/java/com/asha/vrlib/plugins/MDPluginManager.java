package com.asha.vrlib.plugins;

import java.util.Iterator;
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
        Iterator<MDAbsPlugin> iterator = mList.iterator();
        while (iterator.hasNext()){
            MDAbsPlugin plugin = iterator.next();
            if (plugin.removable()){
                mList.remove(plugin);
            }
        }
    }
}
