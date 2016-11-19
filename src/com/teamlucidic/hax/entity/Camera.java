package com.teamlucidic.hax.entity;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.living.Living;
import com.teamlucidic.hax.render.Render;

public class Camera extends Living {
	public Living livingTarget;
	public boolean thirdperson = false;

	public Camera() {
		super("Camera");
	}

	public void update() {
		if (livingTarget != null) {
			posX = livingTarget.posX;
			posY = livingTarget.posY + livingTarget.currentHeight + livingTarget.bobX * .07;
			posZ = livingTarget.posZ;
			rotX = livingTarget.rotX + livingTarget.bobX;
			rotY = livingTarget.rotY + livingTarget.bobZ;
			rotZ = livingTarget.rotZ;
			bobX = livingTarget.bobX;
			bobZ = livingTarget.bobZ;
			inSolid = livingTarget.inSolid;
			inBlock = livingTarget.inBlock;

			if (thirdperson) {
				double[] newpos = Render.getCoordInFront(rotX, rotY, posX, posY, posZ, -2);
				posX = newpos[0];
				posY = newpos[1];
				posZ = newpos[2];
			}
		}
	}

	public void postUpdate() {

	}

	public void setTarget(Entity e) {
		super.setTarget(e);
		livingTarget = (Living) e;
	}

	public void applyDamage(double damage, boolean withGore) {

	}

	public boolean isPointInCameraXZ(double x, double z, double cushion) {
		boolean t = true;
		if (t) {
			return true;
		} else {
			double ang = Render.atan2Deg((Main.m.camera.posX - x), (Main.m.camera.posZ - z));
			if (ang % 360 >= rotY)
				return true;
			return false;
		}
	}

	public boolean isBoxInCameraXZ(AABB bb, double cushion) {
		if (isPointInCameraXZ(bb.minX, bb.minZ, cushion))
			return true;
		if (isPointInCameraXZ(bb.minX, bb.maxZ, cushion))
			return true;
		if (isPointInCameraXZ(bb.maxX, bb.maxZ, cushion))
			return true;
		if (isPointInCameraXZ(bb.maxX, bb.minZ, cushion))
			return true;

		return false;
	}

}
