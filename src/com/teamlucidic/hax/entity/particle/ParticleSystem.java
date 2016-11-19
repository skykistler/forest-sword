package com.teamlucidic.hax.entity.particle;

import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.AABB;
import com.teamlucidic.hax.entity.Entity;
import com.teamlucidic.hax.render.Texture;

public class ParticleSystem extends Entity {
	public Texture tex;
	public int color;
	public int life;
	public boolean continuous;
	public int rate;
	public double speedForwardParticle;
	public double speedVertParticle;
	public double speedVertQuiver;
	public int lifeOfParticle;
	public int lifeQuiver;
	public double sizeOfParticle;
	public double particleSizeChange;
	public boolean particleQuiver;
	public boolean gravityOnParticle;

	public ParticleSystem(double x, double y, double z, Texture t, int col, int lfe, int rte, double spdForwP, double spdVertP, double spdVertPQ, int lfeP, int lfQ, double szeP, double pSzeC, double rtX, double rtY, double rotQ, double posQ, boolean pQuiv, boolean gravity) {
		posX = x;
		posY = y;
		posZ = z;
		tex = t;
		color = col;
		life = lfe;
		if (life < 0)
			continuous = true;
		rate = rte;
		speedForwardParticle = spdForwP;
		speedVertParticle = spdVertP;
		speedVertQuiver = spdVertPQ;
		lifeOfParticle = lfeP;
		lifeQuiver = lfQ;
		sizeOfParticle = szeP;
		particleSizeChange = pSzeC;
		rotX = rtX;
		rotY = rtY;
		posQuiver = posQ;
		rotQuiver = rotQ;
		particleQuiver = pQuiv;
		gravityOnParticle = gravity;
		affectedByGravity = false;
		bb = new AABB(this, 0, 0);
	}

	public void update() {
		if (!continuous && entityTicks > life) {
			setDead(null, false);
			return;
		}

		for (int i = 0; i < rate; i++)
			spawnParticle();
	}

	public Particle spawnParticle() {
		return new Particle(this, posX, posY, posZ, speedForwardParticle, speedVertParticle, speedVertQuiver, lifeOfParticle, lifeQuiver, sizeOfParticle, rotX, rotY, rotQuiver, posQuiver, particleQuiver, gravityOnParticle);
	}

}

class Particle extends Entity {
	public ParticleSystem parent;
	public int life;

	public Particle(ParticleSystem parent, double x, double y, double z, double spdForw, double spdVert, double spdVertQ, int lfe, int lfQ, double szeP, double rtX, double rtY, double rotQ, double posQ, boolean quiver, boolean gravity) {
		this.parent = parent;
		posX = x;
		posY = y;
		posZ = z;
		rotX = rtX;
		rotY = rtY;
		rotQuiver = rotQ;
		posQuiver = posQ;
		if (quiver) {
			quiverPosition();
			quiverRotation();
		}
		life = lfe + (int) (getNegPosRand() * lfQ);
		baseHeight = szeP + getNegPosRand() * 0.05;
		bb = new AABB(this, 0.1, 0.1);
		setTrig();
		double speedForward = spdForw + getNegPosRand() * 0.05;
		applyForce(speedForward * getNegPosRand(), 0, speedForward * getNegPosRand());
		velY += spdVert + getNegPosRand() * spdVertQ;
		affectedByGravity = gravity;
	}

	public void update() {
		if (entityTicks >= life)
			setDead(null, false);
	}

	public void render() {
		GL11.glPushMatrix();
		GL11.glTranslated(posX, posY, posZ);
		rotYToEntity(Main.m.camera);
		rotXToEntity(Main.m.camera);
		double w = baseHeight + (entityTicks / (double) life) * parent.particleSizeChange;
		double x = -(w / 2);
		parent.tex.bind();
		Main.m.modeler.setHexColor(parent.color);
		Main.m.modeler.drawRect3D(x, 0, 0, w, w);
		GL11.glPopMatrix();
	}
}
