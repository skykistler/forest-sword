package com.teamlucidic.hax.entity.inventory.item;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.inventory.Inventory;
import com.teamlucidic.hax.entity.inventory.InventoryItem;

public class AutoWalk extends InventoryItem {

	public AutoWalk(Inventory i) {
		super(i, Main.m.invItemLoader.getInvItemType("AutoWalk"));
	}

	public void update() {
		if (Main.m.input.forward.isDown() || Main.m.input.back.isDown())
			parent.abilities.autowalking = false;
		if (quickSlot != null && Inventory.checkKey(quickSlot))
			parent.abilities.autowalking = !parent.abilities.autowalking;
	}

	public InventoryItem copy() {
		AutoWalk copy = new AutoWalk(parent);
		super.addParamsToCopy(copy);
		return copy;
	}

}
