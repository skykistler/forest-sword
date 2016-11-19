package com.teamlucidic.hax.render.gui.menu;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.NameGenerator;
import com.teamlucidic.hax.Sound;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.entity.AABB;
import com.teamlucidic.hax.entity.Flock;
import com.teamlucidic.hax.entity.inventory.item.TheCure;
import com.teamlucidic.hax.quest.Quest;
import com.teamlucidic.hax.quest.QuestObjective;
import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.Font;
import com.teamlucidic.hax.world.World;

public class AsunderIntro extends Menu {
	public static boolean win = false;
	public static int escapeTimer = -1;
	public static int explosionTimer = 0;

	public int step;
	public String mapName;
	public boolean newGame;
	public String toDraw = "";
	public double rate = 2;
	public int loc;

	public AsunderIntro(String map) {
		super("");
		win = false;
		escapeTimer = -1;
		explosionTimer = 0;
		TheCure.released = false;
		openPost = openPost.replace("%n", Main.m.settings.playerName);
		blogPost = blogPost.replace("%n", Main.m.settings.playerName);
		chat = chat.replace("%n", Main.m.settings.playerName);
		if (map == null) {
			mapName = "The Island of " + NameGenerator.getNameFrom("cities");
			newGame = true;
		} else
			mapName = Main.m.wName;
		chat = chat.replace("%i", mapName);

		openPost = Font.currentFont.wordWrap(openPost, Display.getWidth() - 70, 2);
		blogPost = Font.currentFont.wordWrap(blogPost, Display.getWidth() - 70, 2);
		chat = Font.currentFont.wordWrap(chat, Display.getWidth() - 70, 2);
	}

	public void update() {
		if (step <= 3 && Main.m.input.checkKey(Keyboard.KEY_Y)) {
			step = step + 1;
			loc = 0;
			rate = 2;
		}
		if (step == 4)
			startAsunder();

		if (toDraw.endsWith("pieces.") && !Main.m.input.isDown(Keyboard.KEY_SPACE))
			return;

		toDraw = "";
		if (step <= 3) {
			String cur = step == 0 ? openPost : step == 1 ? blogPost : step == 2 ? incomingChat : chat;
			if (menuTicks % rate == 0 && loc < cur.length() - 1)
				loc++;
			toDraw = cur.substring(0, loc >= cur.length() ? cur.length() - 1 : loc);
			if (cur.length() - toDraw.length() < 2) {
				if (loc < cur.length() + 60 + (step == 1 ? 60 : 0))
					loc++;
				else {
					step++;
					loc = 0;
				}
			}
			if (step == 1 && toDraw.endsWith("Read out:"))
				rate = 4;
			if (step == 2)
				rate = 2;
			if (step == 3) {
				rate = 4;
				if (toDraw.endsWith("pieces."))
					return;
			}
			if (toDraw.endsWith("`") || cur.length() - toDraw.length() < 2) {
				if (menuTicks % 30 < 10)
					toDraw += "_";
			} else
				toDraw += Render.rand.nextDouble() > .8 ? "" : "_";
		}
	}

	public void drawMenu() {
		drawRect(0, 0, Display.getWidth(), Display.getHeight(), 0xFF000000);

		drawString(toDraw, 5, 10, 2, 0xFFFFFFFF);
		drawString("Press 'Y' to skip the intro", 2, Display.getHeight() - 20, 1, 0xFF222222);
		if (toDraw.endsWith("rip you to pieces.")) {
			String toCont = "Press 'Space' to continue";
			drawString(toCont, Display.getWidth() / 2 - Font.currentFont.getWidth(toCont, 2) / 2, Display.getHeight() - 40, 2, 0xFFFFFFFF);
		}
	}

	public void startAsunder() {
		if (newGame) {
			Main.m.wName = mapName;
			Main.m.wType = 1;
			Main.m.wSeed = World.generateSeed();
			Main.m.wWidth = 24;
			Main.m.startGame("asunder:new");
		} else {
			Main.m.wName = mapName;
			Main.m.startGame("asunder:load");
		}
		Console.runCommand("world.time.set night");
		Main.m.world.changeTime = false;
		Main.m.world.setWeather("rain");
		int centerX = Main.m.world.centerX;
		int centerZ = Main.m.world.centerZ;
		double width = Main.m.world.widthInChunks * 10;
		Main.m.world.zombieFlock = new Flock(Main.m.world.widthInChunks / 6, 3, (int) width, centerX, centerZ);
		width = Main.m.world.widthInChunks * 11;

		for (int i = 0; i < Main.m.world.widthInChunks * Main.m.world.widthInChunks * .67; i++) {
			double x = Render.rand.nextDouble() * width + centerX - width / 2;
			double z = Render.rand.nextDouble() * width + centerZ - width / 2;
			Main.m.livingLoader.getLivingType("Zombie").spawnNewAt(x, z);
		}

		for (int i = 0; i < Main.m.world.widthInChunks * .7; i++) {
			double x = Render.rand.nextDouble() * width + centerX - width / 2;
			double z = Render.rand.nextDouble() * width + centerZ - width / 2;
			Main.m.livingLoader.getLivingType("Survivor").spawnNewAt(x, z);
		}

		Block b = Main.m.world.getTopBlock(40, 40, true, true);
		Main.m.world.setSpawn(b);
		Main.m.world.placeBlock("Sand", Main.m.world.spawnX, Main.m.world.spawnY - 1, Main.m.world.spawnZ, true);
		Main.m.world.placeBlock("Sand", Main.m.world.spawnX + 1, Main.m.world.spawnY - 1, Main.m.world.spawnZ + 1, true);
		Main.m.world.placeBlock("Sand", Main.m.world.spawnX + 1, Main.m.world.spawnY - 1, Main.m.world.spawnZ - 1, true);
		Main.m.world.placeBlock("Sand", Main.m.world.spawnX - 1, Main.m.world.spawnY - 1, Main.m.world.spawnZ + 1, true);
		Main.m.world.placeBlock("Sand", Main.m.world.spawnX - 1, Main.m.world.spawnY - 1, Main.m.world.spawnZ - 1, true);
		Main.m.world.placeBlock("Sand", Main.m.world.spawnX + 1, Main.m.world.spawnY - 1, Main.m.world.spawnZ, true);
		Main.m.world.placeBlock("Sand", Main.m.world.spawnX - 1, Main.m.world.spawnY - 1, Main.m.world.spawnZ, true);
		Main.m.world.placeBlock("Sand", Main.m.world.spawnX, Main.m.world.spawnY - 1, Main.m.world.spawnZ + 1, true);
		Main.m.world.placeBlock("Sand", Main.m.world.spawnX, Main.m.world.spawnY - 1, Main.m.world.spawnZ - 1, true);
		Main.m.world.spawnRot = 125;

		Main.m.world.saveWorld();

		Main.m.world.respawnPlayer();

		addAndCheckItem("The Cure");
		addAndCheckItem("Health Potion");
		addAndCheckItem("Energy Potion");
		addAndCheckItem("Hand Torch");
		addAndCheckItem("Telescope");
		addAndCheckItem("Sword");
		addAndCheckItem("Sword");
		addAndCheckItem("Shuriken");
		addAndCheckItem("Fulminate");
		addAndCheckItem("Bubble Blast");
		addAndCheckItem("Bubble Blast");
		addAndCheckItem("Fire Blast");
		addAndCheckItem("Fire Blast");
		addAndCheckItem("Health Blast");

		Quest winQuest = new Quest("Cure the island", Texture.getTexture("item/thecureicon.png"));
		QuestObjective deliver = new QuestObjective(winQuest, "Deliver the cure", Texture.getTexture("item/thecureicon.png"), "travel", "Reach the middle of the island") {
			public boolean inArea;

			public void update() {
				if (targetArea.intersectsWith(Main.m.player.posX, Main.m.player.posY, Main.m.player.posZ)) {
					if (!inArea)
						Menu.currentHud.showNotification(icon, "Release the cure", "");
					inArea = true;
				} else
					inArea = false;
				if (TheCure.released)
					complete();
			}

			public void complete() {
				super.complete();
				AsunderIntro.escape();
			}
		};
		deliver.setArea(new AABB(centerX, 0, centerZ, 3, Main.m.world.height, 3));
		winQuest.addObjective(deliver);
		QuestObjective escape = new QuestObjective(winQuest, "Escape the island", Texture.getTexture("gui/sprint.png"), "travel", "Run off the island before the cure detonates.") {
			public void update() {
				if (Main.m.player.inLiquid)
					complete();
			}

			public void complete() {
				super.complete();
				AsunderIntro.win = true;
			}
		};
		winQuest.addObjective(escape);
		Main.m.player.setQuest(winQuest);

		Sound.getSound("music/tension.ogg").playInBackground(true);
		Main.m.resetTimer();
	}

	public void addAndCheckItem(String item) {
		Main.m.player.inventory.addItem(item);
		//		InventoryItem it = Main.m.player.inventory.addItem(item);
		//		String[] keys = InventoryMenu.acceptableKeys.split(" ");
		//		for (int i = 0; i < keys.length; i++) {
		//			if (Player.savedQuickslots[i] != null && Player.savedQuickslots.equals(item)) {
		//				Player.savedQuickslots[i] = null;
		//				Main.m.player.inventory.setQuickSlot(keys[i], it);
		//			}
		//		}
	}

	public String openPost = "%n@mission-console:~# curl blog.teamlucidic.com -L";
	public String blogPost = "Fetching SSL cert...`````````````````````````\rSecure transfer socket established...`````````````````````\rRead out:\r`````````````````````````All I can say is I hope someday someone will read this.```````````` I have nothing left except the words on this post and soon I won't have that.``````` I miss my family.```````` I miss my friends.````````````` Some blame the super powers that made this hellfire.````````` Some blame the radicals that stole it.`````` Most blame each other.```````````` But no one blames themselves.````````````` We were in this together and if anyone is to blame it's ourselves.````````` Ourselves for letting the world hate.````````` Ourselves for letting the world hurt.`````````````` Ourselves for giving up.`````````````````````\rI have the words on this page and I have hope, and soon I will have nothing.`````````````````\r#### END TRANSMISSION ####";
	public String incomingChat = "Incoming message! Connecting...`````";
	public String chat = "Captain: Another island has been attacked.\r\r%n: ``````These guys never quit.````````````````\r\rCaptain: Tell me about it. ````We'll drop you off at %i with all the necessary supplies.````` You must deliver the cure to the center of the island.```` The cure will bring salvation to these poor souls, but it's harmful to the living.```` You have to get off the island and into the water before it detonates.````` Do I make myself clear?\r\r%n: ```````````````Yes sir.``````````````````\r\rCaptain: Good.````` Keep in mind that they're attracted to light and fast movement.`````` They also tend to horde up together, be careful not to stumble into a group of them.``` These things will rip you to pieces.\r\r%n: ````Noted. Begin transport.```````````";

	public static void escape() {
		Sound.getSound("music/tension.ogg").stopBackground();
		Sound.getSound("music/heavy action drums.ogg").playInBackground(false);
		escapeTimer = 80 * 60;
	}

	public static void win() {
		win = false;
		Menu.setHud(null);
		Main.m.stopGame();
		Menu.setMenu(new AsunderEnd());
	}
}