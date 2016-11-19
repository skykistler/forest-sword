package com.teamlucidic.hax.render;

import java.io.IOException;
import java.util.HashMap;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.world.World;

public class Shader {
	public static HashMap<Integer, Shader> shaderMap = new HashMap<Integer, Shader>();

	public String name;
	public int program;
	public int vert;
	public int frag;
	public String vertFile;
	public String fragFile;
	public boolean successful;
	public boolean enabled = true;

	public Shader(String n) {
		name = n;
		shaderMap.put(name.hashCode(), this);
	}

	public void load() {
		if (vert != 0 || frag != 0 || program != 0)
			delete();
		try {
			vertFile = Main.loadFile("shaders/" + name + ".vert");
			fragFile = Main.loadFile("shaders/" + name + ".frag");
		} catch (IOException e) {
			e.printStackTrace();
			Main.m.error("Can't load shader file!");
		}

		vert = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		GL20.glShaderSource(vert, vertFile);
		GL20.glCompileShader(vert);

		frag = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		GL20.glShaderSource(frag, fragFile);
		GL20.glCompileShader(frag);

		program = GL20.glCreateProgram();
		GL20.glAttachShader(program, vert);
		GL20.glAttachShader(program, frag);
		GL20.glLinkProgram(program);
		GL20.glValidateProgram(program);

		//		System.out.println("Link status: " + GL20.glGetProgram(program, GL20.GL_LINK_STATUS));
		//		System.out.println("Program id: " + program);
		if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_TRUE && GL20.glGetProgrami(program, GL20.GL_VALIDATE_STATUS) == GL11.GL_TRUE) {
			successful = true;
			System.out.println("Shader program " + name + " compiled successfully.");
		} else {
			successful = false;
			System.out.println("Vert log:");
			System.out.println(GL20.glGetShaderInfoLog(vert, 10000));
			System.out.println("Frag log:");
			System.out.println(GL20.glGetShaderInfoLog(frag, 10000));
			System.out.println("Program log:");
			System.out.println(GL20.glGetProgramInfoLog(program, 10000));
			System.out.println("Shader program " + name + " did not compile successfully!");
		}
	}

	public void bind() {
		if (!enabled)
			return;
		GL20.glUseProgram(program);
		GL20.glUniform2f(GL20.glGetUniformLocation(program, "displayDimensions"), Display.getWidth(), Display.getHeight());
		GL20.glUniform2f(GL20.glGetUniformLocation(program, "cameraNearAndFar"), Render.near, Render.far);
		GL20.glUniform3f(GL20.glGetUniformLocation(program, "skyLight"), Main.m.world.skyLight[0], Main.m.world.skyLight[1], Main.m.world.skyLight[2]);
		GL20.glUniform1i(GL20.glGetUniformLocation(program, "lantern"), Main.m.player.lightSurroundings ? 1 : 0);
		GL20.glUniform1f(GL20.glGetUniformLocation(program, "timeMod"), (float) Main.m.world.time);
		GL20.glUniform1f(GL20.glGetUniformLocation(program, "renderDistance"), World.renderDistance);
		GL20.glUniform1i(GL20.glGetUniformLocation(program, "fog"), Main.m.settings.fog ? 1 : 0);

		GL20.glUniform1f(GL20.glGetUniformLocation(program, "lightX"), (float) Main.m.world.lightX);
		GL20.glUniform1f(GL20.glGetUniformLocation(program, "lightY"), (float) Main.m.world.lightY);
		GL20.glUniform1f(GL20.glGetUniformLocation(program, "lightZ"), (float) Main.m.world.lightZ);

		if (Texture.currentTexture == null)
			GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL20.glUniform1i(GL20.glGetUniformLocation(program, "tex"), Texture.currentTexture == null ? 1 : 0);
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		Main.m.render.fb.bindDepth();
		GL20.glUniform1i(GL20.glGetUniformLocation(program, "depthTex"), 2);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
	}

	public void delete() {
		GL20.glDetachShader(program, vert);
		GL20.glDetachShader(program, frag);
		GL20.glDeleteShader(vert);
		GL20.glDeleteShader(frag);
		GL20.glDeleteProgram(program);
		vert = frag = program = 0;
	}

	public static void unbind() {
		GL20.glUseProgram(0);
	}

	public static void deleteShaders() {
		for (Shader s : shaderMap.values())
			s.delete();
	}
}
