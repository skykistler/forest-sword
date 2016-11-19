package com.teamlucidic.hax.world.generator;

import com.teamlucidic.hax.render.gui.menu.LoadingScreen;

public class BasicHillsGenerator {
	public long seed;
	public int startX, startZ;
	public int width;
	public int height;
	public int octaveCount = 4;
	public double[][] octaves;
	public int[] noise;
	public int smoothness = 4; //7
	public int scale = 6; //4
	public double exponent = .68; //.5

	public BasicHillsGenerator(long sd, int w, int h) {
		seed = sd;
		width = w;
		height = h;
		startX = startZ = width / 2;
	}

	public int[] generateHeightMap() {
		noise = new int[width * width];
		octaves = new double[octaveCount][width * width];
		generateOctaves();
		LoadingScreen.setPercentDone(.03);
		blend();
		LoadingScreen.setPercentDone(.06);
		smooth();
		LoadingScreen.setPercentDone(.09);
		check();
		return noise;
	}

	public void generateOctaves() {
		for (int i = 1; i < octaveCount + 1; i++) {
			for (int x = 0; x < width; x++)
				for (int z = 0; z < width; z++)
					octaves[(i - 1)][(x + z * width)] = getRand(x / i, z / i);
		}

		double[][] oct = new double[octaveCount][width * width];
		System.arraycopy(octaves, 0, oct, 0, octaves.length);
		for (int i = 0; i < octaveCount; i++)
			for (int e = 0; e < width * width; e++) {
				double d1 = getValueInArray(oct[i], e - width);
				double d2 = getValueInArray(oct[i], e + width);
				double d3 = getValueInArray(oct[i], e - 1);
				double d4 = getValueInArray(oct[i], e + 1);
				octaves[i][e] = interpolateValues(interpolateValues(d1, d2, 0.5D), interpolateValues(d3, d4, 0.5D), 0.5D);
			}
	}

	public void blend() {
		double persistance = 1D;
		double amplitude = 1.0D;
		for (int i = octaveCount - 1; i > 0; i--) {
			amplitude = Math.pow(persistance, i);
			for (int e = 0; e < width * width; e++) {
				noise[e] += (int) ((octaves[i][e] * height * .7) * amplitude);
			}
		}
	}

	public void smooth() {
		int scan = (int) Math.sqrt(noise.length);
		for (int i = 0; i < smoothness; i++)
			for (int z = 0; z < scan; z++)
				for (int x = 0; x < scan; x++)
					noise[(z * scan + x)] = smoothValue(x, z);
	}

	public void check() {
		for (int i = 0; i < noise.length; i++)
			noise[i] = (int) Math.pow(noise[i], exponent);
		for (int z = 0; z < width; z++)
			for (int x = 0; x < width; x++) {
				int index = z * width + x;
				int value = noise[index];
				value = value < 2 ? 2 : value;
				value = value > height ? height : value;
				noise[index] = value;
			}
	}

	public double getValueInArray(double[] array, int index) {
		if (index >= array.length)
			return array[(array.length - 1)];
		if (index < 0)
			return array[0];
		return array[index];
	}

	public int smoothValue(int x, int z) {
		int farsides = getNoiseValue(x - 2, z) + getNoiseValue(x + 2, z) + getNoiseValue(x, z - 2) + getNoiseValue(x, z + 2);
		int corners = getNoiseValue(x - 1, z - 1) + getNoiseValue(x + 1, z - 1) + getNoiseValue(x - 1, z + 1) + getNoiseValue(x + 1, z + 1);
		int sides = getNoiseValue(x - 1, z) + getNoiseValue(x + 1, z) + getNoiseValue(x, z - 1) + getNoiseValue(x, z + 1);
		int center = getNoiseValue(x, z);
		farsides /= 32;
		corners /= 16;
		sides /= 8;
		center /= 4;
		return farsides + corners + sides + center;
	}

	public double interpolateValues(double a, double b, double x) {
		x = x > 1.0D ? 1.0D : x;
		x = x < 0.0D ? 0.0D : x;

		double xp = x * 3.141592741012573D;
		double xc = 1.0D - Math.cos(xp) * 0.5D;

		return a * (1.0D - xc) + b * xc;
	}

	public int getNoiseValue(int x, int z) {
		int scan = (int) Math.sqrt(noise.length);
		if ((x >= 0) && (x < scan) && (z >= 0) && (z < scan))
			return noise[(z * scan + x)];
		return 0;
	}

	public double getRand(int x, int z) {
		x /= scale;
		z /= scale;
		int n = x + z * 57 + (int) seed * 19;
		n = n << 13 ^ n;
		double nd = (n * (n * n * 15731 + 789221) + 1376312589 & 0x7FFFFFFF) / 1073741824.0D;
		return nd /= 2d;
	}
}