package com.teamlucidic.hax.render.gui;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.menu.Menu;

public class Gui {
	public static Texture topBorder = Texture.getTexture("gui/window.png").getSubTexture(16, 0, 1, 4);
	public static Texture bottomBorder = Texture.getTexture("gui/window.png").getSubTexture(16, 4, 1, 4);
	public static Texture leftBorder = Texture.getTexture("gui/window.png").getSubTexture(12, 4, 4, 1);
	public static Texture rightBorder = Texture.getTexture("gui/window.png").getSubTexture(16, 4, 4, 1);
	public static Texture topLeftCorner = Texture.getTexture("gui/window.png").getSubTexture(12, 0, 4, 4);
	public static Texture topRightCorner = Texture.getTexture("gui/window.png").getSubTexture(16, 0, 4, 4);
	public static Texture bottomLeftCorner = Texture.getTexture("gui/window.png").getSubTexture(12, 5, 4, 4);
	public static Texture bottomRightCorner = Texture.getTexture("gui/window.png").getSubTexture(16, 5, 4, 4);

	public static void render() {
		Menu.drawCurrentMenu();
	}

	public void drawString(String txt, int x, int y, double scale, int color) {
		Font.drawStringWithCurFont(txt, x, y, scale, color);
	}

	public void drawShadowedString(String txt, int x, int y, double scale, int color) {
		Font.drawShadowed(txt, x, y, scale, color);
	}

	public void drawBorderedString(String txt, int x, int y, double scale, int color, int borderColor) {
		Font.drawBordered(txt, x, y, scale, color, borderColor);
	}

	public void drawCenteredString(String txt, int y, double scale, int color, String type) {
		if (type.equals("shadow"))
			drawShadowedString(txt, (Display.getWidth() / 2) - (Font.currentFont.getWidth(txt, scale) / 2), y, scale, color);
		else if (type.equals("bordered"))
			drawBorderedString(txt, (Display.getWidth() / 2) - (Font.currentFont.getWidth(txt, scale) / 2), y, scale, color, 0xFF000000);
		else
			drawString(txt, (Display.getWidth() / 2) - (Font.currentFont.getWidth(txt, scale) / 2), y, scale, color);
	}

	public void drawRect(int x, int y, int w, int h, int color) {
		Texture.unbind();
		Main.m.modeler.setHexColor(color);
		Main.m.modeler.drawRect2D(x, y, w, h);
	}

	public void drawRect(int x, int y, int w, int h, float r, float g, float b, float a) {
		Texture.unbind();
		Main.m.modeler.setRGBAColor(r, g, b, a);
		Main.m.modeler.drawRect2D(x, y, w, h);
	}

	public void drawRect(int x, int y, int w, int h, float[] rgba) {
		Texture.unbind();
		Main.m.modeler.setRGBAColor(rgba);
		Main.m.modeler.drawRect2D(x, y, w, h);
	}

	public void drawTexturedRect(int x, int y, int w, int h, Texture tex) {
		if (tex == null)
			tex = Texture.getTexture("gui/unknown.png");
		tex.bind();
		Main.m.modeler.drawRect2D(x, y, w, h);
	}

	public void drawTexturedRect(int x, int y, int w, int h, Texture tex, int color) {
		Main.m.modeler.setHexColor(color);
		drawTexturedRect(x, y, w, h, tex);
	}

	public void drawTexturedRect(int x, int y, int w, int h, Texture tex, int texX, int texY, int texW, int texH, int color, boolean tiled, boolean startAndFinish) {
		if (tex == null)
			tex = Texture.getTexture("gui/unknown.png");
		float txf = (float) texX / (float) tex.width;
		float tyf = (float) texY / (float) tex.height;
		float twf = (float) texW / (float) tex.width;
		float thf = (float) texH / (float) tex.height;
		if (tiled) {
			twf += txf;
			thf += tyf;
		}
		tex.bind();
		Main.m.modeler.setHexColor(color);
		if (startAndFinish)
			Main.m.modeler.start();
		Main.m.modeler.addVertexTex(x, y, 0, txf, tyf);
		Main.m.modeler.addVertexTex(x, y + h, 0, txf, thf);
		Main.m.modeler.addVertexTex(x + w, y + h, 0, twf, thf);
		Main.m.modeler.addVertexTex(x + w, y, 0, twf, tyf);
		if (startAndFinish)
			Main.m.modeler.finish();
	}

	public void drawFancyBorder(int x, int y, int w, int h, int color, int scale) {
		Main.m.modeler.setHexColor(color);
		drawTexturedRect(x + 4 * scale, y, w - 8 * scale, 4 * scale, topBorder);
		drawTexturedRect(x + 4 * scale, y + h - 4 * scale, w - 8 * scale, 4 * scale, bottomBorder);
		drawTexturedRect(x, y + 4 * scale, 4 * scale, h - 7 * scale, leftBorder);
		drawTexturedRect(x + w - 4 * scale, y + 4 * scale, 4 * scale, h - 7 * scale, rightBorder);

		drawTexturedRect(x, y, 4 * scale, 4 * scale, topLeftCorner);
		drawTexturedRect(x + w - 4 * scale, y, 4 * scale, 4 * scale, topRightCorner);
		drawTexturedRect(x, y + h - 3 * scale, 4 * scale, 4 * scale, bottomLeftCorner);
		drawTexturedRect(x + w - 4 * scale, y + h - 3 * scale, 4 * scale, 4 * scale, bottomRightCorner);
	}
}
