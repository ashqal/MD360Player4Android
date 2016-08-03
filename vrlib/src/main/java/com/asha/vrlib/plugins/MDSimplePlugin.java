package com.asha.vrlib.plugins;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.asha.vrlib.MD360Director;
import com.asha.vrlib.MD360Program;
import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDPosition;
import com.asha.vrlib.objects.MDAbsObject3D;
import com.asha.vrlib.objects.MDObject3DHelper;
import com.asha.vrlib.objects.MDPlane;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.asha.vrlib.texture.MD360Texture;

import static com.asha.vrlib.common.GLUtil.glCheck;

/**
 * Created by hzqiujiadi on 16/8/2.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDSimplePlugin extends MDAbsPlugin{

    MDAbsObject3D object3D;

    MD360Program program;

    MD360Texture texture;

    MDPosition position = MDPosition.newInstance().setZ(-9).setAngleX(45);

    public MDSimplePlugin(final Context context) {
        //
        texture = new MD360BitmapTexture(new MDVRLibrary.IBitmapProvider() {
            @Override
            public void onProvideBitmap(MD360BitmapTexture.Callback callback) {
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.star_on);
                callback.texture(bitmap);
            }
        });
    }

    @Override
    public void init(Context context) {

        program = new MD360Program(MDVRLibrary.ContentType.BITMAP);
        program.build(context);

        texture.create();

        object3D = new MDPlane();
        MDObject3DHelper.loadObj(context,object3D);

    }

    @Override
    public void renderer(int index, int width, int height, MD360Director director) {

        // Update Projection
        director.updateViewport(width, height);

        // Set our per-vertex lighting program.
        program.use();
        glCheck("MDSimplePlugin mProgram use");

        object3D.uploadVerticesBufferIfNeed(program, index);

        object3D.uploadTexCoordinateBufferIfNeed(program, index);

        // Pass in the combined matrix.
        director.shot(program, getModelPosition());

        texture.texture(program);

        object3D.draw();
    }

    @Override
    public void destroy() {

    }

    @Override
    protected MDPosition getModelPosition() {
        return position;
    }
}
