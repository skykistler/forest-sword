package com.teamlucidic.hax.render.gui.menu;

import java.util.ArrayList;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.gui.menu.components.Button;
import com.teamlucidic.hax.render.gui.menu.components.Component;
import com.teamlucidic.hax.world.World;

public class Pause extends Menu {
	public Button resumeBtn;
	public Button resetBtn;
	public Button saveBtn;
	public Button settingsBtn;
	public Button mainmenuBtn;
	public Button quitBtn;

	public Pause() {
		super("Paused");
		pauseMenu = true;
	}

	public void init() {
		components = new ArrayList<Component>();
		int spaceBetween = 30;
		int width = 400;
		int height = 50;
		int middle = (Display.getHeight() / 2) + 30;
		String reset = Main.m.gameMode.contains("new") ? "Regenerate Map" : "Reload Map";
		reset = Main.m.gameMode.contains("asunder") ? "New Map" : reset;
		resumeBtn = new Button(this, "Resume", middle - (height * 4 + spaceBetween * 3), width, height);
		resetBtn = new Button(this, reset, middle - (height * 3 + spaceBetween * 2), width, height);
		saveBtn = new Button(this, "Save Map", middle - (height * 2 + spaceBetween * 1), width, height);
		saveBtn.enabled = Main.m.gameMode.contains("asunder") ? false : true;
		settingsBtn = new Button(this, "Settings", middle - height, width, height);
		mainmenuBtn = new Button(this, "Main Menu", middle + spaceBetween, width, height);
		quitBtn = new Button(this, "Quit Game", middle + height + spaceBetween * 2, width, height);
	}

	public void drawMenu() {
		drawRect(0, 0, Display.getWidth(), Display.getHeight(), 0xAF111111);
		drawLabel2();
	}

	public void componentAction(Component com) {
		if (com == resumeBtn)
			Main.m.pause();

		if (com == resetBtn) {
			if (Main.m.gameMode.contains("asunder")) {
				AsunderIntro asi = new AsunderIntro(null);
				asi.step = 2;
				Menu.setHud(null);
				Main.m.stopGame();
				Menu.setMenu(asi);
			} else {
				if (Main.m.gameMode.contains("new"))
					Main.m.wSeed = World.generateSeed();
				Main.m.startGame(Main.m.gameMode);
			}
		}

		if (com == saveBtn && saveBtn.enabled) {
			Main.m.world.saveWorld();
			Main.m.pause();
		}

		if (com == settingsBtn)
			Menu.setMenu(new SettingsMenu(this));

		if (com == mainmenuBtn) {
			Menu.setHud(null);
			Menu.setMenu(new StartMenu());
			Main.m.stopGame();
		}

		if (com == quitBtn)
			Main.m.shutDown();
	}
}
