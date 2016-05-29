package com.SkyIsland.QuestManager.Magic;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Magic.Spell.Effect.ImbuementEffect;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.Event.CombatEvent;

/**
 * An active imbuement. Watches for events, takes action, fights crime, you name it! 
 * @author Skyler
 * @see {@link com.SkyIsland.QuestManager.Player.QuestPlayer QuestPlayer}
 * @see {@link ImbuementHandler}
 */
public class Imbuement implements Listener {
	
	public static final Sound defaultSlashSound = Sound.BLOCK_FENCE_GATE_CLOSE;
	
	private List<ImbuementEffect> effects;
	
	private QuestPlayer player;
	
	private Sound sound;
	
	private double cost;
	
	public Imbuement(QuestPlayer player, List<ImbuementEffect> effects, double cost, Sound hitSound) {
		if (effects == null || player == null || effects.isEmpty()) {
			return;
		}
		
		this.player = player;
		this.effects = effects;
		this.sound = hitSound;
		this.cost = cost;
		
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	public Imbuement(QuestPlayer player, List<ImbuementEffect> effects, double cost) {
		this(player, effects, cost, defaultSlashSound);
	}
	
	/**
	 * Cancels this imbuement. It will no longer watch for events and is ready to be nulled and freed
	 */
	public void cancel() {
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerCombat(CombatEvent e) {
		if (e.isMiss()) {
			return;
		}
		
		if (!player.getPlayer().isOnline()) {
			return;
		}
		
		if (!e.getPlayer().equals(player)) {
			return;
		}
		
		if (!player.getPlayer().getPlayer().getGameMode().equals(GameMode.CREATIVE) 
				&& player.getMP() < cost) {
			player.getPlayer().getPlayer().playSound(player.getPlayer().getPlayer().getLocation(), Sound.BLOCK_WATERLILY_PLACE, 1.0f, 0.5f);
			return;
		}
		
		player.addMP(-cost);
		
		
		for (ImbuementEffect effect : effects) {
			effect.apply(e.getTarget(), e.getPlayer());
		}
		
		player.getPlayer().getPlayer().getWorld().playSound(e.getTarget().getLocation(), sound, 1, 1);
	}
}
