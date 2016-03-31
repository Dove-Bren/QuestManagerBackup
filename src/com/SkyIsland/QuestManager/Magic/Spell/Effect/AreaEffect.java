package com.SkyIsland.QuestManager.Magic.Spell.Effect;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;

import com.SkyIsland.QuestManager.Magic.MagicUser;

public class AreaEffect extends SpellEffect {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(AreaEffect.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(AreaEffect.class);
	}
	

	private enum aliases {
		DEFAULT(AreaEffect.class.getName()),
		LONGI("SpellAreaOfEffect"),
		LONG("AreaOfEffectSpell"),
		SHORT("SArea");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	public static AreaEffect valueOf(Map<String, Object> map) {
		AreaEffect ret = new AreaEffect((double) map.get("radius"));
		//load effects
		@SuppressWarnings("unchecked")
		List<SpellEffect> effects = (List<SpellEffect>) map.get("effects");
		
		for (SpellEffect effect : effects) {
			ret.addEffect(effect);
		}
		
		return ret;
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("radius", radius);
		map.put("effects", effects);
		
		return map;
	}
	
	private List<SpellEffect> effects;
	
	private double radius;
	
	/**
	 * Makes an empty area of effect shell. It contains no spell effects.
	 * @see #addEffect(SpellEffect)
	 * @param radius
	 */
	public AreaEffect(double radius) {
		this.radius = radius;
		this.effects = new LinkedList<SpellEffect>();
	}
	
	public void addEffect(SpellEffect effect) {
		effects.add(effect);
	}
	
	@Override
	public void apply(Entity e, MagicUser cause) {
		apply(e.getLocation(), cause);
	}
	
	@Override
	public void apply(Location loc, MagicUser cause) {
		
		Collection<Entity> nearby = loc.getWorld().getNearbyEntities(
				loc, radius, radius, radius);
		
		for (Entity near : nearby)
		for (SpellEffect ef : effects) {
			ef.apply(near, cause);
		}

		loc = loc.getBlock().getLocation();
		loc.add(-radius, -radius, -radius);
		for (int i = 0; i < radius * 2; i++)
		for (int j = 0; j < radius * 2; j++)
		for (int k = 0; k < radius * 2; k++) {
			for (SpellEffect ef : effects) {
				ef.apply(loc.getWorld().getBlockAt(
						loc.getBlockX() + i, loc.getBlockY() + j, 
						loc.getBlockZ() + k).getLocation(),
						cause);
			}
		}
	}
	
}
