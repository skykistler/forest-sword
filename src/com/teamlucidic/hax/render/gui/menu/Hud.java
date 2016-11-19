package com.teamlucidic.hax.render.gui.menu;

import java.util.ArrayList;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.block.Block;
import com.teamlucidic.hax.block.BlockType;
import com.teamlucidic.hax.entity.inventory.InventoryItem;
import com.teamlucidic.hax.render.Shader;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.Font;
import com.teamlucidic.hax.render.gui.menu.components.Compass;
import com.teamlucidic.hax.render.gui.menu.components.Component;
import com.teamlucidic.hax.render.gui.menu.components.Slot;

public class Hud extends Menu {
	public static Texture rightHand = Texture.getTexture("gui/fist.png");
	public static Texture leftHand = Texture.getTexture("gui/fist.png");
	public static float fadeTime = 60;
	public static boolean debugInfo;

	public int w, h;
	public double wScale, hScale;
	public Slot[] quickSlots;
	public InventoryItem[] items = new InventoryItem[10];
	public int goodHealthTicks = (int) fadeTime;
	public int goodEnergyTicks = (int) fadeTime;
	public int titleTimer;
	public int yOff;
	public Compass compass;

	public Hud() {
		super("Forest Sword");
	}

	public void init() {
		w = Display.getWidth();
		wScale = w / 920d;
		h = Display.getHeight();
		hScale = h / 605d;
		components = new ArrayList<Component>();
		quickSlots = new Slot[10];

		for (int y = 0; y < 2; y++) {
			for (int i = 0; i < 4; i++) {
				String label = y < 1 ? InventoryMenu.topRow[i] : InventoryMenu.bottomRow[i];
				int x = w / 2 - 10 - 4 * Component.scale - Slot.widthOfSlot * 5 + i * Slot.widthOfSlot;
				if (y < 1)
					x = w / 2 + 10 + 4 * Component.scale + Slot.widthOfSlot * (i + 1);
				quickSlots[i + y * 4] = new Slot(this, label, x, h - Slot.widthOfSlot, -1, true);
			}
		}

		quickSlots[8] = new Slot(this, "RB", w / 2 + 10, h - Slot.widthOfSlot - 8 * Component.scale, -1, true);
		quickSlots[9] = new Slot(this, "LB", w / 2 - 10 - Slot.widthOfSlot, h - Slot.widthOfSlot - 8 * Component.scale, -1, true);
		compass = new Compass(this, 30);
	}

	public void update() {
		//		if (currentMenu == null && Main.m.input.checkKey(Keyboard.KEY_RETURN))
		//			showNotification(null, "Test Notification", "This is a test notification :) These can be used for anything");
		if (!Main.m.paused) {
			if (Main.m.player.health >= Main.m.player.maxHealth) {
				if (goodHealthTicks < fadeTime)
					goodHealthTicks++;
			} else
				goodHealthTicks = 0;

			if (Main.m.player.energy >= Main.m.player.inventory.abilities.maxEnergy) {
				if (goodEnergyTicks < fadeTime)
					goodEnergyTicks++;
			} else
				goodEnergyTicks = 0;

			if (menuTicks > 160)
				titleTimer += 2;
		}

		String[] keys = InventoryMenu.acceptableKeys.split(" ");
		for (int i = 0; i < items.length; i++) {
			InventoryItem item = Main.m.player.inventory.getQuickSlot(keys[i]);
			if (item != null)
				items[i] = item;
			else
				items[i] = null;
		}
		yOff = (int) ((Main.m.player.inventory.drawn ? Main.m.player.inventory.drawTimer : 7) / 7D * Slot.widthOfSlot);
		for (Slot slot : quickSlots) {
			slot.yOff = yOff;
		}

		quickSlots[8].yOff /= 4;
		quickSlots[9].yOff /= 4;
	}

	public void drawMenu() {
		w = Display.getWidth();
		wScale = w / 920d;
		h = Display.getHeight();
		hScale = h / 605d;

		Main.m.render.main.bind();

		if (Math.abs(Main.m.render.currentFOV - Main.m.render.baseFOV) < 26 && Main.m.player.inventory.drawn) {
			int scale = (int) (5 * hScale);
			int drawTimerOffset = (int) (Main.m.player.inventory.drawTimer / 7D * h / 2) + (int) ((Main.m.camera.bobX / .4 + 1) * 7);

			InventoryItem left = Main.m.player.inventory.getQuickSlot("LB");
			if ((left == null || left.type.drawHand))
				drawTexturedRect(w / 2 - leftHand.width * scale, h - leftHand.height * scale + drawTimerOffset, leftHand.width * scale, leftHand.height * scale, leftHand);

			InventoryItem right = Main.m.player.inventory.getQuickSlot("RB");
			if ((right == null || right.type.drawHand))
				drawTexturedRect(w / 2, h - rightHand.height * scale + drawTimerOffset, rightHand.width * scale, rightHand.height * scale, rightHand, rightHand.width, 0, 0, rightHand.height, 0xFFFFFFFF, false, true);
		}

		Block.drawLiquidBacks = false;
		if (Main.m.camera.inBlock != null) {
			Texture inTex = Texture.getTexture("block/fast-terrain.png").getSubTexture(Main.m.camera.inBlock.uS * BlockType.texResolution, Main.m.camera.inBlock.vS * BlockType.texResolution, BlockType.texResolution, BlockType.texResolution);
			if (Main.m.camera.inBlock.type.isLiquid) {
				double xTranslate = 100 * Main.m.camera.inBlock.type.flowSpeed * Math.cos((double) Main.m.gameTicks / (double) 20);
				double yTranslate = 100 * Main.m.camera.inBlock.type.flowSpeed * Math.sin((double) Main.m.gameTicks / (double) 20);
				Main.m.modeler.setOffset(xTranslate, yTranslate, 0);
				drawTexturedRect(-50, -50, w + 55, h + 55, inTex, 0x9FFFFFFF);
				Main.m.modeler.setOffset(0, 0, 0);
				Block.drawLiquidBacks = true;
			} else if (Main.m.camera.inSolid && !Main.m.camera.inBlock.type.hasAlpha) {
				drawRect(0, 0, w, h, 0xFF666666);
				drawTexturedRect(0, 0, w, h, inTex);
			}
		}

		Shader.unbind();

		if (AsunderIntro.escapeTimer > 180)
			drawRect(0, 0, Display.getWidth(), Display.getHeight(), Main.m.modeler.RGBAtoHex(255, 0, 0, 200 - (int) (AsunderIntro.escapeTimer * 2.5 / 60)));
		else if (AsunderIntro.escapeTimer > -1)
			drawRect(0, 0, Display.getWidth(), Display.getHeight(), Main.m.modeler.RGBAtoHex(255, 255, 255, (int) (AsunderIntro.escapeTimer * 1.42)));

		if (titleTimer < 255)
			drawBorderedString(Main.m.wName, (Display.getWidth() / 2) - (Font.currentFont.getWidth(Main.m.wName, 3) / 2), Display.getHeight() * 1 / 3, 3, Main.m.modeler.RGBAtoHex(255, 255, 255, 255 - titleTimer), Main.m.modeler.RGBAtoHex(0, 0, 0, 255 - titleTimer));

		//crosshair
		if (Main.m.player.inventory.drawn && Mouse.isGrabbed()) {
			int cw = 20;
			int ch = 2;
			drawRect(w / 2 - cw / 2, h / 2 - ch / 2, cw, ch, 0xAAF0F0F0);
			drawRect(w / 2 - ch / 2, h / 2 - cw / 2, ch, cw / 2 - ch / 2, 0xAAF0F0F0);
			drawRect(w / 2 - ch / 2, h / 2 + ch / 2, ch, cw / 2 - ch / 2, 0xAAF0F0F0);
		}

		if (Main.m.player.targetLiving != null)
			drawTargetInfo();
		if (debugInfo)
			drawDebug();
		//		drawLabel();
		drawBar(Texture.getTexture("gui/health.png"), 0, Math.ceil(Main.m.player.health), Main.m.player.maxHealth, fadeTime - goodHealthTicks, true);
		drawBar(Texture.getTexture("gui/energy.png"), (int) (-35 * hScale), Main.m.player.energy, Main.m.player.inventory.abilities.maxEnergy, fadeTime - goodEnergyTicks, true);

		//		if (Main.m.world.cloudGen != null)
		//			drawTexturedRect(0, 200, 400, 200, Main.m.world.cloudGen.tex, 0xFFFFFFFF);
	}

	public void postComponentDraw() {
		int color = 0xBBFFFFFF;
		for (int y = 0; y < 2; y++) {
			for (int i = 0; i < 4; i++) {
				int x = w / 2 - 10 - 4 * Component.scale - Slot.widthOfSlot * 5 + i * Slot.widthOfSlot + 8 * Component.scale;
				if (y < 1)
					x = w / 2 + 10 + 4 * Component.scale + Slot.widthOfSlot * (i + 1) + 8 * Component.scale;
				if (items[i + y * 4] != null) {
					drawTexturedRect(x, h - Slot.widthOfSlot + 8 * Component.scale + yOff, Component.scale * 16, Component.scale * 16, items[i + y * 4].icon != null ? items[i + y * 4].icon : items[i + y * 4].tex, color);
					String s = items[i + y * 4].usesLeft + "";
					if (items[i + y * 4].type.uses > 0 && yOff < 20)
						drawBorderedString(s, x - 2, h - (int) (Font.currentFont.letterHeight * 1.4) - 17 * Component.scale + yOff, 1.4, 0xFFFFFFFF, 0xFF000000);
				}
			}
		}

		if (items[8] != null) {
			drawTexturedRect(w / 2 + 10 + 8 * Component.scale, h - Slot.widthOfSlot + yOff / 4, Component.scale * 16, Component.scale * 16, items[8].icon != null ? items[8].icon : items[8].tex, color);
			String s = items[8].usesLeft + "";
			if (items[8].type.uses > 0)
				if (yOff < 20)
					drawBorderedString(s, w / 2 + 8 + 8 * Component.scale + Slot.widthOfSlot / 4 - Font.currentFont.getWidth(s, 2) / 2, h - (int) (Font.currentFont.letterHeight * 2) - 36 * Component.scale - 10 + yOff / 4, 2, 0xFFFFFFFF, 0xFF000000);
		}
		if (items[9] != null) {
			drawTexturedRect(w / 2 - 10 - Slot.widthOfSlot + 8 * Component.scale, h - Slot.widthOfSlot + yOff / 4, Component.scale * 16, Component.scale * 16, items[9].icon != null ? items[9].icon : items[9].tex, color);
			String s = items[9].usesLeft + "";
			if (items[9].type.uses > 0)
				if (yOff < 20)
					drawBorderedString(s, w / 2 - 10 - Slot.widthOfSlot + 8 * Component.scale + Slot.widthOfSlot / 4 - Font.currentFont.getWidth(s, 2) / 2, h - (int) (Font.currentFont.letterHeight * 2) - 36 * Component.scale - 10 + yOff / 4, 2, 0xFFFFFFFF, 0xFF000000);
		}
	}

	public void drawBar(Texture tex, int yOff, double var, double maxVar, float mod, boolean scale) {
		var = var < 0 ? 0 : var;
		int barHeight = (int) (30 * (scale ? hScale : 1));
		int barWidth = (int) (100 * (scale ? wScale : 1));
		int length = var < 1 ? 0 : (int) Math.ceil((var + 1) / maxVar * barWidth);
		mod = mod / fadeTime;
		yOff -= (int) (15 * (scale ? hScale : 1));
		int color = Main.m.modeler.RGBAtoHex(1, 1, 1, mod);
		int color2 = Main.m.modeler.RGBAtoHex(0, 0, 0, mod);
		int borderWidth = (int) (3 * Component.scale * (scale ? wScale : 1));
		drawRect(w / 2 - barWidth - 2, h - barHeight - Slot.widthOfSlot + yOff - borderWidth / 2 + (int) (Component.scale * (scale ? wScale : 1)), barWidth * 2 + 4, barHeight - borderWidth - (int) (Component.scale * (scale ? wScale : 1)) * 2, color2);

		drawTexturedRect(w / 2 - length, h - barHeight - Slot.widthOfSlot + yOff - borderWidth / 2 + (int) (Component.scale * (scale ? wScale : 1)), length * 2, barHeight - borderWidth - (int) (Component.scale * (scale ? wScale : 1)) * 2, tex, color);

		drawFancyBorder(w / 2 - barWidth - borderWidth, h - barHeight - Slot.widthOfSlot - borderWidth + yOff, barWidth * 2 + borderWidth * 2, barHeight, color, (int) (Component.scale * (scale ? wScale : 1)));
	}

	public void drawTargetInfo() {
		float mod = (Main.m.player.targetTimer > fadeTime ? fadeTime : Main.m.player.targetTimer) / fadeTime;
		int color = Main.m.modeler.RGBAtoHex(1, 1, 1, mod);
		int color2 = Main.m.modeler.RGBAtoHex(0, 0, 0, mod * .8F);
		int borderWidth = 3 * Component.scale;
		int compassOffset = (compass != null && compass.enabled ? compass.posY + compass.height + compass.borderSize * 2 : 0);
		double scale = 2;
		int width = (int) Math.max(250, Font.currentFont.getWidth(Main.m.player.targetLiving.name, scale) + 20);

		drawRect(w / 2 - width / 2, -borderWidth + compassOffset, width, 75, color2);

		drawBar(Texture.getTexture("gui/health.png"), getBarYOffCancel() + 50 + compassOffset, Main.m.player.targetLiving.health, Main.m.player.targetLiving.maxHealth, Main.m.player.targetTimer, false);

		drawString(Main.m.player.targetLiving.name, w / 2 - Font.currentFont.getWidth(Main.m.player.targetLiving.name, scale) / 2, -1 + compassOffset, scale, color);

		drawFancyBorder(w / 2 - width / 2, -borderWidth + compassOffset, width, 75, color, Component.scale);
	}

	public int getBarYOffCancel() {
		int barHeight = 30;
		int borderWidth = 3 * Component.scale;
		return -(h - barHeight - Slot.widthOfSlot - borderWidth / 2 + Component.scale);
	}

	public static void setHand(String hand, Texture tex) {
		if (hand.equals("RB"))
			rightHand = tex;
		if (hand.equals("LB"))
			leftHand = tex;
	}

}
