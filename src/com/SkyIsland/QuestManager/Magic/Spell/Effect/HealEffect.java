package com.SkyIsland.QuestManager.Magic.Spell.Effect;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

import com.SkyIsland.QuestManager.Magic.MagicUser;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.Event.MagicApplyEvent;

public class HealEffect extends SpellEffect {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(HealEffect.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(HealEffect.class);
	}
	

	private enum aliases {
		DEFAULT(HealEffect.class.getName()),
		LONGI("SpellHeal"),
		LONG("HealSpell"),
		SHORT("SHeal");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	public static HealEffect valueOf(Map<String, Object> map) {
		return new HealEffect((double) map.get("amount"));
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("amount", amount);
		
		return map;
	}
	
	private double amount;
	
	public HealEffect(double amount) {
		this.amount = amount;
	}
	
	@Override
	public void apply(Entity caster, MagicUser cause) {
		if (caster instanceof LivingEntity) {
			LivingEntity e = (LivingEntity) caster;
			
			double curAmount = amount;
			if (cause instanceof QuestPlayer) {
				QuestPlayer qp = (QuestPlayer) cause;
				MagicApplyEvent aEvent = new MagicApplyEvent(qp, -curAmount); //negative cause healing
				Bukkit.getPluginManager().callEvent(aEvent);
				
				curAmount = -aEvent.getFinalDamage(); //negative cause healing
			}
			
			EntityRegainHealthEvent event = new EntityRegainHealthEvent(e, curAmount, RegainReason.MAGIC);
			Bukkit.getPluginManager().callEvent(event);
			
			if (event.isCancelled()) {
				return;
			}
			
			e.setHealth(Math.min(e.getMaxHealth(), 
			e.getHealth() + curAmount));
		}
	}
	
	@Override
	public void apply(Location loc, MagicUser cause) {
		//can't damage a location
		//do nothing 
		;
	}
	
	
	
}
