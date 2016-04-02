package com.SkyIsland.QuestManager.UI.Menu.Action;


import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.SkyIsland.QuestManager.NPC.Utils.ServiceCraft;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.UI.ChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.SimpleChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.Message.Message;

import io.puharesource.mc.titlemanager.api.TitleObject;

/**
 * Takes a list of requirements and a fee and produces results
 * @author Skyler
 *
 */
public class CraftServiceAction implements MenuAction {
	
	private ServiceCraft trade;
	
	private QuestPlayer player;
	
	private Message denial;
	
	public CraftServiceAction(ServiceCraft trade, QuestPlayer player, Message denialMessage) {
		this.trade = trade;
		this.player = player;
		this.denial = denialMessage;
	}

	@Override
	public void onAction() {
		if (!player.getPlayer().isOnline()) {
			System.out.println("Very bad Service error!!!!!!!!!!!!!");
			return;
		}
		//check their money
		if (player.getMoney() >= trade.getCost()) {
			//they have enough money
			
			Player p = player.getPlayer().getPlayer();
			
			//check if they have the required items
			boolean pass = true;
			for (ItemStack req : trade.getRequired()) {
				if (!player.hasItem(req)) {
					pass = false;
					break;
				}
			}
			
			if (!pass) {
				deny();
				return;
			}
			
			//had money, had items
			
			//play smith sound, take money,
			//deduct required items
			//give new item
			
			for (ItemStack req : trade.getRequired()) {
				player.removeItem(req);
			}
			player.addMoney(-trade.getCost());
			
			p.getInventory().addItem(trade.getResult());
			
			p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
			
			(new TitleObject("", ChatColor.GOLD + "Item Crafted"))
				.setFadeIn(20).setFadeOut(20).setStay(40).send(p);
			
			
			
			
		} else {
			//not enough money
			//show them a menu, sorrow
						
			deny();
		}
		
	}
	
	private void deny() {
		ChatMenu menu = new SimpleChatMenu(denial.getFormattedMessage());
		
		menu.show(player.getPlayer().getPlayer());
	}

}
