package com.teamlucidic.hax.quest;

import java.util.ArrayList;

import com.teamlucidic.hax.entity.inventory.InventoryItemType;
import com.teamlucidic.hax.render.Texture;

public class QuestTemplate {
	public String title;
	public Texture icon;
	public Texture completeIcon;
	public ArrayList<InventoryItemType> rewards;
	public int vitriReward;

	public QuestTemplate(String t, String i, String ci, ArrayList<InventoryItemType> r, int vr) {
		title = t;
		icon = Texture.getTexture(i);
		completeIcon = Texture.getTexture(ci);
		rewards = r;
		vitriReward = vr;
	}

	public Quest newQuest() {
		return null;
	}
}
