package com.teamlucidic.hax.block;

import java.util.ArrayList;

import com.teamlucidic.hax.render.Texture;

public class BlockType {
	public static int texResolution = Texture.getTexture("block/fast-terrain.png").width / 16;
	public static double[][] defaultModel = makeModel();
	public static double[][] liquidModel1 = makeLiquidModel();
	public static double[][] liquidModel2 = makeLiquidModel2();
	public String name;
	public int id;
	public Texture tex;
	public int[] texSheetPos = new int[6];
	public int targetSide;
	public boolean isDestroyable = true;
	public boolean isSolid;
	public boolean isLiquid = false;
	public boolean isLight = false;
	public boolean hasAlpha = false;
	public float flowSpeed = .05F;
	public ArrayList<String> placeable;
	public boolean grassRender = false;
	public boolean xRender = false;
	public boolean affectedByWind = false;
	public float[] lightColor;// = { 1.0F, .8F, .13F, 1.0F };
	public float lightIntensity = 1F;
	public int[] mapColor;
	public double[][] model = defaultModel;

	public BlockType(String type, int i, int u, int v, boolean destroyable, boolean solid, boolean liquid, boolean light, boolean alpha, int[] mapC) {
		name = type;
		id = i;
		mapColor = mapC;
		setTopTexture(u, v);
		setSideTexture(u, v);
		setBottomTexture(u, v);
		isDestroyable = destroyable;
		isSolid = solid;
		isLiquid = liquid;
		isLight = light;
		hasAlpha = alpha;
	}

	public void setTopTexture(int u, int v) {
		texSheetPos[0] = u;
		texSheetPos[1] = v;
	}

	public void setSideTexture(int u, int v) {
		texSheetPos[2] = u;
		texSheetPos[3] = v;
	}

	public void setBottomTexture(int u, int v) {
		texSheetPos[4] = u;
		texSheetPos[5] = v;
	}

	public void addPlaceable(String type) {
		if (!placeable.contains(type))
			placeable.add(type);
	}

	public boolean canBePlacedOn(Block b) {
		if (placeable.size() == 0)
			return true;
		if ((b == null && placeable.contains("Air")) || (b != null && placeable.contains(b.type.name)))
			return true;
		else
			return false;
	}

	public static Texture getBlockTexture(BlockType type) {
		int u = type.texSheetPos[2] * BlockType.texResolution;
		int v = type.texSheetPos[3] * BlockType.texResolution;
		int u1 = BlockType.texResolution;
		int v1 = BlockType.texResolution;
		return Texture.getTexture("block/fast-terrain.png").getSubTexture(u, v, u1, v1);
	}

	//  torch render
	//	Main.m.modeler.setHexColor(0xFFFFFFFF);
	//	GL11.glPushMatrix();
	//	GL11.glTranslated(posX + halfCube, posY, posZ + halfCube);
	//	GL11.glRotated(-Main.m.camera.rotY, 0, 1, 0);
	//	GL11.glTranslated(-halfCube, 0, 0);
	//
	//	tex.bind();
	//	Main.m.modeler.drawRect3D(0, 0, 0, cubeSize, cubeSize);
	//
	//	GL11.glPopMatrix();

	public static double[][] makeModel() {
		double[][] model = new double[8][3];
		double size = 1;
		double minn = 0;

		model[0] = makeVertex(minn, minn, minn);
		model[1] = makeVertex(minn, size, minn);
		model[2] = makeVertex(size, size, minn);
		model[3] = makeVertex(size, minn, minn);
		model[4] = makeVertex(minn, minn, size);
		model[5] = makeVertex(minn, size, size);
		model[6] = makeVertex(size, size, size);
		model[7] = makeVertex(size, minn, size);
		return model;
	}

	public static double[][] makeLiquidModel() {
		double[][] model = makeModel();
		for (int i = 0; i < 8; i++)
			model[i][1] -= .17;
		return model;
	}

	public static double[][] makeLiquidModel2() {
		double[][] model = makeModel();
		model[1][1] -= .17;
		model[2][1] -= .17;
		model[5][1] -= .17;
		model[6][1] -= .17;
		return model;
	}

	public static double[] makeVertex(double x, double y, double z) {
		double[] vertex = new double[3];
		vertex[0] = x;
		vertex[1] = y;
		vertex[2] = z;
		return vertex;
	}

}
