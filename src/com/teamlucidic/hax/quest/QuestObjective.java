package com.teamlucidic.hax.quest;

import com.teamlucidic.hax.Main;
import com.teamlucidic.hax.entity.AABB;
import com.teamlucidic.hax.entity.inventory.InventoryItem;
import com.teamlucidic.hax.entity.living.Living;
import com.teamlucidic.hax.render.Texture;

public class QuestObjective {
	public String title;
	public Texture icon;
	public String description;
	public Quest parent;
	public boolean getItem, travelToArea, speakWithLiving, killTarget;

	public int quantity;
	public InventoryItem targetItem;
	public AABB targetArea;
	public Living targetLiving;

	public boolean completed;

	public QuestObjective(Quest q, String name, Texture ico, String type, String desc) {
		parent = q;
		title = name;
		icon = ico;
		description = desc;
		if (type.equals("item"))
			getItem = true;
		if (type.equals("travel"))
			travelToArea = true;
		if (type.equals("speak"))
			speakWithLiving = true;
		if (type.equals("kill"))
			killTarget = true;
	}

	public void update() {
		if (travelToArea && targetArea != null) {
			//			if (Main.m.camera.frustum.pointInFrustum(targetArea.minX, targetArea.minY, targetArea.minZ))
			//				System.out.println("hi");
			if (targetArea.intersectsWith(Main.m.player.posX, Main.m.player.posY, Main.m.player.posZ))
				complete();
		}
	}

	public void render() {
		if (travelToArea && targetArea != null)
			targetArea.render();
	}

	public void setQuantity(int amount) {
		quantity = amount;
	}

	public void setItem(InventoryItem item, int amount) {
		targetItem = item;
		setQuantity(amount);
	}

	public void setArea(AABB bb) {
		targetArea = bb;
		setQuantity(0);
	}

	public void setPoint(int x, int y, int z) {
		setArea(new AABB(x, y, z, 1, 1, 1));
	}

	public void setLivingTarget(Living living, int amount) {
		targetLiving = living;
		setQuantity(amount);
	}

	public String getName() {
		return title.replace("%q", quantity + "");
	}

	public void complete() {
		completed = true;
		parent.completeObjective(this);
	}
}
