package com.SkyIsland.QuestManager.Magic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import com.SkyIsland.QuestManager.Magic.Spell.Effect.ImbuementEffect;

/**
 * Holds the effects of an imbuement and their potencies together for easier use everywhere
 * @author Skyler
 *
 */
public class ImbuementSet implements ConfigurationSerializable {

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
