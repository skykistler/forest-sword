package com.teamlucidic.hax.entity.inventory;

import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.living.Living;
import com.teamlucidic.hax.entity.living.Player;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.menu.Hud;

public class InventoryItem {
	public Inventory parent;
	public InventoryItemType type;
	public Texture tex;
	public Texture icon;
	public String name;
	public int index;
	public String quickSlot;
	public int usesLeft;
	public int swingTimer;
	public int swingDelay;
	public boolean removed;
	public boolean justUsed;
	public boolean toggled;

	public InventoryItem(Inventory i, InventoryItemType iit) {
		parent = i;
		type = iit;
		tex = type.tex;
		icon = type.icon;
		name = type.name;
		usesLeft = type.uses;
		swingDelay = type.swingDelay;
	}

	public void update() {
		if (quickSlot == null || !Inventory.keyIsDown(quickSlot))
			justUsed = false;

		if (type.texOnUse != null)
			tex = type.tex;

		if (type.lightBehavior == 2) {
			tex = type.tex;
			if ((!type.requiresDraw || (parent.drawn && parent.drawTimer == 0)) && (quickSlot != null && Inventory.keyIsDown(quickSlot) && (!type.requiresMouseButton || Inventory.keyIsMouseButton(quickSlot))) && !(type.energyEffect < 0 && parent.owner.energy < -type.energyEffect)) {
				Main.m.player.lightSurroundings = true;
				if (type.texOnUse != null)
					tex = type.texOnUse;
			}
		}
		if (type.lightBehavior == 1 && toggled && !(type.energyEffect < 0 && parent.owner.energy < -type.energyEffect)) {
			Main.m.player.lightSurroundings = true;
			if (type.texOnUse != null)
				tex = type.texOnUse;
		}

		if (type.type.equals("spell")) {
			if (swingTimer > 0) {
				swingTimer--;
				return;
			}
			if ((!type.requiresDraw || (parent.drawn && parent.drawTimer == 0)) && type.uses == 0 && type.requiresMouseButton && quickSlot != null && Inventory.keyIsMouseButton(quickSlot) && Inventory.keyIsDown(quickSlot)) {
				tex = type.tex;
				if (type.drawHand)
					Hud.setHand(quickSlot, type.handOnUse);
				else if (type.texOnUse != null)
					tex = type.texOnUse;
				if (type.energyEffect < 0 && parent.owner.energy < -type.energyEffect)
					return;

				int buttonDown = Inventory.quickSlotKeys.get(quickSlot);
				if (type.projectile != null)
					type.projectile.spawnProjectileAtOwner(parent.owner, buttonDown == 0 ? "left" : buttonDown == 1 ? "right" : "center");
				if (type.effect != null)
					type.effect.spawnSystem(parent.owner.posX, parent.owner.posY, parent.owner.posZ);
				swingTimer = swingDelay;
				parent.owner.affectEnergy(type.energyEffect);
			}
		}

		if (type.type.equals("weapon")) {
			if (type.requiresMouseButton && type.requiresDraw) {
				if (swingTimer > 0) {
					swingTimer--;
					return;
				}
				if (!parent.drawn)
					return;
				tex = type.tex;
				if (quickSlot != null && Inventory.keyIsMouseButton(quickSlot) && parent.drawTimer == 0) {
					if (!Inventory.keyIsDown(quickSlot))
						swingTimer = 0;
					else {
						if (swingTimer == 0) {
							swingTimer = swingDelay;

							if (type.projectile == null) {
								Living living = parent.owner.rayTraceToLiving(parent.owner.currentHeight, .01, 1.2 + (tex.height / 32d * .4));
								if (living != null)
									parent.owner.hurtEntity(living, -type.targetHealthEffect, parent.abilities.knockback, true);
							} else {
								int buttonDown = Inventory.quickSlotKeys.get(quickSlot);
								usesLeft--;
								if (usesLeft < 1 && type.uses > 0)
									parent.removeItem(this);
								type.projectile.spawnProjectileAtOwner(parent.owner, buttonDown == 0 ? "left" : buttonDown == 1 ? "right" : "center");
							}
						}
					}
				}
			}
		}
	}

	public void renderInHand() {
		if (!type.drawHand && parent.drawn && parent.owner instanceof Player) {
			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glTranslated(Main.m.camera.posX, Main.m.camera.posY + -Main.m.camera.bobX * .07, Main.m.camera.posZ);
			GL11.glRotated(-Main.m.camera.rotY, 0, 1, 0);
			GL11.glRotated(-Main.m.camera.rotX, 1, 0, 0);
			if (tex != null)
				tex.bind();
			else
				Texture.getTexture("gui/unknown.png").bind();
			int button = Inventory.quickSlotKeys.get(quickSlot);
			double mod = (double) swingTimer / swingDelay;
			double y = -parent.owner.currentHeight * .45 - parent.drawTimer / 7d;
			double z = -1;
			double w = Texture.currentTexture.width / 64d;
			double h = Texture.currentTexture.height / 64d;
			double rx = -10 * mod;
			double ry = 15 * mod;
			double rz = -30 * mod;
			double ty = -.1 * mod;
			double tz = -.1 * mod + (Main.m.render.currentFOV / Main.m.render.baseFOV - 1);
			double tx = .7 * mod;
			if (button == 0) {
				GL11.glRotated(rz, 0, 0, 1);
				GL11.glRotated(10 + ry, 0, 1, 0);
				GL11.glRotated(-10 + rx, 1, 0, 0);
				GL11.glTranslated(-.1 + tx, .3 + ty, tz);
				Main.m.modeler.start();
				Main.m.modeler.addVertexTex(-w, y + 0, z, 0.0, 1.0);
				Main.m.modeler.addVertexTex(0, y + 0, z, 1.0, 1.0);
				Main.m.modeler.addVertexTex(0, y + h, z, 1.0, 0.0);
				Main.m.modeler.addVertexTex(-w + 0, y + h, z, 0.0, 0.0);
				Main.m.modeler.finish();
			} else {
				GL11.glRotated(-rz, 0, 0, 1);
				GL11.glRotated(-10 - ry, 0, 1, 0);
				GL11.glRotated(-10 + rx, 1, 0, 0);
				GL11.glTranslated(.1 - tx, .3 + ty, tz);
				Main.m.modeler.start();
				Main.m.modeler.addVertexTex(0, y + 0, z, 1.0, 1.0);
				Main.m.modeler.addVertexTex(w, y + 0, z, 0.0, 1.0);
				Main.m.modeler.addVertexTex(w, y + h, z, 0.0, 0.0);
				Main.m.modeler.addVertexTex(0, y + h, z, 1.0, 0.0);
				Main.m.modeler.finish();
			}
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glPopMatrix();
		}
	}

	public void onUse() {
		if (type.type.equals("usable") && !justUsed) {
			if (usesLeft > 0) {
				parent.owner.health += type.selfHealthEffect;
				if (parent.owner.health > parent.owner.maxHealth)
					parent.owner.health = parent.owner.maxHealth;

				parent.owner.energy += type.energyEffect;
				if (parent.owner.energy > parent.abilities.maxEnergy)
					parent.owner.energy = parent.abilities.maxEnergy;

				if (type.effect != null)
					type.effect.spawnSystem(parent.owner.posX, parent.owner.posY, parent.owner.posZ);

				if (quickSlot != null && !type.requiresMouseButton && Inventory.keyIsMouseButton(quickSlot)) {
					int buttonDown = Inventory.quickSlotKeys.get(quickSlot);
					if (type.projectile != null)
						type.projectile.spawnProjectileAtOwner(parent.owner, buttonDown == 0 ? "left" : buttonDown == 1 ? "right" : "center");
				}

				usesLeft--;
			}
			if (usesLeft < 1 && type.uses > 0)
				parent.removeItem(this);
		}
		if (type.lightBehavior == 1 && !justUsed)
			toggled = !toggled;

		justUsed = true;
	}

	/* ATTENTION: You must inherit this method and change the two InventoryItem's in the first line to whatever item youre making */
	public InventoryItem copy() {
		InventoryItem copy = new InventoryItem(parent, type);
		addParamsToCopy(copy);
		return copy;
	}

	public void addParamsToCopy(InventoryItem copy) {
		copy.index = index;
		copy.quickSlot = quickSlot;
		copy.tex = tex;
		copy.icon = icon;
		copy.name = name;
		copy.usesLeft = usesLeft;
		copy.swingDelay = swingDelay;
	}

}
