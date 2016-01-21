#extension GL_OES_EGL_image_external : require
uniform sampler2D u_Texture;

precision mediump float;

varying vec3 v_Position;
//varying vec3 v_Normal;
varying vec2 v_TexCoordinate;

void main()                    		
{
    gl_FragColor = texture2D(u_Texture, v_TexCoordinate);
}
