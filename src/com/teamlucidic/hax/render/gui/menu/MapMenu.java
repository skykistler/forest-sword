package com.teamlucidic.hax.render.gui.menu;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.gui.menu.components.Component;
import com.teamlucidic.hax.render.gui.menu.components.Minimap;
import com.teamlucidic.hax.render.gui.menu.components.Window;

public class MapMenu extends Menu {

	public Window window;
	public Minimap map;

	public MapMenu() {
		super("Map");
	}

	public void init() {
		components = new ArrayList<Component>();
		float mod = Display.getWidth() / 920f;
		float mod2 = Display.getHeight() / 605f;
		mod = Math.min(mod, mod2);
		window = new Window(this, (int) (mod * 500), (int) (mod * 500));
		map = new Minimap(this, (int) (mod * 470), (int) (mod * 470));
	}

	public void update() {
		if (!Main.m.input.isDown(Keyboard.KEY_TAB))
			Menu.setMenu(null);
	}

	public void drawMenu() {
		drawRect(0, 0, Display.getWidth(), Display.getHeight(), 0xAF111111);
		drawLabel2();
	}

}
