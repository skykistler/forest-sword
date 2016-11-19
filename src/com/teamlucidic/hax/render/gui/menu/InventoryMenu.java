package com.teamlucidic.hax.render.gui.menu;

import java.util.ArrayList;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.inventory.Inventory;
import com.teamlucidic.hax.entity.inventory.InventoryItem;
import com.teamlucidic.hax.entity.living.Player;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.menu.components.Button;
import com.teamlucidic.hax.render.gui.menu.components.Component;
import com.teamlucidic.hax.render.gui.menu.components.Slot;
import com.teamlucidic.hax.render.gui.menu.components.SlotItem;
import com.teamlucidic.hax.render.gui.menu.components.Window;

/*The whole InventoryMenu GUI system is very messy and unorganized and just generally not coded well. I apologize if you're trying to modify it, and I will try to make a new one soon.*/
public class InventoryMenu extends Menu {
	public static String top = "1 2 3 4";
	public static String bottom = "Sh Z X C";
	public static String acceptableKeys = top + " " + bottom + " RB LB";
	public static String[] topRow = top.split(" ");
	public static String[] bottomRow = bottom.split(" ");
	public static int currentTab = 0;

	public Window window;
	public int rows = 5;
	public int columns = 8;
	public Slot[] itemCat = new Slot[Inventory.size];
	public Slot[] weaponCat = new Slot[Inventory.size];
	public Slot[] apparelCat = new Slot[Inventory.size];
	public Slot[] abiliCat = new Slot[Inventory.size];
	public Slot[] editCat = new Slot[Inventory.size];
	public Slot[] quickSlots = null;
	public Button lrSwitch;

	public InventoryMenu() {
		super("Inventory");
	}

	public void init() {
		components = new ArrayList<Component>();
		window = new Window(this, 700, 500);
		window.tabbed = true;
		window.tabs = new String[5];
		window.tabs[0] = "Items";
		window.tabs[1] = "Weapons";
		window.tabs[2] = "Apparel";
		window.tabs[3] = "Abilities";
		window.tabs[4] = "Editor";
		window.currentTab = currentTab;

		lrSwitch = new Button(this, "", getSlotYOnGrid(5) + 20, 32, 128) {
			public void drawComponent() {
				Texture.getTexture("gui/switch.png").bind();
				Main.m.modeler.setHexColor(0xFFFFFFFF);
				Main.m.modeler.drawRect2D(posX, posY, width, height);
			}
		};
		lrSwitch.posX = getSlotXOnGrid(5) + 70;

		setItems(Main.m.player.inventory.items, itemCat);
		setItems(Main.m.player.inventory.weapons, weaponCat);
		setItems(Main.m.player.inventory.apparel, apparelCat);
		setItems(Main.m.player.inventory.abilityItems, abiliCat);
		setItems(Main.m.player.inventory.editorItems, editCat);

		setQuickSlots();
	}

	public void update() {
		if (window.currentTab != currentTab) {
			currentTab = window.currentTab;
			for (int i = 0; i < window.tabs.length; i++)
				updateHidden(getCategorySlots(i));
		}
	}

	public void updateHidden(Slot[] slots) {
		if (slots == null)
			return;
		boolean hidden = slots != getCurrentCategorySlots();
		for (int i = 0; i < slots.length; i++)
			if (slots[i].item != null)
				slots[i].item.hidden = hidden;
	}

	public void drawMenu() {
		drawRect(0, 0, Display.getWidth(), Display.getHeight(), 0xAF111111);
	}

	public void postComponentDraw() {
		Main.m.modeler.setOffset(32, 0, 0);
		drawString("$", window.posX + window.width - 201, window.posY + window.height - 133, 2, 0xFF009900);
		drawString(Main.m.player.inventory.vitri + "", window.posX + window.width - 175, window.posY + window.height - 133, 2, 0xFFFFFFFF);

		drawString("H", window.posX + window.width - 200, window.posY + window.height - 93, 2, 0xFF000099);
		drawString((int) Main.m.player.health + "/" + Main.m.player.maxHealth, window.posX + window.width - 175, window.posY + window.height - 93, 2, 0xFFFFFFFF);

		drawString("E", window.posX + window.width - 201, window.posY + window.height - 50, 2, 0xFF990000);
		drawString((int) Main.m.player.energy + "/" + Main.m.player.inventory.abilities.maxEnergy, window.posX + window.width - 175, window.posY + window.height - 50, 2, 0xFFFFFFFF);

		Main.m.modeler.setOffset(0, 0, 0);
	}

	public void componentAction(Component com) {
		if (com instanceof Slot)
			((Slot) com).onClick();

		if (com == lrSwitch) {
			Main.m.player.inventory.switchLandR();
			setQuickSlots();
		}
	}

	public void setItems(InventoryItem[] collection, Slot[] slots) {
		if (slots == null || collection == null)
			return;
		//		slots = new Slot[Inventory.size];
		for (int x = 0; x < columns; x++)
			for (int y = 0; y < rows; y++)
				slots[x + y * columns] = new Slot(this, getSlotXOnGrid(x), getSlotYOnGrid(y), x + y * columns, false);

		if (Main.m.player != null) {
			for (int i = 0; i < Inventory.size; i++) {
				Slot slot = slots[i];
				InventoryItem invItem = Main.m.player.inventory.getItem(collection, i);
				if (invItem != null) {
					SlotItem item = new SlotItem(this, slot, invItem);
					item.setParentSlot(slot);
					slot.setItem(item);
				}
			}
		}

		updateHidden(slots);
	}

	public void setQuickSlots() {
		if (quickSlots != null)
			for (Slot s : quickSlots)
				remove(s);
		quickSlots = new Slot[10];
		for (int y = 0; y < 2; y++) {
			for (int i = 0; i < 4; i++) {
				String label = y < 1 ? topRow[i] : bottomRow[i];
				quickSlots[i + y * 4] = new Slot(this, label, getSlotXOnGrid(i), getSlotYOnGrid(y + 5) + 20, -1, false);
			}
		}

		quickSlots[8] = new Slot(this, "RB", getSlotXOnGrid(5), getSlotYOnGrid(5) + 20, -1, false);
		quickSlots[9] = new Slot(this, "LB", getSlotXOnGrid(5), getSlotYOnGrid(6) + 20, -1, false);

		String[] keys = acceptableKeys.split(" ");
		for (int i = 0; i < keys.length; i++) {
			Slot slot = quickSlots[i];
			InventoryItem invItem = Main.m.player.inventory.getQuickSlot(keys[i]);
			if (invItem != null) {
				SlotItem item = getMatchingSlots(Main.m.player.inventory.getCategory(invItem.type.type))[invItem.index].item;
				item.invItem.quickSlot = keys[i];
				slot.setItem(item);
				Player.savedQuickslots[i] = item.invItem.name;
			}
		}
	}

	public Slot getClosestEmptySlot(SlotItem item, int x, int y) {
		double smallestDistance = Double.MAX_VALUE;
		Slot result = null;
		for (int i = 0; i < Inventory.size + 10; i++) {
			Slot target = null;
			if (i < Inventory.size)
				target = getCurrentCategorySlots()[i];
			else
				target = quickSlots[i - Inventory.size];

			if (target == null)
				continue;
			double disX = (target.posX + target.width / 2) - x;
			double disY = (target.posY + target.height / 2) - y;
			double dis = disX * disX + disY * disY;
			if (dis < smallestDistance && (target.isEmpty || target.isQuickSlot || target == item.parentSlot)) {
				result = target;
				smallestDistance = dis;
			}
		}
		return result;
	}

	public Slot getQuickSlot(String label) {
		for (int i = 0; i < 10; i++) {
			Slot slot = quickSlots[i];
			if (slot.isQuickSlot && label.equals(slot.label))
				return slot;
		}
		return null;
	}

	public void setQuickSlot(String label, SlotItem item) {
		Slot slot = getQuickSlot(label);
		if (slot != null)
			slot.setItem(item);
	}

	public int getSlotXOnGrid(int x) {
		return x * Slot.widthOfSlot + window.posX + (window.width - Slot.widthOfSlot * 8) / 2;
	}

	public int getSlotYOnGrid(int y) {
		return y * Slot.widthOfSlot + window.posY + 10 * Window.scale;
	}

	public Slot[] getCurrentCategorySlots() {
		return getCategorySlots(currentTab);
	}

	public Slot[] getCategorySlots(int i) {
		switch (i) {
		case 0:
			return itemCat;
		case 1:
			return weaponCat;
		case 2:
			return apparelCat;
		case 3:
			return abiliCat;
		case 4:
			return editCat;
		default:
			return null;
		}
	}

	public Slot[] getMatchingSlots(InventoryItem[] cat) {
		if (cat == Main.m.player.inventory.items)
			return itemCat;
		if (cat == Main.m.player.inventory.weapons)
			return weaponCat;
		if (cat == Main.m.player.inventory.apparel)
			return apparelCat;
		if (cat == Main.m.player.inventory.abilityItems)
			return abiliCat;
		if (cat == Main.m.player.inventory.editorItems)
			return editCat;
		return null;
	}

	public static InventoryItem[] getCurrentCategory() {
		switch (currentTab) {
		case 0:
			return Main.m.player.inventory.items;
		case 1:
			return Main.m.player.inventory.weapons;
		case 2:
			return Main.m.player.inventory.apparel;
		case 3:
			return Main.m.player.inventory.abilityItems;
		case 4:
			return Main.m.player.inventory.editorItems;
		default:
			return null;
		}
	}

}
