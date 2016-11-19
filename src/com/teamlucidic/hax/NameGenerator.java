package com.teamlucidic.hax;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import com.teamlucidic.hax.render.Render;

public class NameGenerator {
	public static String folder = "namegen/";
	public static HashMap<Integer, String[][]> tables = new HashMap<Integer, String[][]>();

	public static void loadAllTables() {
		String[] files = new File(Main.m.resourcePack + folder).list();
		String list = "";
		for (String s : files) {
			try {
				String file = Main.loadFile(folder + s);
				s = s.replace(".txt", "");
				String[] lines = file.split("\n|\\r");
				String[][] table = new String[lines.length][lines[0].indexOf(" ") + 1];
				for (int i = 0; i < lines.length; i++) {
					String l = lines[i];
					if (l.length() < 1)
						continue;
					String[] parts = l.split(" ");
					for (int p = 0; p < parts.length; p++)
						table[i][p] = parts[p];
				}
				tables.put(s.toLowerCase().hashCode(), table);
				list += s + ", ";
			} catch (IOException e) {
				System.out.println("Invalid file " + folder + s);
				e.printStackTrace();
			}
		}
		list = list.substring(0, list.lastIndexOf(","));
		list += ".";
		System.out.println("Successfully loaded " + tables.size() + " name generators: " + list);
	}

	public static String getNameFrom(String file) {
		int location = file.toLowerCase().hashCode();
		String[][] table = tables.get(location);
		if (table == null)
			return "Table not found";
		String result = "";
		String get = null;
		for (int i = 0; i < 3; i++) {
			while (get == null)
				get = table[Render.floor(Render.rand.nextDouble() * table.length)][i];
			result += get;
			get = null;
		}
		return result;
	}
}
