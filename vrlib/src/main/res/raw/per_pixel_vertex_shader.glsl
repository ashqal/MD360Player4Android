uniform mat4 u_MVPMatrix;		// A constant representing the combined model/view/projection matrix.      		       
uniform mat4 u_MVMatrix;		// A constant representing the combined model/view matrix.
uniform mat4 u_STMatrix;
uniform bool u_UseSTM;
uniform bool u_IsSkybox;
		  			
attribute vec4 a_Position;		// Per-vertex position information we will pass in.   				
//attribute vec4 a_Color;			// Per-vertex color information we will pass in.
attribute vec3 a_Normal;		// Per-vertex normal information we will pass in.
attribute vec4 a_TexCoordinate; // Per-vertex texture coordinate information we will pass in.
		  
varying vec3 v_Position;		// This will be passed into the fragment shader.
//varying vec4 v_Color;			// This will be passed into the fragment shader.
//varying vec3 v_Normal;			// This will be passed into the fragment shader.
varying vec2 v_TexCoordinate;   // This will be passed into the fragment shader.    		
		  
// The entry point for our vertex shader.  
void main()                                                 	
{                                                         
    // Transform the vertex into eye space.
    if(!u_IsSkybox) {
        v_Position = vec3(u_MVMatrix * a_Position);
    } else {
        // When using cubemap, coordinates are the same as the position
        v_Position = vec3(a_Position);
    }
		
	// Pass through the color.
	//v_Color = a_Color;
	
	// Pass through the texture coordinate.
	if(u_UseSTM){
	    v_TexCoordinate = (u_STMatrix * a_TexCoordinate).xy;
	} else {
	    v_TexCoordinate = a_TexCoordinate.xy;
	}

	
	// Transform the normal's orientation into eye space.
    //v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));
          
	// gl_Position is a special variable used to store the final position.
	// Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
	gl_Position = u_MVPMatrix * a_Position;                       		  
}                                                          