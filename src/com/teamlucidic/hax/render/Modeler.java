package com.teamlucidic.hax.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.Main;

public class Modeler {
	/* how large the buffer can get before drawing is forced */
	public static int bufferLimit = 0x100000;

	/* array holding vertex data */
	public int buffer[];
	/* used to put buffer[] into a byte buffer */
	public ByteBuffer bBuffer;
	/* used to put buffer[] into an int buffer */
	public IntBuffer iBuffer;
	/* used to put buffer[] into a float buffer */
	public FloatBuffer fBuffer;
	/* tracks amount of vertexes */
	public int vertexCount = 0;
	/* tracks position  */
	public int bufferCount = 0;
	/* if a color is set this is set to true */
	public boolean colored = false;
	/* if a texture position is set this is set to true */
	public boolean textured = false;
	/* if a normal is set this is set to true */
	public boolean normaled = false;
	/* set to true when startDrawing is called */
	public boolean started = false;
	/* current vertex color in ABGR hex */
	public int color = 0xFFFFFFFF;
	/* offset added to the color */
	public float[] colorPosOffset = new float[4];
	/* offset subtracted from the color */
	public float[] colorNegOffset = new float[4];
	/* currenct textures coords, these range 0 <= x <= 1 */
	public float textureU = 0.0F;
	public float textureV = 0.0F;
	/* current normal */
	public int normal = 0;
	/* equivalent of glTranslated() but a lot faster */
	public double offsetX = 0.0;
	public double offsetY = 0.0;
	public double offsetZ = 0.0;

	public Modeler() {
		buffer = new int[bufferLimit];
		bBuffer = makeByteBuffer(bufferLimit * 4);
		iBuffer = bBuffer.asIntBuffer();
		fBuffer = bBuffer.asFloatBuffer();
	}

	/* Resets the buffers and texture values after rendering, notice color is not reset */
	public void reset() {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		bBuffer.clear();
		vertexCount = 0;
		bufferCount = 0;
		textured = false;
		normaled = false;
		started = false;
	}

	/* Call this before you start adding vertexes */
	public void start() {
		if (started) {
			Main.m.error("Drawing already started!");
			return;
		}
		reset();
		started = true;
	}

	/* Call this after you add all your vertexes, mode is used like GL_QUADS, GL_LINES, all that fun stuff */
	public void finish(int mode) {
		if (!started) {
			Main.m.error("Modeler hasn't started drawing!");
			return;
		}
		if (vertexCount == 0) {
			started = false;
			return;
		}
		int stride = 28;

		iBuffer.clear();
		iBuffer.put(buffer, 0, bufferCount);
		bBuffer.position(0);
		bBuffer.limit(bufferCount * 4);

		if (textured) {
			fBuffer.position(3);
			GL11.glTexCoordPointer(2, stride, fBuffer);
			GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		}

		if (colored) {
			bBuffer.position(20);
			GL11.glColorPointer(4, true, stride, bBuffer);
			GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
		}

		if (normaled) {
			bBuffer.position(24);
			GL11.glNormalPointer(stride, bBuffer);
			GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		}

		fBuffer.position(0);
		GL11.glVertexPointer(3, stride, fBuffer);
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDrawArrays(mode, 0, vertexCount);
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

		if (textured)
			GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		if (colored)
			GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);

		if (normaled)
			GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);

		started = false;
		reset();
	}

	/* If you're lazy and don't want to put in GL11.GL_QUADS every time you type the method (you'll probably use quads the most) then just call this */
	public void finish() {
		finish(GL11.GL_QUADS);
	}

	public void setPosRGBAOffset(float[] offset) {
		for (int i = 0; i < 3; i++)
			colorPosOffset[i] = offset[i];
	}

	public void setNegRGBAOffset(float[] offset) {
		for (int i = 0; i < 3; i++)
			colorNegOffset[i] = offset[i];
	}

	public void clearRGBAOffset() {
		colorPosOffset[0] = 0;
		colorPosOffset[1] = 0;
		colorPosOffset[2] = 0;
		colorPosOffset[3] = 0;
		colorNegOffset[0] = 0;
		colorNegOffset[1] = 0;
		colorNegOffset[2] = 0;
		colorNegOffset[3] = 0;
	}

	public void setRGBAColor(float r, float g, float b, float a) {
		setHexColor(RGBAtoHex(r, g, b, a));
	}

	public void setRGBAColor(float[] rgba) {
		setRGBAColor(rgba[0], rgba[1], rgba[2], rgba[3]);
	}

	/* converts rgba values 0 < x < 1 to abgr hex */
	public int RGBAtoHex(float r, float g, float b, float a) {
		int col = 0;
		int ri = (int) (r * 255F);
		int gi = (int) (g * 255F);
		int bi = (int) (b * 255F);
		int ai = (int) (a * 255F);
		col = RGBAtoHex(ri, gi, bi, ai);
		return col;
	}

	/* converts rgba values 0 < x < 255 to abgr hex, could be optimized a little with bytes instead of ints but meh */
	public int RGBAtoHex(int r, int g, int b, int a) {
		int col = 0;
		r = r > 255 ? 255 : r;
		g = g > 255 ? 255 : g;
		b = b > 255 ? 255 : b;
		a = a > 255 ? 255 : a;
		r = r < 0 ? 0 : r;
		g = g < 0 ? 0 : g;
		b = b < 0 ? 0 : b;
		a = a < 0 ? 0 : a;
		col = a << 24 | b << 16 | g << 8 | r;
		return col;
	}

	/* set a hex color and add and subtract the color offsets */
	public void setHexColor(int col) {
		color = col;
		color += RGBAtoHex(colorPosOffset[0], colorPosOffset[1], colorPosOffset[2], colorPosOffset[3]);
		color -= RGBAtoHex(colorNegOffset[0], colorNegOffset[1], colorNegOffset[2], colorNegOffset[3]);
		colored = true;
	}

	/* set texture coords 0 < x < 1 */
	public void setTextureCoord(double u, double v) {
		textureU = (float) u;
		textureV = (float) v;
		textured = true;
	}

	public void setNormal(float x, float y, float z) {
		normaled = true;
		byte bX = (byte) (int) (x * 127F);
		byte bY = (byte) (int) (y * 127F);
		byte bZ = (byte) (int) (z * 127F);
		normal = bX & 0xff | (bY & 0xff) << 8 | (bZ & 0xff) << 16;
	}

	/* add a vertex pos and texture pos in one line of code */
	public void addVertexTex(double x, double y, double z, double u, double v) {
		setTextureCoord(u, v);
		addVertex(x, y, z);
	}

	/* add a vertex pos, a texture pos, and rgba color in one line of code */
	public void addVertexTexCol(double x, double y, double z, double u, double v, float[] rgba) {
		setRGBAColor(rgba);
		setTextureCoord(u, v);
		addVertex(x, y, z);
	}

	/* add a vertex pos, a texture pos, and hex color in one line of code */
	public void addVertexTexCol(double x, double y, double z, double u, double v, int col) {
		setHexColor(col);
		setTextureCoord(u, v);
		addVertex(x, y, z);
	}

	/* add all the vertex data into the buffer */
	public void addVertex(double x, double y, double z) {
		buffer[bufferCount] = Float.floatToRawIntBits((float) (x + offsetX));
		buffer[bufferCount + 1] = Float.floatToRawIntBits((float) (y + offsetY));
		buffer[bufferCount + 2] = Float.floatToRawIntBits((float) (z + offsetZ));

		if (textured) {
			buffer[bufferCount + 3] = Float.floatToRawIntBits(textureU);
			buffer[bufferCount + 4] = Float.floatToRawIntBits(textureV);
		}

		if (colored)
			buffer[bufferCount + 5] = color;

		if (normaled)
			buffer[bufferCount + 6] = normal;

		vertexCount++;
		bufferCount += 7;

		if (bufferCount >= bufferLimit) {
			finish();
			Main.m.error("Reached Modeler buffer limit");
			start();
		}
	}

	/* A much faster way to do glTranslated, obviously I don't have any rotation code in yet so if you need to rotate around a point you still need to use glTranslate */
	public void setOffset(double x, double y, double z) {
		offsetX = x;
		offsetY = y;
		offsetZ = z;
	}

	/* same thing except it add to the previous translation */
	public void translate(double x, double y, double z) {
		offsetX += x;
		offsetY += y;
		offsetZ += z;
	}

	/* Example of how to draw a 2d rectangle with texture coords */
	public void drawRect2D(int x, int y, int w, int h) {
		start();
		addVertexTex(x + 0, y + 0, 0, 0, 0);
		addVertexTex(x + 0, y + h, 0, 0, 1);
		addVertexTex(x + w, y + h, 0, 1, 1);
		addVertexTex(x + w, y + 0, 0, 1, 0);
		finish();
	}

	/* 3d retangle with tex coords */
	public void drawRect3D(double x, double y, double z, double w, double h) {
		start();
		addVertexTex(x + 0, y + 0, z, 0.0, 1.0);
		addVertexTex(x + w, y + 0, z, 1.0, 1.0);
		addVertexTex(x + w, y + h, z, 1.0, 0.0);
		addVertexTex(x + 0, y + h, z, 0.0, 0.0);
		finish();
	}

	public static ByteBuffer makeByteBuffer(int size) {
		return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
	}

	public static IntBuffer makeIntBuffer(int size) {
		return makeByteBuffer(size << 2).asIntBuffer();
	}

	public static FloatBuffer fBuffer(float[] fa) {
		return (FloatBuffer) BufferUtils.createFloatBuffer(fa.length).put(fa).flip();
	}
}
