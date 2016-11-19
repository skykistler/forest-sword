package com.teamlucidic.hax.world;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;

public class Map {
	public static String path = Main.m.resourcePack + "maps/";
	public String name;
	public File map;
	public StringBuilder toWrite;
	public String file;
	public String[] lines;
	public int start = 10;

	public Map(String n) {
		name = n;
		map = new File(path + name + ".map");
	}

	public void saveMap(boolean overwrite) throws IOException {
		map.delete();
		map.createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(map));
		toWrite = new StringBuilder();
		toWrite.append("width in chunks: " + Main.m.world.widthInChunks + "\r");
		toWrite.append("height: " + Main.m.world.height + "\r");
		toWrite.append("center: " + Main.m.world.centerX + ", " + Main.m.world.centerZ + "\r");
		toWrite.append("spawn: " + Main.m.world.spawnX + ", " + Main.m.world.spawnY + ", " + Main.m.world.spawnZ + ", " + Main.m.world.spawnRot + "\r");
		toWrite.append("sea level: " + World.seaLevel + "\r");
		toWrite.append("gravity: " + Main.m.world.gravity + "\r");
		toWrite.append("daylength: " + Main.m.world.lengthOfDay + "\r");
		toWrite.append("change time: " + Main.m.world.changeTime + "\r");
		toWrite.append("time: " + Main.m.world.worldTicks + "\r");
		toWrite.append("weather: " + Main.m.world.weather);
		start = 10;
		for (int x = 0; x < Main.m.world.widthInChunks; x++)
			for (int z = 0; z < Main.m.world.widthInChunks; z++) {
				toWrite.append("\r");
				if (overwrite)
					writeChunk(Main.m.world.chunkMap.get(Main.m.world.getChunkKey(x, z)), toWrite);
				else
					toWrite.append(lines[x * Main.m.world.widthInChunks + z + start]);
			}

		writer.write(toWrite.toString());
		writer.close();
		toWrite = null;
		Main.m.gc();
		loadFile();
		start = getLastParameterLine();
	}

	public void writeChunk(Chunk chunk, StringBuilder sb) {
		sb.append(chunk.posX + ", " + chunk.posZ + ": ");
		int id = -1;
		int skip = 1;
		for (int y = 0; y < Main.m.world.height; y++)
			for (int x = 0; x < Chunk.width; x++)
				for (int z = 0; z < Chunk.width; z++) {
					Block bl = chunk.getBlock(x + chunk.posX, y, z + chunk.posZ);
					int curId = bl == null ? -1 : bl.type.id;
					if (x == 0 && y == 0 && z == 0) {
						id = curId;
					} else {
						if (id == curId)
							skip++;
						else {
							sb.append("#" + skip + "!" + (id == -1 ? "n" : id));
							id = curId;
							skip = 1;
						}
					}
				}
		sb.append("#" + skip + "!" + (id == -1 ? "n" : id));
		chunk.changesMade = false;
	}

	public void saveChunk(Chunk chunk) {
		StringBuilder sb = new StringBuilder();
		writeChunk(chunk, sb);
		lines[chunk.posX / Chunk.width * Main.m.world.widthInChunks + chunk.posZ / Chunk.width + start] = sb.toString();
	}

	public void loadFile() throws IOException {
		file = Main.loadFile("maps/" + name + ".map");
		lines = file.split("\n|\\r");
	}

	public void loadMap() {
		if (!exists()) {
			Main.m.error("Map not found: " + name);
			return;
		}
		try {
			loadFile();
		} catch (IOException e) {
			e.printStackTrace();
			Main.m.error("Unable to load map " + name);
		}
		int i = 0;
		while (!lines[i].startsWith("0")) {
			if (lines[i].startsWith("width")) {
				String[] widthParts = lines[i].split(": ");
				Main.m.world.widthInChunks = Integer.parseInt(widthParts[1]);
			}
			if (lines[i].startsWith("height")) {
				String[] heightParts = lines[i].split(": ");
				Main.m.world.height = Integer.parseInt(heightParts[1]);
			}
			if (lines[i].startsWith("center")) {
				String centerParam = lines[i].split(": ")[1];
				String[] paramP = centerParam.split(",");
				Main.m.world.centerX = Integer.parseInt(paramP[0].trim());
				Main.m.world.centerZ = Integer.parseInt(paramP[1].trim());
			}
			if (lines[i].startsWith("spawn")) {
				String spawnParam = lines[i].split(": ")[1];
				String[] paramP = spawnParam.split(",");
				Main.m.world.setSpawn(Integer.parseInt(paramP[0].trim()), Integer.parseInt(paramP[1].trim()), Integer.parseInt(paramP[2].trim()));
				Main.m.world.spawnRot = Integer.parseInt(paramP[3].trim());
			}
			if (lines[i].startsWith("sea")) {
				String[] seaParts = lines[i].split(": ");
				World.seaLevel = Integer.parseInt(seaParts[1]);
			}
			if (lines[i].startsWith("gravity"))
				Main.m.world.gravity = Double.parseDouble(lines[i].split(": ")[1]);
			if (lines[i].startsWith("daylength"))
				Main.m.world.lengthOfDay = Integer.parseInt(lines[i].split(": ")[1]);
			if (lines[i].startsWith("change time"))
				Main.m.world.changeTime = lines[i].split(": ")[1].toLowerCase().startsWith("t") ? true : false;
			if (lines[i].startsWith("time"))
				Main.m.world.worldTicks = Integer.parseInt(lines[i].split(": ")[1]);
			if (lines[i].startsWith("weather"))
				Main.m.world.setWeather(lines[i].split(": ")[1]);
			i++;
		}
		start = getLastParameterLine();
		Main.m.world.clearWorld();

	}

	public void loadChunk(Chunk chunk) {
		if (chunk == null)
			return;
		String[] data = lines[chunk.posX / Chunk.width * Main.m.world.widthInChunks + chunk.posZ / Chunk.width + start].split("#");
		chunk.clearChunk();
		int id = -1;
		int skip = 0;
		int index = 0;
		int[] idMap = new int[Chunk.width * Chunk.width * Main.m.world.height];
		for (int i = 1; i < data.length; i++) {
			String part = data[i];
			String[] lengthAndId = part.split("!");
			skip = Integer.parseInt(lengthAndId[0]);
			id = lengthAndId[1].indexOf('n') > -1 ? -1 : Integer.parseInt(lengthAndId[1]);
			for (int i2 = 0; i2 < skip; i2++) {
				idMap[index] = id;
				index++;
			}
		}

		index = 0;
		for (int y = 0; y < Main.m.world.height; y++)
			for (int x = 0; x < Chunk.width; x++)
				for (int z = 0; z < Chunk.width; z++)
					chunk.placeBlock(idMap[index++], x + chunk.posX, y, z + chunk.posZ, false);
	}

	public boolean exists() {
		return map.exists();
	}

	public int getLastParameterLine() {
		int i = 0;
		while (!lines[i].contains("#"))
			i++;
		return i;
	}
}
