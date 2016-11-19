package com.teamlucidic.hax.render.gui.menu.components;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.inventory.InventoryItem;
import com.teamlucidic.hax.render.gui.Font;
import com.teamlucidic.hax.render.gui.menu.InventoryMenu;
import com.teamlucidic.hax.render.gui.menu.Menu;

public class SlotItem extends Component {
	public static boolean itemGrabbed;
	public static SlotItem grabbedItem;

	public Slot parentSlot;
	public InventoryItem invItem;
	public boolean grabbed;
	public boolean hidden;

	public SlotItem(Menu m, Slot s, InventoryItem item) {
		super(m, s.posX, s.posY, scale * 16, scale * 16);
		invItem = item;
	}

	public void update() {
		if (!Main.m.input.mouse.isDown(0))
			if (grabbed && grabbedItem == this && itemGrabbed)
				drop();
		super.update();
		if (mouseDown) {
			if (!grabbed && !itemGrabbed)
				grab();
		}

		if (grabbed) {
			posX = Main.m.input.mouse.getX() - width / 2;
			posY = Display.getHeight() - (Main.m.input.mouse.getY() + height / 2);
		} else {
			if (parentSlot != null) {
				posX = parentSlot.posX + 8 * scale;
				posY = parentSlot.posY + 8 * scale;
			}
		}
		if (mouseOver)
			if (invItem.name != null)
				setToolTip(invItem.name + (invItem.type.uses > 0 ? " : " + invItem.usesLeft : ""), invItem.type.requiresMouseButton);
			else
				setToolTip("???", false);

		if (hidden)
			posX = Display.getWidth();
	}

	public void drawComponent() {
		drawTexturedRect(posX, posY, width, height, invItem.icon != null ? invItem.icon : invItem.tex, 0xFFFFFFFF);
		if (invItem.type.uses > 0)
			drawString(invItem.usesLeft + "", posX, posY, 1, 0xFFFFFFFF);

		if (invItem.quickSlot != null) {
			Slot s = ((InventoryMenu) parentMenu).getQuickSlot(invItem.quickSlot);
			drawTexturedRect(s.posX + 8 * scale, s.posY + 8 * scale, width, height, invItem.icon != null ? invItem.icon : invItem.tex, 0xFFFFFFFF);
			int fontWidth = Font.currentFont.getWidth(invItem.quickSlot, 1.4);
			drawBorderedString(invItem.quickSlot, posX + width - fontWidth + 2, posY + height - Font.currentFont.letterHeight - 2, 1.4, 0xFFFFFFFF, 0xFF000000);
			if (invItem.type.uses > 0)
				drawString(invItem.usesLeft + "", s.posX + 8 * scale, s.posY + 8 * scale, 1, 0xFFFFFFFF);
		}
	}

	public void grab() {
		grabbed = itemGrabbed = true;
		grabbedItem = this;
	}

	public void drop() {
		grabbed = false;
		itemGrabbed = false;
		grabbedItem = null;

		Slot s = ((InventoryMenu) parentMenu).getClosestEmptySlot(this, posX + width / 2, posY + height / 2);
		if (s != null) {
			if (s.item == this)
				return;
			InventoryItem copy = invItem.copy();
			if (s.isQuickSlot) {
				InventoryItem oldItem = Main.m.player.inventory.getQuickSlot(s.label);
				if (oldItem != null)
					oldItem.quickSlot = null;
				if (invItem.quickSlot != null) {
					Main.m.player.inventory.setQuickSlot(invItem.quickSlot, null);
					invItem = copy;
					((InventoryMenu) parentMenu).getQuickSlot(invItem.quickSlot).setItem(null);
				}
				Main.m.player.inventory.setQuickSlot(s.label, invItem);
				invItem.quickSlot = s.label;
			} else {
				Main.m.player.inventory.setItem(InventoryMenu.getCurrentCategory(), parentSlot.index, null);
				invItem = copy;
				setParentSlot(s);
				Main.m.player.inventory.setItem(parentSlot.index, invItem);
				if (invItem.quickSlot != null)
					Main.m.player.inventory.setQuickSlot(invItem.quickSlot, invItem);
			}
			s.setItem(this);
		}
	}

	public void setParentSlot(Slot s) {
		if (!s.isQuickSlot) {
			if (parentSlot != null)
				parentSlot.setItem(null);
			parentSlot = s;
		}
	}
}