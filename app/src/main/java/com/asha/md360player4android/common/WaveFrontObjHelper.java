package com.asha.md360player4android.common;

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
 * Created by nitro888 on 15. 4. 5..
 * Exports Option is -Z Foward, Y Up and UV and Normal
 * UV mirror - Y Axis
 */
public class WaveFrontObjHelper {
    private static final String TAG                     = WaveFrontObjHelper.class.getSimpleName();

    public static MeshBufferHelper loadObj(final Context context, final int resourceId) {
        ArrayList<String> vertexes  = new ArrayList<String>();
        ArrayList<String> textures  = new ArrayList<String>();
        ArrayList<String> normals   = new ArrayList<String>();
        ArrayList<String> faces     = new ArrayList<String>();

        InputStream iStream         = context.getResources().openRawResource(resourceId);
        InputStreamReader isr       = new InputStreamReader(iStream);
        BufferedReader bReader      = new BufferedReader(isr);
        String line;
        try {
            while ((line = bReader.readLine()) != null) {
                if (line.startsWith("v "))  vertexes.add(line.substring(2));
                if (line.startsWith("vt ")) textures.add(line.substring(3));
                if (line.startsWith("vn ")) normals.add(line.substring(3));
                if (line.startsWith("f "))  faces.add(line.substring(2));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        final float[] vertexBuffer  = new float[faces.size() * 3 * 3];
        final float[] normalBuffer  = new float[faces.size() * 3 * 3];
        final float[] textureBuffer = new float[faces.size() * 3 * 2];
        final short[] indexBuffer   = new short[faces.size() * 3];

        int vertexIndex = 0;
        int textureIndex= 0;
        int normalIndex = 0;
        int faceIndex   = 0;

        for (String i : faces) {
            for (String j : i.split(" ")) {
                indexBuffer[faceIndex] = (short)faceIndex++;
                String[] faceComponent = j.split("/");

                // only support f v/t/n mode
                String vertex   = vertexes.get(Integer.parseInt(faceComponent[0]) - 1);
                String texture  = textures.get(Integer.parseInt(faceComponent[1]) - 1);
                String normal   = normals.get(Integer.parseInt(faceComponent[2]) - 1);

                String vertexComp[]     = vertex.split(" ");
                String textureComp[]    = texture.split(" ");
                String normalComp[]     = normal.split(" ");

                for (String v : vertexComp)     vertexBuffer[vertexIndex++]= Float.parseFloat(v);
                for (String t : textureComp)    textureBuffer[textureIndex++]  = Float.parseFloat(t);
                for (String n : normalComp)     normalBuffer[normalIndex++]= Float.parseFloat(n);
            }
        }

        final FloatBuffer[] mesh = new FloatBuffer[3];  // vertex, texture, normal

        mesh[0] = ByteBuffer.allocateDirect(vertexBuffer.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mesh[0].put(vertexBuffer).position(0);
        mesh[1] = ByteBuffer.allocateDirect(textureBuffer.length* 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mesh[1].put(textureBuffer).position(0);
        mesh[2] = ByteBuffer.allocateDirect(normalBuffer.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mesh[2].put(normalBuffer).position(0);

        return new MeshBufferHelper(indexBuffer.length,mesh);
    }
}
