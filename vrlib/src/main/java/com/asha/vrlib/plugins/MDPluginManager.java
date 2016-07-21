package com.asha.vrlib.plugins;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hzqiujiadi on 16/7/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDPluginManager {

    private List<MDAbsPlugin> mList;

    public MDPluginManager() {
        mList = new LinkedList<>();
    }

    public void add(MDAbsPlugin plugin){
        mList.add(plugin);
    }

    public List<MDAbsPlugin> getPlugins() {
        return mList;
    }
}
