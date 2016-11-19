package com.teamlucidic.hax.entity.particle;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.Texture;

public class ParticleSystemLoader {
	public static String path = Main.m.resourcePack + "particles/";
	public HashMap<Integer, ParticleSystemType> particleSystemTypes = new HashMap<Integer, ParticleSystemType>();
	public ArrayList<String> partSysLoaded = new ArrayList<String>();

	public ParticleSystemLoader() {
		loadParticleSystems();
	}

	public void loadParticleSystems() {
		File folder = new File(path);
		String[] files = folder.list();
		for (int i = 0; i < files.length; i++) {
			try {
				if (!files[i].endsWith(".prtsys"))
					continue;
				String name = files[i].replace(".prtsys", "");

				String file = Main.loadFile("particles/" + files[i]);
				String[] lines = file.split("\n|\\r");

				Texture tex = null;
				int color = 0xFFFFFFFF;
				int life = 0;
				int rate = 0;
				double speedForwardParticle = 0;
				double speedVertParticle = 0;
				double speedVertQuiver = 0;
				int lifeOfParticle = 0;
				int lifeQuiver = 0;
				double sizeOfParticle = 0;
				double particleSizeChange = 0;
				double rotX = 0;
				double rotY = 0;
				double rotQuiver = 0;
				double posQuiver = 0;
				boolean particleQuiver = false;
				boolean gravityOnParticle = true;
				double xSpawnOffset = 0;
				double ySpawnOffset = 0;
				double zSpawnOffset = 0;

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

					if (line.startsWith("color")) {
						String[] values = param.split(",");
						for (int v = 0; v < values.length; v++)
							values[v] = values[v].trim();
						color = Main.m.modeler.RGBAtoHex(Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), values.length > 3 ? Integer.parseInt(values[3]) : 255);
					}

					if (line.startsWith("life-"))
						life = Integer.parseInt(param);

					if (line.startsWith("rate"))
						rate = Integer.parseInt(param);

					if (line.startsWith("speedForward"))
						speedForwardParticle = Double.parseDouble(param);

					if (line.startsWith("speedVerticle"))
						speedVertParticle = Double.parseDouble(param);

					if (line.startsWith("verticalSpeedQuiver"))
						speedVertQuiver = Double.parseDouble(param);

					if (line.startsWith("lifeOfParticle"))
						lifeOfParticle = Integer.parseInt(param);

					if (line.startsWith("lifeQuiver"))
						lifeQuiver = Integer.parseInt(param);

					if (line.startsWith("sizeOfParticle"))
						sizeOfParticle = Double.parseDouble(param);

					if (line.startsWith("sizeChange"))
						particleSizeChange = Double.parseDouble(param);

					if (line.startsWith("rotationX"))
						rotX = Double.parseDouble(param);

					if (line.startsWith("rotationY"))
						rotY = Double.parseDouble(param);

					if (line.startsWith("rotQuiver"))
						rotQuiver = Double.parseDouble(param);

					if (line.startsWith("positionQuiver"))
						posQuiver = Double.parseDouble(param);

					if (line.startsWith("particleQuiver"))
						particleQuiver = param.equals("true") ? true : false;

					if (line.startsWith("gravityOnParticle"))
						gravityOnParticle = param.equals("true") ? true : false;

					if (line.startsWith("xSpawnOffset"))
						xSpawnOffset = Double.parseDouble(param);

					if (line.startsWith("ySpawnOffset"))
						ySpawnOffset = Double.parseDouble(param);

					if (line.startsWith("zSpawnOffset"))
						zSpawnOffset = Double.parseDouble(param);
				}
				ParticleSystemType result = new ParticleSystemType(tex, color, life, rate, speedForwardParticle, speedVertParticle, speedVertQuiver, lifeOfParticle, lifeQuiver, sizeOfParticle, particleSizeChange, rotX, rotY, rotQuiver, posQuiver, particleQuiver, gravityOnParticle, xSpawnOffset, ySpawnOffset, zSpawnOffset);
				particleSystemTypes.put(name.hashCode(), result);
				partSysLoaded.add(name);
			} catch (Exception e) {
				e.printStackTrace();
				Main.m.error("Couldn't load particle system " + files[i]);
			}
		}

		String loaded = "Successfully loaded " + partSysLoaded.size() + " particle systems: ";
		for (int i = 0; i < partSysLoaded.size(); i++) {
			loaded += partSysLoaded.get(i) + ", ";
		}
		loaded = loaded.substring(0, loaded.length() - 2);
		loaded += ".";
		System.out.println(loaded);
	}

	public ParticleSystemType getParticleSystemType(String name) {
		return particleSystemTypes.get(name.hashCode());
	}
}
