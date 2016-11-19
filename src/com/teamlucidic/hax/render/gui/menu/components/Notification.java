package com.teamlucidic.hax.render.gui.menu.components;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.Font;
import com.teamlucidic.hax.render.gui.menu.Menu;

public class Notification extends Component {
	public static int defaultTimer = 360;
	public static int defaultWidth = 260;
	public static int defaultHeight = 100;

	public Texture icon = Texture.getTexture("gui/unknown.png");
	public String title = "";
	public String description = "";
	public int timer = defaultTimer;

	public Notification(Menu m, Texture ico, String label, String desc, int timeout) {
		super(m, Display.getWidth() - defaultWidth - 15, Display.getHeight(), defaultWidth, defaultHeight);
		if (ico != null)
			icon = ico;
		if (label != null)
			setTitle(label);
		if (desc != null)
			setDescription(desc);
		timer = timeout;
	}

	public void update() {
		int speed = 4;
		if (timer != 0) {
			if (posY != Display.getHeight() - height)
				posY -= speed;
			else
				timer--;
		} else
			posY += speed;
		if (posY > Display.getHeight())
			dead = true;
	}

	public void drawComponent() {
		int size = 10;
		drawWindowRect(posX - size, posY - size, width + size, height + size + 2, 2);

		drawTexturedRect(posX + size + 5, posY + size + 5, width / 4, width / 4, icon);
		drawBorderedString(title, posX + size + width / 4 + 10, posY + size + 5, 1, 0xFFFFFFFF, 0xFF111111);
		drawShadowedString(description, posX + size + width / 4 + 2, posY + size + 20, 1, 0xFFFFFFFF);
	}

	public void setTitle(String t) {
		title = t.length() > 21 ? t.substring(0, 21) : t;
	}

	public void setDescription(String desc) {
		description = Font.currentFont.wordWrap(desc, width * 2 / 3, 1);
	}
}
