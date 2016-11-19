package com.teamlucidic.hax.entity.particle;

import com.teamlucidic.hax.render.Texture;

public class ParticleSystemType {
	public Texture tex;
	public int color;
	public int life;
	public int rate;
	public double speedForwardParticle;
	public double speedVertParticle;
	public double speedVertQuiver;
	public int lifeOfParticle;
	public int lifeQuiver;
	public double sizeOfParticle;
	public double particleSizeChange;
	public double rotX, rotY, rotQuiver, posQuiver;
	public boolean particleQuiver;
	public boolean gravityOnParticle;
	public double xSpawnOffset, ySpawnOffset, zSpawnOffset;

	public ParticleSystemType(Texture t, int col, int lfe, int rte, double spFP, double spVP, double spVQ, int lfeP, int lfeQ, double sP, double pSzeC, double rX, double rY, double rQ, double posQ, boolean prtQ, boolean gravity, double xso, double yso, double zso) {
		tex = t;
		color = col;
		life = lfe;
		rate = rte;
		speedForwardParticle = spFP;
		speedVertParticle = spVP;
		speedVertQuiver = spVQ;
		lifeOfParticle = lfeP;
		sizeOfParticle = sP;
		particleSizeChange = pSzeC;
		rotX = rX;
		rotY = rY;
		rotQuiver = rQ;
		posQuiver = posQ;
		particleQuiver = prtQ;
		gravityOnParticle = gravity;
		xSpawnOffset = xso;
		ySpawnOffset = yso;
		zSpawnOffset = zso;
	}

	public ParticleSystem spawnSystem(double x, double y, double z) {
		return new ParticleSystem(x + xSpawnOffset, y + ySpawnOffset, z + zSpawnOffset, tex, color, life, rate, speedForwardParticle, speedVertParticle, speedVertQuiver, lifeOfParticle, lifeQuiver, sizeOfParticle, particleSizeChange, rotX, rotY, rotQuiver, posQuiver, particleQuiver, gravityOnParticle);
	}
}
