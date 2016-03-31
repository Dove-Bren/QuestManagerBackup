package com.SkyIsland.QuestManager.Magic.Spell.Effect;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

import com.SkyIsland.QuestManager.Magic.MagicUser;

/**
 * Wrapper class for potion effects put into spells
 * @author Skyler
 *
 */
public class StatusEffect extends SpellEffect {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(StatusEffect.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(StatusEffect.class);
	}
	

	private enum aliases {
		DEFAULT(StatusEffect.class.getName()),
		LONGI("SpellStatus"),
		LONG("StatusSpell"),
		SHORT("SStatus");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	public static StatusEffect valueOf(Map<String, Object> map) {
		return new StatusEffect((PotionEffect) map.get("effect"));
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("effect", effect);
		
		return map;
	}
	
	private PotionEffect effect;
	
	public StatusEffect(PotionEffect effect) {
		this.effect = effect;
	}
	
	@Override
	public void apply(Entity e, MagicUser cause) {
		if (e instanceof LivingEntity) {
			LivingEntity targ = (LivingEntity) e;
			effect.apply(targ);
		}
	}
	
	@Override
	public void apply(Location loc, MagicUser cause) {
		//can't damage a location
		//do nothing 
		;
	}
	
	
	
}
