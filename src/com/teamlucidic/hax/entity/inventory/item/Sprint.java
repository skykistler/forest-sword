package com.teamlucidic.hax.entity.inventory.item;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.inventory.Inventory;
import com.teamlucidic.hax.entity.inventory.InventoryItem;

public class Sprint extends InventoryItem {

	public Sprint(Inventory i) {
		super(i, Main.m.invItemLoader.getInvItemType("Sprint"));
	}

	public void update() {
		if (quickSlot != null && Inventory.keyIsDown(quickSlot) && !parent.owner.flying && parent.owner.moveForward > 0 && (Math.abs(parent.owner.posX - parent.owner.prevPosX) > .05 || Math.abs(parent.owner.posZ - parent.owner.prevPosZ) > .05) && !parent.owner.crouching) {
			if (!parent.owner.sprinting)
				Main.m.render.targetFOV = Main.m.render.baseFOV + 25;
			parent.owner.sprinting = true;
		} else {
			if (parent.owner.sprinting)
				Main.m.render.targetFOV = Main.m.render.baseFOV;
			parent.owner.sprinting = false;
		}
	}

	public InventoryItem copy() {
		Sprint copy = new Sprint(parent);
		super.addParamsToCopy(copy);
		return copy;
	}

}
