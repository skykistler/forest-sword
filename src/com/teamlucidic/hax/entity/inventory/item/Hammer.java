package com.teamlucidic.hax.entity.inventory.item;

import java.util.ArrayList;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.entity.AABB;
import com.teamlucidic.hax.entity.inventory.Inventory;
import com.teamlucidic.hax.entity.inventory.InventoryItem;
import com.teamlucidic.hax.render.Render;

public class Hammer extends InventoryItem {
	public static ArrayList<Action> actionHistory = new ArrayList<Action>();
	public static int[] p1, p2;
	public static AABB selection = new AABB(0, 0, 0, 0, 0, 0);
	public static int lastActionBlocks;

	public static void renderSelection() {
		if (p1 != null && p2 == null) {
			Block b = Main.m.world.getBlock(p1[0], p1[1], p1[2], false);
			if (b != null)
				new AABB(b).render();
		}
		if (p1 != null && p2 != null) {
			selection.maxX += 1;
			selection.maxY += 1;
			selection.maxZ += 1;
			selection.render();
			selection.maxX -= 1;
			selection.maxY -= 1;
			selection.maxZ -= 1;
		}
	}

	public static void shiftXZ(int rot, int amount) {
		rot %= 4;
		if (rot == 0) {
			selection.maxX += amount;
			selection.minX += amount;
		}
		if (rot == 1) {
			selection.maxZ += amount;
			selection.minZ += amount;
		}
		if (rot == 2) {
			selection.maxX -= amount;
			selection.minX -= amount;
		}
		if (rot == 3) {
			selection.maxZ -= amount;
			selection.minZ -= amount;
		}
	}

	public static void expandXZ(int rot, int amount) {
		rot %= 4;
		if (rot == 0)
			selection.maxX += amount;
		if (rot == 1)
			selection.maxZ += amount;
		if (rot == 2)
			selection.minX -= amount;
		if (rot == 3)
			selection.minZ -= amount;
	}

	public static String undo() {
		if (actionHistory.size() > 0) {
			actionHistory.get(actionHistory.size() - 1).undo();
			actionHistory.remove(actionHistory.size() - 1);
			return "Undid the last action";
		}
		return "Nothing to undo!";
	}

	public static int set(String mode, String place, String replace, double percent) {
		actionHistory.add(new Action(mode, place, replace, percent));
		return lastActionBlocks;
	}

	public Hammer(Inventory i) {
		super(i, Main.m.invItemLoader.getInvItemType("Hammer"));
	}

	public void update() {
		if (swingTimer > 0)
			swingTimer--;
		if (!parent.drawn && type.requiresDraw)
			return;
		if (quickSlot != null && Inventory.keyIsMouseButton(quickSlot) && parent.drawTimer == 0) {
			Main.m.player.editorMode = true;
			Main.m.player.setTargetBlock();
			if (!Inventory.keyIsDown(quickSlot))
				swingTimer = 0;
			else {
				if (swingTimer == 0) {
					swingTimer = 20;
					if (Main.m.player.targetBlock != null) {
						int[] point = new int[] { Main.m.player.targetBlock.posX, Main.m.player.targetBlock.posY, Main.m.player.targetBlock.posZ };
						if (p1 == null)
							p1 = point;
						else if (p2 == null) {
							p2 = point;
							selection.minX = Math.min(p1[0], p2[0]);
							selection.maxX = Math.max(p1[0], p2[0]);
							selection.minY = Math.min(p1[1], p2[1]);
							selection.maxY = Math.max(p1[1], p2[1]);
							selection.minZ = Math.min(p1[2], p2[2]);
							selection.maxZ = Math.max(p1[2], p2[2]);
						} else {
							p1 = point;
							p2 = null;
						}
					} else if (p1 != null & p2 != null) {
						p1 = null;
						p2 = null;
					}
				}
			}
		}
	}

	public InventoryItem copy() {
		Hammer copy = new Hammer(parent);
		super.addParamsToCopy(copy);
		return copy;
	}

	public static class Action {
		public int width, height, depth, minX, maxX, minY, maxY, minZ, maxZ;
		public int[] blockMap;
		public String placeType, replaceType;
		public double percentFill;

		public Action(String mode, String place, String replace, double percent) {
			minX = Render.floor(Math.min(selection.minX, selection.maxX));
			maxX = Render.floor(Math.max(selection.minX, selection.maxX));
			minY = Render.floor(Math.min(selection.minY, selection.maxY));
			maxY = Render.floor(Math.max(selection.minY, selection.maxY));
			minZ = Render.floor(Math.min(selection.minZ, selection.maxZ));
			maxZ = Render.floor(Math.max(selection.minZ, selection.maxZ));
			//			int worldWidth = Main.m.world.getWidth();
			//			minX = minX < 0 ? 0 : minX > worldWidth ? worldWidth : minX;
			//			maxX = maxX < 0 ? 0 : maxX > worldWidth ? worldWidth : maxX;
			//			minY = minY < 0 ? 0 : minY > worldWidth ? worldWidth : minY;
			//			maxY = maxY < 0 ? 0 : maxY > worldWidth ? worldWidth : maxY;
			//			minZ = minZ < 0 ? 0 : minZ > worldWidth ? worldWidth : minZ;
			//			maxZ = maxZ < 0 ? 0 : maxZ > worldWidth ? worldWidth : maxZ;
			width = maxX - minX + 1;
			height = maxY - minY + 1;
			depth = maxZ - minZ + 1;
			blockMap = new int[width * height * depth];
			placeType = place;
			replaceType = replace;
			percentFill = percent;
			if (width == 0 || height == 0 || depth == 0) {
				Main.m.error("Invalid selection!");
				return;
			}
			lastActionBlocks = 0;
			doAction(mode);
		}

		public void doAction(String mode) {
			mode = mode.toLowerCase();
			for (int x = minX; x <= maxX; x++)
				for (int y = minY; y <= maxY; y++)
					for (int z = minZ; z <= maxZ; z++) {
						blockMap[x - minX + width * (y - minY + (z - minZ) * height)] = -1;

						Block b = Main.m.world.getBlock(x, y, z, true);
						blockMap[x - minX + width * (y - minY + (z - minZ) * height)] = b == null ? 0 : b.type.id;
						boolean shouldPlace = mode.equals("set");
						double centerX = (maxX - minX) / 2d + minX;
						double centerZ = (maxZ - minZ) / 2d + minZ;
						double halfw = width / 2d;
						//						double halfh = height / 2d;
						double halfz = depth / 2d;
						if (mode.startsWith("cyl"))
							shouldPlace = halfw * halfz >= (x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ);
						//						else if (mode.startsWith("dome"))
						//							shouldPlace = halfw * halfh >= (x - centerX) * (x - centerX) + (y - minY) * (y - minY) && halfz * halfh >= (z - centerZ) * (z - centerZ) + (y - minY) * (y - minY);

						if (shouldPlace)
							placeAt(b, x, y, z);
					}
			Main.m.resetTimer();
		}

		public void placeAt(Block b, double posX, double posY, double posZ) {
			if (percentFill < 1 && Render.rand.nextDouble() > percentFill)
				return;
			int x = Render.floor(posX);
			int y = Render.floor(posY);
			int z = Render.floor(posZ);
			if (replaceType != null && replaceType.length() > 0) {
				if ((replaceType.equalsIgnoreCase("anyButAir") && b != null) || (b == null && replaceType.equals("Air")) || (b != null && b.type.name.equals(replaceType))) {
					Main.m.world.placeBlock(placeType.equals("Air") ? null : placeType, x, y, z, true);
					lastActionBlocks++;
				}
			} else {
				Main.m.world.placeBlock(placeType.equals("Air") ? null : placeType, x, y, z, true);
				lastActionBlocks++;
			}
		}

		public void undo() {
			for (int x = minX; x <= maxX; x++)
				for (int y = minY; y <= maxY; y++)
					for (int z = minZ; z <= maxZ; z++) {
						if (blockMap[x - minX + width * (y - minY + (z - minZ) * height)] != -1)
							Main.m.world.placeBlock(blockMap[x - minX + width * (y - minY + (z - minZ) * height)], x, y, z, true);
					}
			Main.m.resetTimer();
		}
	}

}
