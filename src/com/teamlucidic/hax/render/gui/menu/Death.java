package com.teamlucidic.hax.render.gui.menu;

import java.util.ArrayList;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.Sound;
import com.teamlucidic.hax.render.gui.menu.components.Button;
import com.teamlucidic.hax.render.gui.menu.components.Component;

public class Death extends Menu {
	public Button respawnNewBtn;
	public Button respawnBtn;
	public Button mainmenuBtn;
	public Button quitBtn;

	public Death() {
		super("Game Over");
		initDelay = 60;
	}

	public void init() {
		if (Main.m.gameMode.contains("asunder")) {
			Sound.stopAllBackgroundSounds();
			Sound.getSound("sfx/death.ogg").playInBackground(false);
		}

		components = new ArrayList<Component>();
		int spaceBetween = 10;
		int width = 450;
		int height = 50;

		respawnBtn = new Button(this, Main.m.gameMode.contains("asunder") ? "Reset on this map" : "Respawn", (Display.getHeight() / 2) - (int) (height * 1.5), width, height);
		mainmenuBtn = new Button(this, "Main Menu", (Display.getHeight() / 2) + height / 2, width, height);
		quitBtn = new Button(this, "Quit Game", (Display.getHeight() / 2) + height * 2 + spaceBetween * 2, width, height);

		if (Main.m.gameMode.contains("asunder"))
			respawnNewBtn = new Button(this, "Reset to new map", (Display.getHeight() / 2) - (int) (height * 3 + spaceBetween * 2), width, height);
		else {
			respawnBtn.posY -= height * 2;
			mainmenuBtn.posY -= height * 2;
			quitBtn.posY -= height * 2;
		}
	}

	public void drawMenu() {
		drawRect(0, 0, Display.getWidth(), Display.getHeight(), 0x99000099);
		drawLabel2();
	}

	public void componentAction(Component com) {
		if (com == respawnNewBtn) {
			AsunderIntro asi = new AsunderIntro(null);
			asi.step = 2;
			Menu.setHud(null);
			Main.m.stopGame();
			Menu.setMenu(asi);
		}

		if (com == respawnBtn) {
			if (Main.m.gameMode.contains("asunder")) {
				AsunderIntro asi = new AsunderIntro(Main.m.world.name);
				asi.startAsunder();
			} else
				Main.m.world.respawnPlayer();
		}

		if (com == mainmenuBtn) {
			Menu.setHud(null);
			Menu.setMenu(new StartMenu());
			Main.m.stopGame();
		}

		if (com == quitBtn)
			Main.m.shutDown();
	}
}
