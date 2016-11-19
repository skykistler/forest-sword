package com.teamlucidic.hax.render.gui.menu.components;

import java.io.File;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.entity.AABB;
import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.menu.Menu;
import com.teamlucidic.hax.world.Chunk;

public class Minimap extends Component {
	public static Texture map;
	public static boolean blurMap = false;

	public int w, h;
	public double wScale, hScale;

	public Minimap(Menu m, int x, int y, int w, int h) {
		super(m, x, y, w, h);
		getMap();
	}

	public Minimap(Menu m, int w, int h) {
		super(m, 0, 0, w, h);
		posX = Display.getWidth() / 2 - width / 2;
		posY = Display.getHeight() / 2 - height / 2;
		getMap();

	}

	public static void getMap() {
		if (map == null)
			if (new File(Main.m.resourcePack + "maps/" + Main.m.world.name + ".png").exists())
				loadMap();
			else
				createMap();
	}

	public void drawComponent() {
		Main.m.modeler.setHexColor(0xFFFFFFFF);
		GL11.glPushMatrix();
		GL11.glTranslated(posX + width / 2, posY + height / 2, 0);
		GL11.glRotated(90, 0, 0, 1);
		GL11.glTranslated(-(posX + width / 2), -(posY + height / 2), 0);
		drawTexturedRect(posX, posY, width, height, map);

		w = Display.getWidth();
		wScale = w / 920d;
		h = Display.getHeight();
		hScale = h / 605d;

		int size = (int) (Texture.getTexture("gui/player.png").width * 2 * Math.min(wScale, hScale));
		GL11.glPushMatrix();
		GL11.glTranslated(getScreenPosX(Main.m.player.posX), getScreenPosY(Main.m.player.posZ), 0);
		GL11.glRotated(Main.m.player.rotY, 0, 0, 1);
		GL11.glTranslated(-size / 2, -size / 2, 0);
		drawTexturedRect(0, 0, size, size, Texture.getTexture("gui/player.png"), 0xFFFFFFFF);
		GL11.glPopMatrix();

		int size2 = (int) (32 * Math.max(wScale, hScale));
		//		if (Main.m.world.zombieFlock != null)
		//			for (Spot s : Main.m.world.zombieFlock.spots)
		//				drawTexturedRect(getScreenPosX(s.posX) - size2 / 2, getScreenPosY(s.posZ) - size2 / 2, size2, size2, Texture.getTexture("gui/waypoint.png"), 0xFF22FF22);

		if (Main.m.player.currentQuest != null) {
			AABB targetArea = Main.m.player.currentQuest.getCurrentObjective().targetArea;
			if (targetArea != null)
				drawTexturedRect(getScreenPosX((targetArea.minX + targetArea.maxX) / 2) - size2 / 2, getScreenPosY((targetArea.minZ + targetArea.maxZ) / 2) - size2 / 2, size2, size2, Texture.getTexture("gui/waypoint.png"), 0xFFFFFFFF);
		}
		GL11.glPopMatrix();
	}

	public int getScreenPosX(double x) {
		return (int) (posX + x * width / (double) map.width);
	}

	public int getScreenPosY(double y) {
		return (int) (posY + y * height / (double) map.width);
	}

	public static void createMap() {
		if (map != null)
			map.delete();
		map = new Texture(Main.m.world.getWidth(), Main.m.world.getWidth());
		forceUpdateEntireMap();
		saveMap();
	}

	public static void loadMap() {
		if (map != null)
			map.delete();
		map = new Texture("/maps/" + Main.m.world.name + ".png");
		if (map.width != Main.m.world.getWidth())
			createMap();
	}

	public static void updateNearby() {
		for (int x = 0; x < Main.m.world.widthInChunks; x++)
			for (int y = 0; y < Main.m.world.widthInChunks; y++)
				updateChunk(x, y, false, false);
		reloadGL();
	}

	public static void forceUpdateEntireMap() {
		for (int z = 0; z < Main.m.world.widthInChunks; z++)
			for (int x = 0; x < Main.m.world.widthInChunks; x++)
				updateChunk(x, z, true, false);
		reloadGL();
	}

	public static void updateChunk(int x, int z, boolean loadChunkIfUnloaded, boolean reloadGL) {
		boolean wasUnloaded = false;
		Chunk chunk = Main.m.world.getChunk(x * Chunk.width, z * Chunk.width);
		if (!chunk.loaded && loadChunkIfUnloaded) {
			wasUnloaded = true;
			chunk.load();
		}
		if (chunk.loaded)
			for (int x1 = 0; x1 < 16; x1++)
				for (int z1 = 0; z1 < 16; z1++)
					setColor(chunk, x1 + chunk.posX, z1 + chunk.posZ);
		if (wasUnloaded)
			chunk.deLoad(false);
		if (reloadGL)
			reloadGL();
	}

	public static void saveMap() {
		map.save("maps/" + Main.m.world.name + ".png");
	}

	public static void reloadGL() {
		map.setUpGL(blurMap);
	}

	public static void setColor(Chunk chunk, int x, int z) {
		Block block = chunk.getTopBlock(x, z, true);
		if (block == null) {
			map.setPixel(x, z, Main.m.modeler.RGBAtoHex(Render.rand.nextFloat(), Render.rand.nextFloat(), Render.rand.nextFloat(), 1));
			return;
		}
		int[] mapColor = block.type.mapColor;
		if (block.type.isLiquid)
			block = chunk.getTopBlock(x, z, false);
		float yMod = 100 * (float) (block == null ? 0 : block.posY) / (Main.m.world.height - 20);
		yMod = yMod > 100 ? 100 : yMod;
		map.setPixel(x, z, Main.m.modeler.RGBAtoHex((int) (mapColor[2] + yMod), (int) (mapColor[1] + yMod), (int) (mapColor[0] + yMod), 255));
	}
}
