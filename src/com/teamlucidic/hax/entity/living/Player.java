package com.teamlucidic.hax.entity.living;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.Sound;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.entity.AABB;
import com.teamlucidic.hax.entity.Entity;
import com.teamlucidic.hax.entity.inventory.item.TheCure;
import com.teamlucidic.hax.quest.Quest;
import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.render.gui.menu.AsunderIntro;
import com.teamlucidic.hax.render.gui.menu.Console;
import com.teamlucidic.hax.render.gui.menu.Hud;
import com.teamlucidic.hax.render.gui.menu.InventoryMenu;
import com.teamlucidic.hax.render.gui.menu.MapMenu;
import com.teamlucidic.hax.render.gui.menu.Menu;
import com.teamlucidic.hax.render.gui.menu.components.SlotItem;

public class Player extends Living {
	public static String[] savedQuickslots = new String[10];

	public Block targetBlock;
	public double reachDistance = 5;
	public boolean editorMode;
	public boolean superBreaker;
	public int leftSwingTimer;
	public int rightSwingTimer;
	public boolean justTargetedBlock;

	public Quest currentQuest;

	public Living targetLiving;
	public int targetTimer;
	public int timeSinceLastTarget;

	public Player() {
		super("Player");
		inventory.abilities.crouchHeight = .47D;
		targetHeight = currentHeight = baseHeight = 1.75;
		inventory.abilities.healthRegen = .02;
		inventory.abilities.energyRegen = .125;
		hitDelay = 40;
		health = maxHealth = 100;
		energy = inventory.abilities.maxEnergy = 100;
		model = Main.m.modelLoader.getNewModelCopy("human");
		makeFriendly();
	}

	public void update() {
		if (Menu.currentMenu == null || (Menu.currentMenu instanceof InventoryMenu) || (Menu.currentMenu instanceof MapMenu))
			controlInput();

		justTargetedBlock = false;
		setTargetLiving();
		inventory.update();
		if (currentQuest != null) {
			currentQuest.update();
			if (currentQuest.completed)
				setQuest(null);
		}
		if (editorMode) {
			if (leftSwingTimer > 0)
				leftSwingTimer--;
			if (rightSwingTimer > 0)
				rightSwingTimer--;
		}

		if (Main.m.input.inventory.checkKey()) {
			if (Menu.currentMenu == null)
				Menu.setMenu(new InventoryMenu());
			else if (Menu.currentMenu instanceof InventoryMenu) {
				if (SlotItem.grabbedItem != null)
					SlotItem.grabbedItem.drop();
				Menu.setMenu(null);
			}
		}

		if (Main.m.input.isDown(Keyboard.KEY_GRAVE)) {
			if (Menu.currentMenu == null && !Console.justClosed)
				Menu.setMenu(new Console());
		} else
			Console.justClosed = false;

		if (Main.m.input.isDown(Keyboard.KEY_TAB) && Menu.currentMenu == null)
			Menu.setMenu(new MapMenu());

		if (Menu.currentMenu == null) {
			if (Console.upKeyBind.length() > 0 && Main.m.input.checkKey(Keyboard.KEY_UP))
				Console.runCommand(Console.upKeyBind);
			if (Console.leftKeyBind.length() > 0 && Main.m.input.checkKey(Keyboard.KEY_LEFT))
				Console.runCommand(Console.leftKeyBind);
			if (Console.downKeyBind.length() > 0 && Main.m.input.checkKey(Keyboard.KEY_DOWN))
				Console.runCommand(Console.downKeyBind);
			if (Console.rightKeyBind.length() > 0 && Main.m.input.checkKey(Keyboard.KEY_RIGHT))
				Console.runCommand(Console.rightKeyBind);
		}

		int min = 0;
		if (Main.m.gameMode.contains("asunder")) {
			min = 8;
			if (AsunderIntro.escapeTimer == 180) {
				Sound.stopAllBackgroundSounds();
				Sound.getSound("sfx/explosion.ogg").playInBackground(false);
				for (int i = 0; i < Living.livingList.size(); i++) {
					Living l = Living.livingList.get(i);
					if (l != Main.m.player && l != Main.m.camera)
						l.applyDamage(l.health, false);
				}
				if (!AsunderIntro.win)
					setDead(null, true);
			}
			if (AsunderIntro.escapeTimer > -1)
				AsunderIntro.escapeTimer--;
			if (AsunderIntro.win && AsunderIntro.escapeTimer > 180)
				AsunderIntro.escapeTimer = 180;
		}

		if (!flying) {
			int max = Main.m.world.getWidth() - min;
			posX = posX < min ? min : posX;
			posX = posX > max ? max : posX;
			posZ = posZ < min ? min : posZ;
			posZ = posZ > max ? max : posZ;
		}
	}

	public void render() {
		if (Main.m.camera.thirdperson && model != null) {
			model.getPart("body").setRotation(0, -rotY, 0);
			model.render(posX, posY, posZ);
		}
		if (currentQuest != null)
			currentQuest.render();
		Block b = Main.m.world.getTopBlock(Main.m.world.centerX, Main.m.world.centerZ, true, false);
		if (TheCure.released && b != null)
			TheCure.model.render(Main.m.world.centerX, b.posY + 1, Main.m.world.centerZ);

		inventory.render();
	}

	public void controlInput() {
		if (Main.m.input.checkKey(Keyboard.KEY_V)) {
			superBreaker = !superBreaker;
		}

		Block block = Main.m.world.getBlock(posX, posY, posZ, false);

		if (Main.m.input.jump.isDown() && !flying && (block == null || !block.type.isSolid))
			jump();

		if (block != null && !flying && block.type.isLiquid && Main.m.input.jump.isDown()) {
			velY += .15;
			velY = velY > .2 ? .2 : velY;
		}

		if (Mouse.isGrabbed()) {
			rotY += Main.m.input.mouse.velX;
			rotX -= Main.m.input.mouse.velY;
		}
		if (Main.m.input.checkKey(Keyboard.KEY_F) && !Main.m.gameMode.contains("asunder")) {
			flying = !flying;
			fallDistance = velX = velY = velZ = 0;
			if (flying)
				velY += onGround ? inventory.abilities.jumpHeight + .001 : inventory.abilities.jumpHeight / 2D;
		}

		if (Main.m.input.checkKey(Keyboard.KEY_R)) {
			if (inventory.drawn)
				inventory.undrawing = true;
			else
				inventory.draw();
		}

		double speed = inventory.abilities.walkSpeed;

		if (Main.m.input.forward.isDown() || inventory.abilities.autowalking)
			moveForward += speed / 60;
		if (Main.m.input.back.isDown())
			moveForward -= speed / 60;
		if (Main.m.input.right.isDown())
			moveStrafe -= speed / 60;
		if (Main.m.input.left.isDown())
			moveStrafe += speed / 60;
		if (flying) {
			fallDistance = 0;
			if (Main.m.input.jump.isDown())
				moveVert += speed / 60;
			if (Main.m.input.shift.isDown())
				moveVert -= speed / 60;
		}
	}

	public void setTargetBlock() {
		if (!editorMode) {
			targetBlock = null;
			return;
		}
		if (justTargetedBlock)
			return;
		targetBlock = rayTraceToBlock(currentHeight, .001, reachDistance);
		justTargetedBlock = true;
	}

	public void setTargetLiving() {

		if (targetTimer > 0)
			targetTimer--;

		timeSinceLastTarget++;

		if (!inventory.drawn) {
			if (targetTimer > (int) Hud.fadeTime) {
				targetTimer = (int) Hud.fadeTime;
			}
			return;
		}

		Living living = rayTraceToLiving(currentHeight, .05, 10);
		if (living == null) {
			if (targetTimer == 0)
				targetLiving = null;
		} else {
			boolean canTarget = (targetLiving == null || targetLiving.dead);
			if (!canTarget) {
				if (living.isEnemy())
					canTarget = true;
				else if (targetLiving.isFriendly() || targetLiving.isNeutral())
					canTarget = true;
			}
			if (canTarget) {
				if (targetLiving == null && living.type.soundOnLook != null && timeSinceLastTarget >= living.type.soundOnLookTimer)
					living.type.soundOnLook.playInBackground(false);
				targetLiving = living;
				timeSinceLastTarget = 0;
				targetTimer = living.isEnemy() ? 320 : (int) Hud.fadeTime + 10;
			}
		}
	}

	public void hurtEntity(Entity e, double damage, double knockback, boolean withGore) {
		if (e instanceof Living) {
			targetLiving = (Living) e;
			targetTimer = 320;
		}
		if (damage < 0) {
			e.health -= damage;
			if (e.health > e.maxHealth)
				e.health = e.maxHealth;
			return;
		}
		e.applyDamage(damage, withGore);
		e.applyForce(rotX, rotY, damage * knockback, damage * knockback, 0);
		if (e.maxHealth != 0 && e.health <= 0 && !e.dead)
			e.setDead(this, withGore);
		if (e.health <= 0 && e instanceof Living) {
			targetTimer = (int) Hud.fadeTime;
		}
	}

	public boolean placeBlockAtTarget(String type) {
		boolean wasPlaced = true;
		int x = targetBlock.posX;
		int y = targetBlock.posY;
		int z = targetBlock.posZ;
		y += targetSide == 0 ? -1 : targetSide == 1 ? 1 : 0;
		x += targetSide == 2 ? -1 : targetSide == 3 ? 1 : 0;
		z += targetSide == 4 ? -1 : targetSide == 5 ? 1 : 0;
		if (!Main.m.blockLoader.getBlockType(type).canBePlacedOn(Main.m.world.getBlock(x, y - 1, z, false)))
			return false;
		Block bPrev = Main.m.world.getBlock(x, y, z, false);
		Main.m.world.placeBlock(type, x, y, z, true);
		Block b = Main.m.world.getBlock(x, y, z, false);
		bb.minX += Render.epsilon;
		bb.maxX -= Render.epsilon;
		bb.minY += Render.epsilon;
		bb.maxY -= Render.epsilon;
		bb.minZ += Render.epsilon;
		bb.maxZ -= Render.epsilon;
		if (b != null && bb.intersectsWith(new AABB(b))) {
			wasPlaced = false;
			if (bPrev == null)
				Main.m.world.clearBlock(x, y, z);
			else
				Main.m.world.placeBlock(bPrev.type.name, x, y, z, true);
		}
		bb.minX -= Render.epsilon;
		bb.maxX += Render.epsilon;
		bb.minY -= Render.epsilon;
		bb.maxY += Render.epsilon;
		bb.minZ -= Render.epsilon;
		bb.maxZ += Render.epsilon;
		return wasPlaced;
	}

	public void setQuest(Quest quest) {
		currentQuest = quest;
	}

	//	if (parent.getQuickSlot(quickSlot.equals("LB") ? "RB" : "LB") instanceof FireBlast && Main.m.input.mouse.isDown(buttonDown == 0 ? 1 : 0) && parent.owner.rotX > 82) {
	//		double power = .4;
	//		parent.owner.velY += power / 5;
	//		parent.owner.velY = parent.owner.velY > power ? power : parent.owner.velY;
	//		parent.owner.fallDistance = 0;
	//	}

}
