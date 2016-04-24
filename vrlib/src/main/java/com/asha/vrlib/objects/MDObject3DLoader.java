package com.asha.vrlib.objects;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by hzqiujiadi on 16/4/24.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class MDObject3DLoader {

    public interface LoadComplete{
        void onComplete(MDAbsObject3D object3D);
    }

    public static void loadObj(final Context context, final MDAbsObject3D object3D, final LoadComplete loadComplete){
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadObject3D(context, object3D.obtainObjResId(),object3D);
                if (loadComplete != null)
                    loadComplete.onComplete(object3D);
            }
        }).start();
    }

    private static void loadObject3D(final Context context, final int resourceId, final MDAbsObject3D output) {
        ArrayList<String> vertexes  = new ArrayList<String>();
        ArrayList<String> textures  = new ArrayList<String>();
        ArrayList<String> faces     = new ArrayList<String>();

        InputStream iStream         = context.getResources().openRawResource(resourceId);
        InputStreamReader isr       = new InputStreamReader(iStream);
        BufferedReader bReader      = new BufferedReader(isr);
        String line;
        try {
            while ((line = bReader.readLine()) != null) {
                if (line.startsWith("v "))  vertexes.add(line.substring(2));
                if (line.startsWith("vt ")) textures.add(line.substring(3));
                if (line.startsWith("f "))  faces.add(line.substring(2));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        final float[] vertexBuffer  = new float[faces.size() * 3 * 3];
        final float[] textureBuffer = new float[faces.size() * 3 * 2];
        final short[] indexBuffer   = new short[faces.size() * 3];

        int vertexIndex = 0;
        int textureIndex= 0;
        // int normalIndex = 0;
        int faceIndex   = 0;

        for (String i : faces) {
            for (String j : i.split(" ")) {
                indexBuffer[faceIndex] = (short)faceIndex++;
                String[] faceComponent = j.split("/");

                // only support f v/t/n mode
                String vertex   = vertexes.get(Integer.parseInt(faceComponent[0]) - 1);
                String texture  = textures.get(Integer.parseInt(faceComponent[1]) - 1);
                //String normal   = normals.get(Integer.parseInt(faceComponent[2]) - 1);

                String vertexComp[]     = vertex.split(" ");
                String textureComp[]    = texture.split(" ");
                //String normalComp[]     = normal.split(" ");

                for (String v : vertexComp)     vertexBuffer[vertexIndex++]= Float.parseFloat(v);
                for (String t : textureComp)    textureBuffer[textureIndex++]  = Float.parseFloat(t);
                //for (String n : normalComp)     normalBuffer[normalIndex++]= Float.parseFloat(n);
            }
        }

        // Vertex
        FloatBuffer vertex = ByteBuffer.allocateDirect(vertexBuffer.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexBuffer);
        vertex.position(0);

        // Texture Coordinate
        FloatBuffer texture = ByteBuffer.allocateDirect(textureBuffer.length * 4).
                order(ByteOrder.nativeOrder()).asFloatBuffer().put(textureBuffer);
        texture.position(0);

        output.setVerticesBuffer(vertex);
        output.setTexCoordinateBuffer(texture);
        output.setNumIndices(indexBuffer.length);

        //ByteBuffer.allocateDirect(normalBuffer.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(normalBuffer).position(0);
    }
}
