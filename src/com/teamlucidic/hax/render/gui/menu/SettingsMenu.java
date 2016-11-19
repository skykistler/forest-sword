package com.teamlucidic.hax.render.gui.menu;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.gui.menu.components.Button;
import com.teamlucidic.hax.render.gui.menu.components.Component;
import com.teamlucidic.hax.render.gui.menu.components.Spinner;

public class SettingsMenu extends Menu {
	public Menu parentMenu;

	public Button vsync;
	public Button windTest;
	public Button zOrderChunks;
	public Spinner renderDistance;
	public Button fog;
	public Button alphaSort;
	public Button back;

	public SettingsMenu(Menu parent) {
		super("Settings");
		parentMenu = parent;
		pauseMenu = true;
	}

	public void init() {
		components = new ArrayList<Component>();
		int spaceBetween = 30;
		int width = 600;
		int height = 50;
		int middle = (Display.getHeight() / 2) + 30;
		vsync = new Button(this, "Vsync: " + (Main.m.settings.vsync ? "Accurate" : "Fast"), middle - (height * 4 + spaceBetween * 3), width, height);
		windTest = new Button(this, "Wind test: " + (Main.m.settings.windTest ? "Cool looking" : "Off"), middle - (height * 3 + spaceBetween * 2), width, height);
		zOrderChunks = new Button(this, "Z Order Chunks: " + (Main.m.settings.zOrderChunks ? "Accurate" : "Fast"), middle - (height * 2 + spaceBetween * 1), width, height);
		fog = new Button(this, "Fog: " + (Main.m.settings.fog ? "Cool looking" : "Off"), middle - height, width, height);
		alphaSort = new Button(this, "Alpha Sort: " + (Main.m.settings.alphaSort ? "Accurate" : "Fast"), middle + spaceBetween, width, height);
		back = new Button(this, "Back", middle + spaceBetween * 2 + height, width, height);
	}

	public void drawMenu() {
		if (Main.m.gameStarted)
			drawRect(0, 0, Display.getWidth(), Display.getHeight(), 0xAF111111);
		else
			drawBackground(1);
		drawLabel2();
	}

	public void componentAction(Component com) {
		boolean needsSave = false;
		if (com == vsync) {
			Main.m.settings.vsync = !Main.m.settings.vsync;
			Display.setVSyncEnabled(Main.m.settings.vsync);
			vsync.label = "Vsync: " + (Main.m.settings.vsync ? "Accurate" : "Fast");
			needsSave = true;
		}

		if (com == windTest) {
			Main.m.settings.windTest = !Main.m.settings.windTest;
			windTest.label = "Wind test: " + (Main.m.settings.windTest ? "Cool looking" : "Off");
			needsSave = true;
		}

		if (com == zOrderChunks) {
			Main.m.settings.zOrderChunks = !Main.m.settings.zOrderChunks;
			zOrderChunks.label = "Z Order Chunks: " + (Main.m.settings.zOrderChunks ? "Accurate" : "Fast");
			needsSave = true;
		}

		if (com == fog) {
			Main.m.settings.fog = !Main.m.settings.fog;
			fog.label = "Fog: " + (Main.m.settings.fog ? "Cool looking" : "Off");
			needsSave = true;
		}

		if (com == alphaSort) {
			Main.m.settings.alphaSort = !Main.m.settings.alphaSort;
			alphaSort.label = "Alpha Sort: " + (Main.m.settings.alphaSort ? "Accurate" : "Fast");
			needsSave = true;
		}

		if (com == back)
			Menu.setMenu(parentMenu);

		if (needsSave)
			try {
				Main.m.settings.save();
			} catch (IOException e) {
				e.printStackTrace();
				Main.m.error("Could not save settings!");
			}
	}
}
