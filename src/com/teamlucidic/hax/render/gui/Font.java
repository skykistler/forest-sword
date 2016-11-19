package com.teamlucidic.hax.render.gui;

import java.util.ArrayList;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.render.Texture;

public class Font extends Gui {
	public static Font currentFont;
	public static int rowLength;
	public static final String row0 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ,.@#^|";
	public static final String row1 = "abcdefghijklmnopqrstuvwxyz ";
	public static final String row2 = "0123456789?!:;'\"<>()[]-/\\%+$_*~&";
	public static final String legalChars = row0 + row1 + row2 + '\r';

	public String title;
	public Texture fontTex;
	public int letterWidth;
	public int letterHeight;

	public Font(String name) {
		title = name;
		loadFont();
	}

	public void loadFont() {
		fontTex = Texture.getTexture("font/" + title + ".png");
		letterWidth = fontTex.width / row2.length();
		letterHeight = fontTex.height / 3;
	}

	public static void drawStringWithCurFont(String msg, int x, int y, double scale, int color) {
		int nextX = x;
		scale *= 8 / currentFont.letterWidth;
		Main.m.modeler.start();
		for (int i1 = 0; i1 < msg.length(); i1++) {
			char letter = msg.charAt(i1);
			if (letter == '\r') {
				y += (currentFont.letterHeight) * scale;
				nextX = x;
				continue;
			}
			boolean drawn = currentFont.drawLetter(letter, nextX, y, scale, color);
			if (drawn)
				nextX += currentFont.letterWidth * scale;
		}
		Main.m.modeler.finish();
		msg = null;
	}

	public boolean drawLetter(char letter, int x, int y, double scale, int color) {
		if (row0.indexOf(letter) > -1) {
			drawTexturedRect(x, y, (int) (letterWidth * scale), (int) (letterHeight * scale), fontTex, row0.indexOf(letter) * letterWidth, letterHeight * 0, letterWidth, letterHeight, color, true, false);
			return true;
		} else if (row1.indexOf(letter) > -1) {
			drawTexturedRect(x, y, (int) (letterWidth * scale), (int) (letterHeight * scale), fontTex, row1.indexOf(letter) * letterWidth, letterHeight * 1, letterWidth, letterHeight, color, true, false);
			return true;
		} else if (row2.indexOf(letter) > -1) {
			drawTexturedRect(x, y, (int) (letterWidth * scale), (int) (letterHeight * scale), fontTex, row2.indexOf(letter) * letterWidth, letterHeight * 2, letterWidth, letterHeight, color, true, false);
			return true;
		}
		return false;
	}

	public static void drawBordered(String msg, int x, int y, double scale, int color, int color2) {
		drawStringWithCurFont(msg, x + (int) scale, y, scale, color2);
		drawStringWithCurFont(msg, x, y + (int) scale, scale, color2);
		drawStringWithCurFont(msg, x - (int) scale, y, scale, color2);
		drawStringWithCurFont(msg, x, y - (int) scale, scale, color2);
		drawStringWithCurFont(msg, x, y, scale, color);
	}

	public static void drawShadowed(String msg, int x, int y, double scale, int color) {
		drawStringWithCurFont(msg, x + (int) (scale), y + (int) (scale), scale, 0xFF000000);
		drawStringWithCurFont(msg, x, y, scale, color);
	}

	public int getWidth(String str, double scale) {
		str = str.replaceAll("`", "");
		int width = (int) (str.length() * letterWidth * scale);
		return width;
	}

	public String wordWrap(String s, int width, double scale) {
		if (!(s.indexOf(' ') > -1) && !(s.indexOf('\r') > -1))
			return s;
		ArrayList<String> lines = new ArrayList<String>();
		String[] currentLines = s.split("\r");
		for (String l : currentLines)
			lines.add(l);

		String result = "";
		for (int i = 0; i < lines.size(); i++) {
			String[] words = lines.get(i).split(" ");
			String line = "";
			for (int w = 0; w < words.length; w++) {
				int id = line.lastIndexOf("\r");
				if (Font.currentFont.getWidth(line.substring(id > -1 ? id : 0, line.length()) + words[w], scale) > width) {
					line += "\r " + words[w];
				} else
					line += " " + words[w];
			}
			result += line + "\r";
		}
		return result;
	}

	public static int countChar(String s, char c) {
		int count = 0;
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == c)
				count++;
		return count;
	}

	public static void setFont(Font font) {
		currentFont = font;
	}

}
