package com.teamlucidic.hax.entity;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.entity.living.Living;
import com.teamlucidic.hax.entity.living.Player;
import com.teamlucidic.hax.entity.particle.ParticleSystem;
import com.teamlucidic.hax.entity.particle.projectile.Projectile;
import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.world.Chunk;

public class Entity {
	public static ArrayList<Entity> entityList = new ArrayList<Entity>();
	public static float airResistance1 = .905F;
	public static float airResistance2 = .97F;
	public static float blockResistance = .85F;
	public static float liquidResistance = .75F;

	public boolean hasAlpha;
	public boolean isLiving, isItem, isProjectile, isParticle;
	public AABB bb;
	public double posX, posY, posZ;
	public double rotX, rotY, rotZ;
	public double velX, velY, velZ;
	public double prevPosX, prevPosY, prevPosZ;
	public double moveForward, moveVert, moveStrafe;
	public double radianY, radianX, sinY, cosY, sinX, cosX;
	public double rotQuiver = 10, posQuiver = 0.025;
	public double baseHeight = 0;
	public double currentHeight;
	public double targetHeight;
	public boolean flying, onGround, affectedByGravity = true;
	public double fallDistance = 0;
	public double currentResistance;
	public boolean inSolid;
	public boolean inLiquid;
	public Block inBlock;
	public int targetSide;
	public boolean blockCollide;
	public boolean blockCollideOnXZ;
	public boolean blockCollideOnY;
	public int maxHealth;
	public double health;
	public int damageCounter;
	public int entityTicks;
	public boolean dead;
	public boolean onLoadedChunk;
	public boolean moveToTopWhenLoaded;

	public Entity() {
		posX = posY = posZ = 0;
		velX = velY = velZ = 0;
		prevPosX = prevPosY = prevPosZ = 0;
		rotY = rotX = 0;
		entityList.add(this);
	}

	public static void updateEntities() {
		liquidResistance = .5f;
		for (int i = 0; i < entityList.size(); i++) {
			Entity en = entityList.get(i);
			if (!en.isOnLoadedChunk())
				continue;
			if (en.moveToTopWhenLoaded) {
				en.moveToTop();
				en.moveToTopWhenLoaded = false;
			}
			en.update();
			if (!en.dead || en instanceof Player)
				en.postUpdate();
		}
	}

	public static void renderEntities() {
		for (Living living : Living.livingList)
			if (!living.hasAlpha && !(living instanceof Player) && living.onLoadedChunk) {
				Main.m.modeler.setHexColor(0xFFFFFFFF);
				living.render();
			}
		for (Projectile projectile : Projectile.projectileList)
			if (!projectile.hasAlpha && projectile.onLoadedChunk) {
				Main.m.modeler.setHexColor(0xFFFFFFFF);
				projectile.render();
			}

		for (Entity en : entityList)
			if (!en.isItem && !en.isLiving && !en.isProjectile && !en.hasAlpha && !en.isParticle && en.onLoadedChunk) {
				Main.m.modeler.setHexColor(0xFFFFFFFF);
				en.render();
			}
	}

	public static void renderAlpha() {
		for (Entity en : entityList)
			if (en.hasAlpha && en.onLoadedChunk) {
				Main.m.modeler.setHexColor(0xFFFFFFFF);
				en.render();
			}
	}

	public void update() {
	}

	public void render() {
	}

	public void postUpdate() {
		double incr = .2;
		currentHeight += targetHeight == currentHeight ? 0 : targetHeight > currentHeight ? incr : -incr;
		if (Math.abs(currentHeight - targetHeight) < incr)
			currentHeight = targetHeight;

		if (bb != null)
			bb.update();
		inSolid = false;
		inLiquid = false;
		inBlock = null;
		if (bb != null) {
			Block bl1 = Main.m.world.getBlock(posX, bb.maxY - .01, posZ, false);
			if (bl1 != null)
				if (bb.maxY - .01 < (double) bl1.posY + 1) {
					inBlock = bl1;
					inSolid = bl1.type.isSolid;
				}

			Block bl2 = Main.m.world.getBlock(posX, bb.minY, posZ, false);
			if (bl2 != null)
				inLiquid = bl2.type.isLiquid;
		}

		if (inLiquid) {
			fallDistance = 0;
			currentResistance = liquidResistance;
		}

		moveStrafe *= 1 - currentResistance / 2;
		moveForward *= 1 - currentResistance / 2;

		velX *= onGround ? blockResistance : airResistance2;
		velY *= airResistance1;
		velZ *= onGround ? blockResistance : airResistance2;
		if (Math.abs(velX + velY + velZ) < Render.epsilon)
			velX = velY = velZ = 0.0D;

		rotY %= 360;
		rotY += rotY < 0 ? 360 : 0;

		if (!flying && affectedByGravity) {
			double g = Main.m.world.gravity / 60 + fallDistance * .05;
			double cap = 1.9;
			g = g > cap ? cap : g;
			g *= 1 - currentResistance;
			moveVert -= g;
		}
		move();
		currentResistance = 0;
		if (maxHealth != 0 && health <= 0 && !dead)
			setDead(null, true);
		entityTicks++;
	}

	public void onSpawn() {

	}

	public void spawnAtPosition(double x, double y, double z) {
		posX = x + .5D;
		posY = y;
		posZ = z + .5D;
		prevPosX = posX;
		prevPosY = posY + Render.epsilon;
		prevPosZ = posZ;
		if (this instanceof Player) {
			rotY = Main.m.world.spawnRot;
			rotX = 0;
		}
		onSpawn();
	}

	public void moveToTop() {
		Block bl = Main.m.world.getTopBlock(posX, posZ, false, false);
		posY = bl != null ? bl.posY + 1 : 0;
		prevPosY = posY + Render.epsilon;
	}

	public void spawnAtEntity(Entity en, double amountForward, boolean withQuiver, String hand) {
		rotY = en.rotY;
		rotX = en.rotX;
		posX = en.posX;
		posY = en.posY + en.currentHeight - baseHeight * 3 / 2;
		posZ = en.posZ;
		double[] pos = rayTrace(currentHeight, amountForward);
		Block block = Main.m.world.getBlock(pos[0], pos[1], pos[2], false);
		if (block != null && block.type.isSolid)
			pos = rayTrace(currentHeight, .1);
		posX = pos[0];
		posY = pos[1];
		posZ = pos[2];
		if (withQuiver) {
			quiverPosition();
			quiverRotation();
		} else
			setTrig();
		prevPosX = posX;
		prevPosY = posY + Render.epsilon;
		prevPosZ = posZ;
		double mod = .25;
		if (hand.startsWith("r"))
			mod *= -1;
		if (!hand.startsWith("c"))
			moveStrafe += mod;
		moveForward -= rotX / 90 * .13;
		onSpawn();
	}

	public void spawnAtEntityFeet(Entity en, double d, double e) {
		posX = en.posX;
		posY = en.posY + .05;
		posZ = en.posZ;
		rotY = Render.rand.nextDouble() * (e - d) + d;
		setTrig();
		onSpawn();
	}

	public void spawnAtEntity(Entity en, double amountForward, boolean withQuiver) {
		spawnAtEntity(en, amountForward, withQuiver, "center");
	}

	public void quiverPosition() {
		posX += getNegPosRand() * posQuiver;
		posY += getNegPosRand() * posQuiver;
		posZ += getNegPosRand() * posQuiver;
	}

	public void quiverRotation() {
		rotY += getNegPosRand() * rotQuiver;
		rotX += getNegPosRand() * rotQuiver;
		setTrig();
	}

	public double getNegPosRand() {
		return (Render.rand.nextDouble() * 2) - 1;
	}

	public void move() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
		setTrig();
		double newX = (-cosY * moveStrafe + sinY * moveForward);
		double newY = moveVert;
		double newZ = -(cosY * moveForward + sinY * moveStrafe);
		moveAlongAxis(newX + velX, newY + velY, newZ + velZ);
		if (!flying) {
			double prevx = posX;
			double prevy = posY;
			double prevz = posZ;
			posX = posX > Main.m.world.getWidth() ? Main.m.world.getWidth() : posX;
			posZ = posZ > Main.m.world.getWidth() ? Main.m.world.getWidth() : posZ;
			posX = posX < 0 ? 0 : posX;
			posZ = posZ < 0 ? 0 : posZ;
			posY = posY < -50 ? -50 : posY;
			if (prevx != posX || prevy != posY || prevz != posZ)
				blockCollide = true;
		}
		updateOnGround();
		setTrig();
		moveForward = 0;
		moveStrafe = 0;
		moveVert = 0;
	}

	public void moveAlongAxis(double x, double y, double z) {
		double[] result = { x, y, z };
		if (flying) {
			bb.translate(x, y, z);
		} else {
			result = bb.pushOutOfSolids(x, y, z);
		}
		posX = (bb.minX + bb.maxX) / 2D;
		posY = bb.minY;
		posZ = (bb.minZ + bb.maxZ) / 2D;

		blockCollide = blockCollideOnXZ = blockCollideOnY = false;
		if (result[0] != x) {
			blockCollideOnXZ = true;
			velX = 0;
		}
		if (result[1] != y) {
			blockCollideOnY = true;
			velY = 0;
		}
		if (result[2] != z) {
			blockCollideOnXZ = true;
			velZ = 0;
		}
		blockCollide = blockCollideOnXZ || blockCollideOnY;
	}

	public void updateOnGround() {
		onGround = prevPosY == posY;
		if (onGround) {
			onFall();
		} else {
			if (moveVert + velY < 0)
				fallDistance -= moveVert + velY;
			else
				fallDistance = 0;
		}
	}

	public void onFall() {
		if (fallDistance > 5)
			applyDamage((fallDistance - 5) * 3.75, true);
		fallDistance = 0;
	}

	public void applyForce(double forw, double up, double strf) {
		velX += (-cosY * strf + sinY * forw) * cosX;
		velY += up * -sinX;
		velZ -= (cosY * forw + sinY * strf) * cosX;
	}

	public void applyForce(double rotX, double rotY, double forw, double up, double strf) {
		double radianX = Math.toRadians(rotX);
		double sinX = Math.sin(radianX);
		double cosX = Math.cos(radianX);
		double radianY = Math.toRadians(rotY);
		double sinY = Math.sin(radianY);
		double cosY = Math.cos(radianY);
		velX += (-cosY * strf + sinY * forw) * cosX;
		velY += up * -sinX;
		velZ -= (cosY * forw + sinY * strf) * cosX;
	}

	public void applyDamage(double damage, boolean withGore) {
		health -= Math.abs(damage);
		damageCounter = 180;
		if (withGore && this instanceof Living) {
			int piece = 4;
			Texture t = Texture.getTexture("particle/blood.png");
			t = t.getSubTexture((int) (Render.rand.nextDouble() * (t.width - piece)), (int) (Render.rand.nextDouble() * (t.height - piece)), piece, piece);
			ParticleSystem prtcl = Main.m.partSysLoader.getParticleSystemType("Blood").spawnSystem(posX, posY + currentHeight / 2, posZ);
			prtcl.tex = t;
		}
	}

	public void hurtEntity(Entity e, double damage, double knockback, boolean withGore) {
		if (damage < 0) {
			e.health -= damage;
			if (e.health > e.maxHealth)
				e.health = e.maxHealth;
			return;
		}
		e.applyDamage(damage, withGore);
		e.applyForce(rotX, rotY, knockback, knockback, 0);
		if (e.maxHealth != 0 && e.health <= 0 && !e.dead)
			e.setDead(this, withGore);
	}

	public boolean isOnLoadedChunk() {
		if (this instanceof Player || this instanceof Camera || isParticle) {
			onLoadedChunk = true;
			return true;
		}
		Chunk c = Main.m.world.getChunk(posX, posZ);
		return onLoadedChunk = (c != null && c.loaded);
	}

	public void setDead(Entity source, boolean withGore) {
		dead = true;
		Projectile.projectileList.remove(this);
		if (!(this instanceof Player)) {
			Living.livingList.remove(this);
			entityList.remove(this);
		}
	}

	public double[] rayTrace(double yOff, double amount) {
		return Render.getCoordInFront(rotX, rotY, posX, posY + yOff, posZ, amount);
	}

	public Block rayTraceToBlock(double yOff, double sweep, double reach) {
		double radianX = Math.toRadians(rotX);
		double sinX = Math.sin(radianX);
		double cosX = Math.cos(radianX);
		double radianY = Math.toRadians(rotY);
		double sinY = Math.sin(radianY);
		double cosY = Math.cos(radianY);
		for (double i = 0; i < reach; i += sweep) {
			double[] pos = Render.getCoordInFront(sinX, cosX, sinY, cosY, posX, posY + yOff, posZ, i);
			Block block = Main.m.world.getBlock(pos[0], pos[1], pos[2], false);
			Block block2 = Main.m.world.getBlock(posX, posY + yOff, posZ, false);
			if (block != null && (block2 == null || (!block2.type.isSolid && block.type.isSolid))) {
				targetSide = getSideHit(pos, block);
				return block;
			}
		}
		return null;
	}

	public int getSideHit(double[] point, Block block) {
		byte result = -1;
		double smallest = 0;
		AABB bb = new AABB(block);
		double disToMaxX = Math.abs(point[0] - bb.maxX);
		double disToMinX = Math.abs(point[0] - bb.minX);
		double disToMaxY = Math.abs(point[1] - bb.maxY);
		double disToMinY = Math.abs(point[1] - bb.minY);
		double disToMaxZ = Math.abs(point[2] - bb.maxZ);
		double disToMinZ = Math.abs(point[2] - bb.minZ);
		smallest = disToMaxX;
		smallest = disToMinX < smallest ? disToMinX : smallest;
		smallest = disToMaxY < smallest ? disToMaxY : smallest;
		smallest = disToMinY < smallest ? disToMinY : smallest;
		smallest = disToMaxZ < smallest ? disToMaxZ : smallest;
		smallest = disToMinZ < smallest ? disToMinZ : smallest;
		if (smallest == disToMinY)
			result = 0;
		if (smallest == disToMaxY)
			result = 1;
		if (smallest == disToMinX)
			result = 2;
		if (smallest == disToMaxX)
			result = 3;
		if (smallest == disToMinZ)
			result = 4;
		if (smallest == disToMaxZ)
			result = 5;
		return result;
	}

	public double disToEntity(Entity en) {
		double[] point1 = { posX, posY, posZ };
		double[] point2 = { en.posX, en.posY, en.posZ };
		return Render.distanceSqrt(point1, point2);
	}

	public double disToEntityXZ(Entity en) {
		double xDis = en.posX - posX;
		double zDis = en.posZ - posZ;
		return Math.sqrt(zDis * zDis + xDis * xDis);
	}

	public void rotYToEntity(Entity en) {
		GL11.glRotated(-en.rotY, 0, 1, 0);
	}

	public void rotXToEntity(Entity en) {
		GL11.glRotated(-en.rotX, 1, 0, 0);
	}

	public void setTrig() {
		radianY = Math.toRadians(rotY);
		sinY = Math.sin(radianY);
		cosY = Math.cos(radianY);
		radianX = Math.toRadians(rotX);
		sinX = Math.sin(radianX);
		cosX = Math.cos(radianX);
	}
}
