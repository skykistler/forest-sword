package com.teamlucidic.hax.quest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.teamlucidic.hax.Main;

public class QuestLoader {
	public static String path = Main.m.resourcePack + "quests/";
	public HashMap<Integer, QuestTemplate> questTemplates = new HashMap<Integer, QuestTemplate>();
	public ArrayList<String> questsLoaded = new ArrayList<String>();

	public QuestLoader() {
		loadQuests();
	}

	public void loadQuests() {
		File folder = new File(path);
		String[] files = folder.list();
		for (int i = 0; i < files.length; i++) {
			try {
				if (!files[i].endsWith(".qst"))
					continue;
				String name = files[i].replace(".qst", "");

				String file = Main.loadFile("quests/" + files[i]);
				String[] lines = file.split("\n|\\r");

				// declare vars here

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

					//					String param = line.substring(line.indexOf("-") + 1);

					// set vars here
				}
				QuestTemplate result = new QuestTemplate("title", "icon.png", "completionicon.png", null, 250);
				questTemplates.put(name.hashCode(), result);
				questsLoaded.add(name);
			} catch (Exception e) {
				e.printStackTrace();
				Main.m.error("Couldn't load quest template " + files[i]);
			}
		}

		String loaded = "Successfully loaded " + questsLoaded.size() + " quests: ";
		for (int i = 0; i < questsLoaded.size(); i++) {
			loaded += questsLoaded.get(i) + ", ";
		}
		loaded = loaded.substring(0, loaded.length() - 2);
		loaded += ".";
		System.out.println(loaded);
	}

	public Quest getNewQuest(String name) {
		return questTemplates.get(name.hashCode()).newQuest();
	}
}
