package com.teamlucidic.hax.entity;

import java.util.ArrayList;

import com.teamlucidic.hax.render.Render;

public class Flock {
	public static ArrayList<Flock> flocks = new ArrayList<Flock>();
	public int numberOfSpots, width, centerX, centerZ;
	public double speed;
	public ArrayList<Spot> spots = new ArrayList<Spot>();

	public Flock(int numOfSpots, double spd, int w, int x, int z) {
		numberOfSpots = numOfSpots;
		speed = spd / 60;
		width = w;
		centerX = x;
		centerZ = z;

		for (int i = 0; i < numberOfSpots; i++)
			spots.add(new Spot(Render.rand.nextDouble() * width + centerX - width / 2, Render.rand.nextDouble() * width + centerZ - width / 2));

		flocks.add(this);
	}

	public void update() {
		for (Spot s : spots)
			s.update();
	}

	public static void updateAllFlocks() {
		for (Flock f : flocks)
			f.update();
	}

	public class Spot {
		public double posX, posZ;
		public double nextPosX, nextPosZ, cos, sin;

		public Spot(double x, double z) {
			posX = nextPosX = x;
			posZ = nextPosZ = z;
		}

		public void update() {
			if (Math.abs(posX - nextPosX) < 2 && Math.abs(posZ - nextPosZ) < 2) {
				nextPosX = Render.rand.nextDouble() * width + centerX - width / 2;
				nextPosZ = Render.rand.nextDouble() * width + centerZ - width / 2;
			}
			double radian = Math.toRadians(Render.atan2Deg((nextPosX - posX), -(nextPosZ - posZ)));
			sin = Math.sin(radian);
			cos = Math.cos(radian);
			posX += (sin * speed);
			posZ += -(cos * speed);
		}

		public double distance(Entity en) {
			return Math.sqrt((en.posX - posX) * (en.posX - posX) + (en.posZ - posZ) * (en.posZ - posZ));
		}
	}
}
