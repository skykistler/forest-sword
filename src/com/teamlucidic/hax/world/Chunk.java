package com.teamlucidic.hax.world;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.block.BlockType;
import com.teamlucidic.hax.entity.AABB;
import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.world.generator.Tree;

public class Chunk {
	public static int width = 16;

	public ArrayList<Block> blockList;
	public Block[] blocks;
	public int posX;
	public int posZ;
	public AABB bb;
	public boolean withinRenderDis, inCamera;
	public boolean loaded, changesMade, markForDeload;
	public int displayList = -1;
	public boolean useDLs = false;

	public Chunk(int x, int z) {
		posX = x * width;
		posZ = z * width;
		bb = new AABB(posX + width / 2, 0, posZ + width / 2, width / 2, Main.m.world.height, width / 2);
	}

	public void update() {
		if (markForDeload)
			deLoad(true);
		if (updateIsSeen() && Main.m.settings.zOrderChunks)
			Main.m.world.seenChunks.add(this);
		useDLs = true;
		if (!useDLs && displayList > -1) {
			GL11.glDeleteLists(displayList, 1);
			displayList = -1;
		}
	}

	public void render() {
		//		bb.render();
		if (withinRenderDis && inCamera && loaded) {
			if (displayList == -1 || changesMade || !useDLs) {
				if (useDLs) {
					if (displayList == -1)
						displayList = GL11.glGenLists(1);
					GL11.glNewList(displayList, GL11.GL_COMPILE);
				}
				Main.m.modeler.setHexColor(0xFFFFFFFF);
				Main.m.modeler.start();
				for (int i = 0; i < blockList.size(); i++) {
					Block block = blockList.get(i);
					if (block.isSeen && (!Main.m.settings.alphaSort || !block.type.hasAlpha)) {
						block.render();
					}
				}
				if (Main.m.settings.alphaSort)
					for (int i = 0; i < blockList.size(); i++) {
						Block block = blockList.get(i);
						if (block.isSeen && block.type.hasAlpha)
							block.render();
					}
				Texture.getTexture("block/fast-terrain.png").bind();
				Main.m.modeler.finish();
				if (useDLs)
					GL11.glEndList();
				Main.m.modeler.setOffset(0, 0, 0);
				Main.m.modeler.clearRGBAOffset();
				changesMade = false;
			}
			if (useDLs)
				GL11.glCallList(displayList);
		}
	}

	public void updateAllBlocks() {
		if (blockList == null)
			return;
		for (Block b : blockList)
			b.update();
		changesMade = true;
	}

	public void updateSideBlocks(int side) {
		if (blockList != null)
			for (Block b : blockList) {
				if (b.posX - posX == 0 && side == 0)
					b.update();
				if (b.posX - posX == 15 && side == 1)
					b.update();
				if (b.posZ - posZ == 0 && side == 2)
					b.update();
				if (b.posZ - posZ == 15 && side == 3)
					b.update();
			}
		changesMade = true;
	}

	public int getLightMapValue(int x, int y, int z, int side) {
		//		Block tempBlock = Main.m.world.getBlock(x, y, z);
		//		if (tempBlock == null)
		//			return 0xFFFFFFFF;
		//		return tempBlock.type.isSolid ? 0xFFAAAAAA : Main.m.world.getTopBlock(x, z, true).posY > tempBlock.posY ? 0xFFFFFFFF : 0xFF888888;
		return 0xFFFFFFFF;
		//				return ambientMap[(x - posX) + width * (y + (z - posZ) * Main.m.world.height)][side];
	}

	public boolean updateIsSeen() {
		double[] point1 = { posX + width / 2.0D, posZ + width / 2.0D, 0.0D };
		double[] point2 = { Main.m.camera.posX, Main.m.camera.posZ, 0.0D };
		double dis = Math.abs(Render.distance(point1, point2));
		if (dis < World.renderDistance * World.renderDistance) {
			if (!loaded)
				load();
			withinRenderDis = true;
			if (dis > 32 * 32) {
				inCamera = Main.m.camera.isBoxInCameraXZ(bb, 60);
			} else
				inCamera = true;
		} else {
			if (loaded)
				deLoad(true);
			withinRenderDis = false;
		}

		return withinRenderDis && inCamera;
	}

	public void placeBlocks() {
		for (int x = 0; x < width; x++)
			for (int z = 0; z < width; z++)
				setTerrainColumn(x + posX, z + posZ, Main.m.world.getHeightValue(x + posX, z + posZ));
	}

	public void load() {
		Main.m.world.map.loadChunk(this);
		updateAllBlocks();
		loaded = true;
		Chunk c = Main.m.world.getChunk(posX + 16, posZ);
		Chunk c1 = Main.m.world.getChunk(posX - 16, posZ);
		Chunk c2 = Main.m.world.getChunk(posX, posZ + 16);
		Chunk c3 = Main.m.world.getChunk(posX, posZ - 16);
		if (c != null && c.loaded)
			c.updateSideBlocks(0);
		if (c1 != null && c1.loaded)
			c1.updateSideBlocks(1);
		if (c2 != null && c2.loaded)
			c2.updateSideBlocks(2);
		if (c3 != null && c3.loaded)
			c3.updateSideBlocks(3);
	}

	public void deLoad(boolean save) {
		markForDeload = false;
		if (save)
			Main.m.world.map.saveChunk(this);
		loaded = false;
		blocks = null;
		blockList = null;
		GL11.glDeleteLists(displayList, 1);
		displayList = -1;
	}

	public void clearChunk() {
		blocks = new Block[Main.m.world.height * width * width];
		blockList = new ArrayList<Block>();
	}

	public void setTerrainColumn(int x, int z, int h) {
		int grassDepth = (int) Math.ceil(Main.m.world.worldRNG.nextDouble() + 1) + 1;
		for (int i = 0; i < h; i++) {
			String toptype = "Grass";
			String blocktype = h - i <= grassDepth ? i >= World.seaLevel + 2 ? toptype : "Sand" : "Rock";
			blocktype = i == 0 ? "Bedrock" : blocktype;
			this.placeBlock(blocktype, x, i, z, false);
		}
	}

	public void placeBlock(int id, int x, int y, int z, boolean updateSurrounding) {
		placeBlock(id == -1 ? null : Main.m.blockLoader.blockTypesById.get(id), x, y, z, updateSurrounding);
	}

	public void placeBlock(String type, int x, int y, int z, boolean updateSurrounding) {
		BlockType btype = type == null ? null : Main.m.blockLoader.getBlockType(type);
		placeBlock(btype, x, y, z, updateSurrounding);
	}

	public void placeBlock(BlockType type, int x, int y, int z, boolean updateSurrounding) {
		if (blockList != null && x >= posX && x < width + posX && z >= posZ && z < width + posZ && y >= 0 && y < Main.m.world.height) {
			changesMade = true;
			Block old = getBlock(x, y, z);
			if (old != null)
				blockList.remove(old);
			Block block = type == null ? null : new Block(type, x, y, z);
			setBlock(block, x, y, z);
			if (block != null) {
				blockList.add(block);
				block.update();
				block.onPlace();
			}

			if (updateSurrounding)
				Main.m.world.updateSurroundingBlocks(x, y, z);
		}
	}

	public Block getBlock(double x, double y, double z) {
		int xi = Render.floor(x);
		int yi = Render.floor(y);
		int zi = Render.floor(z);
		xi -= posX;
		zi -= posZ;
		if (blocks != null && xi + width * (yi + zi * Main.m.world.height) < width * width * Main.m.world.height && xi >= 0 && yi >= 0 && zi >= 0 && yi < Main.m.world.height)
			return blocks[xi + width * (yi + zi * Main.m.world.height)];
		return null;
	}

	public void setBlock(Block block, int x, int y, int z) {
		if (blocks != null && (x - posX) + width * (y + (z - posZ) * Main.m.world.height) < width * width * Main.m.world.height && x >= 0 && y >= 0 && z >= 0 && y < Main.m.world.height)
			blocks[(x - posX) + width * (y + (z - posZ) * Main.m.world.height)] = block;
	}

	public Block getTopBlock(double x, double z, boolean includeAlpha) {
		for (int i = 0; i < Main.m.world.height; i++) {
			Block block = getBlock(x, Main.m.world.height - i, z);
			if (block != null)
				if (!block.type.hasAlpha || includeAlpha)
					return block;
		}
		return null;
	}

	public void decorate() {
		for (int x = 0; x < Chunk.width; x++)
			for (int z = 0; z < Chunk.width; z++) {
				Block tB = getTopBlock(x + posX, z + posZ, true);

				if (tB != null && tB.type.name.equals("Grass") && Main.m.world.worldRNG.nextInt(150) >= 149)
					for (int i = 0; i < Main.m.world.worldRNG.nextInt(4) + 7; i++) {
						int randomX = x + posX + Main.m.world.worldRNG.nextInt(6) - 3;
						int randomZ = z + posZ + Main.m.world.worldRNG.nextInt(6) - 3;
						Block tB1 = getTopBlock(randomX, randomZ, true);
						if (tB1 != null && tB1.type.name.equals("Grass"))
							placeBlock("GrassBlades", randomX, tB1.posY + 1, randomZ, false);
					}

				if (!Main.m.gameMode.contains("asunder") && tB != null && tB.type.name.equals("Grass") && Main.m.world.worldRNG.nextInt(200) >= 199) {
					int type = (int) (Main.m.world.worldRNG.nextDouble() * 6);
					String tp = "";
					switch (type) {
					case 0:
						tp = "Red";
						break;
					case 1:
						tp = "Yellow";
						break;
					case 2:
						tp = "Blue";
						break;
					case 3:
						tp = "White";
						break;
					case 4:
						tp = "Rose";
						break;
					case 5:
						tp = "Lily";
						break;
					}
					for (int i = 0; i < Main.m.world.worldRNG.nextInt(4) + 4; i++) {
						int randomX = x + posX + Main.m.world.worldRNG.nextInt(4) - 2;
						int randomZ = z + posZ + Main.m.world.worldRNG.nextInt(4) - 2;
						Block tB1 = getTopBlock(randomX, randomZ, true);
						if (tB1 != null && tB1.type.name.equals("Grass"))
							placeBlock("Flower" + tp, randomX, tB1.posY + 1, randomZ, false);
					}
				}
				for (int y = 0; y < World.seaLevel; y++)
					if (getBlock(x + posX, y, z + posZ) == null)
						placeBlock("Water", x + posX, y, z + posZ, false);
			}

		int trees = 2 + (Main.m.world.worldRNG.nextInt(3));
		for (int i = 0; i < trees; i++) {
			int treeHeight = Main.m.world.worldRNG.nextInt(3) + 2;
			int randomX = Main.m.world.worldRNG.nextInt(15) + posX;
			int randomZ = Main.m.world.worldRNG.nextInt(15) + posZ;
			Block tb = getTopBlock(randomX, randomZ, true);
			if (tb != null && tb.type.name.equals("Grass"))
				Tree.placeTreeAt(randomX, tb.posY + 1, randomZ, treeHeight);
		}
	}
}
