package com.teamlucidic.hax.render.gui.menu.components;

import com.teamlucidic.hax.render.Render;
import com.teamlucidic.hax.render.gui.Font;
import com.teamlucidic.hax.render.gui.menu.Menu;

public class TextArea extends Component {
	public String content = "";
	public String wordwrappedContent = "";

	public TextArea(Menu m, int x, int y, int w, int h) {
		super(m, x, y, w, h);
		fontSize = 2;
	}

	public void update() {
		if (wordwrappedContent.length() == 0 && content.length() >= 1)
			wordWrapAndScroll();
	}

	public void drawComponent() {
		drawRect(posX + borderSize, posY + borderSize, width - borderSize, height - borderSize, 0xFF000000);
		if (!enabled)
			drawRect(posX + borderSize, posY + borderSize, width - borderSize, height - borderSize, 0xFF555555);
		drawBorder();
		drawShadowedString(wordwrappedContent, posX + borderSize + 2, posY + borderSize + 2, fontSize, enabled ? 0xFFFFFFFF : 0xFFBBBBBB);
	}

	public void addContent(String s) {
		if (content.length() > 0)
			content += " \r";
		content += s;
		wordWrapAndScroll();
	}

	public void wordWrapAndScroll() {
		wordwrappedContent = Font.currentFont.wordWrap(content, width - borderSize - 2, fontSize);
		while (wordwrappedContent.split("\r").length > Render.floor(height / Font.currentFont.letterHeight) - 2)
			wordwrappedContent = wordwrappedContent.substring(wordwrappedContent.indexOf("\r") + 1, wordwrappedContent.length());

	}

	public void clear() {
		content = wordwrappedContent = "";
	}

	public void drawBorder() {
		int color = 0xFFBBBBBB;
		drawRect(posX, posY, borderSize, height, color);
		drawRect(posX, posY, width, borderSize, color);
		drawRect(posX + width - borderSize, posY, borderSize, height, color);
		drawRect(posX, posY + height - borderSize, width, borderSize, color);
	}

}
