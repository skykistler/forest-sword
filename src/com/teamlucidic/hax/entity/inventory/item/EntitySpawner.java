package com.teamlucidic.hax.entity.inventory.item;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.entity.inventory.Inventory;
import com.teamlucidic.hax.entity.inventory.InventoryItem;
import com.teamlucidic.hax.entity.living.LivingType;
import com.teamlucidic.hax.world.World;

public class EntitySpawner extends InventoryItem {
	public LivingType entityType;

	public EntitySpawner(Inventory i, String en) {
		super(i, Main.m.invItemLoader.getInvItemType("Entity Spawner"));
		entityType = Main.m.livingLoader.getLivingType(en);
		tex = entityType.icon;
		name = entityType.name + " Spawner";
	}

	public void update() {
		if (swingTimer > 0)
			swingTimer--;
		if (!Inventory.keyIsDown(quickSlot))
			swingTimer = 0;
		if (quickSlot != null && Inventory.keyIsDown(quickSlot) && swingTimer == 0) {
			swingTimer = swingDelay;
			Block b = Main.m.player.rayTraceToBlock(Main.m.player.currentHeight, .2, World.renderDistance - 2);
			if (b != null) {
				entityType.spawnNewAt(b.posX, b.posY + 1, b.posZ);
			}
		}
	}

	public InventoryItem copy() {
		EntitySpawner copy = new EntitySpawner(parent, entityType.name);
		super.addParamsToCopy(copy);
		return copy;
	}
}
