package com.teamlucidic.hax.render.gui.menu;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.menu.components.Button;
import com.teamlucidic.hax.render.gui.menu.components.Component;
import com.teamlucidic.hax.render.gui.menu.components.TextBox;

public class PlayGame extends Menu {
	public TextBox nameBox;
	public Button startBtn;
	public Button backBtn;

	public PlayGame() {
		super("Asunder");
	}

	public void init() {
		components = new ArrayList<Component>();
		int width = 400;
		int height = 50;
		nameBox = new TextBox(this, Main.m.settings.playerName, Display.getHeight() / 2 - height, width, height);
		startBtn = new Button(this, "Start Game", Display.getHeight() / 2 + 30, width, height);
		backBtn = new Button(this, "Back", Display.getHeight() / 2 + height + 70, width, height);
		focus = nameBox;
	}

	public void update() {
	}

	public void drawMenu() {
		drawBackground(2);
		drawTexturedRect(Display.getWidth() / 2 - 400, -10, 800, 320, Texture.getTexture("gui/asundertitle.png"));
	}

	public void componentAction(Component com) {
		if (com == startBtn) {
			Main.m.settings.playerName = nameBox.label;
			try {
				Main.m.settings.save();

			} catch (IOException e) {
				Main.m.error("Unable to save player name");
				e.printStackTrace();
			}
			Menu.setMenu(new AsunderIntro(null));
		}
		if (com == backBtn)
			Menu.setMenu(new StartMenu());
		focus = com;
	}
}
