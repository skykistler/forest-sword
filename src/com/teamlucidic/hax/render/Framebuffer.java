package com.teamlucidic.hax.render;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glCheckFramebufferStatusEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glDeleteFramebuffersEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glFramebufferTexture2DEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glGenFramebuffersEXT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NONE;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import com.teamlucidic.hax.Main;

public class Framebuffer {
	public static ArrayList<Framebuffer> bufferList = new ArrayList<Framebuffer>();

	public boolean enabled = true;
	public int width;
	public int height;

	public int id;
	public int depthId;
	public int textureId;

	public Framebuffer(int width, int height, int bits, boolean drawBuffer) {
		int curid = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);

		id = glGenFramebuffersEXT();
		bind();

		depthId = glGenTextures();
		bindDepth();
		glTexParameteri(GL11.GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL11.GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		int depthComp = bits == 16 ? GL14.GL_DEPTH_COMPONENT16 : bits == 32 ? GL14.GL_DEPTH_COMPONENT32 : GL14.GL_DEPTH_COMPONENT24;
		glTexImage2D(GL11.GL_TEXTURE_2D, 0, depthComp, width, height, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, (ByteBuffer) null);
		Texture.unbind();
		glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_TEXTURE_2D, depthId, 0);

		textureId = glGenTextures();
		bindTexture();
		glTexParameteri(GL11.GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL11.GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
		Texture.unbind();
		glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_COLOR_ATTACHMENT0_EXT, GL_TEXTURE_2D, textureId, 0);

		if (glCheckFramebufferStatusEXT(GL_FRAMEBUFFER_EXT) != GL_FRAMEBUFFER_COMPLETE_EXT)
			System.out.println("Something went wrong when making the frame buffer");

		if (!drawBuffer)
			glDrawBuffer(GL_NONE);

		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, curid);
	}

	public Framebuffer(int width, int height) {
		this(width, height, 24, true);
	}

	public void bind() {
		if (!enabled)
			return;
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, id);
	}

	public void bindTexture() {
		if (!enabled)
			return;
		glBindTexture(GL_TEXTURE_2D, textureId);
	}

	public void bindDepth() {
		if (!enabled)
			return;
		glBindTexture(GL_TEXTURE_2D, depthId);
	}

	public void clear() {
		if (!enabled)
			return;
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}

	public void render() {
		if (!enabled)
			return;
		bindTexture();
		Main.m.modeler.setHexColor(0xFFFFFFFF);
		Main.m.modeler.start();
		Main.m.modeler.addVertexTex(0, 0, 0, 0, 1);
		Main.m.modeler.addVertexTex(0, Display.getHeight(), 0, 0, 0);
		Main.m.modeler.addVertexTex(Display.getWidth(), Display.getHeight(), 0, 1, 0);
		Main.m.modeler.addVertexTex(Display.getWidth(), 0, 0, 1, 1);
		Main.m.modeler.finish();

		Texture.unbind();
	}

	public void delete() {
		glDeleteTextures(textureId);
		glDeleteTextures(depthId);
		glDeleteFramebuffersEXT(id);
	}

	public static void unbind() {
		glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
	}

	public static void deleteFramebuffers() {
		for (Framebuffer fb : bufferList)
			fb.delete();
	}
}
