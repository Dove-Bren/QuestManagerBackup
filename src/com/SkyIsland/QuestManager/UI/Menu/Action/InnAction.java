package com.SkyIsland.QuestManager.UI.Menu.Action;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.UI.ChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.SimpleChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.Message.Message;

import io.puharesource.mc.titlemanager.api.TitleObject;

/**
 * Rests a player, restoring health and hunger
 * @author Skyler
 *
 */
public class InnAction implements MenuAction {

	private int cost;
	
	private QuestPlayer player;
	
	private Message denial;
	
	public InnAction(int cost, QuestPlayer player, Message denialMessage) {
		this.cost = cost;
		this.player = player;
		this.denial = denialMessage;
	}
	
	@Override
	public void onAction() {
		
		//check their money
		if (player.getMoney() >= cost) {
			//they have enough money
			
			//blindness for 3 seconds, title saying you're now rested?
			//don't forget to restore health, hunger
			//and take out some money
			
			if (!player.getPlayer().isOnline()) {
				System.out.println("Very bad InnAction error!!!!!!!!!!!!!");
				return;
			}
			
			player.addMoney(-cost);
			
			Player p = player.getPlayer().getPlayer();
			double amount = p.getMaxHealth() - p.getHealth();
			EntityRegainHealthEvent e = new EntityRegainHealthEvent(p, amount, RegainReason.CUSTOM);
			Bukkit.getPluginManager().callEvent(e);
			if (!e.isCancelled()) {
				p.setHealth(p.getMaxHealth());
			}
			
			player.regenMP(-100);
			
			p.setFoodLevel(20);
			p.setExhaustion(0f);
			p.setSaturation(20f);
			
			p.addPotionEffect(
					new PotionEffect(PotionEffectType.BLINDNESS, 60, 5));


//			(new TitleObject(new FancyMessage("Sweet Dreams").color(ChatColor.GREEN).toJSONString(),
//					new FancyMessage("Your health and hunger are restored").color(ChatColor.BLUE).toJSONString()))
//			.setFadeIn(20).setFadeOut(20).setStay(40).send(p);
			new TitleObject(ChatColor.GREEN + "Sweet Dreams" + ChatColor.RESET,
					ChatColor.BLUE + "Health and hunger have been restored")
					.setFadeIn(20).setFadeOut(20).setStay(40).send(p);
	        
	        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, .5f);
			
		} else {
			//not enough money
			//show them a menu, sorrow
						
			ChatMenu menu = new SimpleChatMenu(denial.getFormattedMessage());
			
			menu.show(player.getPlayer().getPlayer());
		}
		
	}

}
