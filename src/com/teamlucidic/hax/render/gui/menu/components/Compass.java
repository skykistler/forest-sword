package com.teamlucidic.hax.render.gui.menu.components;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.AABB;
import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.Font;
import com.teamlucidic.hax.render.gui.menu.Menu;

public class Compass extends Component {

	public int w, h;

	public Compass(Menu m, int y) {
		super(m, Display.getWidth() / 2, y, 400, 28);
		w = Display.getWidth();
		h = Display.getHeight();
	}

	public void update() {
		w = Display.getWidth();
		h = Display.getHeight();
	}

	public void drawComponent() {
		int borderWidth = 3;
		this.drawRect(w / 2 - width / 2, posY + borderWidth, width, height, 0xAA000000);

		drawLetter("W", 0);
		drawLetter("S", 90);
		drawLetter("E", 180);
		drawLetter("N", 270);

		for (int i = 0; i < 4; i++)
			drawDial(15, 45 + 90 * i);

		for (int i = 0; i < 8; i++)
			drawDial(7, 23 + i * 45);

		if (Main.m.player.currentQuest != null) {
			AABB targetArea = Main.m.player.currentQuest.getCurrentObjective().targetArea;
			if (targetArea != null)
				drawTexture(Texture.getTexture("gui/waypoint.png"), getRotation((targetArea.minX + targetArea.maxX) / 2, (targetArea.minZ + targetArea.maxZ) / 2), 1.3);
		}

		//		if (Main.m.player.enemyList.size() > 0)
		//			for (Living enemy : Main.m.player.enemyList)
		//				drawTexture(Texture.getTexture("gui/enemy.png"), getRotation(enemy.posX, enemy.posZ), 1.2);

		if (Main.m.player.targetLiving != null && Main.m.player.targetLiving.isEnemy())
			drawTexture(Texture.getTexture("gui/enemy.png"), getRotation(Main.m.player.targetLiving.posX, Main.m.player.targetLiving.posZ), 1.2);

		drawFancyBorder(w / 2 - width / 2 - borderWidth, posY, width + borderWidth * 2, height + borderWidth * 2, 0xFFFFFFFF, 2);
	}

	public void drawLetter(String letter, int rotation) {
		rotation %= 360;
		int offset = (int) ((Main.m.camera.rotY + rotation));
		offset %= 360;
		int xPos = (int) (offset / 360d * w) - Font.currentFont.letterWidth;
		if (w - xPos < w / 2 + width / 2 - Font.currentFont.letterWidth * 2 && w - xPos > w / 2 - width / 2)
			drawString(letter, w - xPos, posY + 2, 2, 0xFFFFFFFF);
	}

	public void drawDial(int dialHeight, int rotation) {
		rotation %= 360;
		int offset = (int) ((Main.m.camera.rotY + rotation));
		offset %= 360;
		int xPos = (int) (offset / 360d * w) - 1;
		if (w - xPos + 3 < w / 2 + width / 2 && w - xPos > w / 2 - width / 2)
			drawRect(w - xPos, posY + height - dialHeight, 3, dialHeight, 0xFFFFFFFF);
	}

	public void drawTexture(Texture t, int rotation, double scale) {
		rotation += 180;
		int offset = (int) ((Main.m.camera.rotY + rotation));
		offset %= 360;
		int size = (int) ((height - 8) * scale);
		int xPos = (int) (offset / 360d * w) - size / 2;
		if (w - xPos + size / 2 < w / 2 + width / 2 && w - xPos - size / 2 > w / 2 - width / 2)
			drawTexturedRect(w - xPos - size / 2, (posY + height / 2) - size / 2 + 3, size, size, t, 0xFFFFFFFF);
	}

	public int getRotation(double x, double z) {
		//		return 0;
		return (int) Render.atan2Deg((Main.m.camera.posX - x), (Main.m.camera.posZ - z));
		//		return (int) Math.toDegrees(Math.atan2((Main.m.camera.posX - x), (Main.m.camera.posZ - z)));
	}
}
