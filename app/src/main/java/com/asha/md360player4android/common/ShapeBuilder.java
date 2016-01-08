package com.asha.md360player4android.common;

public class ShapeBuilder 
{
	public static float[] generateCubeData(float[] point1,
			float[] point2,
			float[] point3,
			float[] point4,
			float[] point5,
			float[] point6,
			float[] point7,
			float[] point8,
			int elementsPerPoint)
	{
		// Given a cube with the points defined as follows:
		// front left top, front right top, front left bottom, front right bottom,
		// back left top, back right top, back left bottom, back right bottom,		
		// return an array of 6 sides, 2 triangles per side, 3 vertices per triangle, and 4 floats per vertex.
		final int FRONT = 0;
		final int RIGHT = 1;
		final int BACK = 2;
		final int LEFT = 3;
		final int TOP = 4;
		final int BOTTOM = 5;

		final int size = elementsPerPoint * 6 * 6;
		final float[] cubeData = new float[size];		

		for (int face = 0; face < 6; face ++)
		{
			// Relative to the side, p1 = top left, p2 = top right, p3 = bottom left, p4 = bottom right
			final float[] p1, p2, p3, p4;

			// Select the points for this face
			if (face == FRONT)
			{
				p1 = point1; p2 = point2; p3 = point3; p4 = point4; 
			}
			else if (face == RIGHT)
			{
				p1 = point2; p2 = point6; p3 = point4; p4 = point8;
			}
			else if (face == BACK)
			{
				p1 = point6; p2 = point5; p3 = point8; p4 = point7;
			}
			else if (face == LEFT)
			{
				p1 = point5; p2 = point1; p3 = point7; p4 = point3;
			}
			else if (face == TOP)
			{
				p1 = point5; p2 = point6; p3 = point1; p4 = point2;
			}
			else // if (side == BOTTOM)
			{
				p1 = point8; p2 = point7; p3 = point4; p4 = point3;								
			}
			
			// In OpenGL counter-clockwise winding is default. This means that when we look at a triangle, 
			// if the points are counter-clockwise we are looking at the "front". If not we are looking at
			// the back. OpenGL has an optimization where all back-facing triangles are culled, since they
			// usually represent the backside of an object and aren't visible anyways.

			// Build the triangles
			//  1---3,6
			//  | / |
			// 2,4--5
			int offset = face * elementsPerPoint * 6;

			for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = p1[i]; }
			for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = p3[i]; }
			for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = p2[i]; }
			for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = p3[i]; }
			for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = p4[i]; }
			for (int i = 0; i < elementsPerPoint; i++) { cubeData[offset++] = p2[i]; }
		}

		return cubeData;
	}
}
