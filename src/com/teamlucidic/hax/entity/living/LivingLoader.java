package com.teamlucidic.hax.entity.living;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.Sound;
import com.teamlucidic.hax.entity.inventory.InventoryItemType;
import com.teamlucidic.hax.render.Texture;

public class LivingLoader {
	public static String path = Main.m.resourcePack + "living/";
	public HashMap<Integer, LivingType> livingTypes = new HashMap<Integer, LivingType>();
	public ArrayList<String> livLoaded = new ArrayList<String>();

	public LivingLoader() {
		loadLivingTypes();
	}

	public void loadLivingTypes() {
		File folder = new File(path);
		String[] files = folder.list();
		for (int i = 0; i < files.length; i++) {
			try {
				if (!files[i].endsWith(".en"))
					continue;
				String name = files[i].replace(".en", "");
				if (name.equals("friendly") || name.equals("neutral") || name.equals("enemy")) {
					System.out.println("Illegal entity name: " + name);
					continue;
				}

				String file = Main.loadFile("living/" + files[i]);
				String[] lines = file.split("\n|\\r");

				String desc = "No description available.";
				ArrayList<InventoryItemType> inventory = new ArrayList<InventoryItemType>();
				String attitude = "neutral";
				String model = null;
				Texture[] textures = null;
				Texture icon = null;
				Sound soundOnLook = null;
				int soundOnLookTimer = 0;
				Sound soundNearAttack = null;

				double[] dimensions = new double[3];
				double walkSpeed = 5;
				double sprintSpeed = 1.5;
				double jumpHeight = .37D;
				double swimSpeed = .7D;
				double crouchSpeed = .4;
				double crouchHeight = -1337;
				double healthRegen = 0;
				int maxEnergy = 0;
				double energyRegen = 0;
				double punchDamage = .5;
				double knockback = .5;
				int hitDelay = 40;

				int maxHealth = 10;

				String attackTargets = ":";
				String followTargets = ":";
				String parasites = ":";
				HashMap<Integer, Texture> parasitesTex = new HashMap<Integer, Texture>();
				boolean wanders = false;
				boolean flocks = false;
				boolean hops = false;
				double interestDistance = 15;

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

					if (line.startsWith("desc"))
						desc = param;

					if (line.startsWith("atti"))
						attitude = param;

					if (line.startsWith("model"))
						model = param;

					if (line.startsWith("tex")) {
						String[] parts = param.split(",");
						textures = new Texture[parts.length];
						for (int i2 = 0; i2 < parts.length; i2++)
							textures[i2] = Texture.getTexture(parts[i2].trim());
					}

					if (line.startsWith("icon"))
						icon = Texture.getTexture(param);

					if (line.startsWith("soundOnLook-"))
						soundOnLook = Sound.getSound(param);

					if (line.startsWith("soundOnLookTim"))
						soundOnLookTimer = Integer.parseInt(param);

					if (line.startsWith("soundNear"))
						soundNearAttack = Sound.getSound(param);

					if (line.startsWith("bounding") || line.startsWith("size")) {
						String[] parts = param.split(",");
						dimensions[0] = Double.parseDouble(parts[0].trim());
						dimensions[1] = Double.parseDouble(parts[1].trim());
						dimensions[2] = Double.parseDouble(parts[2].trim());
					}

					if (line.startsWith("speed") || line.startsWith("walk"))
						walkSpeed = Double.parseDouble(param);

					if (line.startsWith("sprint"))
						sprintSpeed = Double.parseDouble(param);

					if (line.startsWith("jump"))
						jumpHeight = Double.parseDouble(param);

					if (line.startsWith("swim"))
						swimSpeed = Double.parseDouble(param);

					if (line.toLowerCase().startsWith("crouchs") || line.toLowerCase().startsWith("sneaks"))
						crouchSpeed = Double.parseDouble(param);

					if (line.toLowerCase().startsWith("crouchh") || line.toLowerCase().startsWith("sneakh"))
						crouchHeight = Double.parseDouble(param);

					if (line.toLowerCase().startsWith("healthr"))
						healthRegen = Double.parseDouble(param);

					if (line.startsWith("energy-") || line.startsWith("maxEnergy"))
						maxEnergy = Integer.parseInt(param);

					if (line.toLowerCase().startsWith("energyr"))
						energyRegen = Double.parseDouble(param);

					if (line.startsWith("punch"))
						punchDamage = Double.parseDouble(param);

					if (line.startsWith("knockb"))
						knockback = Double.parseDouble(param);

					if (line.startsWith("hitDelay"))
						hitDelay = Integer.parseInt(param);

					if (line.startsWith("health-") || line.startsWith("maxHealth"))
						maxHealth = Integer.parseInt(param);

					if (line.startsWith("interest") || line.startsWith("targetDis"))
						interestDistance = Double.parseDouble(param);

					if (line.startsWith("parasit")) {
						String[] parts = line.substring(line.indexOf("-") + 1, line.length()).split(",");
						for (int i2 = 0; i2 < parts.length; i2++) {
							String part = parts[i2].toLowerCase();
							String[] parts2 = part.split(":");
							parasites += parts2[0].trim() + ":";
							if (parts2.length > 1)
								parasitesTex.put(parts2[0].trim().toLowerCase().hashCode(), Texture.getTexture(parts2[1].trim()));
						}
					}

					if (line.startsWith("behav")) {
						String[] params = param.split(",");
						for (int i2 = 0; i2 < params.length; i2++) {
							String partParam = params[i2].trim();

							if (partParam.startsWith("attack "))
								attackTargets += partParam.substring(7) + ":";

							if (partParam.startsWith("follow "))
								followTargets += partParam.substring(7) + ":";

							if (partParam.startsWith("wander"))
								wanders = true;

							if (partParam.startsWith("flock"))
								flocks = true;

							if (partParam.startsWith("hop"))
								hops = true;
						}
					}
				}
				if (attackTargets.length() == 1)
					attackTargets = "";
				if (followTargets.length() == 1)
					followTargets = "";
				if (crouchHeight == -1337)
					crouchHeight = dimensions[1] / 2;

				LivingType result = new LivingType(name, desc, inventory, attitude, model, textures, icon, soundOnLook, soundOnLookTimer, soundNearAttack, dimensions, walkSpeed, sprintSpeed, jumpHeight, swimSpeed, crouchSpeed, crouchHeight, healthRegen, maxEnergy, energyRegen, punchDamage, knockback, hitDelay, maxHealth, attackTargets, followTargets, parasites, parasitesTex, wanders, flocks, hops, interestDistance);
				livingTypes.put(name.hashCode(), result);
				livLoaded.add(name);
			} catch (Exception e) {
				e.printStackTrace();
				Main.m.error("Couldn't load living type " + files[i]);
			}
		}

		String loaded = "Successfully loaded " + livLoaded.size() + " living types: ";
		for (int i = 0; i < livLoaded.size(); i++) {
			loaded += livLoaded.get(i) + ", ";
		}
		loaded = loaded.substring(0, loaded.length() - 2);
		loaded += ".";
		System.out.println(loaded);
	}

	public LivingType getLivingType(String name) {
		return livingTypes.get(name.hashCode());
	}
}
