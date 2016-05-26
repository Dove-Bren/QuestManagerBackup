package com.SkyIsland.QuestManager.UI.Menu.Action;


import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.SkyIsland.QuestManager.Player.QuestPlayer;

/**
 * Opens a simple inventory. Does no syncing or saving of data, or managing
 * of the inventory. Just shows it to a player
 * @author Skyler
 *
 */
public class OpenInventoryAction implements MenuAction {
	
	private Inventory inventory;
	
	private QuestPlayer player;
	
	public OpenInventoryAction(Inventory inventory, QuestPlayer player) {
		this.inventory = inventory;
		this.player = player;
	}

	@Override
	public void onAction() {
		if (!player.getPlayer().isOnline()) {
			System.out.println("Very bad Service error!!!!!!!!!!!!!");
			return;
		}

		Player p = player.getPlayer().getPlayer();
		
		p.openInventory(inventory);
		
	}
}
