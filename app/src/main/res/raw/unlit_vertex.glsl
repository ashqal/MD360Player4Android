uniform mat4    u_MVPMatrix;
uniform mat4    u_MVMatrix;
uniform vec4    u_Offset;       // x=width, y=height, z=offsetW, w=offsetH

attribute vec4  a_Position;
attribute vec2  a_TexCoordinate;

varying vec3    v_Position;
varying vec2    v_TexCoordinate;
		  
// The entry point for our vertex shader.  
void main()                                                 	
{                                                         
	v_Position          = vec3(u_MVMatrix * a_Position);
	v_TexCoordinate.x   = u_Offset.z + a_TexCoordinate.x * u_Offset.x;
	v_TexCoordinate.y   = u_Offset.w + a_TexCoordinate.y * u_Offset.y;
    //v_Normal            = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
	gl_Position         = u_MVPMatrix * a_Position;
}                                                          