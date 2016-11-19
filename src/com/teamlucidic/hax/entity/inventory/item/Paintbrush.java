package com.teamlucidic.hax.entity.inventory.item;

import java.util.ArrayList;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.entity.inventory.Inventory;
import com.teamlucidic.hax.entity.inventory.InventoryItem;
import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.world.World;

public class Paintbrush extends InventoryItem {
	public static ArrayList<Stroke> strokeHistory = new ArrayList<Stroke>();
	public static String mode = "paint";
	public static String placeType = "Air";
	public static String replaceType = "";
	public static int shape = 1;
	public static int radius = 0;
	public static double percentFill = 1;
	public static int delay = 20;

	public Paintbrush(Inventory i) {
		super(i, Main.m.invItemLoader.getInvItemType("Paintbrush"));
	}

	public void update() {
		if (swingTimer > 0)
			swingTimer--;
		if (!parent.drawn && type.requiresDraw)
			return;
		if (quickSlot != null && Inventory.keyIsMouseButton(quickSlot) && parent.drawTimer == 0) {
			Main.m.player.editorMode = true;
			Block bl = Main.m.player.rayTraceToBlock(Main.m.player.currentHeight, .5, World.renderDistance + 32);
			if (!Inventory.keyIsDown(quickSlot))
				swingTimer = 0;
			else {
				if (swingTimer == 0) {
					swingTimer = delay;
					if (bl != null)
						strokeHistory.add(new Stroke(bl.posX, bl.posY, bl.posZ));
				}
			}
		}
	}

	public InventoryItem copy() {
		Paintbrush copy = new Paintbrush(parent);
		super.addParamsToCopy(copy);
		return copy;
	}

	public static String undo() {
		if (strokeHistory.size() > 0) {
			strokeHistory.get(strokeHistory.size() - 1).undo();
			strokeHistory.remove(strokeHistory.size() - 1);
			return "Undid the last stroke";
		}
		return "Nothing to undo!";
	}

	public static class Stroke {
		public int sRadius;
		public int width;
		public int[] blockMap;
		public int centerX, centerY, centerZ;

		public Stroke(int centerX, int centerY, int centerZ) {
			this.centerX = centerX;
			this.centerY = centerY;
			this.centerZ = centerZ;
			sRadius = radius;
			width = sRadius * 2 + 1;
			blockMap = new int[width * width * width];
			doStroke();
		}

		public void doStroke() {
			for (int x = centerX - sRadius; x <= centerX + sRadius; x++)
				for (int y = centerY - sRadius; y <= centerY + sRadius; y++)
					for (int z = centerZ - sRadius; z <= centerZ + sRadius; z++) {
						int x2 = x - centerX + sRadius;
						int y2 = y - centerY + sRadius;
						int z2 = z - centerZ + sRadius;
						if (x2 + width * (y2 + z2 * width) < width * width * width && x2 >= 0 && y2 >= 0 && z2 >= 0)
							blockMap[x2 + width * (y2 + z2 * width)] = -1;
						if ((shape == 2 || shape == 3) && y != centerY)
							continue;

						if (Paintbrush.mode.equals("paint")) {
							double dis = shape == 0 || shape == 3 ? 0 : ((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY) + (z - centerZ) * (z - centerZ));
							if (dis <= sRadius * sRadius) {
								Block b = Main.m.world.getBlock(x, y, z, true);
								if (x2 + width * (y2 + z2 * width) < width * width * width && x2 >= 0 && y2 >= 0 && z2 >= 0)
									blockMap[x2 + width * (y2 + z2 * width)] = b == null ? 0 : b.type.id;
								placeAt(b, x, y, z);
							}
						}

						if (Paintbrush.mode.equals("smooth")) {
						}

					}
			Main.m.resetTimer();
		}

		public int getTopY(int x, int z) {
			Block b = Main.m.world.getTopBlock(x, z, true, true);
			if (b != null)
				return b.posY;
			else
				return 0;
		}

		public void placeAt(Block b, double posX, double posY, double posZ) {
			if (percentFill < 1 && Render.rand.nextDouble() > percentFill)
				return;
			int x = Render.floor(posX);
			int y = Render.floor(posY);
			int z = Render.floor(posZ);
			if (replaceType != null && replaceType.length() > 0) {
				if ((replaceType.equalsIgnoreCase("anyButAir") && b != null) || (b == null && replaceType.equals("Air")) || (b != null && b.type.name.equals(replaceType)))
					Main.m.world.placeBlock(placeType.equals("Air") ? null : placeType, x, y, z, true);
			} else
				Main.m.world.placeBlock(placeType.equals("Air") ? null : placeType, x, y, z, true);
		}

		public void undo() {
			for (int x = centerX - sRadius; x <= centerX + sRadius; x++)
				for (int y = centerY - sRadius; y <= centerY + sRadius; y++)
					for (int z = centerZ - sRadius; z <= centerZ + sRadius; z++) {
						int x2 = x - centerX + sRadius;
						int y2 = y - centerY + sRadius;
						int z2 = z - centerZ + sRadius;
						if (blockMap[x2 + width * (y2 + z2 * width)] != -1)
							Main.m.world.placeBlock(blockMap[x2 + width * (y2 + z2 * width)], x, y, z, true);
					}
			Main.m.resetTimer();
		}
	}
}
