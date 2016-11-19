package com.teamlucidic.hax.render.gui.menu.components;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.menu.Menu;
import com.teamlucidic.hax.world.Map;

public class ListComponent extends Component {
	public ArrayList<ListItem> listItems = new ArrayList<ListItem>();

	public ListComponent(Menu m, String listFile, int x, int y, int w, int h) {
		super(m, x, y, w, h);
		try {
			String file = Main.loadFile(listFile);
			String[] lines = file.split("\n");
			ListItem current = null;
			for (String l : lines) {
				String param = "";
				if (l.contains("-"))
					param = l.split("-")[1];
				if (l.startsWith("name-")) {
					if (current != null)
						listItems.add(current);
					current = new ListItem(param);
				}

				if (l.startsWith("desc-"))
					if (current != null)
						current.description = param;

				if (l.startsWith("icon-"))
					if (current != null)
						current.icon = Texture.getTexture(param);
			}
			if (current != null)
				listItems.add(current);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ListComponent(Menu m, String listFile, int y, int h) {
		this(m, listFile, 100, 100, Display.getWidth() - 100, 400);
	}

	public void drawComponent() {
		drawWindowRect(posX, posY, width, height, borderSize);
		for (int i = 0; i < listItems.size(); i++)
			listItems.get(i).render(posX, posY + i * 20);
	}

	public class ListItem {
		public String title;
		public String description;
		public Texture icon;

		public ListItem(String title, String desc, String icon) {
			this.title = title;
			description = desc;
			if (icon.length() > 0)
				this.icon = Texture.getTexture(icon);
		}

		public ListItem(String title) {
			this(title, "", "");
		}

		public void render(int posX, int posY) {
			drawString(title, posX, posY, fontSize, 0xFFFFFFFF);
		}
	}

	public static void generateMapListFile() {
		try {
			File folder = new File(Map.path);
			String[] files = folder.list();
			StringBuilder sb = new StringBuilder();
			for (String f : files) {
				String nf = f.replace(".map", "");
				sb.append("name-" + nf + "\n");
				sb.append("desc-" + new File(Map.path + f).lastModified() + "\n");
				sb.append("icon-" + "maps/" + nf + ".png" + "\n\n");
			}
			File sf = new File(Map.path + "maps.list");
			sf.delete();
			sf.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(sf));
			writer.write(sb.toString());
			writer.close();
			sb = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
