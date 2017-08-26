precision mediump float;

varying vec3 v_Position; // Direction vector representing a 3D texture coordinate
uniform samplerCube u_Texture;  // Cubemap texture sampler

vec3 m_TmpCoordinate;

void main()
{
    // Mirror image to be shown correctly
    m_TmpCoordinate[0] = 1. - v_Position[0];
    m_TmpCoordinate[1] = v_Position[1];
    m_TmpCoordinate[2] = v_Position[2];

    gl_FragColor = textureCube(u_Texture, m_TmpCoordinate);
}