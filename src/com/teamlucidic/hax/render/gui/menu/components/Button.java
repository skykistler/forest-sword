package com.teamlucidic.hax.render.gui.menu.components;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.gui.Font;
import com.teamlucidic.hax.render.gui.menu.Menu;

public class Button extends Component {
	public String label;

	public Button(Menu m, String lab, int x, int y, int w, int h) {
		super(m, x, y, w, h);
		label = lab;
	}

	public Button(Menu m, String lab, int y, int w, int h) {
		super(m, Display.getWidth() / 2 - w / 2, y, w, h);
		label = lab;
		fontSize = h / (Font.currentFont.letterHeight);
	}

	public void drawComponent() {
		//		drawRect(posX + borderSize, posY + borderSize, width - borderSize, height - borderSize, 0xFF222222);
		Main.m.modeler.setHexColor(0xFFDDDDDD);
		drawWindowRect(posX, posY, width, height, 2);
		if (mouseOver && !mouseClicked && enabled)
			drawRect(posX, posY, width, height, 0x66333333);
		if (mouseClicked && enabled)
			drawRect(posX, posY, width, height, 0x66111111);
		if (!enabled)
			drawRect(posX, posY, width, height, 0x66555555);
		//		drawBorder();
		drawShadowedString(label, posX + (width / 2) - (Font.currentFont.getWidth(label, fontSize) / 2), posY + ((height - borderSize * 2) / 2) - (int) ((Font.currentFont.letterHeight / 2) * fontSize), fontSize, enabled ? 0xFFFFFFFF : 0xFFBBBBBB);
	}

}
