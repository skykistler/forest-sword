package com.teamlucidic.hax.render.gui.menu;

import java.util.ArrayList;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.Sound;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.Font;
import com.teamlucidic.hax.render.gui.menu.components.Button;
import com.teamlucidic.hax.render.gui.menu.components.Component;

public class AsunderEnd extends Menu {
	public Button playAgainBtn;
	public Button mainMenuBtn;

	public AsunderEnd() {
		super(Main.m.wName + " has been neutralized.");
	}

	public void init() {
		components = new ArrayList<Component>();
		int width = 400;
		int height = 50;
		Main.m.wType = 0;
		playAgainBtn = new Button(this, "Play Again", (Display.getHeight() / 2), width, height);
		mainMenuBtn = new Button(this, "Main Menu", (Display.getHeight() / 2) + 100, width, height);

		Sound.getSound("music/a walk in the park.ogg").playInBackground(false);
	}

	public void update() {

	}

	public void drawMenu() {
		drawBackground(2);
		drawTexturedRect(Display.getWidth() / 2 - 400, -10, 800, 320, Texture.getTexture("gui/asundertitle.png"));
		drawBorderedString(title, Display.getWidth() / 2 - Font.currentFont.getWidth(title, 2) / 2, 230, 2, 0xFFFFFFFF, 0xFF000000);
		drawBorderedString("You win!", Display.getWidth() / 2 - Font.currentFont.getWidth("You win!", 4) / 2, (Display.getHeight() / 2) + 200, 4, 0xFFFFFFFF, 0xFF000000);
	}

	public void componentAction(Component com) {
		if (com == playAgainBtn) {
			Sound.stopAllBackgroundSounds();
			Menu.setMenu(new PlayGame());
		}
		if (com == mainMenuBtn) {
			Sound.stopAllBackgroundSounds();
			Menu.setMenu(new StartMenu());
		}
	}
}
