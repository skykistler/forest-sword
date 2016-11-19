package com.teamlucidic.hax.render.gui.menu.components;

import com.teamlucidic.hax.InputHandler.Trigger;
import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.gui.Font;
import com.teamlucidic.hax.render.gui.menu.Menu;

public class Slot extends Component {
	public static int widthOfSlot = 32 * scale;

	public static int slotscale = scale * 2;

	public SlotItem item;
	public int index;
	public boolean drawBackground;
	public boolean isEmpty = true;
	public String label = "";
	public boolean isQuickSlot;
	public Trigger trigger;
	public int yOff;

	public Slot(Menu m, int x, int y, int i, boolean background) {
		super(m, x, y, widthOfSlot, widthOfSlot);
		index = i;
		drawBackground = background;
	}

	public Slot(Menu m, String lab, int x, int y, int i, boolean background) {
		this(m, x, y, i, background);
		label = lab;
		isQuickSlot = true;
	}

	public void onClick() {
		if (isQuickSlot && !isEmpty) {
			item.invItem.quickSlot = null;
			Main.m.player.inventory.setQuickSlot(label, null);
			setItem(null);
		}
	}

	public void setItem(SlotItem i) {
		item = i;
		if (i == null)
			isEmpty = true;
		else
			isEmpty = false;
	}

	public void drawComponent() {
		if (drawBackground) {
			drawRect(posX, posY + yOff, width, height, 0xBB000000);
		}
		drawFancyBorder(posX, posY + yOff, width, height, 0xFFFFFFFF, slotscale);

		if (label != null && (Main.m.settings.drawSlotLettersOnHud || parentMenu != Menu.currentHud)) {
			int color = isEmpty ? 0xFFFFFFFF : 0x44FFFFFF;
			int fWidth = Font.currentFont.getWidth(label, 2);
			drawString(label, posX + width / 2 - fWidth / 2, posY + 9 * scale + yOff, 2, color);
		}

	}
}
