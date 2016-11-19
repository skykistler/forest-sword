package com.teamlucidic.hax.entity;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.entity.living.Living;
import com.teamlucidic.hax.render.Texture;

public class AABB {
	public ArrayList<AABB> possiblyCollidingBBList;
	public Entity en;
	public Block bl;
	public double xOffset, yOffset, zOffset;
	public double minX, maxX, minY, maxY, minZ, maxZ;
	public boolean ent, blck;

	public AABB(double x, double y, double z, double xOff, double yOff, double zOff) {
		xOffset = xOff;
		zOffset = zOff;
		minX = x - xOffset;
		maxX = x + xOffset;
		minY = y;
		maxY = y + yOff;
		minZ = z - zOffset;
		maxZ = z + zOffset;
	}

	public AABB(Entity e, double xOff, double zOff) {
		this((double) e.posX, (double) e.posY, (double) e.posZ, xOff, (double) e.currentHeight, zOff);
		en = e;
		ent = true;
		possiblyCollidingBBList = new ArrayList<AABB>();
	}

	public AABB(Block b) {
		this(b.posX + .5, b.posY, b.posZ + .5, .5, 1, .5);
		bl = b;
		blck = true;
	}

	public void update() {
		if (!ent)
			return;
		double fX = (double) en.posX;
		double fY = (double) en.posY;
		double fZ = (double) en.posZ;
		minX = fX - xOffset;
		maxX = fX + xOffset;
		minY = fY;
		maxY = fY + (double) en.currentHeight + yOffset + (en == Main.m.camera.livingTarget ? .04f : 0);
		minZ = fZ - zOffset;
		maxZ = fZ + zOffset;
	}

	public void render() {
		GL11.glLineWidth(8f);

		Texture.unbind(false);
		Main.m.modeler.setHexColor(0xFFFFFFFF);

		Main.m.modeler.start();
		Main.m.modeler.addVertex(minX, minY, minZ);
		Main.m.modeler.addVertex(minX, minY, maxZ);
		Main.m.modeler.addVertex(maxX, minY, maxZ);
		Main.m.modeler.addVertex(maxX, minY, minZ);
		Main.m.modeler.addVertex(minX, minY, minZ);
		Main.m.modeler.finish(GL11.GL_LINE_STRIP);

		Main.m.modeler.start();
		Main.m.modeler.addVertex(maxX, maxY, minZ);
		Main.m.modeler.addVertex(minX, maxY, minZ);
		Main.m.modeler.addVertex(minX, maxY, maxZ);
		Main.m.modeler.addVertex(maxX, maxY, maxZ);
		Main.m.modeler.addVertex(maxX, maxY, minZ);
		Main.m.modeler.finish(GL11.GL_LINE_STRIP);

		Main.m.modeler.start();
		Main.m.modeler.addVertex(minX, minY, minZ);
		Main.m.modeler.addVertex(minX, maxY, minZ);

		Main.m.modeler.addVertex(minX, minY, maxZ);
		Main.m.modeler.addVertex(minX, maxY, maxZ);

		Main.m.modeler.addVertex(maxX, minY, maxZ);
		Main.m.modeler.addVertex(maxX, maxY, maxZ);

		Main.m.modeler.addVertex(maxX, minY, minZ);
		Main.m.modeler.addVertex(maxX, maxY, minZ);
		Main.m.modeler.finish(GL11.GL_LINES);
	}

	public boolean intersectsWith(AABB bb) {
		if (maxX < bb.minX || minX > bb.maxX)
			return false;
		if (maxY < bb.minY || minY > bb.maxY)
			return false;
		if (maxZ < bb.minZ || minZ > bb.maxZ)
			return false;
		return true;
	}

	public boolean intersectsWith(double x, double y, double z) {
		if (x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ)
			return true;
		return false;
	}

	public void setCollidingBoxes(double x, double y, double z) {
		double mix = minX + x - 1;
		double max = maxX + x + 1;
		double miy = minY + y - 1;
		double may = maxY + y + 1;
		double miz = minZ + z - 1;
		double maz = maxZ + z + 1;
		possiblyCollidingBBList = new ArrayList<AABB>();
		for (int x1 = (int) mix; x1 < max; x1++)
			for (int y1 = (int) miy; y1 < may; y1++)
				for (int z1 = (int) miz; z1 < maz; z1++)
					addBlockToColliding(Main.m.world.getBlock(x1, y1, z1, false));

		if (en != null && (en.isLiving || en.isProjectile))
			for (Living living : Living.livingList) {
				if (living.onLoadedChunk)
					possiblyCollidingBBList.add(living.bb);
			}
	}

	public void addBlockToColliding(Block bbBlk) {
		if (bbBlk != null && bbBlk.type.isSolid)
			possiblyCollidingBBList.add(new AABB(bbBlk));
	}

	public double[] pushOutOfSolids(double x, double y, double z) {
		if (!ent)
			return null;
		double offX = (double) x, offY = (double) y, offZ = (double) z;

		setCollidingBoxes(offX, offY, offZ);

		for (AABB bb : possiblyCollidingBBList)
			offY = bb.getLargestYOffset(this, offY);
		translate(0, offY, 0);

		for (AABB bb : possiblyCollidingBBList)
			offX = bb.getLargestXOffset(this, offX);
		translate(offX, 0, 0);

		for (AABB bb : possiblyCollidingBBList)
			offZ = bb.getLargestZOffset(this, offZ);
		translate(0, 0, offZ);

		double[] result = { offX, offY, offZ };
		return result;
	}

	public double getLargestXOffset(AABB bb, double x) {
		if (bb.maxY <= minY || bb.minY >= maxY || bb.maxZ <= minZ || bb.minZ >= maxZ)
			return x;

		if (x > 0 && bb.maxX <= minX) {
			double result = minX - bb.maxX;
			if (result < x)
				x = result;
		}
		if (x < 0 && bb.minX >= maxX) {
			double result = maxX - bb.minX;
			if (result > x)
				return result;
		}

		return x;
	}

	public double getLargestYOffset(AABB bb, double y) {
		if (bb.maxX <= minX || bb.minX >= maxX || bb.maxZ <= minZ || bb.minZ >= maxZ)
			return y;

		if (y > 0 && bb.maxY <= minY) {
			double result = minY - bb.maxY;
			if (result < y)
				y = result;
		}
		if (y < 0 && bb.minY >= maxY) {
			double result = maxY - bb.minY;
			if (result > y)
				return result;
		}

		return y;
	}

	public double getLargestZOffset(AABB bb, double z) {
		if (bb.maxX <= minX || bb.minX >= maxX)
			return z;
		if (bb.maxY <= minY || bb.minY >= maxY)
			return z;

		if (z > 0 && bb.maxZ <= minZ) {
			double result = minZ - bb.maxZ;
			if (result < z)
				z = result;
		}
		if (z < 0 && bb.minZ >= maxZ) {
			double result = maxZ - bb.minZ;
			if (result > z)
				return result;
		}

		return z;
	}

	public void translate(double x, double y, double z) {
		minX += x;
		maxX += x;
		minY += y;
		maxY += y;
		minZ += z;
		maxZ += z;
	}

	public double getFarthestFromZero(double x, double y, double z) {
		x = Math.abs(x);
		y = Math.abs(y);
		z = Math.abs(z);
		double result = x;
		if (y > result)
			result = y;
		if (z > result)
			result = z;
		return result;
	}

}
