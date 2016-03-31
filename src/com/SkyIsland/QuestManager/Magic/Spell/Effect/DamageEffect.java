package com.SkyIsland.QuestManager.Magic.Spell.Effect;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.SkyIsland.QuestManager.Magic.MagicUser;

public class DamageEffect extends SpellEffect {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(DamageEffect.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(DamageEffect.class);
	}
	

	private enum aliases {
		DEFAULT(DamageEffect.class.getName()),
		LONGI("SpellDamage"),
		LONG("DamageSpell"),
		SHORT("SDamage");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	public static DamageEffect valueOf(Map<String, Object> map) {
		return new DamageEffect((double) map.get("damage"));
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("damage", damage);
		
		return map;
	}
	
	private double damage;
	
	public DamageEffect(double damage) {
		this.damage = damage;
	}
	
	@Override
	public void apply(Entity e, MagicUser cause) {
		if (e instanceof LivingEntity) {
			LivingEntity targ = (LivingEntity) e;
			targ.damage(damage, cause.getEntity());
		}
	}
	
	@Override
	public void apply(Location loc, MagicUser cause) {
		//can't damage a location
		//do nothing 
		;
	}
	
	
	
}
