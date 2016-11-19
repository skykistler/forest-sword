package com.teamlucidic.hax.entity.particle.projectile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.Model;
import com.teamlucidic.hax.render.Texture;

public class ProjectileLoader {
	public static String path = Main.m.resourcePack + "particles/projectiles/";
	public HashMap<Integer, ProjectileType> projectileTypes = new HashMap<Integer, ProjectileType>();
	public ArrayList<String> prjctLoaded = new ArrayList<String>();

	public ProjectileType blockBreakEffect;

	public ProjectileLoader() {
		loadProjectiles();
	}

	public void loadProjectiles() {
		File folder = new File(path);
		String[] files = folder.list();
		for (int i = 0; i < files.length; i++) {
			try {
				if (!files[i].endsWith(".prj"))
					continue;
				String name = files[i].replace(".prj", "");

				String file = Main.loadFile("particles/projectiles/" + files[i]);
				String[] lines = file.split("\n|\\r");

				Texture tex = null;
				Model model = null;
				double speed = 0;
				double height = 1;
				int life = 0;
				double damage = 0;
				double knockback = 0;
				boolean causesGore = false;
				int blockCollisionType = 0;
				int entityCollisionType = 0;
				boolean gravity = false;
				boolean quiver = true;

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

					if (line.startsWith("texture"))
						tex = Texture.getTexture(param);

					if (line.startsWith("model"))
						model = Main.m.modelLoader.getNewModelCopy(param);

					if (line.startsWith("speed"))
						speed = Double.parseDouble(param);

					if (line.startsWith("height"))
						height = Double.parseDouble(param);

					if (line.startsWith("life")) {
						life = Integer.parseInt(param);
						if (life < 1)
							throw new Exception("Invalid lifetime setting: " + life);
					}

					if (line.startsWith("damage"))
						damage = Double.parseDouble(param);

					if (line.startsWith("knockback"))
						knockback = Double.parseDouble(param);

					if (line.startsWith("causesGore"))
						causesGore = param.equals("false") ? false : true;

					if (line.startsWith("collision") || line.startsWith("blockCol")) {
						int i2 = param.equalsIgnoreCase("stick") ? 1 : param.equalsIgnoreCase("destroy") ? 2 : param.equalsIgnoreCase("explode") ? 3 : 0;
						blockCollisionType = i2;
					}

					if (line.startsWith("entityCollision")) {
						int i2 = param.equalsIgnoreCase("stick") ? 1 : param.equalsIgnoreCase("destroy") ? 2 : param.equalsIgnoreCase("explode") ? 3 : 0;
						entityCollisionType = i2;
					}

					if (line.startsWith("gravity"))
						gravity = param.equals("false") ? false : true;

					if (line.startsWith("quiver"))
						quiver = param.equals("false") ? false : true;

				}
				if (model != null && tex != null)
					model.setTexture(tex.path);
				ProjectileType result = new ProjectileType(tex, model, speed, height, life, damage, knockback, causesGore, blockCollisionType, entityCollisionType, gravity, quiver);
				projectileTypes.put(name.hashCode(), result);
				prjctLoaded.add(name);
			} catch (Exception e) {
				e.printStackTrace();
				Main.m.error("Couldn't load projectile " + files[i]);
			}
		}

		String loaded = "Successfully loaded " + prjctLoaded.size() + " projectiles: ";
		for (int i = 0; i < prjctLoaded.size(); i++) {
			loaded += prjctLoaded.get(i) + ", ";
		}
		loaded = loaded.substring(0, loaded.length() - 2);
		loaded += ".";
		System.out.println(loaded);
	}

	public ProjectileType getProjectileType(String name) {
		return projectileTypes.get(name.hashCode());
	}
}
