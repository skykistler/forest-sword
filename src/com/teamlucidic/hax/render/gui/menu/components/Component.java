package com.teamlucidic.hax.render.gui.menu.components;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.Font;
import com.teamlucidic.hax.render.gui.Gui;
import com.teamlucidic.hax.render.gui.menu.Menu;

public class Component extends Gui {
	public static int scale = 2;
	public Menu parentMenu;
	public int posX, posY, width, height;
	public boolean enabled = true, mouseOver, mouseDown, mouseClicked;
	public int borderSize = 3;
	public double fontSize = 2;
	public boolean dead;

	public Component(Menu m, int x, int y, int w, int h) {
		parentMenu = m;
		posX = x;
		posY = y;
		width = w;
		height = h;
		m.add(this);
	}

	public void update() {
		int x = Main.m.input.mouse.getX();
		int y = Display.getHeight() - Main.m.input.mouse.getY();
		mouseOver = mouseDown = mouseClicked = false;
		if (x >= posX && x <= posX + width && y >= posY && y <= posY + height) {
			mouseOver = true;
			if (Main.m.input.mouse.isDown(0))
				mouseDown = true;
			mouseClicked = Main.m.input.mouse.checkButton(0);
			if (mouseClicked)
				parentMenu.componentAction(this);
		}
	}

	public void drawComponent() {

	}

	public void drawBorder() {
		int color = 0xFF000000;
		drawRect(posX, posY, borderSize, height, color);
		drawRect(posX, posY, width, borderSize, color);
		drawRect(posX + width - borderSize, posY, borderSize, height, color);
		drawRect(posX, posY + height - borderSize, width, borderSize, color);
	}

	public void drawWindowRect(int posX, int posY, int width, int height, int borderSize) {
		int size = borderSize * windowBorderWidth;

		drawTexturedRect(posX, posY, width, height, windowFill);
		drawTexturedRect(posX, posY - size, width, size, topBorder);
		drawTexturedRect(posX, posY + height, width, size, bottomBorder);
		drawTexturedRect(posX - size, posY, size, height, leftBorder);
		drawTexturedRect(posX + width, posY, size, height, rightBorder);

		drawTexturedRect(posX - size, posY - size, size, size, topLeftCorner);
		drawTexturedRect(posX + width, posY - size, size, size, topRightCorner);
		drawTexturedRect(posX - size, posY + height, size, size, bottomLeftCorner);
		drawTexturedRect(posX + width, posY + height, size, size, bottomRightCorner);

		int cColor = tlCorner;
		if (Main.m.modeler.color != 0xFFFFFFFF)
			cColor = Main.m.modeler.color;
		drawRect(posX, posY, scale, scale, cColor);

		if (Main.m.modeler.color == 0xFFFFFFFF)
			cColor = trCorner;
		drawRect(posX + width - scale, posY, scale, scale, cColor);

		if (Main.m.modeler.color == 0xFFFFFFFF)
			cColor = blCorner;
		drawRect(posX, posY + height - scale, scale, scale, cColor);

		if (Main.m.modeler.color == 0xFFFFFFFF)
			cColor = brCorner;
		drawRect(posX + width - scale, posY + height - scale, scale, scale, cColor);
	}

	public void setToolTip(String label, boolean mouse) {
		parentMenu.currentToolTip = new ToolTip(label, mouse);
	}

	public boolean isFocused() {
		return parentMenu.focus == this;
	}

	public void mousePress(int x, int y, int k) {

	}

	public void mouseRelease(int x, int y, int k) {

	}

	public void mouseDrag(int x, int y, int k) {

	}

	public void keyTyped(char c) {

	}

	public class ToolTip {
		public String label;
		public boolean itemRequiresMouse;
		public String requiresMouse = "Requires Mouse";

		public ToolTip(String l, boolean mouse) {
			label = l;
			itemRequiresMouse = mouse;
		}

		public ToolTip(String l) {
			this(l, false);
		}

		public void draw() {
			int x = Main.m.input.mouse.getX();
			int borderSize = 2;
			int height = Font.currentFont.letterHeight + borderSize * 2;
			if (itemRequiresMouse)
				height *= 2;
			int y = Display.getHeight() - Main.m.input.mouse.getY() - height;
			int labelWidth = Font.currentFont.getWidth(label, 1);
			int fontWidth = itemRequiresMouse ? Math.max(labelWidth, Font.currentFont.getWidth(requiresMouse, 1)) : labelWidth;
			int width = fontWidth + borderSize * 2;
			int color = 0xFF101010;
			drawRect(x, y, width, height, 0xAA663320);
			drawBorderedString(label, x + 2, y + 2, 1, 0xFFFFFFFF, color);
			if (itemRequiresMouse)
				drawBorderedString(requiresMouse, x + 2, y + 4 + Font.currentFont.letterHeight, 1, 0xFFFFFFFF, color);
			drawRect(x, y, borderSize, height, color);
			drawRect(x, y, width, borderSize, color);
			drawRect(x + width - borderSize, y, borderSize, height, color);
			drawRect(x, y + height - borderSize, width, borderSize, color);
		}
	}

	public static int windowBorderWidth = 5;
	public static Texture bottomRightCorner = Texture.getTexture("gui/window.png").getSubTexture(Component.windowBorderWidth + 1, Component.windowBorderWidth + 1, Component.windowBorderWidth, Component.windowBorderWidth);
	public static Texture bottomLeftCorner = Texture.getTexture("gui/window.png").getSubTexture(0, Component.windowBorderWidth + 1, Component.windowBorderWidth, Component.windowBorderWidth);
	public static Texture topRightCorner = Texture.getTexture("gui/window.png").getSubTexture(Component.windowBorderWidth + 1, 0, Component.windowBorderWidth, Component.windowBorderWidth);
	public static Texture topLeftCorner = Texture.getTexture("gui/window.png").getSubTexture(0, 0, Component.windowBorderWidth, Component.windowBorderWidth);
	public static Texture topBorder = Texture.getTexture("gui/window.png").getSubTexture(windowBorderWidth, 0, 1, windowBorderWidth);
	public static Texture bottomBorder = Texture.getTexture("gui/window.png").getSubTexture(windowBorderWidth, windowBorderWidth + 1, 1, windowBorderWidth);
	public static Texture rightBorder = Texture.getTexture("gui/window.png").getSubTexture(Component.windowBorderWidth + 1, Component.windowBorderWidth, Component.windowBorderWidth, 1);
	public static Texture leftBorder = Texture.getTexture("gui/window.png").getSubTexture(0, Component.windowBorderWidth, Component.windowBorderWidth, 1);
	public static Texture windowFill = Texture.getTexture("gui/window.png").getSubTexture(Component.windowBorderWidth, Component.windowBorderWidth, 1, 1);

	public static int tlCorner = Texture.getTexture("gui/window.png").getPixel(windowBorderWidth - 1, windowBorderWidth - 1);
	public static int trCorner = Texture.getTexture("gui/window.png").getPixel(windowBorderWidth + 1, windowBorderWidth - 1);
	public static int blCorner = Texture.getTexture("gui/window.png").getPixel(windowBorderWidth - 1, windowBorderWidth + 1);
	public static int brCorner = Texture.getTexture("gui/window.png").getPixel(windowBorderWidth + 1, windowBorderWidth + 1);

}
