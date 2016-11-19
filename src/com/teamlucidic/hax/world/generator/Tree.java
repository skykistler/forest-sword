package com.teamlucidic.hax.world.generator;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;

public class Tree {

	public static void placeTreeAt(int x, int y, int z, int h) {
		int height = h;
		for (int i = 0; i <= height; i++)
			Main.m.world.placeBlock(i == 0 && Main.m.world.worldRNG.nextDouble() > .4 ? "Stump" : "Wood", x, i + y, z, false);
		for (int i = height - 1; i <= height + 1; i++) {
			placeLeaf(x, y + i, z - 1);
			placeLeaf(x + 1, y + i, z);
			placeLeaf(x, y + i, z + 1);
			placeLeaf(x - 1, y + i, z);

			if (i == height) {
				placeLeaf(x + 1, y + i, z - 1);
				placeLeaf(x - 1, y + i, z + 1);
				placeLeaf(x + 1, y + i, z + 1);
				placeLeaf(x - 1, y + i, z - 1);
			}
		}
		placeLeaf(x, y + height + 1, z);
	}

	public static void placeLeaf(int x, int y, int z) {
		Block block = Main.m.world.getBlock(x, y, z, false);
		if (block == null || !block.type.isSolid)
			Main.m.world.placeBlock("Leaves", x, y, z, false);
	}
}
