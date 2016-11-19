package com.teamlucidic.hax.render;

import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.gui.Gui;

public class Render {
	public static final float pi = 3.1415926535897932384626433832795F;
	public static final float radianRatio = pi / 180f;
	public static final double epsilon = 0.000001;
	public static float near = .01F, far = 1000F;
	public static Random rand = new Random();

	public float baseFOV = 70;
	public float currentFOV = baseFOV, targetFOV = baseFOV;
	public float[] lightPos;
	public Shader main;
	public Shader post;
	public Framebuffer fb;
	public Framebuffer sunFb;

	public Render() {
		init();
	}

	public void init() {
		makeOpenGL();
		if (main == null) {
			main = new Shader("main");
			main.load();
		}
		if (post == null) {
			post = new Shader("post");
			post.load();
		}
		if (sunFb == null)
			sunFb = new Framebuffer(500, 500);
	}

	public void update() {
		float[] fogColor = new float[4];
		fogColor[3] = 1;
		if (Main.m.world.weather.contains("rain")) {
			fogColor[0] = .25F;
			fogColor[1] = .25F;
			fogColor[2] = .3f;
		} else
			fogColor = Main.m.world.skyColor2;
		GL11.glFog(GL11.GL_FOG_COLOR, Modeler.fBuffer(fogColor));
		currentFOV += targetFOV == currentFOV ? 0 : targetFOV > currentFOV ? 1 : -5;
		if (Math.abs(currentFOV - targetFOV) < 5 && targetFOV < currentFOV)
			currentFOV = targetFOV;
	}

	public void renderFromSun() {
		Texture.unbind();
		fb.bind();
		fb.clear();

	}

	public void renderWorld() {
		Texture.unbind();
		fb.bind();
		fb.clear();

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		Main.m.world.renderSky();
		GL11.glEnable(GL11.GL_DEPTH_TEST);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		org.lwjgl.util.glu.GLU.gluPerspective(currentFOV, ((float) Display.getWidth() / (float) Display.getHeight()), near, far);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		lookThroughCamera();

		main.bind();
		Main.m.world.render();

		if (Main.m.modeler.started)
			Main.m.modeler.finish();

		Framebuffer.unbind();

		Shader.unbind();
	}

	public void renderGui() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, Display.getWidth(), Display.getHeight(), 0, 0, 1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		Main.m.modeler.reset();
		Main.m.modeler.setHexColor(0xFFFFFFFF);
		if (Main.m.gameStarted) {
			post.bind();
			fb.render();
			Shader.unbind();
		}
		Gui.render();

		if (Main.m.modeler.started)
			Main.m.modeler.finish();
	}

	public void lookThroughCamera() {
		GL11.glRotated(Main.m.camera.rotX, 1.0D, 0.0D, 0.0D);
		GL11.glRotated(Main.m.camera.rotZ, 0.0D, 0.0D, 1.0D);
		GL11.glRotated(Main.m.camera.rotY, 0.0D, 1.0D, 0.0D);
		GL11.glTranslated(-Main.m.camera.posX, -Main.m.camera.posY, -Main.m.camera.posZ);
	}

	public void makeOpenGL() {
		GL11.glViewport(0, 0, Display.getWidth(), Display.getHeight());

		if (fb != null)
			fb.delete();
		fb = new Framebuffer(Display.getWidth(), Display.getHeight());

		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glDepthFunc(GL11.GL_LEQUAL);

		GL11.glClearDepth(1000F);
		GL11.glClearColor(1, 1, 1, 1);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, .0001F);

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}

	public static double[] getCoordInFront(double rotX, double rotY, double posX, double posY, double posZ, double amount) {
		double[] result = new double[3];
		double radianX = Math.toRadians(rotX);
		double sinX = Math.sin(radianX);
		double cosX = Math.cos(radianX);
		double radianY = Math.toRadians(rotY);
		double sinY = Math.sin(radianY);
		double cosY = Math.cos(radianY);
		result[0] = posX + sinY * cosX * amount;
		result[1] = posY - sinX * amount;
		result[2] = posZ - cosY * cosX * amount;
		return result;
	}

	public static double[] getCoordInFront(double sinX, double cosX, double sinY, double cosY, double posX, double posY, double posZ, double amount) {
		double[] result = new double[3];
		result[0] = posX + sinY * cosX * amount;
		result[1] = posY - sinX * amount;
		result[2] = posZ - cosY * cosX * amount;
		return result;
	}

	public static void addToArray(float[] array1, float[] array2) {
		int arraySize = array1.length < array2.length ? array1.length : array2.length;
		for (int i = 0; i < arraySize; i++)
			array1[i] += array2[i];
	}

	public static double distanceSqrt(double[] point1, double[] point2) {
		return Math.sqrt(distance(point1, point2));
	}

	public static double distance(double[] point1, double[] point2) {
		double xDis = point1[0] - point2[0];
		double yDis = point1[1] - point2[1];
		double zDis = point1[2] - point2[2];
		return xDis * xDis + yDis * yDis + zDis * zDis;
	}

	public static int floor(double d) {
		return (int) (long) d;
	}

	public static double linearLerp(double a, double b, double x) {
		return a * (1 - x) + b * x;
	}

	/* The following code was written by Riven at http://riven8192.blogspot.com, he says anyone can use it so feel free to implement it where ever you may need it! This isn't a verbatim copy but it's essentially the same */

	private static final int ATAN2_BITS = 7;

	private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
	private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
	private static final int ATAN2_COUNT = ATAN2_MASK + 1;
	private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);

	private static final float INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);
	private static final float DEG = 180.0F / pi;

	private static final float[] atan2 = new float[ATAN2_COUNT];

	static {
		for (int i = 0; i < ATAN2_DIM; i++) {
			for (int j = 0; j < ATAN2_DIM; j++) {
				float x0 = (float) i / ATAN2_DIM;
				float y0 = (float) j / ATAN2_DIM;

				atan2[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
			}
		}
	}

	public static double atan2Deg(double y, double x) {
		return atan2(y, x) * DEG;
	}

	public static final double atan2(double y, double x) {
		double add, mul;

		if (x < 0.0) {
			if (y < 0.0) {
				x = -x;
				y = -y;

				mul = 1.0;
			} else {
				x = -x;
				mul = -1.0;
			}

			add = -pi;
		} else {
			if (y < 0.0) {
				y = -y;
				mul = -1.0f;
			} else
				mul = 1.0f;

			add = 0.0f;
		}

		double invDiv = 1.0 / (((x < y) ? y : x) * INV_ATAN2_DIM_MINUS_1);

		int xi = (int) (x * invDiv);
		int yi = (int) (y * invDiv);

		return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
	}
}
