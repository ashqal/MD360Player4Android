package com.asha.vrlib.strategy.projection;

import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.objects.MDAbsObject3D;

/**
 * Created by hzqiujiadi on 16/6/25.
 * hzqiujiadi ashqalcn@gmail.com
 */
public interface IProjectionMode {
    MDAbsObject3D getObject3D();
    MDPosition getModelPosition();
}
