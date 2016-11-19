package com.teamlucidic.hax.entity.living;

import java.util.ArrayList;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.entity.AABB;
import com.teamlucidic.hax.entity.Camera;
import com.teamlucidic.hax.entity.Entity;
import com.teamlucidic.hax.entity.Flock.Spot;
import com.teamlucidic.hax.entity.inventory.Inventory;
import com.teamlucidic.hax.entity.particle.ParticleSystem;
import com.teamlucidic.hax.render.Model;
import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.render.Texture;

public class Living extends Entity {
	public static ArrayList<Living> livingList = new ArrayList<Living>();
	public static boolean aiEnabled = true;

	public LivingType type;
	public String name;
	public String desc = "";
	public Inventory inventory;
	public String attitude = "neutral";
	public Model model;
	public Texture texture;

	public double bobX, bobZ;
	public boolean crouching;
	public boolean sprinting;
	public double sprintDistance = 0;
	public boolean lightSurroundings;
	public boolean justWarnedNearAttack;

	public int energyCounter;
	public double energy;

	public Entity target;
	public Spot spotTarget;
	public double interestDistance = 15;
	public double targetRotY = rotY;
	public int walkTime;
	public String attackTargets = "";
	public String followTargets = "";
	public String parasites = "";
	public boolean wanders;
	public boolean flocks;
	public boolean hops;
	public int hitDelay;
	public int hitTimer;
	public int oxygenTimer;

	public Living(String name) {
		super();
		this.name = name;
		baseHeight = currentHeight = targetHeight = .7D;
		bb = new AABB(this, .25, .25);

		inventory = new Inventory(this);
		isLiving = true;
		livingList.add(this);
	}

	public Living(LivingType livingType) {
		type = livingType;

		name = new String(type.name);
		desc = new String(type.desc);
		attitude = new String(type.attitude);
		model = Main.m.modelLoader.getNewModelCopy(type.model);
		if (type.textures == null) {
			texture = model.texture;
		} else {
			texture = type.textures[Render.floor((Render.rand.nextDouble() - .01) * type.textures.length)];
			if (texture != null)
				model.setTexture(texture.path);
		}

		baseHeight = currentHeight = targetHeight = type.dimensions[1];
		bb = new AABB(this, type.dimensions[0] / 2, type.dimensions[2] / 2);

		inventory = new Inventory(this);
		for (int i = 0; i < type.inventory.size(); i++)
			inventory.addItem(type.inventory.get(i));

		inventory.abilities.walkSpeed = type.walkSpeed;
		inventory.abilities.sprintSpeed = type.sprintSpeed;
		inventory.abilities.jumpHeight = type.jumpHeight;
		inventory.abilities.swimSpeed = type.swimSpeed;
		inventory.abilities.crouchSpeed = type.crouchSpeed;
		inventory.abilities.crouchHeight = type.crouchHeight;
		inventory.abilities.healthRegen = type.healthRegen;
		inventory.abilities.maxEnergy = type.maxEnergy;
		inventory.abilities.energyRegen = type.energyRegen;
		inventory.abilities.punchDamage = type.punchDamage;
		inventory.abilities.knockback = type.knockback;
		hitDelay = type.hitDelay;

		health = maxHealth = type.maxHealth;
		energy = inventory.abilities.maxEnergy = type.maxEnergy;

		attackTargets = new String(type.attackTargets);
		followTargets = new String(type.followTargets);
		parasites = new String(type.parasites);
		wanders = type.wanders;
		flocks = type.flocks;
		hops = type.hops;
		interestDistance = type.interestDistance;

		isLiving = true;
		livingList.add(this);
	}

	public void postUpdate() {
		if (this != Main.m.player && Main.m.world.worldTicks < 2)
			return;
		if (aiEnabled && this != Main.m.player)
			doAI();

		Block b = Main.m.world.getBlock(posX, posY + currentHeight, posZ, false);
		if (b != null && !flying) {
			if (oxygenTimer == -1)
				oxygenTimer = 300;
			else if (oxygenTimer > 0)
				oxygenTimer--;
			else if (oxygenTimer == 0)
				applyDamage(.1, false);
		} else
			oxygenTimer = -1;

		if (damageCounter > 0)
			damageCounter--;
		if (damageCounter == 0 && health < maxHealth && health > 0 && !dead) {
			health += inventory.abilities.healthRegen;
		}
		if (health > maxHealth)
			health = maxHealth;
		if (health < 0)
			health = 0;

		if (energyCounter > 0)
			energyCounter--;
		if (energy > inventory.abilities.maxEnergy)
			energy = inventory.abilities.maxEnergy;
		if (energy < 0)
			energy = 0;
		if (energyCounter < 1 && energy < inventory.abilities.maxEnergy && !dead)
			energy += inventory.abilities.energyRegen;

		rotX = rotX < -89.9 ? -89.9 : rotX;
		rotX = rotX > 88 ? 88 : rotX;

		targetRotY %= 360;
		targetRotY += targetRotY < 0 ? 360 : 0;

		super.postUpdate();
	}

	public void doAI() {
		setTarget();
		if (target != null && target instanceof Living) {
			if (isFollowable((Living) target))
				followTarget();
			else if (isAttackable((Living) target))
				attackTarget();
			else
				wander();
		} else if (spotTarget != null)
			followSpotTarget();
		else
			wander();

		Block block = Main.m.world.getBlock(posX, posY + currentHeight / 2, posZ, false);
		Block block2 = rayTraceToBlock(Math.min(currentHeight * .3, .5), .05, 1);
		Block block3 = Main.m.world.getBlock(posX, posY + currentHeight + 1, posZ, false);
		if (block != null && block.type.isLiquid && (block2 == null || !block2.type.isSolid)) {
			velY += .15;
			velY = velY > .2 ? .2 : velY;
		} else {
			if (block2 != null && block2.type.isSolid && moveForward != 0 && (block3 == null || !block3.type.isSolid))
				jump();
			else if (hops && moveForward != 0 && onGround && Render.rand.nextDouble() > .5 + Render.rand.nextDouble() * .1 && velY == 0)
				hop();
		}

	}

	public void setTarget() {
		double closestDis = 10000;
		double curDis = 0;
		Entity oldTarget = target;
		target = null;
		if (followTargets.length() > 0 || attackTargets.length() > 0)
			for (Living l : livingList) {
				if (l == this || l instanceof Camera)
					continue;
				curDis = disToEntity(l);
				if (curDis <= interestDistance * (l.sprinting ? 3 : (l.lightSurroundings ? 2 : (l.crouching ? .25 : 1))) && curDis < closestDis) {
					if ((target == null || isAttackable(l)) || (isFollowable(l) && isFollowable((Living) target))) {
						if (!l.dead) {
							target = l;
							closestDis = curDis;
						}
					}
				}
			}
		if (oldTarget != target)
			justWarnedNearAttack = false;

		spotTarget = null;
		if (target == null && Main.m.world.zombieFlock != null && flocks) {
			closestDis = 10000;
			curDis = 0;
			for (Spot s : Main.m.world.zombieFlock.spots) {
				curDis = s.distance(this);
				if (curDis <= interestDistance * 10 && curDis < closestDis) {
					spotTarget = s;
					closestDis = curDis;
				}
			}
		}
	}

	public void attackTarget() {
		double dis = disToEntityXZ(target);
		if (dis > Math.max(bb.xOffset, bb.zOffset) + .4) {
			targetRotY = rotY = Render.atan2Deg((target.posX - posX), -(target.posZ - posZ));
			moveForward += (.5 + .5 * Render.rand.nextDouble()) * type.walkSpeed / 60;
			if (dis < 4 && type.soundNearAttack != null && !justWarnedNearAttack && target instanceof Player) {
				//				type.soundNearAttack.playAtPosition(0, 0, 0);
				justWarnedNearAttack = true;
			}
			if (dis > 6 || !(target instanceof Player))
				justWarnedNearAttack = false;
		} else if (hitTimer >= hitDelay && ((posY + currentHeight >= target.posY && posY + currentHeight <= target.posY + target.currentHeight) || (posY >= target.posY && posY <= target.posY + target.currentHeight))) {
			hurtEntity(target, inventory.abilities.punchDamage, inventory.abilities.punchDamage * inventory.abilities.knockback, true);
			hitTimer = 0;
			if (target.health <= 0)
				setTarget(null);
		}

		hitTimer++;
	}

	public void followTarget() {
		targetRotY = rotY = Math.toDegrees(Math.atan2((target.posX - posX), -(target.posZ - posZ)));
		if (disToEntityXZ(target) > Math.max(bb.xOffset, bb.zOffset) + 2)
			moveForward += Render.rand.nextDouble() * type.walkSpeed / 60;
	}

	public void followSpotTarget() {
		targetRotY = rotY = Math.toDegrees(Math.atan2((spotTarget.posX - posX), -(spotTarget.posZ - posZ)));
		if (spotTarget.distance(this) > Math.max(bb.xOffset, bb.zOffset) + 2)
			moveForward += Render.rand.nextDouble() * type.walkSpeed / 60;
	}

	public void wander() {
		if (wanders) {
			if (entityTicks % 120 == 0 && Render.rand.nextDouble() > .6) {
				targetRotY += Render.rand.nextDouble() * 120;
				if (Render.rand.nextDouble() > .1) {
					walkTime += Render.rand.nextDouble() * 110 + 10;
					inventory.abilities.walkSpeed = Render.rand.nextDouble() * 1.5 + .5;
				}
			}

			double incr = 6;
			rotY += targetRotY == rotY ? 0 : targetRotY > rotY ? incr : -incr;
			if (Math.abs(rotY - targetRotY) < incr)
				rotY = targetRotY;

			if (walkTime > 0) {
				walkTime--;
				moveForward += inventory.abilities.walkSpeed / 60;
			}
		}
	}

	public void render() {
		if (model != null) {
			model.getPart("body").setRotation(0, -rotY, 0);
			model.render(posX, posY, posZ);
		}
	}

	public void move() {
		moveStrafe = crouching ? moveStrafe * inventory.abilities.crouchSpeed : moveStrafe;
		moveForward = crouching ? moveForward * inventory.abilities.crouchSpeed : moveForward;

		moveForward = sprinting ? moveForward * (inventory.abilities.sprintSpeed) : moveForward;
		moveStrafe = sprinting ? moveStrafe * (inventory.abilities.sprintSpeed) : moveStrafe;

		Block block = Main.m.world.getBlock(posX, posY, posZ, false);
		if (block != null) {
			moveForward = block.type.isLiquid ? moveForward * inventory.abilities.swimSpeed : moveForward;
			moveStrafe = block.type.isLiquid ? moveStrafe * inventory.abilities.swimSpeed : moveStrafe;
		}

		if (onGround && !flying) {
			if (moveForward != 0 || moveStrafe != 0) {
				double bobModifier = crouching ? inventory.abilities.crouchHeight : 1;
				bobModifier = sprinting ? inventory.abilities.sprintSpeed : bobModifier;
				bobX = Math.sin(Main.m.gameTicks / 2D * .7 * bobModifier) * .4;
				bobZ = 0;
				//				bobZ = -Math.cos(Main.m.gameTicks / 2D * .7 * bobModifier) * .5;
				//				bobX = 0;
				//				if (bobX <= -.39) {
				//					System.out.println("step");
				//					Main.m.sound.playSoundAtPos("sfx/step.ogg", posX, posY, posZ);
				//				}

			}
			if (onGround)
				sprintDistance = moveForward > 0 ? sprinting ? sprintDistance + moveForward * inventory.abilities.sprintSpeed : 0 : 0;
		}

		super.move();
	}

	public void jump() {
		if (onGround)
			velY += inventory.abilities.jumpHeight;
	}

	public void hop() {
		if (onGround)
			velY += Render.rand.nextDouble() * .02 + .15;
	}

	public void setTarget(Entity e) {
		target = e;
	}

	public void setDead(Entity source, boolean withGore) {
		super.setDead(source, withGore);
		if (!withGore)
			return;
		int piece = 4;
		Texture t = null;
		if (type != null)
			t = type.icon;
		if (t == null)
			t = Texture.getTexture("particle/blood.png");
		t = t.getSubTexture((int) (Render.rand.nextDouble() * (t.width - piece)), (int) (Render.rand.nextDouble() * (t.height - piece)), piece, piece);
		ParticleSystem prtcl = Main.m.partSysLoader.getParticleSystemType("DeathGore").spawnSystem(posX, posY + currentHeight * 3 / 4, posZ);
		prtcl.tex = t;
		if (source != null && source instanceof Living && parasites.contains(":" + ((Living) source).name.toLowerCase() + ":")) {
			Living l = new Living(Main.m.livingLoader.getLivingType(((Living) source).name));
			Texture tex = type.parasitesTex.get(((Living) source).name.toLowerCase().hashCode());
			if (tex != null) {
				l.texture = tex;
				l.model.setTexture(l.texture.path);
			}
			l.spawnAtPosition(posX, posY, posZ);
		}
	}

	public Living rayTraceToLiving(double yOff, double sweep, double reach) {
		double radianX = Math.toRadians(rotX);
		double sinX = Math.sin(radianX);
		double cosX = Math.cos(radianX);
		double radianY = Math.toRadians(rotY);
		double sinY = Math.sin(radianY);
		double cosY = Math.cos(radianY);
		for (double i = 0; i < reach; i += sweep) {
			double[] pos = Render.getCoordInFront(sinX, cosX, sinY, cosY, posX, posY + yOff, posZ, i);
			for (Living living : livingList) {
				if (living == this || living instanceof Camera)
					continue;
				if (living.bb.intersectsWith(pos[0], pos[1], pos[2]))
					return living;
			}
		}
		return null;
	}

	public void affectEnergy(double amount) {
		energy += amount;
		if (amount < 0)
			energyCounter = 90;
	}

	public boolean isFollowable(Living l) {
		if (followTargets.length() < 1)
			return false;
		return (l.isFriendly() && followTargets.contains(":friendly:")) || (l.isNeutral() && followTargets.contains(":neutral:")) || (l.isEnemy() && followTargets.contains(":enemy:")) || (l instanceof Player && followTargets.contains(":player:")) || (followTargets.contains(":" + l.name + ":"));
	}

	public boolean isAttackable(Living l) {
		if (attackTargets.length() < 1)
			return false;
		return (l.isFriendly() && attackTargets.contains(":friendly:")) || (l.isNeutral() && attackTargets.contains(":neutral:")) || (l.isEnemy() && attackTargets.contains(":enemy:")) || (l instanceof Player && attackTargets.contains(":player:")) || (attackTargets.contains(":" + l.name + ":"));
	}

	public boolean isFriendly() {
		return attitude.startsWith("f");
	}

	public boolean isNeutral() {
		return attitude.startsWith("n");
	}

	public boolean isEnemy() {
		return attitude.startsWith("e");
	}

	public void makeFriendly() {
		attitude = "friendly";
	}

	public void makeNeutral() {
		attitude = "neutral";
	}

	public void makeEnemy() {
		attitude = "enemy";
	}
}
