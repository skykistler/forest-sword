package com.teamlucidic.hax.entity.inventory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.particle.ParticleSystemType;
import com.teamlucidic.hax.entity.particle.projectile.ProjectileType;
import com.teamlucidic.hax.render.Texture;

public class InventoryItemLoader {
	public static String path = Main.m.resourcePack + "items/";
	public HashMap<Integer, InventoryItemType> itemTypes = new HashMap<Integer, InventoryItemType>();
	public ArrayList<String> itemsLoaded = new ArrayList<String>();

	public InventoryItemLoader() {
		loadInventoryItems();
	}

	public void loadInventoryItems() {
		File folder = new File(path);
		String[] files = folder.list();
		for (int i = 0; i < files.length; i++) {
			try {
				if (!files[i].endsWith(".itm"))
					continue;
				String name = files[i].replace(".itm", "");

				String file = Main.loadFile("items/" + files[i]);
				String[] lines = file.split("\n|\\r");

				String type = "collectible";
				Texture tex = Texture.getTexture("gui/unknown.png");
				Texture icon = null;
				int vitriValue = 0;
				boolean requiresMouseButton = false;
				boolean requiresDraw = true;
				boolean drawHand = false;
				Texture handOnUse = null;
				Texture texOnUse = null;
				String target = "enemy";
				double energyEffect = 0;
				double targetHealthEffect = 0;
				double selfHealthEffect = 0;
				int lightBehavior = 0;
				int uses = 0;
				int swingDelay = 15;
				ProjectileType projectile = null;
				ParticleSystemType effect = null;
				boolean usesExtraCode = false;

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

					if (line.startsWith("type")) {
						type = param;
						if (type.equals("spell"))
							swingDelay = 0;
					}

					if (line.startsWith("text"))
						tex = Texture.getTexture(param);

					if (line.startsWith("icon"))
						icon = Texture.getTexture(param);

					if (line.startsWith("vitri"))
						vitriValue = Integer.parseInt(param);

					if (line.startsWith("requiresMouse"))
						requiresMouseButton = param.equals("false") ? false : true;

					if (line.startsWith("requiresDraw"))
						requiresDraw = param.equals("false") ? false : true;

					if (line.startsWith("drawHand"))
						drawHand = param.equals("false") ? false : true;

					if (line.startsWith("handOnUse"))
						handOnUse = Texture.getTexture(param);

					if (line.startsWith("texOnUse"))
						texOnUse = Texture.getTexture(param);

					if (line.startsWith("target"))
						target = param;

					if (line.startsWith("energy"))
						energyEffect = Double.parseDouble(param);

					if (line.startsWith("targetHealth"))
						targetHealthEffect = Double.parseDouble(param);

					if (line.startsWith("selfHealth"))
						selfHealthEffect = Double.parseDouble(param);

					if (line.startsWith("lightBehav"))
						lightBehavior = Integer.parseInt(param);

					if (line.startsWith("uses-"))
						uses = Integer.parseInt(param);

					if (line.startsWith("swing") || line.startsWith("delay"))
						swingDelay = Integer.parseInt(param);

					if (line.startsWith("projectile"))
						projectile = Main.m.projectileLoader.getProjectileType(param);

					if (line.startsWith("effect"))
						effect = Main.m.partSysLoader.getParticleSystemType(param);

					if (line.startsWith("usesExtraCode"))
						usesExtraCode = param.equals("false") ? false : true;
				}
				InventoryItemType result = new InventoryItemType(name, type, tex, icon, vitriValue, requiresMouseButton, requiresDraw, drawHand, handOnUse, texOnUse, target, energyEffect, targetHealthEffect, selfHealthEffect, lightBehavior, uses, swingDelay, projectile, effect, usesExtraCode);
				itemTypes.put(name.hashCode(), result);
				itemsLoaded.add(name);
			} catch (Exception e) {
				e.printStackTrace();
				Main.m.error("Couldn't load item type " + files[i]);
			}
		}

		String loaded = "Successfully loaded " + itemsLoaded.size() + " item types: ";
		for (int i = 0; i < itemsLoaded.size(); i++) {
			loaded += itemsLoaded.get(i) + ", ";
		}
		loaded = loaded.substring(0, loaded.length() - 2);
		loaded += ".";
		System.out.println(loaded);
	}

	public InventoryItemType getInvItemType(String name) {
		return itemTypes.get(name.hashCode());
	}
}
