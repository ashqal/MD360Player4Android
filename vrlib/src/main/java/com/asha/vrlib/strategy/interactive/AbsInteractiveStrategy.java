package com.asha.vrlib.strategy.interactive;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.strategy.IModeStrategy;

import java.util.List;

/**
 * Created by hzqiujiadi on 16/3/19.
 * hzqiujiadi ashqalcn@gmail.com
 */
public abstract class AbsInteractiveStrategy implements IModeStrategy, IInteractiveMode {
    private List<MD360Director> mDirectorList;

    public AbsInteractiveStrategy(List<MD360Director> mDirectorList) {
        this.mDirectorList = mDirectorList;
    }

    protected List<MD360Director> getDirectorList() {
        return mDirectorList;
    }
}
