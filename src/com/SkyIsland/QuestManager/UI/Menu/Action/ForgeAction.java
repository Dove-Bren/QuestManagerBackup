package com.SkyIsland.QuestManager.UI.Menu.Action;


import java.util.ListIterator;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.SkyIsland.QuestManager.Fanciful.FancyMessage;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.UI.ChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.SimpleChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.Message.Message;

import io.puharesource.mc.titlemanager.api.TitleObject;

/**
 * Repairs a player's equipment
 * @author Skyler
 *
 */
public class ForgeAction implements MenuAction {
	
	public enum Repairable {
		SWORD,
		BOW,
		PICKAXE,
		SPADE,
		AXE,
		CHESTPLATE,
		LEGGINGS,
		HELMET,
		BOOTS;
		
		public static boolean isRepairable(Material mat) {
			
			String cname = mat.name();
			for (Repairable rep : Repairable.values()) {
				if (cname.endsWith(rep.name())) {
					return true;
				}
			}
			
			
			return false;
		}
	}

	private int cost;
	
	private QuestPlayer player;
	
	private Message denial;
	
	public ForgeAction(int cost, QuestPlayer player, Message denialMessage) {
		this.cost = cost;
		this.player = player;
		this.denial = denialMessage;
	}

	@Override
	public void onAction() {
		
		//check their money
		if (player.getMoney() >= cost) {
			//they have enough money
			
			//play smith sound, take money,
			//search inventory and find all equipment (:S) and repair it
			//and display a title
			
			
			if (!player.getPlayer().isOnline()) {
				System.out.println("Very bad ForgeAction error!!!!!!!!!!!!!");
				return;
			}
			
			
			
			Player p = player.getPlayer().getPlayer();
			
			Inventory inv = p.getInventory();
			ListIterator<ItemStack> items = inv.iterator();
			
			ItemStack item;
			int count = 0;
			
			while (items.hasNext()) {
				item = items.next();
				if (item == null) {
					continue;
				}
				if (!item.getType().equals(Material.AIR))
				if (Repairable.isRepairable(item.getType()))
				if (item.getDurability() > 0) {
					item.setDurability((short) 0);
					count++;
				}
			}
			
			//also run check over their equipment
			for (ItemStack it : p.getEquipment().getArmorContents()) {
				if (it == null) {
					continue;
				}
				if (!it.getType().equals(Material.AIR))
				if (Repairable.isRepairable(it.getType()))
				if (it.getDurability() > 0) {
					it.setDurability((short) 0);
					count++;
				}
			}
			
			//make sure they had items to repair
			if (count == 0) {
				//no items were repaired!
				ChatMenu whoops = new SimpleChatMenu(
						new FancyMessage("Actually, you don't seem to have any equipment"
								+ " in need of repair!"));
				whoops.show(p);
				return;
			}
			
			player.addMoney(-cost);
			
			p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 0);
			
			(new TitleObject(ChatColor.GREEN + "Forge",
					ChatColor.BLUE + "" + count + " item(s) have been repaired"))
			.setFadeIn(20).setFadeOut(20).setStay(40).send(p);
		
			
			
		} else {
			//not enough money
			//show them a menu, sorrow
						
			ChatMenu menu = new SimpleChatMenu(denial.getFormattedMessage());
			
			menu.show(player.getPlayer().getPlayer());
		}
		
	}

}
