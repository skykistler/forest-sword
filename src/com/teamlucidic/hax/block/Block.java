package com.teamlucidic.hax.block;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.particle.ParticleSystem;
import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.world.Chunk;

public class Block {
	public static float[] darken = { .23F, .23F, .23F, 0 };
	public static float[] halfDarken = { .07F, .07F, .07F, 0 };
	public static boolean drawLiquidBacks = true;

	public static double[][] tempModel;

	public Chunk chunk;
	public BlockType type;
	public int posX, posY, posZ;
	public byte uT, vT, uS, vS, uB, vB;

	public boolean rTop = true;
	public boolean rBottom = true;
	public boolean rLeft = true;
	public boolean rRight = true;
	public boolean rFront = true;
	public boolean rBack = true;
	public boolean isSeen = true;
	public boolean isNonLiquidBelow = false;

	public Block(BlockType bt, int x, int y, int z) {
		chunk = Main.m.world.getChunk(x, z);
		type = bt;
		posX = x;
		posY = y;
		posZ = z;
		setTopTexture(type.texSheetPos[0], type.texSheetPos[1]);
		setSideTexture(type.texSheetPos[2], type.texSheetPos[3]);
		setBottomTexture(type.texSheetPos[4], type.texSheetPos[5]);
	}

	public void update() {
		chunk.changesMade = true;
		rTop = true;
		rBottom = true;
		rLeft = true;
		rRight = true;
		rFront = true;
		rBack = true;
		Block blockx1 = Main.m.world.getBlock(posX + 1, posY, posZ, false);
		Block blockx2 = Main.m.world.getBlock(posX - 1, posY, posZ, false);
		Block blocky1 = Main.m.world.getBlock(posX, posY - 1, posZ, false);
		Block blocky2 = Main.m.world.getBlock(posX, posY + 1, posZ, false);
		Block blockz1 = Main.m.world.getBlock(posX, posY, posZ + 1, false);
		Block blockz2 = Main.m.world.getBlock(posX, posY, posZ - 1, false);
		if (blockx1 != null && (!blockx1.type.hasAlpha || (blockx1.type == type)))
			rRight = false;
		if (blockx2 != null && (!blockx2.type.hasAlpha || (blockx2.type == type)))
			rLeft = false;
		if (blocky1 != null && (!blocky1.type.hasAlpha || (blocky1.type == type)))
			rBottom = false;
		if (blocky2 != null && (!blocky2.type.hasAlpha || (blocky2.type == type)))
			rTop = false;
		if (blockz1 != null && (!blockz1.type.hasAlpha || (blockz1.type == type)))
			rBack = false;
		if (blockz2 != null && (!blockz2.type.hasAlpha || (blockz2.type == type)))
			rFront = false;

		if (type.isLiquid && blocky2 != null && !blocky2.type.isLiquid)
			rTop = true;

		//		if (Main.m.world.worldTicks == 0)
		//			setModel();
		updateIsSeen();

		if (blocky1 != null && !blocky1.type.isLiquid)
			isNonLiquidBelow = true;
		else
			isNonLiquidBelow = false;

		if (type.grassRender) {
			Block block = Main.m.world.getBlock(posX, posY + 1, posZ, false);
			if (block == null || (block.type.hasAlpha && !block.type.isLiquid)) {
				setTopTexture(2, 0);
				setSideTexture(1, 0);
			} else {
				setTopTexture(0, 0);
				setSideTexture(0, 0);
			}
		}
	}

	public void render() {
		Main.m.modeler.clearRGBAOffset();

		if (Main.m.settings.windTest && !type.xRender && type.affectedByWind)
			applyWindEffect();
		else
			tempModel = type.isLiquid ? (isNonLiquidBelow ? BlockType.liquidModel2 : BlockType.liquidModel1) : type.model;

		if (type.xRender)
			xRender();
		else {
			Main.m.modeler.setOffset(posX, posY, posZ);
			if (rTop)
				drawTop();
			Main.m.modeler.setNegRGBAOffset(darken);
			if (rBottom)
				drawBottom();
			if (rRight)
				drawRight();
			if (rLeft)
				drawLeft();
			Main.m.modeler.setNegRGBAOffset(halfDarken);
			if (rFront)
				drawFront();
			if (rBack)
				drawBack();
		}
		Main.m.modeler.setOffset(0, 0, 0);
		Main.m.modeler.clearRGBAOffset();
	}

	public void breakBlock(boolean withParticles) {
		Main.m.world.clearBlock(this);
		if (!withParticles)
			return;
		int piece = 4;
		Texture t = type.tex;
		if (t == null)
			t = Texture.getTexture("block/fast-terrain.png").getSubTexture(uT * BlockType.texResolution + (int) (Render.rand.nextDouble() * (BlockType.texResolution - piece)), vT * BlockType.texResolution + (int) (Render.rand.nextDouble() * (BlockType.texResolution - piece)), piece, piece);
		else
			t = type.tex.getSubTexture((int) (Render.rand.nextDouble() * (type.tex.width - piece)), (int) (Render.rand.nextDouble() * (type.tex.height - piece)), piece, piece);
		float[] rgba = Main.m.world.ambientLight;
		ParticleSystem prtcl = Main.m.partSysLoader.getParticleSystemType("BlockBreak").spawnSystem(posX, posY, posZ);
		prtcl.tex = t;
		prtcl.color = Main.m.modeler.RGBAtoHex(rgba[0], rgba[1], rgba[2], rgba[3]);
	}

	public void updateIsSeen() {
		isSeen = (rTop || rBottom || rRight || rLeft || rFront || rBack);
	}

	public boolean isSideSeen(int side) {
		return true;
	}

	public void setTopTexture(int u, int v) {
		byte ub = (byte) u;
		byte vb = (byte) v;
		uT = ub;
		vT = vb;
	}

	public void setSideTexture(int u, int v) {
		byte ub = (byte) u;
		byte vb = (byte) v;
		uS = ub;
		vS = vb;
	}

	public void setBottomTexture(int u, int v) {
		byte ub = (byte) u;
		byte vb = (byte) v;
		uB = ub;
		vB = vb;
	}

	public void onPlace() {
		//		if (isLight)
		//			Main.m.world.lightNeedsUpdate = true;
	}

	//	public void setModel() {
	//		double size = cubeSize;// - Render.epsilon;
	//		double minn = 0;// + Render.epsilon;
	//		double yOff = isLiquid && rTop ? -0.1 : 0;
	//		double yOff2 = 0;
	//		Block block = Main.m.world.getBlock(posX, posY - 1, posZ);
	//		if (block != null && isLiquid && block.isLiquid)
	//			yOff2 -= .1;
	//
	//		setVertex(0, minn, minn + yOff2, minn);
	//		setVertex(1, minn, size + yOff, minn);
	//		setVertex(2, size, size + yOff, minn);
	//		setVertex(3, size, minn + yOff2, minn);
	//		setVertex(4, minn, minn + yOff2, size);
	//		setVertex(5, minn, size + yOff, size);
	//		setVertex(6, size, size + yOff, size);
	//		setVertex(7, size, minn + yOff2, size);
	//	}

	public void xRender() {
		Block.tempU = uT * BlockType.texResolution / Block.texWidth;
		Block.tempV = vT * BlockType.texResolution / Block.texWidth;
		Block.tempU1 = Block.tempU + BlockType.texResolution / Block.texWidth;
		Block.tempV1 = Block.tempV + BlockType.texResolution / Block.texWidth;

		Main.m.modeler.setHexColor(0xFFFFFFFF);

		if (Main.m.settings.windTest) {
			tempOffX = type.affectedByWind ? Main.m.world.windOffsetX : 0;
			tempOffY = type.affectedByWind ? Main.m.world.windOffsetY : 0;
			tempOffZ = type.affectedByWind ? Main.m.world.windOffsetZ : 0;
		}

		Main.m.modeler.addVertexTex(posX + tempOffX, posY + 1 + tempOffY, posZ + tempOffZ, Block.tempU, Block.tempV);
		Main.m.modeler.addVertexTex(posX + 1 + tempOffX, posY + 1 + tempOffY, posZ + 1 + tempOffZ, Block.tempU1, Block.tempV);
		Main.m.modeler.addVertexTex(posX + 1, posY, posZ + 1, Block.tempU1, Block.tempV1);
		Main.m.modeler.addVertexTex(posX, posY, posZ, Block.tempU, Block.tempV1);

		Main.m.modeler.addVertexTex(posX + 1 + tempOffX, posY + 1 + tempOffY, posZ + tempOffZ, Block.tempU, Block.tempV);
		Main.m.modeler.addVertexTex(posX + tempOffX, posY + 1 + tempOffY, posZ + 1 + tempOffZ, Block.tempU1, Block.tempV);
		Main.m.modeler.addVertexTex(posX, posY, posZ + 1, Block.tempU1, Block.tempV1);
		Main.m.modeler.addVertexTex(posX + 1, posY, posZ, Block.tempU, Block.tempV1);

		Main.m.modeler.addVertexTex(posX + tempOffX, posY + 1 + tempOffY, posZ + tempOffZ, Block.tempU, Block.tempV);
		Main.m.modeler.addVertexTex(posX, posY, posZ, Block.tempU, Block.tempV1);
		Main.m.modeler.addVertexTex(posX + 1, posY, posZ + 1, Block.tempU1, Block.tempV1);
		Main.m.modeler.addVertexTex(posX + 1 + tempOffX, posY + 1 + tempOffY, posZ + 1 + tempOffZ, Block.tempU1, Block.tempV);

		Main.m.modeler.addVertexTex(posX + 1 + tempOffX, posY + 1 + tempOffY, posZ + tempOffZ, Block.tempU, Block.tempV);
		Main.m.modeler.addVertexTex(posX + 1, posY, posZ, Block.tempU, Block.tempV1);
		Main.m.modeler.addVertexTex(posX, posY, posZ + 1, Block.tempU1, Block.tempV1);
		Main.m.modeler.addVertexTex(posX + tempOffX, posY + 1 + tempOffY, posZ + 1 + tempOffZ, Block.tempU1, Block.tempV);
	}

	public void drawTop() {
		//float texCoord = (float) (isLiquid ? flowOffset * texResolution : 0);
		Block.tempU = uT * BlockType.texResolution / Block.texWidth;
		Block.tempV = vT * BlockType.texResolution / Block.texWidth;
		Block.tempU1 = Block.tempU + BlockType.texResolution / Block.texWidth;
		Block.tempV1 = Block.tempV + BlockType.texResolution / Block.texWidth;

		Main.m.modeler.setNormal(0, 1, 0);
		Main.m.modeler.addVertexTexCol(tempModel[1][0], tempModel[1][1], tempModel[1][2], Block.tempU1, Block.tempV1, chunk.getLightMapValue(posX, posY + 1, posZ, 1));
		Main.m.modeler.addVertexTexCol(tempModel[5][0], tempModel[5][1], tempModel[5][2], Block.tempU1, Block.tempV, chunk.getLightMapValue(posX, posY + 1, posZ + 1, 5));
		Main.m.modeler.addVertexTexCol(tempModel[6][0], tempModel[6][1], tempModel[6][2], Block.tempU, Block.tempV, chunk.getLightMapValue(posX + 1, posY + 1, posZ + 1, 6));
		Main.m.modeler.addVertexTexCol(tempModel[2][0], tempModel[2][1], tempModel[2][2], Block.tempU, Block.tempV1, chunk.getLightMapValue(posX + 1, posY + 1, posZ, 2));

		if (type.isLiquid && drawLiquidBacks) {
			Main.m.modeler.setNormal(0, -1, 0);
			Main.m.modeler.addVertexTexCol(tempModel[1][0], tempModel[1][1], tempModel[1][2], Block.tempU1, Block.tempV1, chunk.getLightMapValue(posX, posY + 1, posZ, 1));
			Main.m.modeler.addVertexTexCol(tempModel[2][0], tempModel[2][1], tempModel[2][2], Block.tempU, Block.tempV1, chunk.getLightMapValue(posX + 1, posY + 1, posZ, 2));
			Main.m.modeler.addVertexTexCol(tempModel[6][0], tempModel[6][1], tempModel[6][2], Block.tempU, Block.tempV, chunk.getLightMapValue(posX + 1, posY + 1, posZ + 1, 6));
			Main.m.modeler.addVertexTexCol(tempModel[5][0], tempModel[5][1], tempModel[5][2], Block.tempU1, Block.tempV, chunk.getLightMapValue(posX, posY + 1, posZ + 1, 5));
		}
	}

	public void drawBottom() {
		Block.tempU = uB * BlockType.texResolution / Block.texWidth;
		Block.tempV = vB * BlockType.texResolution / Block.texWidth;
		Block.tempU1 = Block.tempU + BlockType.texResolution / Block.texWidth;
		Block.tempV1 = Block.tempV + BlockType.texResolution / Block.texWidth;

		Main.m.modeler.setNormal(0, -1, 0);
		Main.m.modeler.addVertexTexCol(tempModel[0][0], tempModel[0][1], tempModel[0][2], Block.tempU1, Block.tempV1, chunk.getLightMapValue(posX, posY, posZ, 0));
		Main.m.modeler.addVertexTexCol(tempModel[3][0], tempModel[3][1], tempModel[3][2], Block.tempU, Block.tempV1, chunk.getLightMapValue(posX + 1, posY, posZ, 3));
		Main.m.modeler.addVertexTexCol(tempModel[7][0], tempModel[7][1], tempModel[7][2], Block.tempU, Block.tempV, chunk.getLightMapValue(posX + 1, posY, posZ + 1, 7));
		Main.m.modeler.addVertexTexCol(tempModel[4][0], tempModel[4][1], tempModel[4][2], Block.tempU1, Block.tempV, chunk.getLightMapValue(posX, posY, posZ + 1, 4));

		if (type.isLiquid && drawLiquidBacks) {
			Main.m.modeler.setNormal(0, 1, 0);
			Main.m.modeler.addVertexTexCol(tempModel[0][0], tempModel[0][1], tempModel[0][2], Block.tempU1, Block.tempV1, chunk.getLightMapValue(posX, posY, posZ, 0));
			Main.m.modeler.addVertexTexCol(tempModel[4][0], tempModel[4][1], tempModel[4][2], Block.tempU1, Block.tempV, chunk.getLightMapValue(posX, posY, posZ + 1, 4));
			Main.m.modeler.addVertexTexCol(tempModel[7][0], tempModel[7][1], tempModel[7][2], Block.tempU, Block.tempV, chunk.getLightMapValue(posX + 1, posY, posZ + 1, 7));
			Main.m.modeler.addVertexTexCol(tempModel[3][0], tempModel[3][1], tempModel[3][2], Block.tempU, Block.tempV1, chunk.getLightMapValue(posX + 1, posY, posZ, 3));
		}

	}

	public void drawRight() {
		Block.tempU = uS * BlockType.texResolution / Block.texWidth;
		Block.tempV = vS * BlockType.texResolution / Block.texWidth;
		Block.tempU1 = Block.tempU + BlockType.texResolution / Block.texWidth;
		Block.tempV1 = Block.tempV + BlockType.texResolution / Block.texWidth;

		Main.m.modeler.setNormal(1, 0, 0);
		Main.m.modeler.addVertexTexCol(tempModel[3][0], tempModel[3][1], tempModel[3][2], Block.tempU1, Block.tempV1, chunk.getLightMapValue(posX + 1, posY, posZ, 3));
		Main.m.modeler.addVertexTexCol(tempModel[2][0], tempModel[2][1], tempModel[2][2], Block.tempU1, Block.tempV, chunk.getLightMapValue(posX + 1, posY + 1, posZ, 2));
		Main.m.modeler.addVertexTexCol(tempModel[6][0], tempModel[6][1], tempModel[6][2], Block.tempU, Block.tempV, chunk.getLightMapValue(posX + 1, posY + 1, posZ + 1, 6));
		Main.m.modeler.addVertexTexCol(tempModel[7][0], tempModel[7][1], tempModel[7][2], Block.tempU, Block.tempV1, chunk.getLightMapValue(posX + 1, posY, posZ + 1, 7));

		if (type.isLiquid && drawLiquidBacks) {
			Main.m.modeler.setNormal(-1, 0, 0);
			Main.m.modeler.addVertexTexCol(tempModel[3][0], tempModel[3][1], tempModel[3][2], Block.tempU1, Block.tempV1, chunk.getLightMapValue(posX + 1, posY, posZ, 3));
			Main.m.modeler.addVertexTexCol(tempModel[7][0], tempModel[7][1], tempModel[7][2], Block.tempU, Block.tempV1, chunk.getLightMapValue(posX + 1, posY, posZ + 1, 7));
			Main.m.modeler.addVertexTexCol(tempModel[6][0], tempModel[6][1], tempModel[6][2], Block.tempU, Block.tempV, chunk.getLightMapValue(posX + 1, posY + 1, posZ + 1, 6));
			Main.m.modeler.addVertexTexCol(tempModel[2][0], tempModel[2][1], tempModel[2][2], Block.tempU1, Block.tempV, chunk.getLightMapValue(posX + 1, posY + 1, posZ, 2));
		}
	}

	public void drawLeft() {
		if (!rRight) {
			Block.tempU = uS * BlockType.texResolution / Block.texWidth;
			Block.tempV = vS * BlockType.texResolution / Block.texWidth;
			Block.tempU1 = Block.tempU + BlockType.texResolution / Block.texWidth;
			Block.tempV1 = Block.tempV + BlockType.texResolution / Block.texWidth;
		}

		Main.m.modeler.setNormal(-1, 0, 0);
		Main.m.modeler.addVertexTexCol(tempModel[0][0], tempModel[0][1], tempModel[0][2], Block.tempU1, Block.tempV1, chunk.getLightMapValue(posX, posY, posZ, 0));
		Main.m.modeler.addVertexTexCol(tempModel[4][0], tempModel[4][1], tempModel[4][2], Block.tempU, Block.tempV1, chunk.getLightMapValue(posX, posY, posZ + 1, 4));
		Main.m.modeler.addVertexTexCol(tempModel[5][0], tempModel[5][1], tempModel[5][2], Block.tempU, Block.tempV, chunk.getLightMapValue(posX, posY + 1, posZ + 1, 5));
		Main.m.modeler.addVertexTexCol(tempModel[1][0], tempModel[1][1], tempModel[1][2], Block.tempU1, Block.tempV, chunk.getLightMapValue(posX, posY + 1, posZ, 1));

		if (type.isLiquid && drawLiquidBacks) {
			Main.m.modeler.setNormal(1, 0, 0);
			Main.m.modeler.addVertexTexCol(tempModel[0][0], tempModel[0][1], tempModel[0][2], Block.tempU1, Block.tempV1, chunk.getLightMapValue(posX, posY, posZ, 0));
			Main.m.modeler.addVertexTexCol(tempModel[1][0], tempModel[1][1], tempModel[1][2], Block.tempU1, Block.tempV, chunk.getLightMapValue(posX, posY + 1, posZ, 1));
			Main.m.modeler.addVertexTexCol(tempModel[5][0], tempModel[5][1], tempModel[5][2], Block.tempU, Block.tempV, chunk.getLightMapValue(posX, posY + 1, posZ + 1, 5));
			Main.m.modeler.addVertexTexCol(tempModel[4][0], tempModel[4][1], tempModel[4][2], Block.tempU, Block.tempV1, chunk.getLightMapValue(posX, posY, posZ + 1, 4));
		}
	}

	public void drawFront() {
		Block.tempU = uS * BlockType.texResolution / Block.texWidth;
		Block.tempV = vS * BlockType.texResolution / Block.texWidth;
		Block.tempU1 = Block.tempU + BlockType.texResolution / Block.texWidth;
		Block.tempV1 = Block.tempV + BlockType.texResolution / Block.texWidth;

		Main.m.modeler.setNormal(0, 0, 1);
		Main.m.modeler.addVertexTexCol(tempModel[0][0], tempModel[0][1], tempModel[0][2], Block.tempU1, Block.tempV1, chunk.getLightMapValue(posX, posY, posZ, 0));
		Main.m.modeler.addVertexTexCol(tempModel[1][0], tempModel[1][1], tempModel[1][2], Block.tempU1, Block.tempV, chunk.getLightMapValue(posX, posY + 1, posZ, 1));
		Main.m.modeler.addVertexTexCol(tempModel[2][0], tempModel[2][1], tempModel[2][2], Block.tempU, Block.tempV, chunk.getLightMapValue(posX + 1, posY + 1, posZ, 2));
		Main.m.modeler.addVertexTexCol(tempModel[3][0], tempModel[3][1], tempModel[3][2], Block.tempU, Block.tempV1, chunk.getLightMapValue(posX + 1, posY, posZ, 3));

		if (type.isLiquid && drawLiquidBacks) {
			Main.m.modeler.setNormal(0, 0, -1);
			Main.m.modeler.addVertexTexCol(tempModel[0][0], tempModel[0][1], tempModel[0][2], Block.tempU1, Block.tempV1, chunk.getLightMapValue(posX, posY, posZ, 0));
			Main.m.modeler.addVertexTexCol(tempModel[3][0], tempModel[3][1], tempModel[3][2], Block.tempU, Block.tempV1, chunk.getLightMapValue(posX + 1, posY, posZ, 3));
			Main.m.modeler.addVertexTexCol(tempModel[2][0], tempModel[2][1], tempModel[2][2], Block.tempU, Block.tempV, chunk.getLightMapValue(posX + 1, posY + 1, posZ, 2));
			Main.m.modeler.addVertexTexCol(tempModel[1][0], tempModel[1][1], tempModel[1][2], Block.tempU1, Block.tempV, chunk.getLightMapValue(posX, posY + 1, posZ, 1));
		}
	}

	public void drawBack() {
		if (!rFront) {
			Block.tempU = uS * BlockType.texResolution / Block.texWidth;
			Block.tempV = vS * BlockType.texResolution / Block.texWidth;
			Block.tempU1 = Block.tempU + BlockType.texResolution / Block.texWidth;
			Block.tempV1 = Block.tempV + BlockType.texResolution / Block.texWidth;
		}

		Main.m.modeler.setNormal(0, 0, -1);
		Main.m.modeler.addVertexTexCol(tempModel[4][0], tempModel[4][1], tempModel[4][2], Block.tempU1, Block.tempV1, chunk.getLightMapValue(posX, posY, posZ + 1, 4));
		Main.m.modeler.addVertexTexCol(tempModel[7][0], tempModel[7][1], tempModel[7][2], Block.tempU, Block.tempV1, chunk.getLightMapValue(posX + 1, posY, posZ + 1, 7));
		Main.m.modeler.addVertexTexCol(tempModel[6][0], tempModel[6][1], tempModel[6][2], Block.tempU, Block.tempV, chunk.getLightMapValue(posX + 1, posY + 1, posZ + 1, 6));
		Main.m.modeler.addVertexTexCol(tempModel[5][0], tempModel[5][1], tempModel[5][2], Block.tempU1, Block.tempV, chunk.getLightMapValue(posX, posY + 1, posZ + 1, 5));

		if (type.isLiquid && drawLiquidBacks) {
			Main.m.modeler.setNormal(0, 0, 1);
			Main.m.modeler.addVertexTexCol(tempModel[4][0], tempModel[4][1], tempModel[4][2], Block.tempU1, Block.tempV1, chunk.getLightMapValue(posX, posY, posZ + 1, 4));
			Main.m.modeler.addVertexTexCol(tempModel[5][0], tempModel[5][1], tempModel[5][2], Block.tempU1, Block.tempV, chunk.getLightMapValue(posX, posY + 1, posZ + 1, 5));
			Main.m.modeler.addVertexTexCol(tempModel[6][0], tempModel[6][1], tempModel[6][2], Block.tempU, Block.tempV, chunk.getLightMapValue(posX + 1, posY + 1, posZ + 1, 6));
			Main.m.modeler.addVertexTexCol(tempModel[7][0], tempModel[7][1], tempModel[7][2], Block.tempU, Block.tempV1, chunk.getLightMapValue(posX + 1, posY, posZ + 1, 7));
		}
	}

	public void drawTarget() {
		Main.m.modeler.setHexColor(0xFFFFFFFF);
		Main.m.modeler.setOffset(posX, posY, posZ);
		Main.m.modeler.clearRGBAOffset();

		Texture.getTexture("misc/select2.png").bind();

		float offset = .01f;

		Main.m.modeler.start();
		Main.m.modeler.addVertexTex(0 - offset, 0 - offset, 0 - offset, 0.0, 1.0);
		Main.m.modeler.addVertexTex(0 - offset, 1 + offset, 0 - offset, 0.0, 0.0);
		Main.m.modeler.addVertexTex(1 + offset, 1 + offset, 0 - offset, 1.0, 0.0);
		Main.m.modeler.addVertexTex(1 + offset, 0 - offset, 0 - offset, 1.0, 1.0);

		Main.m.modeler.addVertexTex(0 - offset, 0 - offset, 1 + offset, 0.0, 1.0);
		Main.m.modeler.addVertexTex(1 + offset, 0 - offset, 1 + offset, 1.0, 1.0);
		Main.m.modeler.addVertexTex(1 + offset, 1 + offset, 1 + offset, 1.0, 0.0);
		Main.m.modeler.addVertexTex(0 - offset, 1 + offset, 1 + offset, 0.0, 0.0);

		Main.m.modeler.addVertexTex(0 - offset, 0 - offset, 0 - offset, 1, 1);
		Main.m.modeler.addVertexTex(0 - offset, 0 - offset, 1 + offset, 0, 1);
		Main.m.modeler.addVertexTex(0 - offset, 1 + offset, 1 + offset, 0, 0);
		Main.m.modeler.addVertexTex(0 - offset, 1 + offset, 0 - offset, 1, 0);

		Main.m.modeler.addVertexTex(1 + offset, 0 - offset, 0 - offset, 1, 1);
		Main.m.modeler.addVertexTex(1 + offset, 1 + offset, 0 - offset, 1, 0);
		Main.m.modeler.addVertexTex(1 + offset, 1 + offset, 1 + offset, 0, 0);
		Main.m.modeler.addVertexTex(1 + offset, 0 - offset, 1 + offset, 0, 1);

		Main.m.modeler.addVertexTex(0 - offset, 1 + offset, 0 - offset, 1, 1);
		Main.m.modeler.addVertexTex(0 - offset, 1 + offset, 1 + offset, 0, 1);
		Main.m.modeler.addVertexTex(1 + offset, 1 + offset, 1 + offset, 0, 0);
		Main.m.modeler.addVertexTex(1 + offset, 1 + offset, 0 - offset, 1, 0);

		Main.m.modeler.addVertexTex(0 - offset, 0 - offset, 0 - offset, 1, 1);
		Main.m.modeler.addVertexTex(1 + offset, 0 - offset, 0 - offset, 1, 0);
		Main.m.modeler.addVertexTex(1 + offset, 0 - offset, 1 + offset, 0, 0);
		Main.m.modeler.addVertexTex(0 - offset, 0 - offset, 1 + offset, 0, 1);

		Main.m.modeler.finish();

		Main.m.modeler.setOffset(0, 0, 0);
	}

	public void applyWindEffect() {
		tempModel = new double[8][3];
		for (int i3 = 0; i3 < 8; i3++) {
			for (int i2 = 0; i2 < 3; i2++)
				tempModel[i3][i2] += type.model[i3][i2];
		}

		tempMod = posY % 2;
		tempMod = tempMod > 0 ? 1 : -1;

		tempMod2 = (posY + 1) % 2;
		tempMod2 = tempMod2 > 0 ? 1 : -1;

		/* Along X axis */
		if (rLeft) {
			//0145
			tempModel[0][0] += Main.m.world.windOffsetX * tempMod;
			tempModel[1][0] += Main.m.world.windOffsetX * tempMod2;
			tempModel[4][0] += Main.m.world.windOffsetX * tempMod;
			tempModel[5][0] += Main.m.world.windOffsetX * tempMod2;
		}

		if (rRight) {
			//2367
			tempModel[2][0] += Main.m.world.windOffsetX * tempMod2;
			tempModel[3][0] += Main.m.world.windOffsetX * tempMod;
			tempModel[6][0] += Main.m.world.windOffsetX * tempMod2;
			tempModel[7][0] += Main.m.world.windOffsetX * tempMod;
		}

		/* Multidirectional wind
		if (rBottom && rLeft && rFront) {
			tempModel[0][0] += Main.m.world.windOffsetX;
			tempModel[0][1] += Main.m.world.windOffsetY;
			tempModel[0][2] += Main.m.world.windOffsetZ;
		}

		if (rTop && rLeft && rFront) {
			tempModel[1][0] += Main.m.world.windOffsetX;
			tempModel[1][1] += Main.m.world.windOffsetY;
			tempModel[1][2] += Main.m.world.windOffsetZ;
		}

		if (rTop && rRight && rFront) {
			tempModel[2][0] += Main.m.world.windOffsetX;
			tempModel[2][1] += Main.m.world.windOffsetY;
			tempModel[2][2] += Main.m.world.windOffsetZ;
		}

		if (rBottom && rRight && rFront) {
			tempModel[3][0] += Main.m.world.windOffsetX;
			tempModel[3][1] += Main.m.world.windOffsetY;
			tempModel[3][2] += Main.m.world.windOffsetZ;
		}

		if (rBottom && rLeft && rBack) {
			tempModel[4][0] += Main.m.world.windOffsetX;
			tempModel[4][1] += Main.m.world.windOffsetY;
			tempModel[4][2] += Main.m.world.windOffsetZ;
		}

		if (rTop && rLeft && rBack) {
			tempModel[5][0] += Main.m.world.windOffsetX;
			tempModel[5][1] += Main.m.world.windOffsetY;
			tempModel[5][2] += Main.m.world.windOffsetZ;
		}

		if (rTop && rRight && rBack) {
			tempModel[6][0] += Main.m.world.windOffsetX;
			tempModel[6][1] += Main.m.world.windOffsetY;
			tempModel[6][2] += Main.m.world.windOffsetZ;
		}

		if (rBottom && rRight && rBack) {
			tempModel[7][0] += Main.m.world.windOffsetX;
			tempModel[7][1] += Main.m.world.windOffsetY;
			tempModel[7][2] += Main.m.world.windOffsetZ;
		}*/
	}

	public double[] rayTrace(double amount, double rotX, double rotY, double x, double y, double z) {
		return Render.getCoordInFront(rotX, rotY, x, y, z, amount);
	}

	public Block rayTraceToBlock(double sweep, double reach, double rotX, double rotY, double x, double y, double z) {
		for (double i = sweep; i < reach; i += sweep) {
			double[] pos = rayTrace(i, rotX, rotY, x, y, z);
			Block block = Main.m.world.getBlock(pos[0], pos[1], pos[2], false);
			if (block != null && block.type.isSolid) {
				return block;
			}
		}
		return null;
	}

	public static float texWidth = Texture.getTexture("block/fast-terrain.png").width;
	public static float tempU;
	public static float tempV;
	public static float tempU1;
	public static float tempV1;

	public static double tempOffX;
	public static double tempOffY;
	public static double tempOffZ;
	public static int tempMod, tempMod2;
}
