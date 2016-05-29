package com.SkyIsland.QuestManager.Magic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import com.SkyIsland.QuestManager.Magic.Spell.Effect.ImbuementEffect;

/**
 * Holds the effects of an imbuement and their potencies together for easier use everywhere
 * @author Skyler
 *
 */
public class ImbuementSet implements ConfigurationSerializable {

	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(ImbuementSet.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(ImbuementSet.class);
	}
	

	private enum aliases {
		DEFAULT(ImbuementSet.class.getName()),
		SHORT("ImbuementSet");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	private Map<ImbuementEffect, Double> effects;
	
	public ImbuementSet(Map<ImbuementEffect, Double> effects) {
		this.effects = effects;
	}
	
	public Set<ImbuementEffect> getEffects() {
		return effects.keySet();
	}
	
	public Map<ImbuementEffect, Double> getEffectMap() {
		return effects;
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		int count = 1;
		Map<String, Object> submap;
		for (Map.Entry<ImbuementEffect, Double> entry : effects.entrySet()) {
			submap = new HashMap<>();
			submap.put("effect", entry.getKey());
			submap.put("potency", entry.getValue());
			
			map.put(count + "", submap);
		}
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static ImbuementSet valueOf(Map<String, Object> map) {
		ImbuementSet set = new ImbuementSet(new HashMap<>());
		
		Map<String, Object> submap;
		for (String key : map.keySet()) {
			if (key.startsWith("==")) {
				continue;
			}
			
			submap = (Map<String, Object>) map.get(key);
			ImbuementEffect ef = (ImbuementEffect) submap.get("effect");
			double potency = (double) submap.get("potency");
			
			set.effects.put(ef, potency);
		}
		
		return set;
	}
	
}
