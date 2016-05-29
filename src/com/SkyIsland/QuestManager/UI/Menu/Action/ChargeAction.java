package com.SkyIsland.QuestManager.UI.Menu.Action;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Effects.AuraEffect;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Scheduling.Alarm;
import com.SkyIsland.QuestManager.Scheduling.Alarmable;

/**
 * An action that must charge for a while, and then happens
 * @author Skyler
 *
 */
public class ChargeAction implements Listener, Alarmable<Integer> {
	
	public static final String disturbedMessage = ChatColor.RED + "Your charging action was disturbed" + ChatColor.RESET;
	
	public static final String cancelMessage = ChatColor.YELLOW + "You cancelled your action" + ChatColor.RESET;
	
	private static final Effect defaultEffect = Effect.MAGIC_CRIT;
	
	private static final Sound defaultSound = Sound.BLOCK_BREWING_STAND_BREW;
	
	private boolean canMove;
	
	private boolean canGetHit;
	
	private MenuAction action;
	
	private QuestPlayer player;
	
	private AuraEffect effect;
	
	public ChargeAction(MenuAction action, QuestPlayer player, boolean canMove, boolean canGetHit, double chargingTime) {
		this(action, player, defaultEffect, defaultSound, canMove, canGetHit, chargingTime);
	}
	
	public ChargeAction(MenuAction action, QuestPlayer player, Effect effect, Sound sound, boolean canMove, 
			boolean canGetHit, double chargingTime) {
		if (!player.getPlayer().isOnline()) {
			return;
		}
		
		this.action = action;
		this.canMove = canMove;
		this.canGetHit = canGetHit;
		this.player = player;
		this.effect = new AuraEffect(effect);
		this.effect.play(player.getEntity());
		
		player.getEntity().getWorld().playSound(player.getEntity().getLocation(), sound, 1, 1);

		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
		Alarm.getScheduler().schedule(this, 0, chargingTime);
	}
	
	private void stopListening() {
		HandlerList.unregisterAll(this);
	}
	
	private void doneCasting() {
		effect.stop();
		stopListening();
		Alarm.getScheduler().unregister(this);
	}

	@EventHandler
	public void onEntityMove(PlayerMoveEvent e) {
		if (canMove) {
			return;
		}
		
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getWorlds()
				.contains(e.getPlayer().getWorld().getName())) {
			return;
		}
		QuestPlayer qp = QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer(e.getPlayer());
		
		if (!qp.equals(this.player)) {
			return;
		}
		
		//a current charge has moved, and is not allowed to
		e.getPlayer().sendMessage(disturbedMessage);
		doneCasting();
	}
	
	@EventHandler
	public void onEntityHurt(EntityDamageEvent e) {
		if (canGetHit) {
			return;
		}
		
		if (!(e.getEntity() instanceof Player)) {
			return;
		}
		
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getWorlds()
				.contains(e.getEntity().getWorld().getName())) {
			return;
		}
		QuestPlayer qp = QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer((Player) e.getEntity());
		
		if (!qp.equals(this.player)) {
			return;
		}
		
		//a current charge has moved, and is not allowed to
		((Player) e.getEntity()).sendMessage(disturbedMessage);
		doneCasting();
	}
	
	public void alarm(Integer a) {
		doneCasting();
		action.onAction();
	}
}
