package com.teamlucidic.hax.entity.inventory.item;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.BlockType;
import com.teamlucidic.hax.entity.inventory.Inventory;
import com.teamlucidic.hax.entity.inventory.InventoryItem;

public class BlockPlacer extends InventoryItem {
	public BlockType blt;

	public BlockPlacer(Inventory i, BlockType type) {
		super(i, Main.m.invItemLoader.getInvItemType("Block Placer"));
		blt = type;
		tex = BlockType.getBlockTexture(type);
		name = type.name;
	}

	public void update() {
		if (swingTimer > 0)
			swingTimer--;
		if (!parent.drawn && type.requiresDraw)
			return;
		if (quickSlot != null && Inventory.keyIsMouseButton(quickSlot) && parent.drawTimer == 0) {
			Main.m.player.editorMode = true;
			Main.m.player.setTargetBlock();
			if (swingTimer == 0) {
				if (!Inventory.keyIsDown(quickSlot))
					swingTimer = 0;
				else if (Main.m.player.targetBlock != null && Main.m.player.placeBlockAtTarget(blt.name))
					swingTimer = Main.m.player.superBreaker ? 0 : swingDelay;
			}
		}
	}

	public InventoryItem copy() {
		BlockPlacer copy = new BlockPlacer(parent, blt);
		super.addParamsToCopy(copy);
		return copy;
	}
}
