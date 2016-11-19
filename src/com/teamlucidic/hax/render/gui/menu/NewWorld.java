package com.teamlucidic.hax.render.gui.menu;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.gui.menu.components.Button;
import com.teamlucidic.hax.render.gui.menu.components.Component;
import com.teamlucidic.hax.render.gui.menu.components.TextBox;
import com.teamlucidic.hax.world.World;

public class NewWorld extends Menu {
	public TextBox nameBox;
	public TextBox seedBox;
	public TextBox widthBox;
	public Button createBtn;
	public Button typeBtn;
	public Button backBtn;

	public NewWorld() {
		super("Create New World");
	}

	public void init() {
		components = new ArrayList<Component>();
		int width = 400;
		int height = 50;
		Main.m.wType = 0;
		nameBox = new TextBox(this, "New World", (Display.getHeight() / 2) - (height * 2 + 30), width, height);
		seedBox = new TextBox(this, "", (Display.getHeight() / 2) - (height), width, height);
		widthBox = new TextBox(this, "16", (Display.getHeight() / 2) + 30, width, height);
		typeBtn = new Button(this, "Basic Hills", (Display.getHeight() / 2) + height + 57, width, height);
		backBtn = new Button(this, "Back", Display.getWidth() / 2 - 200, (Display.getHeight() / 2) + height * 2 + 95, width / 2 - 20, height);
		createBtn = new Button(this, "Generate", Display.getWidth() / 2 + 20, (Display.getHeight() / 2) + height * 2 + 95, width / 2 - 20, height);
	}

	public void update() {
		if (Main.m.input.checkKey(Keyboard.KEY_RETURN))
			componentAction(createBtn);
		if (Main.m.input.checkKey(Keyboard.KEY_ESCAPE))
			componentAction(backBtn);
	}

	public void drawMenu() {
		drawBackground(1);
		drawCenteredString("Create World", 60, titleSize, 0xFF00BB00, "bordered");
		drawCenteredString("Name:", (Display.getHeight() / 2) - 160, 2, 0xFFBBBBBB, "shadow");
		drawCenteredString("Seed:", (Display.getHeight() / 2) - 80, 2, 0xFFBBBBBB, "shadow");
		drawCenteredString("Width in chunks:", (Display.getHeight() / 2), 2, 0xFFBBBBBB, "shadow");
		drawLabel();
	}

	public void componentAction(Component com) {
		if (com == createBtn) {
			Main.m.wName = nameBox.label;
			Main.m.wSeed = seedBox.label != "" ? seedBox.label.hashCode() : World.generateSeed();
			Main.m.wWidth = Integer.parseInt(widthBox.label);
			Main.m.startGame("freeplay:new");
		}
		focus = com;
		if (com == typeBtn) {
			if (typeBtn.label.equals("Basic Hills")) {
				Main.m.wType = 1;
				typeBtn.label = "Island";
				return;
			}
			if (typeBtn.label.equals("Island")) {
				Main.m.wType = 0;
				typeBtn.label = "Basic Hills";
				return;
			}
		}
		if (com == backBtn)
			Menu.setMenu(new FreeplayMenu());
	}
}
