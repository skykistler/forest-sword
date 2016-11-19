package com.teamlucidic.hax.entity.inventory.item;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.inventory.Inventory;
import com.teamlucidic.hax.entity.inventory.InventoryItem;
import com.teamlucidic.hax.entity.living.Player;
import com.teamlucidic.hax.render.Model;
import com.teamlucidic.hax.render.gui.menu.Menu;

public class TheCure extends InventoryItem {
	public static boolean released;
	public static Model model;

	public TheCure(Inventory i) {
		super(i, Main.m.invItemLoader.getInvItemType("The Cure"));
		released = false;
		model = Main.m.modelLoader.getNewModelCopy("thecure");
	}

	public void update() {
		if (quickSlot != null && Inventory.checkKey(quickSlot))
			Menu.currentHud.showNotification(type.icon, "Not close enough!", "Get to the center of the island before using this.");
	}

	public void onUse() {
		if (parent.owner instanceof Player && Main.m.player.currentQuest.currObj.targetArea.intersectsWith(Main.m.player.posX, Main.m.player.posY, Main.m.player.posZ)) {
			released = true;
			parent.removeItem(this);
		}
		justUsed = true;
	}

	public TheCure copy() {
		TheCure copy = new TheCure(parent);
		addParamsToCopy(copy);
		return copy;
	}
}
