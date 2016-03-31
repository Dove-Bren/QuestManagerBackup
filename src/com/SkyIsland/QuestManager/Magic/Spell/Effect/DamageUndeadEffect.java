package com.SkyIsland.QuestManager.Magic.Spell.Effect;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.SkyIsland.QuestManager.Magic.MagicUser;

public class DamageUndeadEffect extends SpellEffect {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(DamageUndeadEffect.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(DamageUndeadEffect.class);
	}
	

	private enum aliases {
		DEFAULT(DamageUndeadEffect.class.getName()),
		LONGI("SpellDamageUndead"),
		LONG("DamageUndeadSpell"),
		SHORT("SDamageUndead");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	private static boolean isUndead(EntityType type) {
		switch (type) {
		case SKELETON:
		case ZOMBIE:
		case PIG_ZOMBIE:
			return true;
		default:
			return false;
		}
	}
	
	public static DamageUndeadEffect valueOf(Map<String, Object> map) {
		return new DamageUndeadEffect((double) map.get("damage"));
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("damage", damage);
		
		return map;
	}
	
	private double damage;
	
	public DamageUndeadEffect(double damage) {
		this.damage = damage;
	}
	
	@Override
	public void apply(Entity e, MagicUser cause) {
		if (e instanceof LivingEntity)
		if (isUndead(e.getType())) {
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
