package com.teamlucidic.hax;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import com.teamlucidic.hax.block.BlockLoader;
import com.teamlucidic.hax.entity.Camera;
import com.teamlucidic.hax.entity.inventory.InventoryItemLoader;
import com.teamlucidic.hax.entity.living.LivingLoader;
import com.teamlucidic.hax.entity.living.Player;
import com.teamlucidic.hax.entity.particle.ParticleSystemLoader;
import com.teamlucidic.hax.entity.particle.projectile.ProjectileLoader;
import com.teamlucidic.hax.quest.QuestLoader;
import com.teamlucidic.hax.render.Framebuffer;
import com.teamlucidic.hax.render.ModelLoader;
import com.teamlucidic.hax.render.Modeler;
import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.render.Shader;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.menu.AsunderIntro;
import com.teamlucidic.hax.render.gui.menu.Console;
import com.teamlucidic.hax.render.gui.menu.Menu;
import com.teamlucidic.hax.render.gui.menu.Pause;
import com.teamlucidic.hax.render.gui.menu.StartMenu;
import com.teamlucidic.hax.render.gui.menu.components.Minimap;
import com.teamlucidic.hax.world.World;

public class Main implements Runnable {
	public static final String version = "Forest Sword Engine [Beta 1.1.6 Spring]";

	public String resourcePack = "/data/";

	public static Main m;
	public Settings settings;
	public Render render;
	public Modeler modeler;
	public InputHandler input;
	public Camera camera;
	public Player player;

	public World world;
	public String wName;
	public int wType;
	public long wSeed;
	public int wWidth;
	public int wHeight;
	public BlockLoader blockLoader;
	public ParticleSystemLoader partSysLoader;
	public ProjectileLoader projectileLoader;
	public ModelLoader modelLoader;
	public InventoryItemLoader invItemLoader;
	public LivingLoader livingLoader;
	public QuestLoader questLoader;

	public boolean gameStarted;
	public boolean paused;
	public String gameMode;

	public boolean running;
	public int ticksDone;
	public int framesDone;
	public int fps;
	public int ticksps;
	public int gameTicks;
	public long timer;
	public long lastTime;
	public double ticksLeft;
	public int idealTicksPerSecond = 60;

	public static void main(String[] args) {
		m = new Main();
	}

	public Main() {
		System.out.println("Starting " + version);
		makeThread();
	}

	public void init() {
		try {
			setDefaultResourceFolder();
		} catch (Exception e) {
			e.printStackTrace();
			fatalError("Could not load the data folder, is it moved somewhere else?");
		}
		deleteSpecifiedFiles();
		settings = new Settings();
		try {
			makeWindow();
			Keyboard.create();
		} catch (Exception e) {
			e.printStackTrace();
			fatalError("Failed Display Creation");
		}
		render = new Render();
		drawSplash();
		modeler = new Modeler();
		input = new InputHandler();
		Texture.loadAllTextures();
		Sound.init();
		NameGenerator.loadAllTables();
		modelLoader = new ModelLoader();
		blockLoader = new BlockLoader();
		partSysLoader = new ParticleSystemLoader();
		projectileLoader = new ProjectileLoader();
		invItemLoader = new InventoryItemLoader();
		livingLoader = new LivingLoader();
		//		questLoader = new QuestLoader();
		Menu.setMenu(new StartMenu());
		Display.setTitle(version);
	}

	public void run() {
		init();
		running = true;
		lastTime = System.nanoTime();
		timer = System.currentTimeMillis();
		gameTicks = 0;
		while (running) {
			ticksLeft += (System.nanoTime() - lastTime) * idealTicksPerSecond / 1000000000D;
			lastTime = System.nanoTime();
			while (ticksLeft >= 1) {
				ticksDone++;
				try {
					update();
				} catch (Exception e) {
					e.printStackTrace();
					shutDown();
				}
				ticksLeft -= 1;
			}
			render();
			updateFPS();
			running = Display.isCloseRequested() ? false : running;
		}
		shutDown();
	}

	public void resetTimer() {
		Main.m.lastTime = System.nanoTime();
		Main.m.ticksLeft = 0;
		Main.m.timer = System.currentTimeMillis();
	}

	public void update() {
		input.update();
		if (input.checkKey(input.fullscreen))
			setFullscreen();
		Sound.updateAll();
		if (gameStarted) {
			if (!paused) {
				if (Menu.currentMenu != null && Menu.currentMenu.pauseMenu)
					Menu.setMenu(null);
				Main.m.world.update();
				render.update();
				if (AsunderIntro.win && AsunderIntro.escapeTimer < 1)
					AsunderIntro.win();
				gameTicks++;
			}
			if (input.checkKey(input.pause) || (paused && !(Menu.currentMenu != null && Menu.currentMenu.pauseMenu))) {
				if (Menu.currentMenu != null)
					Menu.setMenu(null);
				else
					pause();
			}
		}

		Menu.updateCurrentMenu();
	}

	public void render() {
		if (Display.wasResized()) {
			settings.windowWidth = Display.getWidth();
			settings.windowHeight = Display.getHeight();

			render.init();
			if (Menu.currentHud != null)
				Menu.currentHud.init();
			if (Menu.currentMenu != null)
				Menu.currentMenu.init();
		}
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		if (gameStarted)
			render.renderWorld();
		render.renderGui();
		
		Display.update();
	}

	public void startGame(String gm) {
		gameMode = gm;
		Display.setVSyncEnabled(false);
		new Console().init();
		if (gameStarted)
			stopGame();
		if (gameMode.contains("new"))
			createWorld();
		else
			loadWorld(wName);
		paused = false;
		gameStarted = true;
		Display.setVSyncEnabled(settings.vsync);
	}

	public void stopGame() {
		Sound.stopAllBackgroundSounds();
		paused = false;
		gameStarted = false;
		Main.m.world.unload();
		Main.m.world = null;
		Minimap.map.delete();
		Minimap.map = null;
		Main.m.gc();
	}

	public void pause() {
		paused = !paused;
		if (paused)
			Menu.setMenu(new Pause());
		else if (player != null)
			player.inventory.justUnPaused = true;

		if (!paused)
			Sound.resumePausedBackgroundSounds();
		else
			Sound.pauseAllBackgroundSounds();
	}

	public void createWorld() {
		world = new World(wSeed);
		world.name = wName;
		world.widthInChunks = wWidth;
		world.generateWorld();
	}

	public void loadWorld(String name) {
		world = new World();
		world.loadWorld(name);
	}

	public void makeThread() {
		Thread thread = new Thread(this);
		thread.start();
	}

	public void makeWindow() throws Exception {
		Display.setDisplayMode(new DisplayMode(settings.windowWidth, settings.windowHeight));
		Display.setTitle("Loading " + version);
		Display.setResizable(true);
		Display.setIcon(loadIcons());
		Display.setVSyncEnabled(settings.vsync);
		Display.create();

		System.out.println("Using OpenGL version: " + GL11.glGetString(GL11.GL_VERSION));
	}

	public ByteBuffer[] loadIcons() {
		ByteBuffer[] icons = null;
		if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			icons = new ByteBuffer[3];

			Texture i16 = new Texture("icon16.png");
			i16.loadBuffers();
			icons[0] = i16.pixBuffer;

			Texture i32 = new Texture("icon32.png");
			i32.loadBuffers();
			icons[1] = i32.pixBuffer;

			Texture i128 = new Texture("icon128.png");
			i128.loadBuffers();
			icons[2] = i128.pixBuffer;
		} else {
			icons = new ByteBuffer[2];

			Texture i32 = new Texture("icon32.png");
			i32.loadBuffers();
			icons[0] = i32.pixBuffer;

			Texture i128 = new Texture("icon128.png");
			i128.loadBuffers();
			icons[1] = i128.pixBuffer;
		}

		return icons;
	}

	public void drawSplash() {
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, Display.getWidth(), Display.getHeight(), 0, -0.1f, 0.1f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		Texture.getTexture("splash.png").bind();

		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex3f(0, 0, 0);

		GL11.glTexCoord2f(0, 1);
		GL11.glVertex3f(0, Display.getHeight(), 0);

		GL11.glTexCoord2f(1, 1);
		GL11.glVertex3f(Display.getWidth(), Display.getHeight(), 0);

		GL11.glTexCoord2f(1, 0);
		GL11.glVertex3f(Display.getWidth(), 0, 0);
		GL11.glEnd();
		Display.update();
	}

	boolean vsync = false;
	public void updateFPS() {
		framesDone++;
		if (System.currentTimeMillis() - timer >= 1000) {
			if (fps > 60 && !vsync) {
				Display.setVSyncEnabled(vsync = true);
			} else if (vsync) {
				Display.setVSyncEnabled(vsync = false);
			}
			
			timer += 1000;
			System.out.println((ticksps = ticksDone) + " updates, " + (fps = framesDone) + " frames");
			framesDone = 0;
			ticksDone = 0;
		}
	}

	public void setFullscreen() {
		try {
			if (!Display.isFullscreen()) {
				settings.windowWidth = Display.getWidth();
				settings.windowHeight = Display.getHeight();
				if (Menu.currentMenu == null)
					pause();
				Display.setDisplayMode(Display.getDesktopDisplayMode());
				Display.setFullscreen(true);
			} else {
				Display.setDisplayMode(new DisplayMode(settings.windowWidth, settings.windowHeight));
				Display.setFullscreen(false);
			}

			render.init();
			if (Menu.currentHud != null)
				Menu.currentHud.init();
			if (Menu.currentMenu != null)
				Menu.currentMenu.init();
			Display.update();
		} catch (Exception e) {
			error("Could not make fullscreen.");
			e.printStackTrace();
		}
	}

	public void setDefaultResourceFolder() throws URISyntaxException, UnsupportedEncodingException {
		String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		path = URLDecoder.decode(path, "UTF-8");
		resourcePack = path.substring(0, path.lastIndexOf("/")) + resourcePack;
		if (System.getProperty("os.name").toLowerCase().startsWith("window"))
			resourcePack = resourcePack.substring(1);
		System.out.println("Loading default resources from: " + resourcePack);
	}

	public void deleteSpecifiedFiles() {
		try {
			String del = loadFile("delete.txt");
			String[] parts = del.split("\n|\\r");
			int i = 0;
			for (String s : parts) {
				File f = new File(Main.m.resourcePack + s);
				if (f.exists()) {
					System.out.println("Deleting: " + s);
					delete(f);
					i++;
				}
			}
			delete(new File(Main.m.resourcePack + "delete.txt"));
			System.out.println("Cleaned up " + i + " files.");
		} catch (FileNotFoundException e) {
			System.out.println("No delete file found.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void delete(File f) {
		if (!f.exists())
			return;
		if (f.isDirectory())
			for (File f2 : f.listFiles())
				delete(f2);
		f.delete();
	}

	public void shutDown() {
		System.out.println("Shutting down...");
		if (gameStarted)
			stopGame();
		Texture.deleteTextures();
		Framebuffer.deleteFramebuffers();
		Shader.deleteShaders();
		Sound.cleanup();
		Display.destroy();
		System.out.println("Closing " + version);
		System.exit(0);
		gc();
	}

	public void gc() {
		System.gc();
	}

	public void error(String name) {
		System.out.println("Error: " + name);
	}

	public void fatalError(String name) {
		System.out.println("Fatal error: " + name);
		System.out.println("Forcing exit.");
		shutDown();
	}

	public static String loadFile(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(Main.m.resourcePack + path));
		StringBuilder sb = new StringBuilder();

		String line = br.readLine();
		while (line != null) {
			sb.append(line);
			sb.append("\n");
			line = br.readLine();
		}

		br.close();
		return sb.toString();
	}

	public static String getFileExtension(String file) {
		return file.substring(file.lastIndexOf('.') + 1, file.length());
	}

	public static void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
