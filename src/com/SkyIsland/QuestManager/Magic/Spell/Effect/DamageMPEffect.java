package com.SkyIsland.QuestManager.Magic.Spell.Effect;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.Utils.YamlWriter;
import com.SkyIsland.QuestManager.Magic.MagicUser;
import com.SkyIsland.QuestManager.Player.PlayerOptions;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.Event.MagicApplyEvent;

public class DamageMPEffect extends ImbuementEffect {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(DamageMPEffect.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(DamageMPEffect.class);
	}
	

	private enum aliases {
		DEFAULT(DamageMPEffect.class.getName()),
		LONGI("SpellDamageMP"),
		LONG("DamageMPSpell"),
		SHORT("SDamageMP");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	public static DamageMPEffect valueOf(Map<String, Object> map) {
		return new DamageMPEffect((double) map.get("damage"));
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("damage", damage);
		
		return map;
	}
	
	private double damage;
	
	public DamageMPEffect(double damage) {
		this.damage = damage;
	}
	
	@Override
	public void apply(Entity e, MagicUser cause) {
		double curDamage = damage;
		if (cause instanceof QuestPlayer) {
			QuestPlayer qp = (QuestPlayer) cause;
			MagicApplyEvent aEvent = new MagicApplyEvent(qp, curDamage);
			Bukkit.getPluginManager().callEvent(aEvent);
			
			curDamage = aEvent.getFinalDamage();
		}
		
		if (e instanceof Player) {
			QuestPlayer qp = QuestManagerPlugin.questManagerPlugin.getPlayerManager()
					.getPlayer((Player) e);
			
			if (qp.getOptions().getOption(PlayerOptions.Key.CHAT_COMBAT_DAMAGE)) {
				
				String msg;
				if (cause instanceof QuestPlayer && ((QuestPlayer) cause).getPlayer().getUniqueId()
						.equals(qp.getPlayer().getUniqueId())) {
					//healed self
					msg = ChatColor.DARK_GRAY + "You lost " + ChatColor.DARK_BLUE + "%.2f"
							+ ChatColor.DARK_GRAY + " mana" + ChatColor.RESET;
				} else {
					String name = cause.getEntity().getCustomName();
					if (name == null) {
						name = cause.getEntity().getType().toString();
					}
					msg = ChatColor.GRAY + cause.getEntity().getCustomName() + ChatColor.DARK_GRAY 
							+ " damaged your mana by " + ChatColor.DARK_BLUE + "%.2f" + ChatColor.DARK_GRAY
							+ " points";
				}
				
				qp.getPlayer().getPlayer().sendMessage(String.format(msg, curDamage));
				
			}
			
			qp.addMP((int) -curDamage);
			//return;
		} else if (e instanceof MagicUser) {
			((MagicUser) e).addMP((int) -curDamage);
		}
		
		if (cause instanceof QuestPlayer) {
			QuestPlayer qp = (QuestPlayer) cause;
			if (qp.getOptions().getOption(PlayerOptions.Key.CHAT_COMBAT_DAMAGE)
				&& qp.getPlayer().isOnline()) {
				Player p = qp.getPlayer().getPlayer();
				
				String msg;
				String name = e.getCustomName();
				if (name == null) {
					name = YamlWriter.toStandardFormat(cause.getEntity().getType().toString());
				}
				msg = ChatColor.DARK_GRAY + "You drained " + ChatColor.DARK_BLUE + "%.2f" 
						+ ChatColor.DARK_GRAY + " mana from" + ChatColor.GRAY + name + ChatColor.RESET;
				
				p.sendMessage(String.format(msg, curDamage));
			}
		}
	}
	
	@Override
	public void apply(Location loc, MagicUser cause) {
		//can't damage a location
		//do nothing 
		;
	}

	@Override
	public ImbuementEffect getCopyAtPotency(double potency) {
		DamageMPEffect effect = new DamageMPEffect(damage * potency);
		effect.setDisplayName(getDisplayName());
		return effect;		
	}	
	
}
