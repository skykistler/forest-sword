package com.teamlucidic.hax.quest;

import java.util.ArrayList;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.inventory.InventoryItem;
import com.teamlucidic.hax.render.Texture;
import com.teamlucidic.hax.render.gui.menu.Menu;

public class Quest {
	public ArrayList<QuestObjective> objectives = new ArrayList<QuestObjective>();

	public String title;
	public QuestObjective currObj;
	public int currObjId;
	public boolean completed;
	public Texture completeIcon = Texture.getTexture("gui/checkmark.png");
	public ArrayList<InventoryItem> rewards = new ArrayList<InventoryItem>();
	public int vitriReward;

	public Quest(String name, Texture icon) {
		title = name;
		//		Menu.currentHud.showNotification(icon, title, "Quest started.");
	}

	public void update() {
		currObj.update();
	}

	public void render() {
		currObj.render();
	}

	public void addReward(InventoryItem item) {
		rewards.add(item);
	}

	public void setVitriReward(int amount) {
		vitriReward = amount;
	}

	public void addObjective(QuestObjective qo) {
		objectives.add(qo);
		if (objectives.size() == 1) {
			currObj = qo;
			Menu.currentHud.showNotification(currObj.icon, currObj.title, currObj.description);
		}
	}

	public void completeObjective(QuestObjective qo) {
		currObjId++;
		if (currObjId == objectives.size())
			complete();
		else {
			currObj = getCurrentObjective();
			Menu.currentHud.showNotification(currObj.icon, currObj.title, currObj.description);
		}
	}

	public void complete() {
		completed = true;
		Menu.currentHud.showNotification(completeIcon, title, "completed.");
		Main.m.player.inventory.addVitri(vitriReward);
		if (rewards == null)
			return;
		for (int i = 0; i < rewards.size(); i++)
			Main.m.player.inventory.addItem(rewards.get(i));
	}

	public QuestObjective getCurrentObjective() {
		return objectives.get(currObjId);
	}
}