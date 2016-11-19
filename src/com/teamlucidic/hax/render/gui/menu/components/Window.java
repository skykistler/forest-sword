package com.teamlucidic.hax.render.gui.menu.components;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.Font;
import com.teamlucidic.hax.render.gui.menu.Menu;

public class Window extends Component {
	public boolean tabbed;
	public int currentTab;
	public int xCounter;
	public String[] tabs;
	public static Texture tabLeftCorner = Texture.getTexture("gui/window.png").getSubTexture(0, 12, Component.windowBorderWidth + 1, 12);
	public static Texture tabFill = Texture.getTexture("gui/window.png").getSubTexture(Component.windowBorderWidth, 12, 1, 12);
	public static Texture tabRightCorner = Texture.getTexture("gui/window.png").getSubTexture(Component.windowBorderWidth + 1, 12, Component.windowBorderWidth + 1, 12);

	public Window(Menu m, int w, int h) {
		super(m, 0, 0, w, h);
		posX = Display.getWidth() / 2 - width / 2;
		posY = Display.getHeight() / 2 - height / 2;
	}

	public void update() {
		if (Main.m.input.mouse.checkButton(0)) {
			xCounter = 0;
			int x = Main.m.input.mouse.getX();
			int y = Display.getHeight() - Main.m.input.mouse.getY();
			if (y >= posY - 16 * scale && y <= posY - 4 * scale && x > posX && x < posX + width) {
				for (int i = 0; i < tabs.length; i++) {
					xCounter += 16 * scale + Font.currentFont.getWidth(tabs[i], scale);
					if (x < posX + xCounter) {
						currentTab = i;
						break;
					}
				}
			}
		}
	}

	public void drawComponent() {
		Main.m.modeler.setHexColor(0xFFFFFFFF);
		xCounter = 0;
		if (tabbed)
			for (int i = 0; i < tabs.length; i++) {
				drawTab(tabs[i], posX + xCounter, 0);
			}

		Main.m.modeler.setHexColor(0xFFFFFFFF);
		drawWindowRect(posX, posY, width, height, scale);

		xCounter = 0;
		if (tabbed)
			for (int i = 0; i < tabs.length; i++) {
				if (i == currentTab) {
					drawTab(tabs[i], posX + xCounter, 3);
					break;
				} else
					xCounter += 16 * scale + Font.currentFont.getWidth(tabs[i], scale);
			}
	}

	public void drawTab(String s, int x, int yOff) {
		if (!tabbed)
			return;

		Main.m.modeler.setHexColor(0xFFFFFFFF);
		int size = 12 * scale;
		int width = Font.currentFont.getWidth(s, scale) + scale * 2;
		int y = posY - size - scale * 4;
		drawTexturedRect(x, y - yOff, size / 2, size + yOff, Window.tabLeftCorner);
		drawTexturedRect(x + size / 2, y - yOff, width, size + yOff, Window.tabFill);
		drawTexturedRect(x + size / 2 + width, y - yOff, size / 2, size + yOff, Window.tabRightCorner);
		drawString(s, x + (size + width) / 2 - Font.currentFont.getWidth(s, scale) / 2, (int) (y - scale * 1.5), scale, 0xFF000000);
		xCounter += size + width + 2 * scale;
	}
}
