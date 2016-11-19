package com.teamlucidic.hax.render.gui.menu;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.entity.inventory.InventoryItemType;
import com.teamlucidic.hax.entity.inventory.item.Hammer;
import com.teamlucidic.hax.entity.inventory.item.Paintbrush;
import com.teamlucidic.hax.entity.living.Living;
import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.render.Shader;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.menu.components.Component;
import com.teamlucidic.hax.render.gui.menu.components.Minimap;
import com.teamlucidic.hax.render.gui.menu.components.TextArea;
import com.teamlucidic.hax.render.gui.menu.components.TextBox;
import com.teamlucidic.hax.render.gui.menu.components.Window;
import com.teamlucidic.hax.world.Chunk;
import com.teamlucidic.hax.world.Map;
import com.teamlucidic.hax.world.World;

public class Console extends Menu {
	public static ArrayList<String> commandHistory = new ArrayList<String>();
	public static TextArea output;
	public static TextBox input;
	public static boolean justClosed;
	public static int historyTracker = -1;
	public static String upKeyBind = "echo Up arrow pressed.";
	public static String downKeyBind = "echo Down arrow pressed.";
	public static String leftKeyBind = "echo Left arrow pressed.";
	public static String rightKeyBind = "echo Right arrow pressed.";

	public Window window;
	public boolean justOpened = true;

	public Console() {
		super("Console");
	}

	public void init() {
		components = new ArrayList<Component>();

		historyTracker = -1;
		window = new Window(this, 700, 500);
		window.posY -= 35;

		if (output == null)
			output = new TextArea(this, 0, 0, 690, 448);
		else {
			output.parentMenu = this;
			add(output);
		}
		output.posX = Display.getWidth() / 2 - output.width / 2;
		output.posY = Display.getHeight() / 2 - output.width / 3 - 50;
		output.fontSize = 1;

		if (input == null)
			input = new TextBox(this, "", 0, 0, 690, 40);
		else {
			input.parentMenu = this;
			add(input);
		}
		input.posX = Display.getWidth() / 2 - input.width / 2;
		input.posY = Display.getHeight() / 2 + 170;
		input.limit = 255;
		input.scroll = true;

		focus = input;
	}

	public void update() {
		if (Main.m.input.checkKey(Keyboard.KEY_RETURN)) {
			runCommand(input.label);
			output("");
			input.setLabel("");
		}

		if (Main.m.input.checkKey(Keyboard.KEY_UP) && commandHistory.size() > 1 + historyTracker) {
			historyTracker++;
			input.setLabel(commandHistory.get(commandHistory.size() - 1 - historyTracker));
		}

		if (Main.m.input.checkKey(Keyboard.KEY_DOWN) && commandHistory.size() - (historyTracker - 1) > 0 && historyTracker > 0) {
			historyTracker--;
			input.setLabel(commandHistory.get(commandHistory.size() - 1 - historyTracker));
		}

		if (Main.m.input.isDown(Keyboard.KEY_GRAVE)) {
			if (!justOpened) {
				Menu.setMenu(null);
				justClosed = true;
			}
		} else
			justOpened = false;
	}

	public void drawMenu() {
		drawRect(0, 0, Display.getWidth(), Display.getHeight(), 0xAF111111);
		drawLabel2();
	}

	public static void runCommand(String s) {
		output.addContent("> " + s);
		historyTracker = -1;
		s = s.trim();
		if (s.length() < 1)
			return;
		try {
			commandHistory.add(s);

			if (s.startsWith("fly")) {
				Main.m.player.flying = !Main.m.player.flying;
				output("Player is " + (Main.m.player.flying ? "flying." : "not flying."));
				return;
			}

			if (s.startsWith("echo ")) {
				output(s.substring(s.indexOf(" "), s.length()));
				return;
			}

			if (s.startsWith("up-"))
				Component.scale += 1;

			if (s.startsWith("do-"))
				Component.scale -= 1;

			if (s.equals("togglehud")) {
				Menu.currentHud.show = !Menu.currentHud.show;
				if (Menu.currentHud.show)
					output("HUD turned on");
				else
					output("HUD turned off");
				return;
			}

			if (s.equals("toggledebug")) {
				Hud.debugInfo = !Hud.debugInfo;
				if (Hud.debugInfo)
					output("Debug info turned on");
				else
					output("Debug info turned off");
				return;
			}

			if (s.equals("quit"))
				Main.m.shutDown();

			if (s.equals("cls") || s.equals("clear")) {
				output.clear();
				return;
			}

			if (s.equals("killall")) {
				output("Killing all living entities...", true);
				int wow = Living.livingList.size();
				for (int i = 0; i < wow; i++) {
					Living l = Living.livingList.get(i);
					if (l != Main.m.player && l != Main.m.camera)
						l.applyDamage(l.health, false);
				}
				output("Slayed " + (wow - 2) + " living entities. What a jerk move.");
				output("Done.");
				return;
			}

			if (s.equals("gc")) {
				output("Asking JVM to garbage collect...");
				Main.m.gc();
				output("Done.");
				return;
			}

			if (s.startsWith("bind.")) {
				String param = s.substring(s.indexOf(".") + 1);
				if (param.split(" ").length < 2) {
					if (param.startsWith("u"))
						output("Up key set to: " + upKeyBind);
					else if (param.startsWith("d"))
						output("Down key set to: " + downKeyBind);
					else if (param.startsWith("l"))
						output("Left key set to: " + leftKeyBind);
					else if (param.startsWith("r"))
						output("Right key set to: " + rightKeyBind);
					else
						output("Invalid syntax.");

					return;
				}
				String key = param.split(" ")[0];
				if (key.startsWith("clear")) {
					key = param.split(" ")[1];
					if (key.startsWith("u")) {
						upKeyBind = "";
						output("Up key cleared.");
					}
					if (key.startsWith("l")) {
						leftKeyBind = "";
						output("Left key cleared.");
					}
					if (key.startsWith("d")) {
						downKeyBind = "";
						output("Down key cleared.");
					}
					if (key.startsWith("r")) {
						rightKeyBind = "";
						output("Right key cleared.");
					}
					return;
				}
				if (key.startsWith("u")) {
					upKeyBind = s.substring(s.indexOf(" ") + 1);
					output("Up key set to: " + upKeyBind);
				}
				if (key.startsWith("l")) {
					leftKeyBind = s.substring(s.indexOf(" ") + 1);
					output("Left key set to: " + leftKeyBind);
				}
				if (key.startsWith("d")) {
					downKeyBind = s.substring(s.indexOf(" ") + 1);
					output("Down key set to: " + downKeyBind);
				}
				if (key.startsWith("r")) {
					rightKeyBind = s.substring(s.indexOf(" ") + 1);
					output("Right key set to: " + rightKeyBind);
				}
				return;
			}

			if (s.startsWith("player.")) {
				if (s.equals("player.heal")) {
					Main.m.player.health = Main.m.player.maxHealth;
					output("Healed player completely.");
					return;
				}
				if (s.equals("player.energize")) {
					Main.m.player.energy = Main.m.player.inventory.abilities.maxEnergy;
					output("Energized player completely.");
					return;
				}

				String[] parts = s.split("\\.");
				if (parts.length != 3) {
					output("Invalid syntax: player.<command>.<param>");
					return;
				}
				if (parts[1].equals("heal")) {
					Main.m.player.health += Integer.parseInt(parts[2]);
					output("Healed player by " + parts[2] + " points.");
					return;
				}
				if (parts[1].equals("energize")) {
					Main.m.player.energy += Integer.parseInt(parts[2]);
					output("Energized player by " + parts[2] + " points.");
					return;
				}

				if (parts[1].equals("give")) {
					if (Main.m.invItemLoader.itemTypes.containsKey(parts[2].hashCode())) {
						InventoryItemType type = Main.m.invItemLoader.getInvItemType(parts[2]);
						output("Giving player " + parts[2] + ".");
						if (type.usesExtraCode)
							output("This item requires extra code and may not have spawned correctly");

						Main.m.player.inventory.addItem(type);
					} else
						output("Unrecognized item: " + parts[2]);
					return;
				}
				output("Unknown command.");
				return;
			}

			if (s.startsWith("world.")) {
				if (s.startsWith("world.weather")) {
					if (s.equals("world.weather")) {
						output("Current world weather is " + Main.m.world.weather);
						return;
					}
					if (s.startsWith("world.weather.set ")) {
						String param = s.split(" ")[1];
						Main.m.world.setWeather(param);
						output("World weather set to " + Main.m.world.weather);
						return;
					}
				}
				if (s.startsWith("world.time")) {
					if (s.equals("world.time")) {
						output("Current world time is " + Main.m.world.worldTicks % (Main.m.world.lengthOfDay * 60));
						output("Days are " + (Main.m.world.lengthOfDay * 60) + " ticks long.");
						output("Time is currently " + (Main.m.world.changeTime ? "moving." : "paused"));
						return;
					}
					if (s.startsWith("world.time.set ")) {
						String param = s.split(" ")[1];
						if (param.equals("day"))
							Main.m.world.worldTicks = 3;
						else if (param.equals("night"))
							Main.m.world.worldTicks = Main.m.world.lengthOfDay * 30;
						else
							Main.m.world.worldTicks = (int) Math.ceil((double) Main.m.world.worldTicks / (Main.m.world.lengthOfDay * 60)) + Integer.parseInt(param) - 1;

						output("World time set to " + Main.m.world.worldTicks % (Main.m.world.lengthOfDay * 60));
						return;
					}
					if (s.startsWith("world.time.pause")) {
						Main.m.world.changeTime = !Main.m.world.changeTime;
						output("World time is now " + (Main.m.world.changeTime ? "moving." : "paused."));
						return;
					}

					if (s.startsWith("world.time.daylength ")) {
						String param = s.split(" ")[1];
						if (param.equals("default"))
							Main.m.world.lengthOfDay = 16 * 60;
						else
							Main.m.world.lengthOfDay = Integer.parseInt(param);
						output("Length of day set to " + Main.m.world.lengthOfDay + " seconds.");
						return;
					}
				}
			}

			if (s.startsWith("living.")) {
				if (s.startsWith("living.spawn.")) {
					Block b = Main.m.player.rayTraceToBlock(Main.m.player.currentHeight, .2, World.renderDistance - 5);
					String name = s.substring(13);
					if (b == null) {
						output("Unable to find position to spawn at, try looking at a closer block and try again.");
						return;
					} else {
						output("Spawning 1 " + name + " (" + b.posX + ", " + (b.posY + 1) + ", " + b.posZ + ").");
						Main.m.livingLoader.getLivingType(name).spawnNewAt(b.posX, b.posY + 1, b.posZ);
						output("Done.");
					}
					return;
				}
			}

			if (s.startsWith("map.")) {
				if (s.endsWith("updatenearby")) {
					output("Updating nearby chunks...", true);
					for (Chunk c : Main.m.world.chunkMap.values())
						if (c.withinRenderDis) {
							c.updateAllBlocks();
						}
					output("Done.");
					return;
				}
				if (s.endsWith("save")) {
					output("Saving map...", true);
					Main.m.world.saveWorld();
					output("Done.");
					return;
				}
				if (s.startsWith("map.load ")) {
					String name = s.substring(s.indexOf(" "), s.length());
					name = name.substring(1, name.length());
					Map map = new Map(name);
					if (map.exists()) {
						output("Loading world: " + name + "...", true);
						Main.m.stopGame();
						Main.m.wName = name;
						Main.m.startGame("freeplay:load");
						return;
					} else {
						output("Map " + name + " not found.");
						return;
					}
				}
			}

			if (s.startsWith("minimap.")) {
				if (s.endsWith("update")) {
					output("Updating entire minimap...", true);
					Minimap.forceUpdateEntireMap();
					output("Done.");
					Main.m.resetTimer();
					return;
				}
				if (s.endsWith("save")) {
					output("Saving minimap...", true);
					Minimap.saveMap();
					output("Done.");
					return;
				}
				if (s.endsWith("create")) {
					output("Creating new minimap texture...", true);
					Minimap.createMap();
					output("Done.");
					Main.m.resetTimer();
					return;
				}
			}

			if (s.startsWith("shader.")) {
				String[] parts = s.split("\\.");
				if (parts.length < 3) {
					output("Invalid syntax: shader.<target>.<command>");
					return;
				}
				Shader target = Shader.shaderMap.get(parts[1].hashCode());
				if (target == null) {
					output("Unknown shader: " + parts[1]);
					return;
				}

				if (parts[2].equals("load")) {
					output("Loading shader " + target.name, true);
					target.load();
					if (target.successful)
						output("Shader " + parts[1] + " compiled successfully.");
					else
						output("Shader " + parts[1] + " was not compiled successfully!");
				} else if (parts[2].startsWith("enable")) {
					target.enabled = true;
					output("Shader " + target.name + " is enabled.");
					return;
				} else if (parts[2].startsWith("disable")) {
					target.enabled = false;
					output("Shader " + target.name + " is disabled.");
					return;
				} else {
					output("Unrecognized command.");
					return;
				}
				return;
			}
			if (s.startsWith("texture.")) {
				if (s.startsWith("texture.load ")) {
					String name = s.substring(s.indexOf(" "), s.length());
					name = name.substring(1, name.length());
					output("Loading texture: " + name + "...", true);
					new Texture(name);
					output("Done.");
					return;
				}
			}

			if (s.startsWith("hammer.")) {
				if (s.startsWith("hammer.status")) {
					if (Hammer.p1 != null && Hammer.p2 != null) {
						double total = (Hammer.selection.maxX - Hammer.selection.minX + 1) * (Hammer.selection.maxY - Hammer.selection.minY + 1) * (Hammer.selection.maxZ - Hammer.selection.minZ + 1);
						output("Hammer selection encompasses " + Render.floor(total) + " blocks.");
					} else
						output("No active selection.");
					return;
				}

				if (s.startsWith("hammer.clear")) {
					Hammer.p1 = null;
					Hammer.p2 = null;
					output("Hammer selection cleared.");
					return;
				}

				if (s.equals("hammer.undo")) {
					output(Hammer.undo());
					return;
				}
				if (s.equals("hammer.undoall")) {
					output("Undoing all actions...", true);
					while (Hammer.actionHistory.size() > 0)
						Hammer.undo();
					output("Done.");
					return;
				}

				if (s.startsWith("hammer.set")) {
					String[] parts = s.split(" ");
					String mode = "set";
					String place = "Air";
					String replace = "any";
					String[] parts2 = parts[0].split("hammer.set");
					if (parts2.length > 1)
						mode = parts2[1];
					double percent = 1;
					if (parts.length == 2)
						place = parts[1];
					if (parts.length == 3) {
						replace = parts[1];
						place = parts[2];
					}
					if (parts.length == 4) {
						replace = parts[1];
						place = parts[2];
						percent = Double.parseDouble(parts[3]);
						percent /= percent > 1 ? 100 : 1;
					}
					if (!place.toLowerCase().equals("air") && !Main.m.blockLoader.blockTypes.containsKey(place.hashCode())) {
						output("Place block not recognized: " + place);
						output("(case is important, try capitalizing the first letter)");
						return;
					} else if (!replace.toLowerCase().startsWith("any") && !replace.toLowerCase().equals("air") && !Main.m.blockLoader.blockTypes.containsKey(replace.hashCode())) {
						output("Replace block not recognized: " + replace);
						output("(case is important, try capitalizing the first letter)");
						return;
					} else {
						place = place.toLowerCase().equals("air") ? "Air" : place;
						replace = replace.toLowerCase().equals("air") ? "Air" : replace.toLowerCase().equals("anybutair") ? "anyButAir" : replace.toLowerCase().equals("any") || replace.toLowerCase().startsWith("anyt") ? "" : replace;
						output("Hammer is setting " + (replace.length() > 0 ? replace : "anything") + " to " + place + " with a percent fill of " + percent * 100 + "% with the " + mode + " mode.", true);
						output("Done. " + Hammer.set(mode, place, replace, percent) + " blocks set.");
						return;
					}

				}

				if (s.startsWith("hammer.shift")) {
					String[] parts = s.split(" ");
					if (parts.length > 2) {
						String direction = parts[1];
						int amount = Integer.parseInt(parts[2]);
						if (direction.startsWith("u")) {
							Hammer.selection.minY += amount;
							Hammer.selection.maxY += amount;
						} else if (direction.startsWith("d")) {
							Hammer.selection.minY -= amount;
							Hammer.selection.maxY -= amount;
						} else if (direction.startsWith("f")) {
							Hammer.selection.minZ += amount;
							Hammer.selection.maxZ += amount;
						} else if (direction.startsWith("b")) {
							Hammer.selection.minZ -= amount;
							Hammer.selection.maxZ -= amount;
						} else if (direction.startsWith("l")) {
							Hammer.selection.minX += amount;
							Hammer.selection.maxX += amount;
						} else if (direction.startsWith("r")) {
							Hammer.selection.minX -= amount;
							Hammer.selection.maxX -= amount;
						} else {
							output("Direction " + direction + " is unrecognized.");
							return;
						}
						output("Hammer selection shifted " + amount + " block" + (amount == 1 ? "s" : "") + " to the " + direction + ".");
						runCommand("hammer.status");
						return;
					}
				}
				if (s.startsWith("hammer.expand")) {
					String[] parts = s.split(" ");
					if (parts.length > 2) {
						String direction = parts[1];
						int amount = Integer.parseInt(parts[2]);
						int rot = Render.floor(((315 + Main.m.player.rotY) % 360) / 90);
						if (direction.startsWith("u")) {
							Hammer.selection.maxY += amount;
						} else if (direction.startsWith("d")) {
							Hammer.selection.minY -= amount;
						} else if (direction.startsWith("f")) {
							Hammer.expandXZ(rot, amount);
						} else if (direction.startsWith("b")) {
							Hammer.expandXZ(rot + 2, amount);
						} else if (direction.startsWith("l")) {
							Hammer.expandXZ(rot + 3, amount);
						} else if (direction.startsWith("r")) {
							Hammer.expandXZ(rot + 1, amount);
						} else if (direction.startsWith("a")) {
							Hammer.selection.maxX += amount;
							Hammer.selection.minX -= amount;
							Hammer.selection.maxY += amount;
							Hammer.selection.minY -= amount;
							Hammer.selection.maxZ += amount;
							Hammer.selection.minZ -= amount;
						} else {
							output("Direction " + direction + " is unrecognized.");
							return;
						}
						output("Hammer selection expanded " + amount + " block" + (amount == 1 ? "s" : "") + " to the " + direction + ".");
						runCommand("hammer.status");
						return;
					}
				}
			}

			if (s.startsWith("brush.")) {
				if (s.startsWith("brush.status")) {
					output("Paintbrush mode is " + Paintbrush.mode);
					output("Paintbrush place type set to " + Paintbrush.placeType + ".");
					output("Paintbrush replace type set to " + (Paintbrush.replaceType.length() > 0 ? Paintbrush.replaceType : "anything") + ".");
					output("Paintbrush shape set to " + (Paintbrush.shape == 0 ? "cube" : "sphere"));
					output("Paintbrush radius set to " + Paintbrush.radius + " blocks.");
					output("Paintbrush percent fill set to " + (Paintbrush.percentFill * 100) + "%");
					output("Paintbrush swing delay set to " + Paintbrush.delay + " ticks.");
					return;
				}
				if (s.startsWith("brush.clear")) {
					Paintbrush.mode = "paint";
					Paintbrush.placeType = "Air";
					Paintbrush.replaceType = "";
					Paintbrush.shape = 1;
					Paintbrush.radius = 1;
					Paintbrush.percentFill = 1;
					Paintbrush.delay = 20;
					runCommand("brush.status");
					return;
				}
				if (s.equals("brush.undo")) {
					output(Paintbrush.undo());
					return;
				}
				if (s.equals("brush.undoall")) {
					output("Undoing all strokes...", true);
					while (Paintbrush.strokeHistory.size() > 0)
						Paintbrush.undo();
					output("Done.");
					return;
				}
				if (s.startsWith("brush.shape ")) {
					String[] parts = s.split(" ");
					if (parts[1].equalsIgnoreCase("cube") || "0c".contains(parts[1])) {
						Paintbrush.shape = 0;
						output("Paintbrush shape set to cube");
					} else if (parts[1].equalsIgnoreCase("sphere") || "1s".contains(parts[1])) {
						Paintbrush.shape = 1;
						output("Paintbrush shape set to sphere");
					} else if (parts[1].equalsIgnoreCase("disc") || "2d".contains(parts[1])) {
						Paintbrush.shape = 2;
						output("Paintbrush shape set to disc");
					} else if (parts[1].equalsIgnoreCase("plane") || "3p".contains(parts[1])) {
						Paintbrush.shape = 3;
						output("Paintbrush shape set to plane");
					} else
						output("Unknown paintbrush shape");
					return;
				}
				if (s.startsWith("brush.rad"))
					if (s.indexOf(" ") > 0) {
						Paintbrush.radius = Integer.parseInt(s.split(" ")[1]);
						output("Paintbrush radius set to " + Paintbrush.radius + " blocks.");
						return;
					}

				if (s.startsWith("brush.perc"))
					if (s.indexOf(" ") > 0) {
						Paintbrush.percentFill = Double.parseDouble(s.split(" ")[1]);
						Paintbrush.percentFill /= Paintbrush.percentFill > 1 ? 100 : 1;
						output("Paintbrush percent fill set to " + (Paintbrush.percentFill * 100) + "%");
						return;
					}

				if (s.startsWith("brush.swin") || s.startsWith("brush.dela"))
					if (s.indexOf(" ") > 0) {
						Paintbrush.delay = Integer.parseInt(s.split(" ")[1]);
						output("Paintbrush swing delay set to " + Paintbrush.delay + " ticks.");
						return;
					}

				if (s.startsWith("brush.t") || s.startsWith("brush.pl")) {
					if (s.indexOf(" ") > 0) {
						String[] parts = s.split(" ");
						if (!parts[1].toLowerCase().equals("air") && !Main.m.blockLoader.blockTypes.containsKey(parts[1].hashCode())) {
							output("Block not recognized: " + parts[1]);
							output("(case is important, try capitalizing the first letter)");
							return;
						} else {
							Paintbrush.placeType = parts[1].toLowerCase().equals("air") ? "Air" : parts[1];
							output("Paintbrush place type set to " + Paintbrush.placeType + ".");
							return;
						}
					}
				}

				if (s.startsWith("brush.rep") || s.startsWith("brush.rp")) {
					if (s.indexOf(" ") > 0) {
						String[] parts = s.split(" ");
						if (!parts[1].toLowerCase().startsWith("any") && !parts[1].toLowerCase().equals("air") && !Main.m.blockLoader.blockTypes.containsKey(parts[1].hashCode())) {
							output("Block not recognized: " + parts[1]);
							output("(case is important, try capitalizing the first letter)");
							return;
						} else {
							Paintbrush.replaceType = parts[1].toLowerCase().equals("air") ? "Air" : parts[1].toLowerCase().equals("anybutair") ? "anyButAir" : parts[1].toLowerCase().equals("any") || parts[1].toLowerCase().startsWith("anyt") ? "" : parts[1];
							output("Paintbrush replace type set to " + (Paintbrush.replaceType.length() > 0 ? Paintbrush.replaceType : "anything") + ".");
							return;
						}
					}
				}

				if (s.startsWith("brush.mode")) {
					if (s.indexOf(" ") > 0) {
						String[] parts = s.split(" ");
						Paintbrush.mode = parts[1].toLowerCase();
					} else
						Paintbrush.mode = "paint";
					output("Paintbrush mode set to " + Paintbrush.mode);
					return;
				}
			}

			if (s.equals("help")) {
				output("Type help <number> for more info on: ");
				output("1: general");
				output("2: player");
				output("3: world");
				output("4: living");
				output("5: map");
				output("6: minimap");
				output("7: shader");
				output("8: texture");
				output("9: hammer");
				output("10: brush");
				output("11: brush modes");
				return;
			} else if (s.startsWith("help ")) {
				String pages = "1 2 3 4 5 6 7 8 9 10 11";
				String page = s.split(" ")[1];
				if (pages.contains(page)) {
					int pageN = Integer.parseInt(page);
					if (pageN == 1) {
						output("General commands");
						output("-cls/clear - Clears console");
						output("-echo <anything> - Display a message on screen");
						output("-killall - Kills all living entities");
						output("-togglehud - toggles the HUD on/off");
						output("-toggledebug - toggles the debug overlay");
						output("-gc - Tell the JVM to garbage collect");
						output("-bind.<arrrow key> <command> - binds a command to an arrow key");
						output("-bind.<arrrow key> - prints current command for arrow key");
						output("-bind.clear <arrow key> - clear the binding of an arrow key");
						output("-quit - Quit the game");
					}
					if (pageN == 2) {
						output("Player commands, put player. in front of these");
						output("-heal.<amount> - Heals by specific amount");
						output("-heal - Fully heals player");
						output("-energize.<amount> - Energizes by specific amount");
						output("-energize - Fully energizes player");
						output("-give.<item> - Gives player the specified item");
					}

					if (pageN == 3) {
						output("World commands, put world. in front of these");
						output("-time - outputs time details");
						output("-time.set <amount> - set the time of day, <amount> can be 'night', 'day', or an integer");
						output("-time.pause - toggle time movement");
						output("-time.daylength <amount> - sets length of day in seconds, takes an integer or 'default'");
						output("-weather - outputs current weather");
						output("-weather.set <weather>- set current weather (rain/default)");
					}

					if (pageN == 4) {
						output("NPC commands, put living. in front of these");
						output("-spawn.<name> - spawns the specified entity where the player is looking");
					}

					if (pageN == 5) {
						output("Map commands, put map. in front of these");
						output("-save - Saves the current map");
						output("-load <name> - Loads the specified map");
					}
					if (pageN == 6) {
						output("Minimap commands, put minimap. in front of these");
						output("-create - Recreates the minimap texture and saves it");
						output("-update - Updates the minimap texture");
						output("-save - Saves minimap");
					}

					if (pageN == 7) {
						output("Shader commands. Usage: shader.<target>.<command>");
						output("-load - loads the target shader");
						output("-enable - enable the shader");
						output("-disable - disable the shader");
					}
					if (pageN == 8) {
						output("Texture commands, put texture. in front of these");
						output("-load.<path> - reload the specified texture");
					}

					if (pageN == 9) {
						output("Hammer commands, put hammer. in front of these");
						output("-status - outputs the selection");
						output("-clear - clears the selection");
						output("-undo - un-does the last set action");
						output("-undoall - un-does all set actions");
						output("-set[mode] [replace] <place> [percent] - set the selection to something, if two params are provided then the first is the block that's replaced, and the second is the block that's placed. To set a percent you must provide a replace block and a place block, then the percent");
						output("The mode parameter is used like so: hammer.setcyl Grass. Only mode currently is cyl for cylinder, more to come!");
						output("-up/down/left/right/forward/back/all can be abbreviated with u/d/l/r/f/b/a");
						output("-directions are relative to camera orientation");
						output("-shift <u/d/l/r/f/b> <amount> - shift the selection in the set direction by the set amount of blocks, negative moves the opposite direction");
						output("-expand <a/u/d/l/r/f/b> <amount> - expand in the set direction by the set amount, negative amounts shrink the selection");
					}

					if (pageN == 10) {
						output("Paintbrush commands, put brush. in front of these");
						output("-status - outputs full status of paintbrush");
						output("-clear - sets paintbrush values to default");
						output("-undo - un-does the last stroke");
						output("-undoall - un-does all strokes");
						output("-mode <mode> - set the brush mode (see help 11)");
						output("-place <block type> - set the placement block type");
						output("-replace <block type/anyButAir/any> - set the block type to replace, set to anyButAir to replace anything but Air, set to any to replace everything");
						output("-shape <c/s/d/p> - set placement shape (cube/sphere/disc/plane)");
						output("-radius <amount> - set brush radius, must be a whole number");
						output("-percent <amount> - set percentage to fill");
						output("-delay/swing - set how fast the brush swings");
					}

					if (pageN == 11) {
						output("Paintbrush modes, put set the brush mode with brush.mode");
						output("-paint - default mode, the block type set with brush.replace is replaced with the block type set with brush.place");
						//						output("smooth - used to smooth terrain down");
					}
				}
				return;
			}
			output("Unrecognized command.\rEnter \"help\" for a list of commands.");
		} catch (Exception e) {
			output("Error: " + e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void output(String s) {
		output(s, false);
	}

	public static void output(String s, boolean renderNow) {
		output.addContent(s.trim());
		if (!renderNow)
			return;
		output.drawComponent();
		Display.update();
	}

}
