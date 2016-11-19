package com.teamlucidic.hax.entity.particle.projectile;

import com.teamlucidic.hax.entity.Entity;
import com.teamlucidic.hax.render.Model;
import com.teamlucidic.hax.render.Texture;

public class ProjectileType {
	public Texture tex;
	public Model model;
	public double speed;
	public double baseHeight;
	public int lifetime;
	public double damage;
	public double knockback;
	public boolean causesGore;
	public int blockCollisionType;
	public int entityCollisionType;
	public boolean gravity;
	public boolean quiver;
	public boolean rotateYToCam = true;
	public boolean rotateXToCam = true;

	public ProjectileType(Texture t, Model m, double s, double h, int life, double dam, double kb, boolean cg, int bColType, int eColType, boolean g, boolean q) {
		tex = t;
		model = m;
		speed = s;
		baseHeight = h;
		lifetime = life;
		damage = dam;
		knockback = kb;
		causesGore = cg;
		blockCollisionType = bColType;
		entityCollisionType = eColType;
		gravity = g;
		quiver = q;
	}

	public Projectile newProjectile(Entity owner) {
		return new Projectile(owner, this);
	}

	public Projectile spawnProjectileAtOwner(Entity owner, String hand) {
		Projectile p = new Projectile(owner, this);
		p.spawnAtOwner(hand, quiver);
		return p;
	}
}
