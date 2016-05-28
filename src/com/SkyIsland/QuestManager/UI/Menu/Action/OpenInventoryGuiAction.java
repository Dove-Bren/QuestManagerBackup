package com.SkyIsland.QuestManager.UI.Menu.Action;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.UI.Menu.InventoryMenu;
import com.SkyIsland.QuestManager.UI.Menu.Inventory.GuiInventory;

public class OpenInventoryGuiAction implements MenuAction {
	
	private GuiInventory inv;
	
	private QuestPlayer player;
	
	public OpenInventoryGuiAction(QuestPlayer player, GuiInventory inv) {
		this.inv = inv;
		this.player = player;
	}
	
	@Override
	public void onAction() {
		InventoryMenu menu = new InventoryMenu(player, inv);
		QuestManagerPlugin.questManagerPlugin.getInventoryGuiHandler().showMenu(
				player.getPlayer().getPlayer(), menu);
	}

}
