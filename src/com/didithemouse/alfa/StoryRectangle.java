package com.didithemouse.alfa;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES10;
import android.opengl.GLUtils;

public class StoryRectangle {
	private int image;
	private FloatBuffer vertexBuffer;	// buffer holding the vertices
	private float vertices[];
	private FloatBuffer textureBuffer;	// buffer holding the texture coordinates
	private float texture[] = {
			// Mapping coordinates for the vertices
			0.0f, 1.0f,		// top left		(V2)
			0.0f, 0.0f,		// bottom left	(V1)
			1.0f, 1.0f,		// top right	(V4)
			1.0f, 0.0f		// bottom right	(V3)
	};

	public StoryRectangle(int resource, float escala, float w_offset, float h_offset) {
		
		image = resource;
		float vertices_escalados[] = {
				(-4.0f*escala)+w_offset, (-1.0f*escala)+h_offset,  0.0f,		// V1 - bottom left
				(-4.0f*escala)+w_offset,  (1.0f*escala)+h_offset,  0.0f,		// V2 - top left
				 (4.0f*escala)+w_offset, (-1.0f*escala)+h_offset,  0.0f,		// V3 - bottom right
				 (4.0f*escala)+w_offset,  (1.0f*escala)+h_offset,  0.0f		// V4 - top right
		};
		vertices = vertices_escalados;
		
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuffer.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuffer.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);
	}
	
	/** The texture pointer */
	private int[] textures = new int[1];

	public void loadGLTexture(GL10 gl, Context context) {
		// loading texture
		// loading texture
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inScaled = false;
		opts.inMutable = true;
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),image,opts);

		// generate one texture pointer
		gl.glGenTextures(1, textures, 0);
		// ...and bind it to our array
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		// create nearest filtered texture
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

		// Use Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

		// Clean up
		bitmap.recycle();
	}
	public void draw(GL10 gl) {
		// bind the previously generated texture
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);

		// Point to our buffers
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		// Set the face rotation
		gl.glFrontFace(GL10.GL_CW);

		// Point to our vertex buffer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

		// Draw the vertices as triangle strip
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
	}
	
	public void clear()
	{
		GLES10.glDeleteTextures(1, textures, 0);
	}
}
