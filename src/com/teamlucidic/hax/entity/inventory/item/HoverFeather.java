package com.teamlucidic.hax.entity.inventory.item;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.inventory.Inventory;
import com.teamlucidic.hax.entity.inventory.InventoryItem;
import com.teamlucidic.hax.entity.particle.projectile.Projectile;

public class HoverFeather extends InventoryItem {
	public boolean used;

	public HoverFeather(Inventory i) {
		super(i, Main.m.invItemLoader.getInvItemType("Hover Feather"));
	}

	public void update() {
		if (parent.owner.onGround && used && usesLeft < 1) {
			parent.removeItem(this);
			return;
		}

		if (used) {
			for (int i = 0; i <= 5; i++) {
				Projectile p = type.projectile.newProjectile(parent.owner);
				p.spawnAtEntityFeet(p.owner, 0, 360);
				p.posQuiver = 1;
				p.quiverPosition();
			}
			if (parent.owner.energy < type.energyEffect) {
				if (usesLeft > 0) {
					usesLeft--;
					used = false;
				} else
					parent.removeItem(this);
				return;
			}
			double power = Main.m.world.gravity / 180;
			parent.owner.velY = power;
			parent.owner.fallDistance = 0;
			parent.owner.affectEnergy(type.energyEffect);
		}
	}

	public void onUse() {
		if (parent.owner.onGround)
			return;
		if (usesLeft > 0) {
			usesLeft--;
			used = true;
		}
	}

	public InventoryItem copy() {
		HoverFeather copy = new HoverFeather(parent);
		copy.used = used;
		super.addParamsToCopy(copy);
		return copy;
	}
}
