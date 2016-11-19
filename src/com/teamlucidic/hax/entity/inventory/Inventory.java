package com.teamlucidic.hax.entity.inventory;

import java.util.HashMap;

import org.lwjgl.input.Keyboard;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.BlockType;
import com.teamlucidic.hax.entity.inventory.item.AutoWalk;
import com.teamlucidic.hax.entity.inventory.item.BlockPlacer;
import com.teamlucidic.hax.entity.inventory.item.Crouch;
import com.teamlucidic.hax.entity.inventory.item.Editor;
import com.teamlucidic.hax.entity.inventory.item.EntitySpawner;
import com.teamlucidic.hax.entity.inventory.item.Hammer;
import com.teamlucidic.hax.entity.inventory.item.HoverFeather;
import com.teamlucidic.hax.entity.inventory.item.Paintbrush;
import com.teamlucidic.hax.entity.inventory.item.Sprint;
import com.teamlucidic.hax.entity.inventory.item.Telescope;
import com.teamlucidic.hax.entity.inventory.item.TheCure;
import com.teamlucidic.hax.entity.living.Living;
import com.teamlucidic.hax.entity.living.Player;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.menu.Hud;
import com.teamlucidic.hax.render.gui.menu.InventoryMenu;
import com.teamlucidic.hax.render.gui.menu.Menu;

public class Inventory {
	public static HashMap<String, Integer> quickSlotKeys = new HashMap<String, Integer>();
	public static final int size = 40;

	public Living owner;
	public InventoryItem[] items;
	public InventoryItem[] weapons;
	public InventoryItem[] apparel;
	public InventoryItem[] abilityItems;
	public InventoryItem[] editorItems;
	public Abilities abilities;
	public HashMap<Integer, InventoryItem> quickSlots;

	public int vitri = 0;

	public boolean drawn;
	public boolean undrawing;
	public int drawTimer;
	public boolean justSwitchedTop;
	public boolean justSwitchedBottom;
	public boolean justPunchedRight;
	public boolean justPunchedLeft;
	public String justUsedLeft = "";
	public String justUsedRight = "";
	public boolean justUnPaused;

	public Inventory(Living living) {
		owner = living;
		clear();

		if (owner instanceof Player) {
			if (Main.m.gameMode.contains("freeplay")) {
				for (InventoryItemType type : Main.m.invItemLoader.itemTypes.values())
					if (!type.usesExtraCode) {
						addItem(type);
						if ((":Sword:Fire Blast:Bubble Blast:").contains(type.name))
							addItem(type);
					}
				addItem("Hover Feather");
				addItem("Telescope");
				for (String s : Main.m.livingLoader.livLoaded)
					addItem(new EntitySpawner(this, s));
				addItem("Editor");
				addItem("Hammer");
				addItem("Paintbrush");
				for (BlockType t : Main.m.blockLoader.blockTypesById.values()) {
					if (!t.name.equals("Bedrock") && !t.name.equals("Torch"))
						addItem(new BlockPlacer(this, t));
				}
			}

			quickSlots = new HashMap<Integer, InventoryItem>();
		}
	}

	public void update() {
		if (drawTimer > 0 && !undrawing)
			drawTimer--;

		if (undrawing) {
			drawTimer++;
			if (drawTimer > 7) {
				undrawing = false;
				drawn = false;
			}
		}

		if (Menu.currentMenu != null || owner.dead || !(owner instanceof Player))
			return;

		checkQuickSlots();

		if (owner instanceof Player) {
			Hud.setHand("RB", Texture.getTexture("gui/fist.png"));
			Hud.setHand("LB", Texture.getTexture("gui/fist.png"));
			Main.m.player.targetBlock = null;
			Main.m.player.lightSurroundings = false;
		}

		if (justSwitchedTop && !topRowDown())
			justSwitchedTop = false;
		if (justSwitchedBottom && !bottomRowDown())
			justSwitchedBottom = false;

		if (Main.m.input.mouse.isDown(quickSlotKeys.get("RB")))
			use("RB");
		else
			justPunchedRight = false;

		if (Main.m.input.mouse.isDown(quickSlotKeys.get("LB"))) {
			if (!justUnPaused)
				use("LB");
		} else {
			justPunchedLeft = false;
			justUnPaused = false;
		}

		if (Main.m.input.isDown(quickSlotKeys.get("1")))
			use("1");
		else if (justUsedRight.equals("1"))
			justUsedRight = "";

		if (Main.m.input.isDown(quickSlotKeys.get("2")))
			use("2");
		else if (justUsedRight.equals("2"))
			justUsedRight = "";
		if (Main.m.input.isDown(quickSlotKeys.get("3")))
			use("3");
		else if (justUsedRight.equals("3"))
			justUsedRight = "";
		if (Main.m.input.isDown(quickSlotKeys.get("4")))
			use("4");
		else if (justUsedRight.equals("4"))
			justUsedRight = "";

		if (Main.m.input.isDown(quickSlotKeys.get("Sh")))
			use("Sh");
		else if (justUsedRight.equals("Sh"))
			justUsedLeft = "";
		if (Main.m.input.isDown(quickSlotKeys.get("Z")))
			use("Z");
		else if (justUsedRight.equals("Z"))
			justUsedLeft = "";
		if (Main.m.input.isDown(quickSlotKeys.get("X")))
			use("X");
		else if (justUsedRight.equals("X"))
			justUsedLeft = "";
		if (Main.m.input.isDown(quickSlotKeys.get("C")))
			use("C");
		else if (justUsedRight.equals("C"))
			justUsedLeft = "";

		for (int i = 0; i < items.length; i++) {
			InventoryItem item = items[i];
			if (item != null && !justUnPaused)
				item.update();
		}
		for (int i = 0; i < weapons.length; i++) {
			InventoryItem item = weapons[i];
			if (item != null && !justUnPaused)
				item.update();
		}
		for (int i = 0; i < apparel.length; i++) {
			InventoryItem item = apparel[i];
			if (item != null && !justUnPaused)
				item.update();
		}
		for (int i = 0; i < abilityItems.length; i++) {
			InventoryItem item = abilityItems[i];
			if (item != null && !justUnPaused)
				item.update();
		}
		for (int i = 0; i < editorItems.length; i++) {
			InventoryItem item = editorItems[i];
			if (item != null && !justUnPaused)
				item.update();
		}

		vitri = vitri > 999999999 ? 999999999 : vitri;
	}

	public void render() {
		InventoryItem rb = getQuickSlot("RB");
		if (rb != null)
			rb.renderInHand();
		InventoryItem lb = getQuickSlot("LB");
		if (lb != null)
			lb.renderInHand();
	}

	public void clear() {
		items = new InventoryItem[size];
		weapons = new InventoryItem[size];
		apparel = new InventoryItem[size];
		abilityItems = new InventoryItem[size];
		editorItems = new InventoryItem[size];
		vitri = 0;
		abilities = new Abilities(this);
	}

	public void addVitri(int amount) {
		vitri += amount;
	}

	public void deductVitri(int amount) {
		addVitri(-amount);
	}

	public InventoryItem addItem(String s) {
		if (s != null)
			return addItem(Main.m.invItemLoader.getInvItemType(s));
		return null;
	}

	public InventoryItem addItem(InventoryItemType type) {
		if (type != null) {
			if (type.usesExtraCode) {
				if (type.name.equals("AutoWalk"))
					return addItem(new AutoWalk(this));
				if (type.name.equals("Block Placer"))
					return addItem(new BlockPlacer(this, Main.m.blockLoader.blockTypesById.get(2)));
				if (type.name.equals("Crouch"))
					return addItem(new Crouch(this));
				if (type.name.equals("Editor"))
					return addItem(new Editor(this));
				if (type.name.equals("Entity Spawner"))
					return addItem(new EntitySpawner(this, Main.m.livingLoader.livLoaded.get(0)));
				if (type.name.equals("Hover Feather"))
					return addItem(new HoverFeather(this));
				if (type.name.equals("Sprint"))
					return addItem(new Sprint(this));
				if (type.name.equals("Telescope"))
					return addItem(new Telescope(this));
				if (type.name.equals("The Cure"))
					return addItem(new TheCure(this));
				if (type.name.equals("Hammer"))
					return addItem(new Hammer(this));
				if (type.name.equals("Paintbrush"))
					return addItem(new Paintbrush(this));
			} else
				return addItem(type.getNewItem(this));
		}
		return null;
	}

	public InventoryItem addItem(InventoryItem item) {
		setItem(getFirstEmptySlot(getCategory(item.type.type)), item);
		item.removed = false;
		if (Menu.currentMenu instanceof InventoryMenu)
			Menu.currentMenu.init();
		return item;
	}

	public InventoryItem[] getCategory(String type) {
		if (type.startsWith("usab") || type.startsWith("collec"))
			return items;
		if (type.startsWith("weap"))
			return weapons;
		if (type.startsWith("appa"))
			return apparel;
		if (type.startsWith("abil") || type.startsWith("spell"))
			return abilityItems;
		if (type.startsWith("edit"))
			return editorItems;
		System.out.println("No item category for: " + type);
		return null;
	}

	public int getFirstEmptySlot(InventoryItem[] collection) {
		for (int i = 0; i < size; i++)
			if (getItem(collection, i) == null)
				return i;
		return size;
	}

	public void setItem(int index, InventoryItem item) {
		setItem(getCategory(item.type.type), index, item);
	}

	public void setItem(InventoryItem[] collection, int index, InventoryItem item) {
		if (collection == null) {
			System.out.println("Unable to set " + item.name + " to " + index);
			return;
		}
		if (index < size) {
			collection[index] = item;
			if (item != null)
				item.index = index;
		}
	}

	public InventoryItem getItem(InventoryItem[] collection, int index) {
		if (collection != null && index >= 0 && index < collection.length)
			return collection[index];
		return null;
	}

	public void removeItem(InventoryItem item) {
		if (item.quickSlot != null)
			setQuickSlot(item.quickSlot, null);
		setItem(getCategory(item.type.type), item.index, null);
		item.removed = true;
		if (Menu.currentMenu instanceof InventoryMenu)
			Menu.currentMenu.init();
	}

	public void checkQuickSlots() {
		String[] keys = InventoryMenu.acceptableKeys.split(" ");
		for (int i = 0; i < keys.length; i++) {
			InventoryItem item = getQuickSlot(keys[i]);
			if (item != null) {
				item.quickSlot = keys[i];
				setItem(item.index, item);
			}
		}
	}

	public void setQuickSlot(String key, InventoryItem item) {
		if (quickSlotKeys.containsKey(key)) {
			quickSlots.put(quickSlotKeys.get(key), item);
			if (item != null && item.type != null && item.type.lightBehavior > 0)
				owner.lightSurroundings = false;
		}
	}

	public InventoryItem getQuickSlot(String key) {
		if (quickSlotKeys.containsKey(key))
			return quickSlots.get(quickSlotKeys.get(key));
		return null;
	}

	public void use(String key) {
		InventoryItem item = getQuickSlot(key);
		boolean top = InventoryMenu.top.contains(key) || key.equals("RB");
		boolean bottom = InventoryMenu.bottom.contains(key) || key.equals("LB");
		if (item != null) {
			if (keyIsMouseButton(key) && !drawn && drawTimer == 0 && item.type.requiresDraw)
				draw();
			if (!item.type.requiresMouseButton || keyIsMouseButton(key)) {
				if (drawTimer == 0 && !undrawing && ((top && !justSwitchedTop) || (bottom && !justSwitchedBottom)))
					item.onUse();
				if (item.removed) {
					if (top)
						justUsedRight = key;
					else if (bottom)
						justUsedLeft = key;
				}
			} else {
				switchItem(item);
			}
		} else if (keyIsMouseButton(key) && !drawn && drawTimer == 0)
			draw();
		else if (!justUsedLeft.equals(key) && !justUsedRight.equals(key))
			switchEmpty(key);

		if (drawn && drawTimer == 0 && item == null && keyIsMouseButton(key)) {
			punch(key);
		}
	}

	public void switchItem(InventoryItem item) {
		String key = item.quickSlot;
		boolean top = InventoryMenu.top.contains(key);
		boolean bottom = InventoryMenu.bottom.contains(key);
		if ((justSwitchedTop && top) || (justSwitchedBottom && bottom))
			return;
		if (top)
			justSwitchedTop = true;
		else if (bottom)
			justSwitchedBottom = true;
		String buttonToGo = top ? "RB" : "LB";
		item.quickSlot = buttonToGo;

		InventoryItem toSwitch = getQuickSlot(key).copy();
		InventoryItem current = getQuickSlot(buttonToGo);
		if (current != null) {
			current.quickSlot = key;
			current = current.copy();
		}
		setQuickSlot(key, current);
		setQuickSlot(buttonToGo, toSwitch);
	}

	public void switchLandR() {
		InventoryItem rb = getQuickSlot("RB");
		InventoryItem lb = getQuickSlot("LB");

		if (rb != null) {
			rb.quickSlot = "LB";
			rb = rb.copy();
		}
		if (lb != null) {
			lb.quickSlot = "RB";
			lb = lb.copy();
		}
		setQuickSlot("RB", lb);
		setQuickSlot("LB", rb);
	}

	public void switchEmpty(String key) {
		if (keyIsMouseButton(key) || (key.equals("Sh") && owner == Main.m.player && Main.m.player.flying))
			return;
		boolean top = InventoryMenu.top.contains(key);
		boolean bottom = InventoryMenu.bottom.contains(key);
		if ((justSwitchedTop && top) || (justSwitchedBottom && bottom))
			return;
		if (top)
			justSwitchedTop = true;
		else if (bottom)
			justSwitchedBottom = true;
		String mb = top ? "RB" : "LB";

		InventoryItem current = getQuickSlot(mb);
		if (current == null)
			return;
		current.quickSlot = key;
		current = current.copy();
		setQuickSlot(key, current);
		setQuickSlot(mb, null);
	}

	public void draw() {
		drawTimer = 7;
		drawn = true;
	}

	public void punch(String key) {
		Hud.setHand(key, Texture.getTexture("gui/punch.png"));
		boolean right = key.equals("RB");
		boolean left = key.equals("LB");
		if ((left && justPunchedLeft) || (right && justPunchedRight))
			return;
		if (left)
			justPunchedLeft = true;
		else if (right)
			justPunchedRight = true;
		Living living = owner.rayTraceToLiving(owner.currentHeight, .01, 1.85);
		if (living != null)
			Main.m.player.hurtEntity(living, abilities.punchDamage, abilities.knockback, true);
	}

	public static boolean keyIsDown(String key) {
		if (Menu.currentMenu != null || key == null)
			return false;
		if (keyIsMouseButton(key))
			return Main.m.input.mouse.isDown(quickSlotKeys.get(key));
		else
			return Main.m.input.isDown(quickSlotKeys.get(key));
	}

	public static boolean checkKey(String key) {
		if (Menu.currentMenu != null || key == null)
			return false;
		if (keyIsMouseButton(key))
			return Main.m.input.mouse.checkButton(quickSlotKeys.get(key));
		else
			return Main.m.input.checkKey(quickSlotKeys.get(key));
	}

	public static boolean keyIsMouseButton(String key) {
		if (key.equals("LB") || key.equals("RB"))
			return true;
		return false;
	}

	public boolean topRowDown() {
		return keyIsDown("1") || keyIsDown("2") || keyIsDown("3") || keyIsDown("4");
	}

	public boolean bottomRowDown() {
		return keyIsDown("Sh") || keyIsDown("Z") || keyIsDown("X") || keyIsDown("C");
	}

	static {
		quickSlotKeys.put("1", Keyboard.KEY_1);
		quickSlotKeys.put("2", Keyboard.KEY_2);
		quickSlotKeys.put("3", Keyboard.KEY_3);
		quickSlotKeys.put("4", Keyboard.KEY_4);

		quickSlotKeys.put("Sh", Keyboard.KEY_LSHIFT);
		quickSlotKeys.put("Z", Keyboard.KEY_Z);
		quickSlotKeys.put("X", Keyboard.KEY_X);
		quickSlotKeys.put("C", Keyboard.KEY_C);

		quickSlotKeys.put("RB", 1);
		quickSlotKeys.put("LB", 0);
	}
}
