//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.google.vrtoolkit.cardboard.sensors;

import android.hardware.SensorEventListener;

public interface SensorEventProvider {
    void start();

    void stop();

    void registerListener(SensorEventListener var1);

    void unregisterListener(SensorEventListener var1);
}
