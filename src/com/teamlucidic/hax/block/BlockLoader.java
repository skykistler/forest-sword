package com.teamlucidic.hax.block;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.teamlucidic.hax.Main;

public class BlockLoader {
	public static String path = Main.m.resourcePack + "blocks/";
	public HashMap<Integer, BlockType> blockTypes = new HashMap<Integer, BlockType>();
	public HashMap<Integer, BlockType> blockTypesById = new HashMap<Integer, BlockType>();
	public ArrayList<String> blocksLoaded = new ArrayList<String>();
	public ArrayList<Integer> usedIds = new ArrayList<Integer>();

	public BlockLoader() {
		loadBlocks();
	}

	public void loadBlocks() {
		File folder = new File(path);
		String[] files = folder.list();
		for (int i = 0; i < files.length; i++) {
			try {
				if (!files[i].endsWith(".blck"))
					continue;
				String name = files[i].replace(".blck", "");
				int id = -1;
				boolean destroyable = true;
				boolean solid = true;
				boolean liquid = false;
				float flowSpeed = 0.05F;
				float lightIntensity = 0;
				boolean light = false;
				float[] lightColor = new float[3];
				boolean hasAlpha = false;
				int[] mapColor = new int[3];
				int[] texture = null;
				int[] sidetex = null;
				int[] bottomtex = null;
				ArrayList<String> placeable = new ArrayList<String>();
				boolean grassRender = false;
				boolean xRender = false;
				boolean affectedByWind = false;
				String file = Main.loadFile("blocks/" + files[i]);
				String[] lines = file.split("\n|\\r");
				for (int l = 0; l < lines.length; l++) {
					String line = lines[l];
					if (line.length() < 1)
						continue;
					if (line.startsWith("//"))
						continue;
					if (!line.contains("-")) {
						System.out.println("Invalid line in " + name + " at " + l);
						continue;
					}

					String param = line.substring(line.indexOf("-") + 1);

					if (line.startsWith("id")) {
						id = Integer.parseInt(param);
						if (usedIds.contains(id)) {
							throw new Exception("Already using ID " + id);
						} else
							usedIds.add(id);
					}

					if (line.startsWith("destroyable"))
						if (param.equals("false"))
							destroyable = false;

					if (line.startsWith("solid"))
						if (param.equals("false"))
							solid = false;

					if (line.startsWith("liquid"))
						if (param.equals("true"))
							liquid = true;

					if (line.startsWith("flowSpeed"))
						flowSpeed = Float.parseFloat(param);

					if (line.startsWith("lightIntesisty"))
						lightIntensity = Float.parseFloat(param);

					if (line.startsWith("lightColor")) {
						String[] floatValues = param.split(",");
						for (int i2 = 0; i2 < lightColor.length; i2++)
							lightColor[i2] = Float.parseFloat(floatValues[i2].trim());
					}

					if (line.startsWith("hasAlpha"))
						if (param.equals("true"))
							hasAlpha = true;

					if (line.startsWith("mapColor")) {
						String[] intValues = param.split(",");
						for (int i2 = 0; i2 < mapColor.length; i2++)
							mapColor[i2] = Integer.parseInt(intValues[i2].trim());
					}

					if (line.startsWith("texture")) {
						String[] intValues = param.split(",");
						texture = new int[2];
						for (int i2 = 0; i2 < texture.length; i2++)
							texture[i2] = new Integer(intValues[i2].trim());
					}

					if (line.startsWith("sidetex")) {
						String[] intValues = param.split(",");
						sidetex = new int[2];
						for (int i2 = 0; i2 < sidetex.length; i2++)
							sidetex[i2] = Integer.parseInt(intValues[i2].trim());
					}

					if (line.startsWith("bottomtex")) {
						String[] intValues = param.split(",");
						bottomtex = new int[2];
						for (int i2 = 0; i2 < bottomtex.length; i2++)
							bottomtex[i2] = Integer.parseInt(intValues[i2].trim());
					}

					if (line.startsWith("canBePlacedOn")) {
						String[] validBlocks = param.split(",");
						for (int i2 = 0; i2 < validBlocks.length; i2++)
							placeable.add(validBlocks[i2].trim());
					}

					if (line.startsWith("detail")) {
						if (line.contains("grassRender"))
							grassRender = true;
						if (line.contains("xRender"))
							xRender = true;
						if (line.contains("affectedByWind"))
							affectedByWind = true;
					}

				}
				if (id == -1) {
					id = texture[0] + texture[1] * 16;
					usedIds.add(id);
				}
				if (id < 0)
					throw new Exception("Must specify positive id number");
				if (lightIntensity != 0)
					light = true;
				if (liquid) {
					hasAlpha = true;
					solid = false;
				}
				BlockType result = new BlockType(name, id, texture[0], texture[1], destroyable, solid, liquid, light, hasAlpha, mapColor);
				blockTypes.put(name.hashCode(), result);
				blockTypesById.put(id, result);
				BlockType blockType = getBlockType(name);
				blockType.placeable = placeable;
				if (sidetex != null)
					blockType.setSideTexture(sidetex[0], sidetex[1]);
				if (bottomtex != null)
					blockType.setBottomTexture(bottomtex[0], bottomtex[1]);
				if (liquid) {
					blockType.flowSpeed = flowSpeed;
				}
				if (lightIntensity != 0) {
					blockType.isLight = true;
					blockType.lightIntensity = lightIntensity;
					blockType.lightColor = lightColor;
				}
				blockType.grassRender = grassRender;
				blockType.xRender = xRender;
				blockType.affectedByWind = affectedByWind;
				blocksLoaded.add(blockType.name);
			} catch (Exception e) {
				e.printStackTrace();
				Main.m.error("Couldn't load block " + files[i]);
			}
		}

		String loaded = "Successfully loaded " + blocksLoaded.size() + " blocks: ";
		for (int i = 0; i < blocksLoaded.size(); i++) {
			loaded += blocksLoaded.get(i) + ", ";
		}
		loaded = loaded.substring(0, loaded.length() - 2);
		loaded += ".";
		System.out.println(loaded);
	}

	public BlockType getBlockType(String type) {
		return blockTypes.get(type.hashCode());
	}
}
