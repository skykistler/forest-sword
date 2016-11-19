package com.teamlucidic.hax.render.gui.menu;

import java.util.ArrayList;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.menu.components.Button;
import com.teamlucidic.hax.render.gui.menu.components.Component;

public class StartMenu extends Menu {

	public Button playBtn;
	public Button worldBtn;
	public Button quitBtn;
	public static boolean intro = true;
	public int step;
	public int opacity;

	public StartMenu() {
		super(Main.version.split("Forest Sword ")[1]);
	}

	public void init() {
		components = new ArrayList<Component>();
		step = 0;
		if (!intro)
			setButtons();
	}

	public void update() {
	}

	public void drawMenu() {
		drawBackground(1);
		if (intro) {
			handleIntro();
		} else {
			drawCenteredString("Forest Sword", 60, titleSize, 0xFF00BB00, "bordered");
			drawLabel();
		}

	}

	public void handleIntro() {
		if (step == 0) {
			Texture.getTexture("splash.png").bind();
			Main.m.modeler.drawRect2D(0, 0, Display.getWidth(), Display.getHeight());
			drawRect(0, 0, Display.getWidth(), Display.getHeight(), 0.0F, 0.0F, 0.0F, menuTicks / 90F);
			if (menuTicks > 120)
				step = 1;
		}
		if (step == 1) {
			intro = false;
			setButtons();
		}
	}

	public void setButtons() {
		int spaceBetween = 50;
		int width = 400;
		int height = 50;
		playBtn = new Button(this, "Play a game", Display.getHeight() / 2 - (height + spaceBetween), width, height);
		worldBtn = new Button(this, "Make a world", Display.getHeight() / 2, width, height);
		quitBtn = new Button(this, "Quit Game", Display.getHeight() / 2 + height + spaceBetween, width, height);
	}

	public void titleSequence() {

	}

	public void componentAction(Component com) {
		if (com == playBtn)
			Menu.setMenu(new PlayGame());
		if (com == worldBtn)
			Menu.setMenu(new FreeplayMenu());
		if (com == quitBtn)
			Main.m.shutDown();
	}
}
