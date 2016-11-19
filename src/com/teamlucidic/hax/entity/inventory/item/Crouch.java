package com.teamlucidic.hax.entity.inventory.item;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.entity.inventory.Inventory;
import com.teamlucidic.hax.entity.inventory.InventoryItem;

public class Crouch extends InventoryItem {

	public Crouch(Inventory i) {
		super(i, Main.m.invItemLoader.getInvItemType("Crouch"));
	}

	public void update() {
		if (quickSlot != null && Inventory.keyIsDown(quickSlot)) {
			if (!parent.owner.crouching)
				parent.owner.targetHeight = parent.abilities.crouchHeight;
			parent.owner.crouching = true;
		} else {
			Block b = Main.m.world.getBlock(parent.owner.posX, parent.owner.posY + 1.1, parent.owner.posZ, false);
			if (b == null || !b.type.isSolid)
				if (parent.owner.crouching) {
					parent.owner.targetHeight = parent.owner.baseHeight;
					parent.owner.crouching = false;
				}
		}
	}

	public InventoryItem copy() {
		Crouch copy = new Crouch(parent);
		super.addParamsToCopy(copy);
		return copy;
	}
}
