package com.SkyIsland.QuestManager.UI.Menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.UI.Menu.Action.MenuAction;
import com.SkyIsland.QuestManager.UI.Menu.Inventory.GuiInventory;

/**
 * A menu implemented as an inventory that performs some action when closed
 * @author Skyler
 *
 */
public class ActiveInventoryMenu extends InventoryMenu {
	
	private MenuAction action;
	
	public ActiveInventoryMenu(QuestPlayer player, GuiInventory inv, MenuAction closeAction) {
		super(player, inv);
		this.action = closeAction;		
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory().getName() == null || !e.getInventory().getName().equals(inventory.getName())) {
			return;
		}
		
		if (!(e.getPlayer() instanceof Player) || !(((Player) e.getPlayer()).getUniqueId().equals(
				player.getPlayer().getUniqueId()))) {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Inventory menu event matched names,"
					+ " but not players! [" + e.getPlayer().getName() + "]");
			return;
		}
		
		super.onInventoryClose(e);
		action.onAction();
		
	}
	
	
	
	
}
