package com.teamlucidic.hax.world.generator;

import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.world.World;

public class CloudGenerator extends BasicHillsGenerator {
	public Texture tex;

	public CloudGenerator() {
		super(World.generateSeed(), 80, 15);
		octaveCount = 3;
		smoothness = 2;
		scale = 2;
		exponent = 1;
		generateHeightMap();
		generateTexture();
	}

	public int[] generateHeightMap() {
		noise = new int[width * width];
		octaves = new double[octaveCount][width * width];
		generateOctaves();
		blend();
		smooth();
		check();
		return noise;
	}

	public void generateTexture() {
		tex = new Texture(width, width);
		for (int i = 0; i < width; i++)
			for (int j = 0; j < width; j++)
				tex.setPixel(i, j, noise[i + j * width] < 5 ? 0xAAFFFFFF : 0x00000000);
		tex.setUpGL(false);
	}

}