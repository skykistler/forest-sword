package com.teamlucidic.hax.entity;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.entity.particle.ParticleSystem;
import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.render.Texture;

public class Rain extends ParticleSystem {
	public ArrayList<RainDrop> rainDropList = new ArrayList<RainDrop>();
	public ArrayList<RainDrop> fatDrops = new ArrayList<RainDrop>();
	public int max = 500;

	public Rain() {
		super(0, 0, 0, null, 0x88FF7777, -1, 10, 0, -.9, 0, 0, 0, .22, 0, 0, 0, 75, 5.5, false, true);
		rainDropList.ensureCapacity(max);
	}

	public void update() {
		if (!continuous && entityTicks > life) {
			setDead();
			Main.m.gc();
			return;
		}
		double[] coord = Main.m.camera.rayTrace(0, 1);
		posX = coord[0];
		posZ = coord[2];
		Block b = Main.m.world.getTopBlock(Main.m.camera.posX, Main.m.camera.posZ, true, false);
		if (b != null)
			posY = b.posY - Main.m.camera.posY > 8 ? Main.m.world.height : 10 + Main.m.camera.posY;
		if (rainDropList.size() < max)
			for (int i = 0; i < rate; i++)
				rainDropList.add(spawnRainDrop());

		for (RainDrop drop : rainDropList)
			if (drop.needsReset)
				drop.reset(posX + (Render.rand.nextDouble() * 2 - 1) * posQuiver, posY + getNegPosRand() * 2, posZ + getNegPosRand() * posQuiver, speedForwardParticle, speedVertParticle, speedVertQuiver, sizeOfParticle + getNegPosRand() * .07, particleQuiver);

		if (posY <= 0) {
			if (fatDrops.size() < 4 && Main.m.gameTicks % (Render.rand.nextInt(30) + 1) == 0)
				fatDrops.add(spawnFatDrop());

			for (RainDrop drop : fatDrops)
				if (drop.needsReset) {
					double[] coord2 = Main.m.camera.rayTrace(0, 1);
					drop.reset(coord2[0], Main.m.camera.posY + 3 + getNegPosRand() + 2, coord2[2], speedForwardParticle, speedVertParticle - .2, speedVertQuiver, sizeOfParticle + getNegPosRand() * .02, false);
					drop.moveStrafe = getNegPosRand() * .7;
					drop.rotY = Main.m.camera.rotY;
				}
		}
	}

	public void render() {
		Texture.unbind(false);
		Main.m.modeler.setHexColor(color);

		Main.m.modeler.start();
		for (RainDrop rainDrop : rainDropList)
			rainDrop.render();
		GL11.glLineWidth(1.55f);
		Main.m.modeler.finish(GL11.GL_LINES);

		Main.m.modeler.start();
		for (RainDrop rainDrop : fatDrops)
			rainDrop.render();
		GL11.glLineWidth(8f);
		Main.m.modeler.finish(GL11.GL_LINES);
	}

	public RainDrop spawnRainDrop() {
		return new RainDrop(posX + (Render.rand.nextDouble() * 2 - 1) * posQuiver, posY + getNegPosRand() * 5, posZ + getNegPosRand() * posQuiver, speedForwardParticle, speedVertParticle, speedVertQuiver, sizeOfParticle + getNegPosRand() * .07, rotX, rotY, particleQuiver, gravityOnParticle);
	}

	public RainDrop spawnFatDrop() {
		double[] coord = Main.m.camera.rayTrace(0, 1);
		RainDrop drop = new RainDrop(coord[0], Main.m.camera.posY + 3 + getNegPosRand() + 2, coord[2], speedForwardParticle, speedVertParticle - .2, speedVertQuiver, sizeOfParticle + getNegPosRand() * .02, rotX, rotY, false, gravityOnParticle);
		drop.moveStrafe = getNegPosRand() * .7;
		drop.rotY = Main.m.camera.rotY;
		return drop;
	}

	public void setDead() {
		super.setDead(null, false);
		for (RainDrop drop : rainDropList)
			drop.setDead(null, false);
		for (RainDrop drop : fatDrops)
			drop.setDead(null, false);
		rainDropList.clear();
		fatDrops.clear();
	}

	public class RainDrop extends Entity {
		public int afterCollisionLife = 3;
		public boolean needsReset;

		public RainDrop(double x, double y, double z, double spdForw, double spdVert, double spdVertQ, double szeP, double rtX, double rtY, boolean quiver, boolean gravity) {
			posX = x;
			posY = y;
			posZ = z;
			rotX = rtX;
			rotY = rtY;
			if (quiver)
				quiverPosition();
			baseHeight = szeP + getNegPosRand() * 0.05;
			bb = new AABB(this, 0.1, 0.1);
			setTrig();
			double speedForward = spdForw + getNegPosRand() * 0.05;
			applyForce(speedForward * getNegPosRand(), 0, speedForward * getNegPosRand());
			velY += spdVert + getNegPosRand() * spdVertQ;
			affectedByGravity = gravity;
			isParticle = true;
			entityTicks += Render.rand.nextInt(30);
		}

		public void reset(double x, double y, double z, double spdForw, double spdVert, double spdVertQ, double szeP, boolean quiver) {
			posX = x;
			posY = y;
			posZ = z;
			if (quiver)
				quiverPosition();
			baseHeight = szeP + getNegPosRand() * 0.05;
			setTrig();
			double speedForward = spdForw + getNegPosRand() * 0.05;
			applyForce(speedForward * getNegPosRand(), 0, speedForward * getNegPosRand());
			velY += spdVert + getNegPosRand() * spdVertQ;
			needsReset = false;
		}

		public void update() {
			Block b = Main.m.world.getBlock(posX, posY, posZ, false);
			if (b != null && b.type.isLiquid)
				needsReset = true;
			if (blockCollideOnY)
				if (afterCollisionLife > 0)
					afterCollisionLife--;
				else
					needsReset = true;
			if (!onLoadedChunk)
				needsReset = true;
		}

		public void render() {
			if (needsReset)
				return;
			Main.m.modeler.addVertex(posX, posY - baseHeight / 2, posZ);
			Main.m.modeler.addVertex(posX, posY + baseHeight / 2, posZ);
		}
	}

}
