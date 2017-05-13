precision mediump float;
uniform sampler2D u_Texture;
  
varying vec2 v_TexCoordinate;
vec2 m_TmpCoordinate;

void main()                    		
{
    m_TmpCoordinate[0] = v_TexCoordinate[0];
    m_TmpCoordinate[1] = 1. - v_TexCoordinate[1];
    gl_FragColor =  texture2D(u_Texture, m_TmpCoordinate);
}