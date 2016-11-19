package com.teamlucidic.hax.entity.inventory.item;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.inventory.Inventory;
import com.teamlucidic.hax.entity.inventory.InventoryItem;

public class Telescope extends InventoryItem {

	public Telescope(Inventory i) {
		super(i, Main.m.invItemLoader.getInvItemType("Telescope"));
	}

	public void update() {
		if (quickSlot != null) {
			if (Inventory.keyIsDown(quickSlot)) {
				Main.m.render.currentFOV = 5;
				Main.m.input.mouse.sensitivity = .09F;
				Main.m.input.mouse.drag = .8F;
			} else {
				Main.m.input.mouse.sensitivity = .18F;
				Main.m.input.mouse.drag = .4F;
			}
		}
	}

	public InventoryItem copy() {
		Telescope copy = new Telescope(parent);
		super.addParamsToCopy(copy);
		return copy;
	}
}
