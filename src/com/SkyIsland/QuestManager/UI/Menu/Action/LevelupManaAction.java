package com.SkyIsland.QuestManager.UI.Menu.Action;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.SkyIsland.QuestManager.Effects.ChargeEffect;
import com.SkyIsland.QuestManager.Player.QuestPlayer;

import io.puharesource.mc.titlemanager.api.TitleObject;

/**
 * Levels up a player, awarding them some amount of mana
 * @author Skyler
 *
 */
public class LevelupManaAction implements MenuAction {
	
	private int cost;
	
	private int manaAmount;
	
	private QuestPlayer player;
	
	private static final String denialFame = "You do not have enough fame...";
	
	public LevelupManaAction(QuestPlayer player, int cost, int manaAmount) {
		this.player = player;
		this.cost = cost;
		this.manaAmount = manaAmount;
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
		player.levelUp(0, manaAmount);
		player.addFame(-cost);
		
		p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
		ChargeEffect ef = new ChargeEffect(Effect.WITCH_MAGIC);
		ef.play(p, p.getLocation());
		
		(new TitleObject(ChatColor.GREEN + "Level Up",
				ChatColor.BLUE + "Your maximum mana has been increased"))
		.setFadeIn(20).setFadeOut(20).setStay(40).send(p);
		
	}

}
