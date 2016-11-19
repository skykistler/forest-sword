package com.teamlucidic.hax.entity.inventory.item;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.inventory.Inventory;
import com.teamlucidic.hax.entity.inventory.InventoryItem;

public class Editor extends InventoryItem {

	public Editor(Inventory i) {
		super(i, Main.m.invItemLoader.getInvItemType("Editor"));
	}

	public void update() {
		if (swingTimer > 0)
			swingTimer--;
		if (!parent.drawn && type.requiresDraw)
			return;
		if (quickSlot != null && Inventory.keyIsMouseButton(quickSlot) && parent.drawTimer == 0) {
			Main.m.player.editorMode = true;
			Main.m.player.setTargetBlock();
			if (!Inventory.keyIsDown(quickSlot))
				swingTimer = 0;
			else {
				if (swingTimer == 0) {
					swingTimer = Main.m.player.superBreaker ? 0 : swingDelay;
					if (Main.m.player.targetBlock != null && Main.m.player.targetBlock.type.isDestroyable)
						Main.m.player.targetBlock.breakBlock(true);
				}
			}
		}
	}

	public InventoryItem copy() {
		Editor copy = new Editor(parent);
		super.addParamsToCopy(copy);
		return copy;
	}
}
