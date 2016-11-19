package com.teamlucidic.hax;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Settings {
	public String settingsContent;
	public StringBuilder toWrite;

	public String playerName = "Player";
	public int windowWidth = 920;
	public int windowHeight = 605;
	public boolean vsync = false;
	public boolean windTest = false;
	public boolean zOrderChunks = true;
	public int renderDistance = 5;
	public boolean fog = true;
	public boolean alphaSort = true;
	public boolean drawSlotLettersOnHud = true;
	public boolean useDisplayLists = true;

	public Settings() {
		try {
			File f = new File(Main.m.resourcePack + "settings.txt");
			if (!f.exists())
				save();
			loadSettings();
		} catch (IOException e) {
			e.printStackTrace();
			Main.m.error("Unable to load settings");
		}
	}

	public void loadSettings() throws IOException {
		settingsContent = Main.loadFile("settings.txt");
		String[] lines = settingsContent.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			line = line.toLowerCase();
			if (line.length() < 1)
				continue;
			if (line.startsWith("//"))
				continue;
			if (!line.contains("-")) {
				System.out.println("Invalid line in settings: " + i);
				continue;
			}
			String param = line.split("-")[1];

			if (line.startsWith("name"))
				playerName = lines[i].split("-")[1];

			if (line.startsWith("width"))
				windowWidth = Integer.parseInt(param);

			if (line.startsWith("height"))
				windowHeight = Integer.parseInt(param);

			if (line.startsWith("vsync"))
				if (param.equals("true"))
					vsync = true;
				else
					vsync = false;

			if (line.startsWith("windtest"))
				if (param.equals("true"))
					windTest = true;
				else
					windTest = false;

			if (line.startsWith("zord"))
				if (param.equals("true"))
					zOrderChunks = true;
				else
					zOrderChunks = false;

			if (line.startsWith("renderdis"))
				renderDistance = Integer.parseInt(param);

			if (line.startsWith("fog"))
				if (param.equals("true"))
					fog = true;
				else
					fog = false;

			if (line.startsWith("alphas"))
				if (param.equals("true"))
					alphaSort = true;
				else
					alphaSort = false;

			if (line.startsWith("drawslotlettersonhud"))
				if (param.equals("true"))
					drawSlotLettersOnHud = true;
				else
					drawSlotLettersOnHud = false;

			if (line.startsWith("displaylist") || line.startsWith("useDisplayL"))
				if (param.equals("true"))
					useDisplayLists = true;
				else
					useDisplayLists = false;
		}
	}

	public void save() throws IOException {
		File sf = new File(Main.m.resourcePack + "settings.txt");
		sf.delete();
		sf.createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(sf));
		toWrite = new StringBuilder();
		toWrite.append("name-" + playerName + "\r");
		toWrite.append("width-920\r");
		toWrite.append("height-605\r");
		toWrite.append("vsync-" + vsync + "\r");
		toWrite.append("windTest-" + windTest + "\r");
		toWrite.append("zOrderChunks-" + zOrderChunks + "\r");
		toWrite.append("renderDistance-" + renderDistance + "\r");
		toWrite.append("fog-" + fog + "\r");
		toWrite.append("alphaSort-" + alphaSort + "\r");
		toWrite.append("drawSlotLettersOnHud-" + drawSlotLettersOnHud + "\r");
		toWrite.append("useDisplayLists-" + useDisplayLists + "\r");
		writer.write(toWrite.toString());
		writer.close();
		toWrite = null;
		Main.m.gc();
		loadSettings();
	}
}
