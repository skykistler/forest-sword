package com.teamlucidic.hax.render.gui.menu;

import java.util.ArrayList;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.render.gui.menu.components.Button;
import com.teamlucidic.hax.render.gui.menu.components.Component;

public class FreeplayMenu extends Menu {

	public Button loadBtn;
	public Button generateBtn;
	public Button backBtn;

	public FreeplayMenu() {
		super("Mess around");
	}

	public void init() {
		components = new ArrayList<Component>();
		int spaceBetween = 50;
		int width = 400;
		int height = 50;
		loadBtn = new Button(this, "Load World", Display.getHeight() / 2 - (height + spaceBetween), width, height);
		generateBtn = new Button(this, "New World", Display.getHeight() / 2, width, height);
		backBtn = new Button(this, "Back", Display.getHeight() / 2 + height + spaceBetween, width, height);

	}

	public void update() {
	}

	public void drawMenu() {
		drawBackground(1);
		drawCenteredString("Free Play", 60, titleSize, 0xFF00BB00, "bordered");
		drawLabel();
	}

	public void componentAction(Component com) {
		if (com == loadBtn)
			Menu.setMenu(new LoadWorld());
		if (com == generateBtn)
			Menu.setMenu(new NewWorld());
		if (com == backBtn)
			Menu.setMenu(new StartMenu());
	}
}
