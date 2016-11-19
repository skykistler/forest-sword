package com.teamlucidic.hax.render.gui.menu;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.Font;
import com.teamlucidic.hax.render.gui.Gui;
import com.teamlucidic.hax.render.gui.menu.components.Button;
import com.teamlucidic.hax.render.gui.menu.components.Component;
import com.teamlucidic.hax.render.gui.menu.components.Component.ToolTip;
import com.teamlucidic.hax.render.gui.menu.components.Notification;
import com.teamlucidic.hax.render.gui.menu.components.SlotItem;

public class Menu extends Gui {
	public static Menu currentMenu;
	public static Menu currentHud;

	public String title;
	public Font font;
	public int menuTicks;
	public int initDelay = 2;
	public int titleSize = 6;
	public boolean show = true;
	public Component focus;
	public ArrayList<Component> components = new ArrayList<Component>();
	public ToolTip currentToolTip;
	public boolean pauseMenu;

	public Menu(String name) {
		title = name;
		font = new Font("Fixedsys");
		Font.setFont(font);
	}

	public void init() {

	}

	public void update() {

	}

	public void drawMenu() {
		drawBackground(1);
		drawLabel();
	}

	public void postComponentDraw() {

	}

	public void drawBackground(int bg) {
		Texture.getTexture("gui/bg" + bg + ".png").bind();
		Main.m.modeler.start();
		Main.m.modeler.addVertexTexCol(0, 0, 0, 0, 0, 0xFFFFFFFF);
		Main.m.modeler.addVertexTexCol(0, Display.getHeight(), 0, 0, 3.5F, 0xFF222222);
		Main.m.modeler.addVertexTexCol(Display.getWidth(), Display.getHeight(), 0, 4.0F, 3.5F, 0xFF222222);
		Main.m.modeler.addVertexTexCol(Display.getWidth(), 0, 0, 4.0F, 0, 0xFFFFFFFF);
		Main.m.modeler.finish();
		//		drawRect(0, 0, Display.getWidth(), Display.getHeight(), 0xFFDDCC88);
	}

	public void drawLabel() {
		drawBorderedString(title, 5, (int) (Display.getHeight() - Font.currentFont.letterHeight * 2.1), 2, 0xFF00BB00, 0xFF000000);
	}

	public void drawLabel2() {
		drawShadowedString(title, 10, (int) (Display.getHeight() - Font.currentFont.letterHeight * 6.5), 6, 0xFFFFFFFF);
	}

	public void drawDebug() {
		String curTicksFPS = Main.m.ticksps + " updates, " + Main.m.fps + " fps";
		drawBorderedString(curTicksFPS, Display.getWidth() - Font.currentFont.getWidth(curTicksFPS, 2), 0, 2, 0xFFFFFFFF, 0xFF000000);

		if (Main.m.player != null) {
			drawBorderedString("X: " + (int) Main.m.player.posX, 2, 0, 2, 0xFFFFFFFF, 0xFF000000);
			drawBorderedString("Y: " + (int) Main.m.player.posY, 2, 25, 2, 0xFFFFFFFF, 0xFF000000);
			drawBorderedString("Z: " + (int) Main.m.player.posZ, 2, 50, 2, 0xFFFFFFFF, 0xFF000000);
		}
	}

	public void add(Component component) {
		components.add(component);
	}

	public void remove(Component component) {
		components.remove(component);
	}

	public void keyTyped(char c) {
		if (currentHud != null && currentHud.components != null)
			for (int i = 0; (currentHud != null) && i < currentHud.components.size(); i++)
				currentHud.components.get(i).keyTyped(c);
		if (currentMenu != null && currentMenu.components != null)
			for (int i = 0; (currentMenu != null) && i < currentMenu.components.size(); i++)
				currentMenu.components.get(i).keyTyped(c);
	}

	public void componentAction(Component com) {

	}

	public void showNotification(Texture ico, String title, String desc) {
		add(new Notification(this, ico, title, desc, Notification.defaultTimer));
	}

	public static void updateCurrentMenu() {
		if (currentHud != null) {
			currentHud.update();
			if (currentHud != null) {
				currentHud.currentToolTip = null;
				for (int i = 0; (currentHud != null) && i < currentHud.components.size(); i++)
					if (currentHud != null && currentHud.components != null && currentHud.components.size() > i) {
						Component comp = currentHud.components.get(i);
						comp.update();
						if (comp.dead)
							currentHud.remove(comp);
					}
			}
			if (currentHud != null)
				currentHud.menuTicks++;

		}
		if (currentMenu != null) {
			currentMenu.update();
			if (currentMenu != null) {
				currentMenu.currentToolTip = null;
				for (int i = 0; (currentMenu != null) && i < currentMenu.components.size(); i++)
					if (currentMenu != null && currentMenu.components != null && currentMenu.components.size() > i) {
						Component comp = currentMenu.components.get(i);
						if (currentMenu.menuTicks > currentMenu.initDelay || !(comp instanceof Button))
							comp.update();
						if (comp.dead)
							currentMenu.remove(comp);
					}
			}
			if (currentMenu != null)
				currentMenu.menuTicks++;
		}
	}

	public static void drawCurrentMenu() {
		if (currentHud != null && currentHud.show) {
			currentHud.drawMenu();
			for (Component component : currentHud.components)
				component.drawComponent();
			currentHud.postComponentDraw();
			if (currentHud.currentToolTip != null)
				currentHud.currentToolTip.draw();
		}
		if (currentMenu != null && currentMenu.show) {
			currentMenu.drawMenu();
			for (Component component : currentMenu.components) {
				component.drawComponent();
				if (SlotItem.grabbedItem != null)
					SlotItem.grabbedItem.drawComponent();
			}
			currentMenu.postComponentDraw();
			if (currentMenu.currentToolTip != null)
				currentMenu.currentToolTip.draw();
		}
	}

	public static void setMenu(Menu menu) {
		currentMenu = menu;
		if (currentMenu != null) {
			Keyboard.enableRepeatEvents(true);
			Mouse.setGrabbed(false);
			currentMenu.init();
			drawCurrentMenu();
		}
		if (currentMenu == null) {
			Keyboard.enableRepeatEvents(false);
			Mouse.setGrabbed(true);
		}

	}

	public static void setHud(Menu menu) {
		currentHud = menu;
		if (currentHud != null) {
			currentHud.init();
		}
	}

	public static void keyEvent(char c) {
		if (currentHud != null)
			currentHud.keyTyped(c);
		else if (currentMenu != null)
			currentMenu.keyTyped(c);
	}

}
