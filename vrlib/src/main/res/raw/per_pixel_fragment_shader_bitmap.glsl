precision mediump float;       	// Set the default precision to medium. We don't need as high of a
								// precision in the fragment shader.
//uniform vec3 u_LightPos;       	// The position of the light in eye space.
uniform sampler2D u_Texture;
  
varying vec2 v_TexCoordinate;   // Interpolated texture coordinate per fragment.
vec2 m_TmpCoordinate;
  
// The entry point for our fragment shader.
void main()                    		
{
    m_TmpCoordinate[0] = v_TexCoordinate[0];
    m_TmpCoordinate[1] = 1. - v_TexCoordinate[1];
    gl_FragColor =  texture2D(u_Texture, m_TmpCoordinate);
}                                                                     	

