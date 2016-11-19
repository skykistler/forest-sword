package com.teamlucidic.hax.entity.inventory;

import com.teamlucidic.hax.entity.inventory.item.AutoWalk;
import com.teamlucidic.hax.entity.inventory.item.Crouch;
import com.teamlucidic.hax.entity.inventory.item.Sprint;
import com.teamlucidic.hax.entity.living.Player;

public class Abilities {
	public Inventory parent;

	public double walkSpeed = 5;
	public boolean autowalking;
	public InventoryItem autoWalk;

	public double sprintSpeed = 1.5D;
	public InventoryItem sprint;

	public double jumpHeight = .37D;
	public double swimSpeed = .7D;

	public boolean canFly;

	public double crouchSpeed = .4;
	public double crouchHeight;
	public InventoryItem crouch;

	public double healthRegen;

	public int maxEnergy;
	public double energyRegen;

	public double punchDamage = 5;
	public double knockback = .04;

	public boolean editor;

	public Abilities(Inventory i) {
		parent = i;
		crouchHeight = parent.owner.baseHeight / 2;

		if (parent.owner instanceof Player) {
			sprint = new Sprint(parent);
			crouch = new Crouch(parent);
			autoWalk = new AutoWalk(parent);
			parent.addItem(sprint);
			parent.addItem(crouch);
			parent.addItem(autoWalk);
		}
	}
}
