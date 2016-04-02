package com.SkyIsland.QuestManager.UI.Menu.Action;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.SkyIsland.QuestManager.Effects.ChargeEffect;
import com.SkyIsland.QuestManager.Player.QuestPlayer;

import io.puharesource.mc.titlemanager.api.TitleObject;

/**
 * Levels up a player, awarding them some amount of health
 * @author Skyler
 *
 */
public class LevelupHealthAction implements MenuAction {
	
	private int cost;
	
	private int healthAmount;
	
	private QuestPlayer player;
	
	private static final String denialFame = "You do not have enough fame...";
	
	public LevelupHealthAction(QuestPlayer player, int cost, int healthAmount) {
		this.player = player;
		this.cost = cost;
		this.healthAmount = healthAmount;
	}
	
	@Override
	public void onAction() {
		//check if they have enough fame
		if (!player.getPlayer().isOnline()) {
			return;
		}
		
		Player p = player.getPlayer().getPlayer();
		
		if (player.getFame() < cost) {
			p.sendMessage(denialFame);
			return;
		}
		
		//level them up
		player.levelUp(healthAmount, 0);
		player.addFame(-cost);
		
		p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
		ChargeEffect ef = new ChargeEffect(Effect.HAPPY_VILLAGER);
		ef.play(p, p.getLocation());
		
		(new TitleObject(ChatColor.GREEN + "Level Up",
				ChatColor.BLUE + "Your maximum health has been increased"))
		.setFadeIn(20).setFadeOut(20).setStay(40).send(p);
	
		
		
	}

}
