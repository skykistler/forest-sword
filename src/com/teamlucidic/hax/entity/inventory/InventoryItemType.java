package com.teamlucidic.hax.entity.inventory;

import com.teamlucidic.hax.entity.particle.ParticleSystemType;
import com.teamlucidic.hax.entity.particle.projectile.ProjectileType;
import com.teamlucidic.hax.render.Texture;

public class InventoryItemType {
	public String name;
	public String type;
	public Texture tex;
	public Texture icon;
	public int vitriValue;
	public boolean requiresMouseButton;
	public boolean requiresDraw = true;
	public boolean drawHand;
	public Texture handOnUse;
	public Texture texOnUse;
	public String target;
	public double energyEffect;
	public double targetHealthEffect;
	public double selfHealthEffect;
	public int lightBehavior;
	public int uses;
	public int swingDelay;
	public ProjectileType projectile;
	public ParticleSystemType effect;
	public boolean usesExtraCode;

	public InventoryItemType(String n, String typ, Texture tx, Texture ico, int vv, boolean rmb, boolean rd, boolean dh, Texture hou, Texture tou, String tg, double ee, double the, double she, int lb, int use, int sd, ProjectileType prj, ParticleSystemType eff, boolean xtraCode) {
		name = n;
		type = typ;
		tex = tx;
		icon = ico;
		vitriValue = vv;
		requiresMouseButton = rmb;
		requiresDraw = rd;
		drawHand = dh;
		handOnUse = hou;
		texOnUse = tou;
		target = tg;
		energyEffect = ee;
		targetHealthEffect = the;
		selfHealthEffect = she;
		lightBehavior = lb;
		uses = use;
		swingDelay = sd;
		projectile = prj;
		effect = eff;
		usesExtraCode = xtraCode;
	}

	public InventoryItem getNewItem(Inventory i) {
		return new InventoryItem(i, this);
	}
}
