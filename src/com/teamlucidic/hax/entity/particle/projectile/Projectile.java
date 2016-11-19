package com.teamlucidic.hax.entity.particle.projectile;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.AABB;
import com.teamlucidic.hax.entity.Entity;
import com.teamlucidic.hax.render.Model;
import com.teamlucidic.hax.render.Texture;

public class Projectile extends Entity {
	public static ArrayList<Projectile> projectileList = new ArrayList<Projectile>();

	public Entity owner;
	public ProjectileType type;
	public Texture tex;
	public Model model;
	public double speed, originalSpeed;
	public int lifetime;
	public double damage;
	public double knockback;
	public boolean causesGore;
	public int blockCollisionType;
	public int entityCollisionType;
	public boolean stuck;
	public boolean rotateYToCam = true;
	public boolean rotateXToCam = true;

	public Projectile(Entity en, ProjectileType pt) {
		super();
		owner = en;
		type = pt;
		tex = pt.tex;
		model = pt.model;
		speed = pt.speed + Math.abs(Math.abs(owner.posX - owner.prevPosX) + Math.abs(owner.posZ - owner.prevPosZ));
		originalSpeed = speed;
		baseHeight = currentHeight = targetHeight = pt.baseHeight;
		lifetime = pt.lifetime;
		damage = pt.damage;
		knockback = pt.knockback;
		causesGore = pt.causesGore;
		blockCollisionType = pt.blockCollisionType;
		entityCollisionType = pt.entityCollisionType;
		affectedByGravity = pt.gravity;
		isProjectile = true;
		bb = new AABB(this, baseHeight * .75, baseHeight * .75);
		projectileList.add(this);
	}

	public void update() {
		if (entityTicks >= lifetime && (blockCollisionType != 3 || lifetime != 1)) {
			if (blockCollisionType == 3)
				explode();
			else
				setDead(null, false);
		}

		int collidingAmount = 0;
		for (AABB bb2 : bb.possiblyCollidingBBList)
			if (bb.intersectsWith(bb2)) {
				collidingAmount++;
				if (bb2.ent) {
					if (bb2.en != owner) {
						if (entityCollisionType != 3 || Math.max(Math.max(velX, velY), velZ) > .1)
							owner.hurtEntity(bb2.en, damage, knockback, causesGore);
						if (entityCollisionType != 3)
							setDead(null, false);
					}
					if (entityCollisionType == 3)
						explode();
					return;
				}
				if (bb2.blck && !stuck) {
					if (blockCollisionType == 1) {
						affectedByGravity = false;
						velX = 0;
						velY = 0;
						velZ = 0;
						moveForward = 0;
						moveVert = 0;
						speed = 0;
						stuck = true;
						return;
					}
					if (blockCollisionType == 3 && lifetime == 1)
						explode();
					if (blockCollisionType == 2)
						setDead(null, false);
				}
			}

		if (!stuck) {
			if (!affectedByGravity) {
				moveForward += (speed / 60) * cosX;
				moveVert += (speed / 60) * -sinX;
				speed -= originalSpeed / lifetime;
			} else if (entityTicks == 0)
				applyForce(rotX, rotY, speed, speed, 0);
		} else if (collidingAmount == 0)
			setDead(null, false);

	}

	public void render() {
		if (entityTicks == 0)
			return;
		Main.m.modeler.setHexColor(0xFFFFFFFF);
		if (model == null) {
			GL11.glPushMatrix();
			GL11.glTranslated(posX, posY, posZ);
			if (rotateYToCam)
				rotYToEntity(Main.m.camera);
			else
				GL11.glRotated(-rotY, 0, 1, 0);
			if (rotateXToCam)
				rotXToEntity(Main.m.camera);
			else
				GL11.glRotated(-rotX, 1, 0, 0);
			double x = -(baseHeight / 2D);
			double h = baseHeight;
			double w = baseHeight;
			Main.m.modeler.setHexColor(0xFFFFFFFF);
			tex.bind();
			Main.m.modeler.drawRect3D(x, 0, 0, w, h);
			GL11.glPopMatrix();
		} else {
			model.getPart("body").setRotation(0, rotY, 0);
			model.render(posX, posY, posZ);
		}
	}

	public void spawnAtOwner(String hand, boolean withQuiver) {
		spawnAtEntity(owner, .4, withQuiver, hand);
	}

	public void explode() {
		Main.m.world.explodeAt((int) Math.round(damage), posX, posY, posZ, !Main.m.gameMode.contains("asunder"));
		setDead(null, false);
	}
}
