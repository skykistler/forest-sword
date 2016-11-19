package com.teamlucidic.hax.render.gui.menu;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.gui.menu.components.Button;
import com.teamlucidic.hax.render.gui.menu.components.Component;
import com.teamlucidic.hax.render.gui.menu.components.TextBox;
import com.teamlucidic.hax.world.Map;

public class LoadWorld extends Menu {
	public TextBox nameBox;
	public String validWorldTxt = "";
	public Button loadBtn;
	public Button backBtn;

	public LoadWorld() {
		super("Load World");
	}

	public void init() {
		components = new ArrayList<Component>();
		int spaceBetween = 40;
		int width = 400;
		int height = 50;
		nameBox = new TextBox(this, "New World", (Display.getHeight() / 2) - (height + 40), width, height);
		loadBtn = new Button(this, "Load", (Display.getHeight() / 2) + 5, width, height);
		backBtn = new Button(this, "Back", (Display.getHeight() / 2) + height + spaceBetween, width, height);
	}

	public void update() {
		Map map = new Map(nameBox.label);
		if (map.exists()) {
			validWorldTxt = "Map exists";
			loadBtn.enabled = true;
		} else {
			validWorldTxt = "Map not found";
			loadBtn.enabled = false;
		}
		if (Main.m.input.checkKey(Keyboard.KEY_RETURN))
			componentAction(loadBtn);
		if (Main.m.input.checkKey(Keyboard.KEY_ESCAPE))
			componentAction(backBtn);
	}

	public void drawMenu() {
		drawBackground(1);
		drawCenteredString("Load World", 60, titleSize, 0xFF00BB00, "bordered");
		drawCenteredString("Name:", (Display.getHeight() / 2) - 120, 2, 0xFFBBBBBB, "shadow");
		drawCenteredString(validWorldTxt, (Display.getHeight() / 2) - 40, 2, validWorldTxt.endsWith("exists") ? 0xFF339933 : 0xFF1111BB, "shadow");
		drawLabel();
	}

	public void componentAction(Component com) {
		if (com == loadBtn && loadBtn.enabled) {
			Main.m.wName = nameBox.label;
			Main.m.startGame("freeplay:load");
		}
		if (com == backBtn)
			Menu.setMenu(new FreeplayMenu());
		focus = com;
	}

}
