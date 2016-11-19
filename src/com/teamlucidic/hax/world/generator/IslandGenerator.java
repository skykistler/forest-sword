package com.teamlucidic.hax.world.generator;

import java.util.Random;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.render.gui.menu.LoadingScreen;
import com.teamlucidic.hax.world.Chunk;

public class IslandGenerator extends BasicHillsGenerator {
	public double islandDensity = .7;

	public IslandGenerator(long sd, int w, int h) {
		super(sd, w, h);
	}

	public int[] generateHeightMap() {
		smoothness = 3;
		octaveCount = 16;
		noise = new int[width * width];
		octaves = new double[octaveCount][width * width];
		generateOctaves();
		LoadingScreen.setPercentDone(.02);
		blend();
		LoadingScreen.setPercentDone(.04);
		islandMask1();
		LoadingScreen.setPercentDone(.06);
		smooth();
		LoadingScreen.setPercentDone(.08);
		check();
		return noise;
	}

	public void islandMask1() {
		double[] islandMask = new double[width * width];
		Random rand = new Random(seed);
		double range = .2;
		int x = startX = (int) (rand.nextDouble() * width * range + width * (1 - range) / 2);
		int z = startZ = (int) (rand.nextDouble() * width * range + width * (1 - range) / 2);
		int index = x + z * width;
		islandMask[index] += 1;
		int direction = 0;
		int prevIndex = 0;
		int loops = (int) Math.pow(Main.m.world.widthInChunks * islandDensity * Chunk.width * Chunk.width, 2);
		for (int i = 0; i < loops; i++) {
			prevIndex = index;
			direction = (int) Math.floor(Render.rand.nextDouble() * 8);
			switch (direction) {
			case 0:
				index += 1;
				break;
			case 1:
				index -= width;
				break;
			case 2:
				index -= 1;
				break;
			case 3:
				index += width;
				break;
			case 4:
				index += 1 - width;
				break;
			case 5:
				index -= 1 - width;
				break;
			case 6:
				index -= 1 + width;
				break;
			case 7:
				index += 1 + width;
				break;
			}
			if (index < width || index > islandMask.length - width)
				index = prevIndex = x + z * width;
			if (index % width == 0)
				index = prevIndex = x + z * width;
			if (islandMask[index] <= islandMask[prevIndex])
				islandMask[index] += 1;
		}

		double max = 0;
		for (int i = 0; i < islandMask.length; i++)
			if (islandMask[i] > max)
				max = islandMask[i];

		for (int i = 0; i < islandMask.length; i++)
			islandMask[i] = ((islandMask[i]) / max) * 2 - 1;

		for (int i = 0; i < noise.length; i++)
			noise[i] = noise[i] + (int) (noise[i] * islandMask[i]);
	}
}
