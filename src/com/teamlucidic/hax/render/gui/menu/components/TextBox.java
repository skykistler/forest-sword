package com.teamlucidic.hax.render.gui.menu.components;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.gui.Font;
import com.teamlucidic.hax.render.gui.menu.Menu;

public class TextBox extends Button {
	public int limit = 22;
	public boolean scroll;
	public String displayLabel = "";

	public TextBox(Menu m, String ctnt, int x, int y, int w, int h) {
		super(m, ctnt, x, y, w, h);
	}

	public TextBox(Menu m, String ctnt, int y, int w, int h) {
		this(m, ctnt, Display.getWidth() / 2 - w / 2, y, w, h);
	}

	public void update() {
		super.update();
		if (displayLabel.length() > label.length() || (displayLabel.length() == 0 && label.length() > 0))
			displayLabel = label;
		if (isFocused() && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Main.m.input.checkKey(Keyboard.KEY_V))
			addToLabel(getClipboardContent());
	}

	public void drawComponent() {
		drawRect(posX + borderSize, posY + borderSize, width - borderSize, height - borderSize, 0xFF000000);
		if (!enabled)
			drawRect(posX + borderSize, posY + borderSize, width - borderSize, height - borderSize, 0xFF555555);
		drawBorder();
		String cursor = isFocused() ? parentMenu.menuTicks % 30 < 10 ? "_" : "" : "";
		//posX + (width / 2) - (Font.currentFont.getWidth(label, fontSize) / 2)
		drawShadowedString(displayLabel + cursor, posX + borderSize + 2, posY + ((height - borderSize * 2) / 2) - (int) ((Font.currentFont.letterHeight / 2) * fontSize), fontSize, enabled ? 0xFFFFFFFF : 0xFFBBBBBB);
	}

	public void keyTyped(char c) {
		if (isFocused()) {
			if (c != '\r')
				if (c == '\b' || c == 0x7f) {
					if (label.length() != 0)
						setLabel(label.substring(0, label.length() - 1));
				} else {
					if (label.length() < limit && Font.legalChars.indexOf(c) > -1)
						addToLabel(c + "");
				}
		}
	}

	public void setLabel(String s) {
		label = "";
		addToLabel(s);
	}

	public void addToLabel(String s) {
		if (label.length() < limit)
			label += s;

		if (label.length() > limit)
			label = label.substring(0, limit);
		applyScroll();
	}

	public void applyScroll() {
		displayLabel = label;
		while (scroll && Font.currentFont.getWidth(displayLabel, fontSize) > width - Font.currentFont.letterWidth * 2 - 5)
			displayLabel = displayLabel.substring(1, displayLabel.length());
	}

	public void drawBorder() {
		int color = 0xFFBBBBBB;
		drawRect(posX, posY, borderSize, height, color);
		drawRect(posX, posY, width, borderSize, color);
		drawRect(posX + width - borderSize, posY, borderSize, height, color);
		drawRect(posX, posY + height - borderSize, width, borderSize, color);
	}

	public String getClipboardContent() {
		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

		try {
			if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				String text = (String) t.getTransferData(DataFlavor.stringFlavor);
				return text;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}
}
