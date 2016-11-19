package com.teamlucidic.hax.render.gui.menu;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.Font;
import com.teamlucidic.hax.render.gui.Gui;
import com.teamlucidic.hax.render.gui.menu.components.Component;

public class LoadingScreen extends Menu {
	public static double percentDone;
	public static String label;

	public LoadingScreen(String name) {
		super(name);
		label = name;
		percentDone = 0;
	}

	public void drawMenu() {
		int w = Display.getWidth();
		int h = Display.getHeight();
		int fontSize = 3;
		title = label;
		drawRect(0, 0, w, h, 0xFF000000);
		drawString(title, (w / 2) - (Font.currentFont.getWidth(title, fontSize) / 2), (h / 2) - (int) (Font.currentFont.letterHeight * fontSize), fontSize, 0xFFFFFFFF);

		int barHeight = 30;
		int barWidth = 100;
		int length = (int) (percentDone * barWidth);
		int borderWidth = (int) (3 * Component.scale);

		drawTexturedRect(w / 2 - length, h / 2 + 100 - borderWidth / 2 + Component.scale, length * 2, barHeight - borderWidth - Component.scale * 2, Texture.getTexture("gui/energy.png"), 0xFFFFFFFF);

		drawFancyBorder(w / 2 - barWidth - borderWidth, h / 2 + 100 - borderWidth, barWidth * 2 + borderWidth * 2, barHeight, 0xFFFFFFFF, Component.scale);
	}

	public static void setLabel(String l) {
		label = l;
	}

	public static void setPercentDone(double done) {
		percentDone = done;
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		Main.m.modeler.reset();
		Main.m.modeler.setHexColor(0xFFFFFFFF);
		Gui.render();
		Display.update();
	}

}
