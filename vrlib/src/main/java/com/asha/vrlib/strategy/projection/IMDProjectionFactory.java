package com.asha.vrlib.strategy.projection;

/**
 * Created by hzqiujiadi on 16/8/20.
 * hzqiujiadi ashqalcn@gmail.com
 */
public interface IMDProjectionFactory {
    AbsProjectionStrategy createStrategy(int mode);
}
