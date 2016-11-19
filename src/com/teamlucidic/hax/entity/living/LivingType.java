package com.teamlucidic.hax.entity.living;

import java.util.ArrayList;
import java.util.HashMap;

import com.teamlucidic.hax.Sound;
import com.teamlucidic.hax.entity.Flock;
import com.teamlucidic.hax.entity.inventory.InventoryItemType;
import com.teamlucidic.hax.render.Texture;

public class LivingType {
	public String name;
	public String desc;
	public ArrayList<InventoryItemType> inventory;
	public String attitude;
	public String model;
	public Texture[] textures;
	public Texture icon;
	public Sound soundOnLook;
	public int soundOnLookTimer;
	public Sound soundNearAttack;

	public double[] dimensions;

	public double walkSpeed;
	public double sprintSpeed;
	public double jumpHeight;
	public double swimSpeed;
	public double crouchSpeed;
	public double crouchHeight;
	public double healthRegen;
	public int maxEnergy;
	public double energyRegen;
	public double punchDamage;
	public double knockback;
	public int hitDelay;

	public int maxHealth;

	public String attackTargets;
	public String followTargets;
	public String parasites;
	public HashMap<Integer, Texture> parasitesTex;
	public boolean wanders;
	public boolean flocks;
	public boolean hops;
	public double interestDistance;
	public Flock flock;

	public LivingType(String n, String d, ArrayList<InventoryItemType> inven, String att, String mod, Texture[] t, Texture i, Sound soundOL, int soundOLT, Sound soundNA, double[] dimen, double walkS, double sprintS, double jumpH, double swimS, double crouchS, double crouchH, double healthR, int maxE, double energyR, double punchD, double knockB, int hitD, int maxH, String attactTar, String followTar, String psites, HashMap<Integer, Texture> psitesTex, boolean wander, boolean flck, boolean h, double interestD) {
		name = n;
		desc = d;
		inventory = inven;
		attitude = att;
		model = mod;
		textures = t;
		icon = i;
		soundOnLook = soundOL;
		soundOnLookTimer = soundOLT;
		soundNearAttack = soundNA;
		dimensions = dimen;
		walkSpeed = walkS;
		sprintSpeed = sprintS;
		jumpHeight = jumpH;
		swimSpeed = swimS;
		crouchSpeed = crouchS;
		crouchHeight = crouchH;
		healthRegen = healthR;
		maxEnergy = maxE;
		energyRegen = energyR;
		punchDamage = punchD;
		knockback = knockB;
		hitDelay = hitD;
		maxHealth = maxH;
		maxEnergy = maxE;
		attackTargets = attactTar;
		followTargets = followTar;
		parasites = psites;
		parasitesTex = psitesTex;
		wanders = wander;
		flocks = flck;
		hops = h;
		interestDistance = interestD;
	}

	public Living spawnNewAt(double x, double y, double z) {
		Living l = new Living(this);
		l.spawnAtPosition(x, y, z);
		return l;
	}

	public Living spawnNewAt(double x, double z) {
		Living l = spawnNewAt(x, 0, z);
		l.moveToTopWhenLoaded = true;
		return l;
	}
}
