package com.teamlucidic.hax.render;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.teamlucidic.hax.Main;

public class ModelLoader {
	public static String path = Main.m.resourcePack + "models/";
	public HashMap<Integer, Model> modelMap = new HashMap<Integer, Model>();
	public ArrayList<String> modelsLoaded = new ArrayList<String>();

	public ModelLoader() {
		loadModels();
	}

	public void loadModels() {
		File folder = new File(path);
		String[] files = folder.list();
		for (int i = 0; i < files.length; i++) {
			try {
				if (!files[i].endsWith(".modl"))
					continue;
				String name = files[i].replace(".modl", "");

				String file = Main.loadFile("models/" + files[i]);
				String[] lines = file.split("\n");

				Model result = new Model();

				for (int l = 0; l < lines.length; l++) {
					String line = lines[l];
					if (line.length() < 1)
						continue;
					if (line.startsWith("//"))
						continue;
					if (!line.contains("-")) {
						System.out.println("Invalid line in " + name + " at " + (l + 1));
						continue;
					}

					String param = line.substring(line.indexOf("-") + 1);

					if (line.startsWith("texture"))
						result.setTexture(param);

					if (line.startsWith("scale"))
						result.scale = Integer.parseInt(param);

					if (line.startsWith("part")) {
						String partName = line.substring(line.indexOf(":") + 1, line.length() - 1);

						String parent = null;
						int[] textureCoord = new int[2];
						int[] size = new int[3];
						double[] offset = new double[3];
						double[] attachment = null;

						for (int l2 = 1; l2 < 10; l2++) {
							if (lines.length <= l + l2) {
								l += l2 - 1;
								break;
							}
							String line2 = lines[l + l2];
							if (line2.length() < 1)
								continue;
							if (line2.startsWith("//"))
								continue;
							if (!line2.contains("-")) {
								System.out.println("Invalid line in " + name + ": " + (l + l2 + 1));
								continue;
							}

							String param2 = line2.substring(line2.indexOf("-") + 1);

							if (line2.startsWith("texture")) {
								String[] parts = param2.split(",");
								textureCoord[0] = Integer.parseInt(parts[0].trim());
								textureCoord[1] = Integer.parseInt(parts[1].trim());
							} else if (line2.startsWith("parent")) {
								parent = param2;
							} else if (line2.startsWith("size")) {
								String[] parts = param2.split(",");
								size[0] = Integer.parseInt(parts[0].trim());
								size[1] = Integer.parseInt(parts[1].trim());
								size[2] = Integer.parseInt(parts[2].trim());
							} else if (line2.startsWith("translate")) {
								String[] parts = param2.split(",");
								for (int i2 = 0; i2 < 3; i2++) {
									offset[i2] = Double.parseDouble(parts[i2].trim());
									if (!parts[i2].contains("."))
										offset[i2] /= 32d;
								}
							} else if (line2.startsWith("attachment")) {
								if (param2.startsWith("m"))
									continue;
								String[] parts = param2.split(",");
								attachment = new double[3];
								for (int i2 = 0; i2 < 3; i2++) {
									attachment[i2] = Double.parseDouble(parts[i2].trim());
									if (!parts[i2].contains("."))
										attachment[i2] /= 32d;
								}
							} else {
								l += l2 - 1;
								break;
							}
						}

						result.addCuboid(partName, parent, textureCoord, size, offset, attachment);
					}

					//					boolean
					//					if (line.startsWith("usesExtraCode"))
					//						usesExtraCode = param.equals("false") ? false : true;
				}
				modelMap.put(name.hashCode(), result);
				modelsLoaded.add(name);
			} catch (Exception e) {
				e.printStackTrace();
				Main.m.error("Couldn't load model " + files[i]);
			}
		}

		String loaded = "Successfully loaded " + modelsLoaded.size() + " models: ";
		for (int i = 0; i < modelsLoaded.size(); i++) {
			loaded += modelsLoaded.get(i) + ", ";
		}
		loaded = loaded.substring(0, loaded.length() - 2);
		loaded += ".";
		System.out.println(loaded);
	}

	public Model getNewModelCopy(String name) {
		return modelMap.get(name.hashCode()).copy();
	}
}
