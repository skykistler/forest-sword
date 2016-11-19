package com.teamlucidic.hax.world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.block.BlockType;
import com.teamlucidic.hax.entity.Camera;
import com.teamlucidic.hax.entity.Entity;
import com.teamlucidic.hax.entity.Flock;
import com.teamlucidic.hax.entity.Rain;
import com.teamlucidic.hax.entity.inventory.item.Hammer;
import com.teamlucidic.hax.entity.living.Living;
import com.teamlucidic.hax.entity.living.Player;
import com.teamlucidic.hax.entity.particle.ParticleSystem;
import com.teamlucidic.hax.entity.particle.projectile.Projectile;
import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.render.Shader;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.menu.Death;
import com.teamlucidic.hax.render.gui.menu.Hud;
import com.teamlucidic.hax.render.gui.menu.LoadingScreen;
import com.teamlucidic.hax.render.gui.menu.Menu;
import com.teamlucidic.hax.render.gui.menu.components.Minimap;
import com.teamlucidic.hax.world.generator.BasicHillsGenerator;
import com.teamlucidic.hax.world.generator.CloudGenerator;
import com.teamlucidic.hax.world.generator.IslandGenerator;

public class World {
	public static int seaLevel = 12;
	public static int renderDistance = 64;
	public static int genHeight = 64;

	public String name;
	public Map map;

	public Random worldRNG;
	public int widthInChunks = 2;
	public int height = 80;
	public long seed;
	public int[] heightMap;
	public BasicHillsGenerator gen;
	public HashMap<Integer, Chunk> chunkMap;
	public ArrayList<Chunk> seenChunks = new ArrayList<Chunk>();
	public ArrayList<Chunk> zOrderedChunks = new ArrayList<Chunk>();

	public double gravity = 5.8;
	public float[] ambientLight = { 0.6F, 0.6F, 0.6F, 1F };
	public float[] skyColor = { 0.0F, 0.0F, 0.0F, 1.0F };
	public float[] skyColor2 = { 0.9F, 0.9F, 0.9F, 1.0F };
	public float[] skyLight = { 1.0F, 1.0F, 1.0F, 1.0F };
	public int spawnX, spawnY, spawnZ, spawnRot, centerX, centerZ;
	public int lengthOfDay = 16 * 60; //in seconds, so 16 minutes = 24 hours in game
	public double time = 1;
	public boolean lavaHasLight = false;
	public boolean calcSkyShadows = true;
	public boolean changeTime = true;
	public String weather = "normal";
	public Rain rain;
	public double windOffsetX = 0;
	public double windOffsetY = 0;
	public double windOffsetZ = 0;
	public Texture seaTexture;
	public CloudGenerator cloudGen;

	public Flock zombieFlock;

	public double lightX, lightY, lightZ;

	public int worldTicks;
	public static ArrayList<Block> lights;

	public World() {
		this(generateSeed());
	}

	public World(long sd) {
		seed = sd;
		name = Main.m.wName;
		worldRNG = new Random(sd);
		renderDistance = Main.m.settings.renderDistance * Chunk.width;
	}

	public void update() {
		time = Math.cos((double) worldTicks / (double) (lengthOfDay * Main.m.idealTicksPerSecond) * 2 * Render.pi);
		if (weather.equals("rain")) {
			skyColor2[0] = .0f;
			skyColor2[1] = skyColor2[0];
			skyColor2[2] = skyColor2[0] - .1f;
			skyColor[0] = skyColor2[0] - .1f;
			skyColor[1] = skyColor2[0] - .1f;
			skyColor[2] = skyColor2[0] + .3f;
		} else {
			skyColor[0] = (float) (0.15 + .3 * time);
			skyColor[1] = (float) (0.60 * time);
			skyColor[2] = (float) (0.6 + .3 * time);

			skyColor2[0] = (float) (.6 + 0.20 * time);
			skyColor2[1] = (float) (.6 + 0.20 * time);
			skyColor2[2] = (float) (.8 + 0.10 * time);
		}
		skyColor[3] = 1;
		skyColor2[3] = 1;
		if (weather.equals("rain")) {
			for (int i = 0; i < 3; i++)
				skyLight[i] = (skyColor[i] + skyColor2[i]) / 2 + .4f;
		} else
			for (int i = 0; i < 3; i++)
				skyLight[i] = (skyColor[i] + skyColor2[i]) / 2;

		if (Main.m.settings.windTest) {
			int rate = 60;
			windOffsetX = Math.cos((worldTicks / (double) rate) * Render.pi) * .05;
			//		windOffsetY = Math.cos(worldTicks / 53d * Render.pi) * .017;
			//		windOffsetZ = Math.sin(worldTicks / 190d * 2 * Render.pi) * .06;
		}

		if (rain != null && !rain.continuous && !weather.equals("rain") && !Entity.entityList.contains(rain))
			rain = null;

		if (Main.m.settings.zOrderChunks) {
			seenChunks.clear();
			seenChunks.trimToSize();
		}
		updateChunks();
		if (Main.m.settings.zOrderChunks)
			zOrderChunks();
		Flock.updateAllFlocks();
		Entity.updateEntities();
		if (Main.m.player.dead && !(Menu.currentMenu instanceof Death))
			Menu.setMenu(new Death());

		if (changeTime)
			worldTicks++;
	}

	public void render() {
		if (worldTicks < 2)
			return;
		GL11.glFogf(GL11.GL_FOG_DENSITY, weather.equals("rain") ? 2.5f : .12f * Main.m.settings.renderDistance);
		GL11.glDisable(GL11.GL_CULL_FACE);
		Entity.renderEntities();
		GL11.glEnable(GL11.GL_CULL_FACE);
		renderChunks();
		Entity.renderAlpha();
		if (!weather.equals("rain"))
			GL11.glFogf(GL11.GL_FOG_DENSITY, .1f);
		renderSea();

		GL11.glFogf(GL11.GL_FOG_DENSITY, 0f);
		renderSunAndMoon();
		renderClouds();

		Hammer.renderSelection();
		if (Main.m.player.targetBlock != null)
			Main.m.player.targetBlock.drawTarget();
		Main.m.player.render();
	}

	public void renderClouds() {
		int width = 80;
		int border = width * 4;
		double h = Main.m.player.posY + 30;
		float mod = lengthOfDay * (width) / 2;
		float offset = (worldTicks % mod) / mod;
		cloudGen.tex.bind();
		Main.m.modeler.reset();
		Main.m.modeler.setHexColor(0xFFFFFFFF);
		Main.m.modeler.start();
		Main.m.modeler.addVertexTex(-border * 2 + Main.m.camera.posX, h, -border + Main.m.camera.posZ, offset, 0);
		Main.m.modeler.addVertexTex(width + border * 2 + Main.m.camera.posX, h, -border + Main.m.camera.posZ, 1 + offset, 0);
		Main.m.modeler.addVertexTex(width + border * 2 + Main.m.camera.posX, h, width + border + Main.m.camera.posZ, 1 + offset, 1);
		Main.m.modeler.addVertexTex(-border * 2 + Main.m.camera.posX, h, width + border + Main.m.camera.posZ, offset, 1);
		Main.m.modeler.finish();
	}

	public void renderSea() {
		if (seaTexture == null)
			seaTexture = BlockType.getBlockTexture(Main.m.blockLoader.getBlockType("Water"));
		seaTexture.bind();
		drawBorderingQuads(-.17);
	}

	public void drawBorderingQuads(double offset) {
		int width = getWidth();
		int size = 500;
		Main.m.modeler.reset();
		Main.m.modeler.setHexColor(0xFFFFFFFF);
		Main.m.modeler.start();
		if (Main.m.player.posX < World.renderDistance * 2) {
			Main.m.modeler.addVertexTex(-size, seaLevel + offset, -size, -size, -size);
			Main.m.modeler.addVertexTex(-size, seaLevel + offset, width + size, -size, width + size);
			Main.m.modeler.addVertexTex(0, seaLevel + offset, width + size, 0, width + size);
			Main.m.modeler.addVertexTex(0, seaLevel + offset, -size, 0, -size);

			Main.m.modeler.addVertexTex(-size, seaLevel + offset, -size, -size, -size);
			Main.m.modeler.addVertexTex(0, seaLevel + offset, -size, 0, -size);
			Main.m.modeler.addVertexTex(0, seaLevel + offset, width + size, 0, width + size);
			Main.m.modeler.addVertexTex(-size, seaLevel + offset, width + size, -size, width + size);
		}

		if (width - Main.m.player.posX < World.renderDistance * 2) {
			Main.m.modeler.addVertexTex(size + width, seaLevel + offset, -size, -size, -size);
			Main.m.modeler.addVertexTex(width, seaLevel + offset, -size, 0, -size);
			Main.m.modeler.addVertexTex(width, seaLevel + offset, width + size, 0, width + size);
			Main.m.modeler.addVertexTex(size + width, seaLevel + offset, width + size, -size, width + size);

			Main.m.modeler.addVertexTex(size + width, seaLevel + offset, -size, -size, -size);
			Main.m.modeler.addVertexTex(size + width, seaLevel + offset, width + size, -size, width + size);
			Main.m.modeler.addVertexTex(width, seaLevel + offset, width + size, 0, width + size);
			Main.m.modeler.addVertexTex(width, seaLevel + offset, -size, 0, -size);
		}

		if (Main.m.player.posZ < World.renderDistance * 2) {
			Main.m.modeler.addVertexTex(0, seaLevel + offset, 0, 0, 0);
			Main.m.modeler.addVertexTex(width, seaLevel + offset, 0, width, 0);
			Main.m.modeler.addVertexTex(width, seaLevel + offset, -size, width, -size);
			Main.m.modeler.addVertexTex(0, seaLevel + offset, -size, 0, -size);

			Main.m.modeler.addVertexTex(0, seaLevel + offset, 0, 0, 0);
			Main.m.modeler.addVertexTex(0, seaLevel + offset, -size, 0, -size);
			Main.m.modeler.addVertexTex(width, seaLevel + offset, -size, width, -size);
			Main.m.modeler.addVertexTex(width, seaLevel + offset, 0, width, 0);
		}

		if (width - Main.m.player.posZ < World.renderDistance * 2) {
			Main.m.modeler.addVertexTex(0, seaLevel + offset, size + width, 0, size + width);
			Main.m.modeler.addVertexTex(width, seaLevel + offset, size + width, width, width + size);
			Main.m.modeler.addVertexTex(width, seaLevel + offset, width, width, width);
			Main.m.modeler.addVertexTex(0, seaLevel + offset, width, 0, width);

			Main.m.modeler.addVertexTex(0, seaLevel + offset, size + width, 0, size + width);
			Main.m.modeler.addVertexTex(0, seaLevel + offset, width, 0, width);
			Main.m.modeler.addVertexTex(width, seaLevel + offset, width, width, width);
			Main.m.modeler.addVertexTex(width, seaLevel + offset, size + width, width, width + size);
		}

		Main.m.modeler.finish();
	}

	public void renderSky() {
		float x = (float) ((Main.m.camera.rotX + 90) / 178D);

		int topRight = 0;
		int bottomLeft = 0;
		topRight = Main.m.modeler.RGBAtoHex(skyColor[0], skyColor[1], skyColor[2], 1);
		bottomLeft = Main.m.modeler.RGBAtoHex(skyColor2[0], skyColor2[1], skyColor2[2], 1);

		int topLeft = topRight;
		int bottomRight = bottomLeft;

		Texture.unbind(false);
		Main.m.modeler.reset();
		Main.m.modeler.start();
		Main.m.modeler.setOffset(0, -x * Display.getHeight(), 0);
		Main.m.modeler.setHexColor(topLeft);
		Main.m.modeler.addVertex(0, 0, 0);
		Main.m.modeler.setHexColor(bottomLeft);
		Main.m.modeler.addVertex(0, Display.getHeight() * 2, 0);
		Main.m.modeler.setHexColor(bottomRight);
		Main.m.modeler.addVertex(Display.getWidth(), Display.getHeight() * 2, 0);
		Main.m.modeler.setHexColor(topRight);
		Main.m.modeler.addVertex(Display.getWidth(), 0, 0);
		Main.m.modeler.finish();
		Main.m.modeler.setOffset(0, 0, 0);
	}

	public void renderSunAndMoon() {
		Shader.unbind();
		GL11.glPushMatrix();
		GL11.glTranslated(Main.m.camera.posX, Main.m.camera.posY, Main.m.camera.posZ);
		int l = lengthOfDay * 60;
		double rot = 90 + (((double) worldTicks % l) / l) * 360;
		rot %= 360;
		GL11.glRotated(rot, 1, 0, 0);
		int w = 200;
		Main.m.modeler.setHexColor(0xFFFFFFFF);

		Texture.getTexture("misc/sun.png").bind();
		Main.m.modeler.drawRect3D(-w / 2, -w / 2, -450, w, w);

		GL11.glRotated(180, 1, 0, 0);
		Texture.getTexture("misc/moon.png").bind();
		Main.m.modeler.drawRect3D(-w / 2, -w / 2, -450, w, w);

		GL11.glPopMatrix();
		Main.m.render.main.bind();
	}

	public void zOrderChunks() {
		zOrderedChunks.clear();
		seenChunks.trimToSize();
		Chunk c = null;
		while ((c = getFarthestSeenNonOrderedChunk()) != null)
			zOrderedChunks.add(c);
	}

	public Chunk getFarthestSeenNonOrderedChunk() {
		double curDis = 0;
		double farthestDis = 0;
		Chunk result = null;
		for (int i = 0; i < seenChunks.size(); i++) {
			Chunk c = seenChunks.get(i);
			if (c == null)
				continue;
			if (c.withinRenderDis && c.inCamera && c.loaded && !zOrderedChunks.contains(c)) {
				curDis = Math.abs((Main.m.camera.posX - (c.posX + 8)) * (Main.m.camera.posX - (c.posX + 8)) + (Main.m.camera.posZ - (c.posZ + 8)) * (Main.m.camera.posZ - (c.posZ + 8)));
				if (curDis > farthestDis) {
					farthestDis = curDis;
					result = c;
				}
			}
		}
		return result;
	}

	public void updateChunks() {
		for (Chunk chunk : chunkMap.values())
			chunk.update();
	}

	public void updateAllBlocks() {
		for (Chunk chunk : chunkMap.values())
			chunk.updateAllBlocks();
	}

	public void renderChunks() {
		if (!Main.m.settings.zOrderChunks)
			for (Chunk chunk : chunkMap.values())
				chunk.render();
		else if (zOrderedChunks != null)
			for (int i = 0; i < zOrderedChunks.size(); i++)
				zOrderedChunks.get(i).render();
	}

	public void saveWorld() {
		try {
			for (Chunk chunk : Main.m.world.chunkMap.values())
				if (chunk.loaded)
					map.saveChunk(chunk);
			map.saveMap(false);
			System.out.println("Successfully saved map " + name);
		} catch (IOException e) {
			e.printStackTrace();
			Main.m.error("Unable to save map " + name);
		}
	}

	public void unload() {
		for (Chunk c : chunkMap.values())
			c.deLoad(false);
	}

	public void loadWorld(String name) {
		Menu.setMenu(new LoadingScreen("Loading map..."));
		Main.m.render();

		loadMap();
		LoadingScreen.setPercentDone(.9);
		cloudGen = new CloudGenerator();
		preparePlayer();
		Minimap.getMap();
		LoadingScreen.setLabel("Generating Mini Map...");
		LoadingScreen.setPercentDone(1);

		Main.m.render.currentFOV = Main.m.render.targetFOV = Main.m.render.baseFOV;
		System.out.println("Successfully loaded map " + name);
		Menu.setMenu(null);
		Menu.setHud(new Hud());
		Main.m.gc();
		Main.m.resetTimer();
	}

	public void loadMap() {
		map = new Map(name);
		map.loadMap();
		Main.m.gc();
		//		updateHeightMap();
	}

	public void generateWorld() {
		Menu.setMenu(new LoadingScreen("Generating terrain..."));
		Main.m.render();

		clearWorld();
		makeMap();
		LoadingScreen.setLabel("Saving Map...");
		LoadingScreen.setPercentDone(.95);
		saveWorld();
		LoadingScreen.setLabel("Generating Mini Map...");
		LoadingScreen.setPercentDone(1);
		Minimap.createMap();
		loadWorld(name);
	}

	public void clearWorld() {
		chunkMap = new HashMap<Integer, Chunk>();
		for (int x = 0; x < widthInChunks; x++)
			for (int z = 0; z < widthInChunks; z++)
				makeChunk(x, z);
		int width = getWidth();
		heightMap = new int[width * width];
		World.lights = new ArrayList<Block>();
		Entity.entityList = new ArrayList<Entity>();
		Living.livingList = new ArrayList<Living>();
		Projectile.projectileList = new ArrayList<Projectile>();
	}

	public void makeMap() {
		generateHeightMap();
		map = new Map(name);
		try {
			map.saveMap(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int i = 0;
		LoadingScreen.setLabel("Placing blocks...");
		for (Chunk chunk : chunkMap.values()) {
			chunk.loaded = true;
			chunk.clearChunk();
			chunk.placeBlocks();
			chunk.decorate();
			i++;
			LoadingScreen.setPercentDone(.1 + .9 * i / chunkMap.size());
			chunk.deLoad(true);
		}
	}

	public void generateHeightMap() {
		if (Main.m.wType == 0)
			gen = new BasicHillsGenerator(seed, getWidth(), genHeight);
		if (Main.m.wType == 1)
			gen = new IslandGenerator(seed, getWidth(), genHeight);
		gen.generateHeightMap();
		for (int i = 0; i < heightMap.length; i++)
			heightMap[i] += gen.noise[i];
		spawnX = centerX = gen.startX;
		spawnZ = centerZ = gen.startZ;
	}

	public void preparePlayer() {
		setSpawn(getTopBlock(spawnX, spawnZ, true, true));
		Main.m.camera = new Camera();
		Main.m.player = new Player();
		Main.m.player.spawnAtPosition(spawnX, spawnY + .1, spawnZ);
		Main.m.camera.setTarget(Main.m.player);
		Hud.setHand("LB", Texture.getTexture("gui/fist.png"));
		Hud.setHand("RB", Texture.getTexture("gui/fist.png"));
	}

	public void makeChunk(int x, int z) {
		Chunk chunk = new Chunk(x, z);
		chunkMap.put(getChunkKey(x, z), chunk);
	}

	public int getChunkKey(int x, int z) {
		return (z << 16) ^ x;
	}

	public void setSpawn(int x, int y, int z) {
		spawnX = x;
		spawnY = y;
		spawnZ = z;
	}

	public void setSpawn(Block block) {
		if (block != null)
			setSpawn(block.posX, block.posY + 1, block.posZ);
	}

	public void respawnPlayer() {
		Main.m.player.spawnAtPosition(spawnX, spawnY + .1, spawnZ);
		Main.m.player.health = Main.m.player.maxHealth;
		Main.m.player.energy = Main.m.player.inventory.abilities.maxEnergy;
		Main.m.player.dead = false;
		Menu.setMenu(null);
	}

	public Chunk getChunk(double x, double z) {
		x /= Chunk.width;
		z /= Chunk.width;
		int xi = Render.floor(x);
		int zi = Render.floor(z);
		if (chunkMap == null)
			return null;
		return chunkMap.get(getChunkKey(xi, zi));
	}

	public void placeBlock(String type, int x, int y, int z, boolean updateSurrounding) {
		if (type != null && !Main.m.blockLoader.blockTypes.containsKey(type.hashCode())) {
			Main.m.error("Unable to place block: " + type);
			return;
		}
		if (x >= 0 && x < getWidth() && z >= 0 && z < getWidth() && y >= 0 && y < height) {
			Chunk chunk = getChunk(x, z);
			if (chunk == null)
				return;

			if (type != null && Main.m.blockLoader.getBlockType(type).isLight) {
				lightX = x;
				lightY = y;
				lightZ = z;
			}

			if (!chunk.loaded) {
				chunk.load();
				chunk.markForDeload = true;
			}
			chunk.placeBlock(type, x, y, z, updateSurrounding);
		}
	}

	public void placeBlock(int id, int x, int y, int z, boolean updateSurrounding) {
		BlockType bt = Main.m.blockLoader.blockTypesById.get(id);
		placeBlock(bt == null ? null : bt.name, x, y, z, updateSurrounding);
	}

	public void clearBlock(Block block) {
		if (block != null) {
			placeBlock(null, block.posX, block.posY, block.posZ, true);
			//			if (block.type.isLight || calcSkyShadows)
			//				World.lights.remove(block);
		}
	}

	public void updateSurroundingBlocks(int posX, int posY, int posZ) {
		for (int y = posY - 1; y < posY + 2; y++)
			for (int x = posX - 1; x < posX + 2; x++)
				for (int z = posZ - 1; z < posZ + 2; z++) {
					Block block3 = getBlock(x, y, z, false);
					if (block3 != null) {
						block3.update();
						block3.chunk.changesMade = true;
					}
				}
	}

	public void explodeAt(int radius, double x, double y, double z, boolean breakBlocks) {
		int xi = (int) Math.round(x);
		int yi = (int) Math.round(y);
		int zi = (int) Math.round(z);
		double curDis = 0;
		double range = radius * radius;
		if (breakBlocks)
			for (int y1 = -radius; y1 < radius; y1++)
				for (int x1 = -radius; x1 < radius; x1++)
					for (int z1 = -radius; z1 < radius; z1++) {
						curDis = x1 * x1 + y1 * y1 + z1 * z1;
						if (curDis <= range) {
							Block b = Main.m.world.getBlock(x1 + xi, y1 + yi, z1 + zi, true);
							if (b != null && b.type.isDestroyable && Render.rand.nextDouble() > .1)
								b.breakBlock(false);
						}
					}

		double damage = 0;
		range = (radius * 2) * (radius * 2);
		for (Living l : Living.livingList) {
			curDis = (l.posX - x) * (l.posX - x) + (l.posY - y) * (l.posY - y) + (l.posZ - z) * (l.posZ - z);
			if (curDis <= range) {
				damage = range / ((curDis == 0 ? .01 : curDis)) * 8;
				l.applyDamage(damage, true);
				l.applyForce(0, Math.toDegrees(Math.atan2((l.posX - x), -(l.posZ - z))), Math.min(.8, damage / 8), Math.min(.8, damage / 8), 0);
			}
		}

		ParticleSystem p = Main.m.partSysLoader.getParticleSystemType("Explosion").spawnSystem(x, y, z);
		p.sizeOfParticle = p.particleSizeChange = radius;
	}

	public void clearBlock(int x, int y, int z) {
		clearBlock(getBlock(x, y, z, true));
	}

	public Block getBlock(double x, double y, double z, boolean loadIfUnloaded) {
		Chunk chunk = getChunk(x, z);
		if (chunk == null)
			return null;
		return chunk.getBlock(x, y, z);
	}

	public Block getTopBlock(double x, double z, boolean includeAlpha, boolean loadIfUnloaded) {
		Chunk chunk = getChunk(x, z);
		if (chunk != null) {
			if (!chunk.loaded) {
				chunk.load();
				chunk.markForDeload = true;
			}
			return chunk.getTopBlock(x, z, includeAlpha);
		} else
			return null;
	}

	public int getHeightValue(int x, int z) {
		int width = getWidth();
		if (heightMap != null && x >= 0 && x < width && z >= 0 && z < width)
			return heightMap[z * width + x];
		return 0;
	}

	public int getWidth() {
		return widthInChunks * Chunk.width;
	}

	public void setWeather(String w) {
		weather = w;
		if (weather.contains("rain"))
			rain = new Rain();
		else if (rain != null)
			rain.continuous = false;
	}

	public static long generateSeed() {
		return System.nanoTime() * Render.rand.nextInt();
	}

}
