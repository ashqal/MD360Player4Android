package com.asha.vrlib.common;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.asha.vrlib.objects.MDAbsObject3D;

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
 *
 * modify by hzqiujiadi on 16/1/22.
 * hzqiujiadi ashqalcn@gmail.com
 */
public class GLUtil {

    private static final String TAG = "GLUtil";

    public static final float[] sIdentityMatrix = new float[16];

    static {
        Matrix.setIdentityM(sIdentityMatrix,0);
    }


    /**
     * Check if the system supports OpenGL ES 2.0.
     *
     * @param context
     * @return true:supported
     */
    public static boolean supportsEs2(Context context){
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }

    public static void glCheck(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + android.opengl.GLUtils.getEGLErrorString(error));
        }
    }

    public static String readTextFileFromRaw(final Context context, final int resourceId) {
        final InputStream inputStream = context.getResources().openRawResource(
                resourceId);
        final InputStreamReader inputStreamReader = new InputStreamReader(
                inputStream);
        final BufferedReader bufferedReader = new BufferedReader(
                inputStreamReader);

        String nextLine;
        final StringBuilder body = new StringBuilder();

        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            return null;
        }

        return body.toString();
    }

    /**
     * Helper function to compile a shader.
     *
     * @param shaderType   The shader type.
     * @param shaderSource The shader source code.
     * @return An OpenGL handle to the shader.
     */
    public static int compileShader(final int shaderType, final String shaderSource) {
        int shaderHandle = GLES20.glCreateShader(shaderType);

        if (shaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(shaderHandle, shaderSource);

            // Compile the shader.
            GLES20.glCompileShader(shaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
                GLES20.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }

        if (shaderHandle == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shaderHandle;
    }

    /**
     * Helper function to compile and link a program.
     *
     * @param vertexShaderHandle   An OpenGL handle to an already-compiled vertex shader.
     * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
     * @param attributes           Attributes that need to be bound to the program.
     * @return An OpenGL handle to the program.
     */
    public static int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes) {
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0) {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            if (attributes != null) {
                final int size = attributes.length;
                for (int i = 0; i < size; i++) {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
                }
            }

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0) {
            throw new RuntimeException("Error creating program.");
        }

        return programHandle;
    }

    public static void loadObject3D(final Context context, final int resourceId, final MDAbsObject3D output) {
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

        output.setVerticesBuffer(0,vertex);
        output.setVerticesBuffer(1,vertex);
        output.setTexCoordinateBuffer(0,texture);
        output.setTexCoordinateBuffer(1,texture);
        output.setNumIndices(indexBuffer.length);

        //ByteBuffer.allocateDirect(normalBuffer.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(normalBuffer).position(0);
    }

}
