package com.asha.vrlib.plugins;

/**
 * Created by hzqiujiadi on 16/8/7.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class MDAbsLinePipe extends MDAbsPlugin {
    abstract public void takeOver(int width, int height, int size);
    abstract public void commit(int width, int height, int size);
}
