package com.teamlucidic.hax.render;

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.Main;

public class Texture {
	public static HashMap<Integer, Texture> textureMap = new HashMap<Integer, Texture>();
	public static ArrayList<Texture> miscTextureList = new ArrayList<Texture>();
	public static Texture currentTexture;

	public int id;
	public BufferedImage image;
	public String path;
	public int width;
	public int height;
	public int[] pixels;
	public ByteBuffer pixBuffer;
	public boolean glReady;

	public Texture(BufferedImage img) {
		miscTextureList.add(this);
		image = img;
		loadTexture();
	}

	public Texture(String path) {
		this.path = path;
		textureMap.put(path.toLowerCase().hashCode(), this);
		image = loadImage(path);
		loadTexture();
	}

	public Texture(int w, int h) {
		width = w;
		height = h;
		pixels = new int[height * width];
		miscTextureList.add(this);
	}

	public void loadTexture() {
		if (image != null) {
			width = image.getWidth();
			height = image.getHeight();
			pixels = new int[height * width];
			PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
			try {
				pg.grabPixels();
			} catch (InterruptedException e) {
				Main.m.error("Failed loading texture " + path);
				e.printStackTrace();
			}
		}
	}

	public BufferedImage loadImage(String path) {
		try {
			if (path.startsWith("/"))
				return ImageIO.read(new File(Main.m.resourcePack + path.substring(1, path.length())));
			else {
				return ImageIO.read(new File(Main.m.resourcePack + "textures/" + path));
			}
		} catch (Exception e) {
			Main.m.error("Error loading image " + path);
			e.printStackTrace();
			return null;
		}
	}

	public void loadBuffers() {
		byte[] pixDataRGBA = convertToByteRGBA(pixels);
		pixBuffer = (ByteBuffer) BufferUtils.createByteBuffer(pixDataRGBA.length).put(pixDataRGBA).flip();
	}

	public byte[] convertToByteRGBA(int[] pixels) {
		byte[] bytes = new byte[pixels.length * 4];
		int col, r, g, b, a;
		int j = 0;
		for (int i = 0; i < pixels.length; i++) {
			col = pixels[i];
			a = (col >> 24) & 0xFF;
			r = (col >> 16) & 0xFF;
			g = (col >> 8) & 0xFF;
			b = (col) & 0xFF;
			bytes[j] = (byte) r;
			bytes[j + 1] = (byte) g;
			bytes[j + 2] = (byte) b;
			bytes[j + 3] = (byte) a;
			j += 4;
		}
		return bytes;
	}

	public int[] convertToIntRGBA(int[] pixels) {
		int[] ints = new int[pixels.length * 4];
		int col, r, g, b, a;
		int j = 0;
		for (int i = 0; i < pixels.length; i++) {
			col = pixels[i];
			a = (col >> 24) & 0xFF;
			r = (col >> 16) & 0xFF;
			g = (col >> 8) & 0xFF;
			b = (col) & 0xFF;
			ints[j] = (byte) r;
			ints[j + 1] = (byte) g;
			ints[j + 2] = (byte) b;
			ints[j + 3] = (byte) a;
			j += 4;
		}
		return ints;
	}

	public void setUpGL(boolean blur) {
		if (glReady)
			GL11.glDeleteTextures(id);
		loadBuffers();
		id = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		int type = blur ? GL11.GL_LINEAR : GL11.GL_NEAREST;
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, type);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, type);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, 4, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixBuffer);
		glReady = true;
	}

	public void updateGL() {
		if (!glReady)
			Main.m.error("Never prepared " + path + " for OpenGL");
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, 4, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixBuffer);
		currentTexture.bind();
	}

	public void bind() {
		if (!glReady)
			setUpGL(false);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		currentTexture = this;
	}

	public void delete() {
		if (glReady)
			GL11.glDeleteTextures(id);

	}

	public Texture getSubTexture(int x, int y, int width, int height) {
		if (x >= 0 && x + width <= this.width && y >= 0 && y + height <= this.height)
			return new Texture(image.getSubimage(x, y, width, height));
		return null;
	}

	public int getPixel(int x, int y) {
		if (x >= 0 && x < width && y >= 0 && y < height) {
			return pixels[x + y * width];
		}
		return 0x00000000;
	}

	public int getAveragePixel() {
		int result = 0;
		for (int i = 0; i < pixels.length; i++) {
			result += pixels[i];
		}
		result /= pixels.length;
		return result;
	}

	public void setPixel(int x, int y, int color) {
		pixels[x + y * width] = color;
	}

	public void clear(int color) {
		for (int i = 0; i < pixels.length; i++)
			pixels[i] = color;
	}

	public void save(String path) {
		try {
			BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			im.getRaster().setPixels(0, 0, width, height, convertToIntRGBA(pixels));
			File savedTexture = new File(Main.m.resourcePack + path);
			ImageIO.write(im, Main.getFileExtension(path), savedTexture);
		} catch (Exception e) {
			Main.m.error("Unable to save image: " + path);
			e.printStackTrace();
		}
	}

	public static void loadAllTextures() {
		loadTexturesInFolder(Main.m.resourcePack + "textures/");
		System.out.println("Successfully loaded " + textureMap.values().size() + " textures.");
	}

	public static void loadTexturesInFolder(String path) {
		File folder = new File(path);
		File[] files = folder.listFiles();
		if (files != null)
			for (File file : files)
				if (file.isDirectory())
					loadTexturesInFolder(file.getAbsolutePath());
				else
					new Texture(file.getAbsolutePath().substring((Main.m.resourcePack + "textures/").length()).replace("\\", "/"));
	}

	public static Texture getTexture(String name) {
		if (!textureMap.containsKey(name.toLowerCase().hashCode())) {
			System.out.println("Loading: " + name);
			return new Texture(name);
		}
		return textureMap.get(name.toLowerCase().hashCode());
	}

	public static void unbind() {
		unbind(true);
	}

	public static void unbind(boolean real) {
		if (real)
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		else
			getTexture("misc/blank.png").bind();
	}

	public static void deleteTextures() {
		for (Texture texture : miscTextureList)
			texture.delete();
		for (Texture texture : textureMap.values())
			texture.delete();
	}

}
