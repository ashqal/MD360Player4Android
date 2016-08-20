package com.asha.vrlib.model;

import com.asha.vrlib.MD360Renderer;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.strategy.projection.ProjectionModeManager;
import com.asha.vrlib.texture.MD360Texture;

/**
 * Created by hzqiujiadi on 16/8/20.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDMainPluginBuilder {
    private MD360Texture texture;
    private int contentType = MDVRLibrary.ContentType.DEFAULT;
    private ProjectionModeManager projectionModeManager;

    public MDMainPluginBuilder() {
    }

    public MD360Texture getTexture() {
        return texture;
    }

    public int getContentType() {
        return contentType;
    }

    public ProjectionModeManager getProjectionModeManager() {
        return projectionModeManager;
    }


    public MDMainPluginBuilder setContentType(int contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * set surface{@link MD360Texture} to this render
     * @param texture {@link MD360Texture} surface may used by multiple render{@link MD360Renderer}
     * @return builder
     */
    public MDMainPluginBuilder setTexture(MD360Texture texture){
        this.texture = texture;
        return this;
    }

    public MDMainPluginBuilder setProjectionModeManager(ProjectionModeManager projectionModeManager) {
        this.projectionModeManager = projectionModeManager;
        return this;
    }
}
