package com.asha.vrlib.plugins;

import android.graphics.Rect;
import android.opengl.GLES20;

import java.nio.Buffer;

import static com.asha.vrlib.common.GLUtil.glCheck;

/**
 * Created by hzqiujiadi on 16/8/20.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDDrawingCache {
    private int mTextureIdOutput;

    private int mFrameBufferId;

    private int mRenderBufferId;

    private Rect mViewport = new Rect();

    private int[] originalFramebufferId = new int[1];


    private void setup(int totalWidth, int totalHeight) {
        if (mViewport.width() != totalWidth || mViewport.height() != totalHeight){
            createFrameBuffer(totalWidth, totalHeight);
            mViewport.set(0,0, totalWidth, totalHeight);
        }
    }

    private void createFrameBuffer(int width, int height){

        if (this.mTextureIdOutput != 0) {
            GLES20.glDeleteTextures(1, new int[] { this.mTextureIdOutput}, 0);
        }
        if (this.mRenderBufferId != 0) {
            GLES20.glDeleteRenderbuffers(1, new int[] { this.mRenderBufferId }, 0);
        }
        if (this.mFrameBufferId != 0) {
            GLES20.glDeleteFramebuffers(1, new int[] { this.mFrameBufferId }, 0);
        }

        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, originalFramebufferId, 0);

        // frame buffer
        int[] frameBuffer = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffer, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);
        mFrameBufferId = frameBuffer[0];
        glCheck("Multi Fish Eye frame buffer");

        // renderer buffer
        final int[] renderbufferIds = { 0 };
        GLES20.glGenRenderbuffers(1, renderbufferIds, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderbufferIds[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
        mRenderBufferId = renderbufferIds[0];
        glCheck("Multi Fish Eye renderer buffer");

        final int[] textureIds = { 0 };
        GLES20.glGenTextures(1, textureIds, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, (Buffer)null);
        mTextureIdOutput = textureIds[0];
        glCheck("Multi Fish Eye texture");

        // attach
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextureIdOutput, 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderbufferIds[0]);
        glCheck("Multi Fish Eye attach");

        // check
        final int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            final String s = "Framebuffer is not complete: ";
            final String value = String.valueOf(Integer.toHexString(status));
            throw new RuntimeException((value.length() != 0) ? s.concat(value) : s);
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, originalFramebufferId[0]);
        glCheck("Multi Fish Eye attach");
    }

    public void bind(int totalWidth, int totalHeight) {
        setup(totalWidth,totalHeight);
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, originalFramebufferId, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.mFrameBufferId);
    }

    public int getTextureOutput() {
        return mTextureIdOutput;
    }

    public void unbind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.originalFramebufferId[0]);
    }
}
