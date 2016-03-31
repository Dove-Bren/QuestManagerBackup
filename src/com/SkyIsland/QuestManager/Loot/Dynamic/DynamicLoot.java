package com.SkyIsland.QuestManager.Loot.Dynamic;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Loot.Loot;

/**
 * Loot class which holds a dynamic item rather than a static one. In other words, specifics about the
 * item are generated fresh each time (like enchantments, name, etc)
 * @author Skyler
 *
 */
public class DynamicLoot extends Loot {
	
	private static Map<String, DynamicGenerator> generators = new HashMap<>();
	
	/**
	 * Adds the provided generator to the table of available generators, used when pieces of loot attempt
	 * to specify what method of selecting various attributes.<br />
	 * This method is intended to allow plugins to add their own generators and have the loot use it. If you
	 * are writing a plugin for QM, you are <i>encouraged</i> to use this method.
	 * @param configkey The key specified in the loot's config
	 * @param generator The generator to use when given the above key
	 */
	public static void registerGenerator(String configkey, DynamicGenerator generator) {
		generators.put(configkey, generator);
	}
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(DynamicLoot.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(DynamicLoot.class);
	}
	

	private enum aliases {
		UPPER("DYNAMICLOOT"),
		LOWER("dynamicloot"),
		FORMAL("DynamicLoot"),
		DEFAULT(DynamicLoot.class.getName());
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		map.put("weight", getWeight());
		map.put("type", type.name());
		String gen = null;
		for (String key : map.keySet()) {
			if (generators.get(key).equals(generator)) {
				gen = key;
				break;
			}
		}
		if (gen == null) {
			gen = "default";
		}
		
		map.put("generator", gen);
		
		return map;
	}
	
	public static DynamicLoot valueOf(Map<String, Object> map) {
		if (map == null) {
			return null;
		}
		
		double weight = defaultWeight;
		
		if (map.containsKey("weight")) {
			weight = (Double) map.get("weight");
		}
		
		if (generators == null || generators.isEmpty()) {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("EXTREME: no generators are loaded for "
					+ "dynamic loot generation!");
			return null;
		}
		
		DynamicGenerator gen = null;
		if (map.containsKey("generator")) {
			//either itemstack or material type. We'll take both
			String gName = (String) map.get("item");
			gen = generators.get(gName);
			
			if (gen == null) {
				QuestManagerPlugin.questManagerPlugin.getLogger().warning("Unable to find generator key in dynamicloot. "
						+ "Defaulting to " + generators.values().iterator().next());
				gen = generators.values().iterator().next();
			}
			
		} else {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Unable to find generator key in dynamicloot. "
					+ "Defaulting to " + generators.values().iterator().next());
			gen = generators.values().iterator().next();
		}
		
		Material type = null;
		if (map.containsKey("type")) {
			//either itemstack or material type. We'll take both
			String tName = (String) map.get("type");
			try {
				type = Material.matchMaterial(tName);
			} catch (Exception e) {
				QuestManagerPlugin.questManagerPlugin.getLogger().warning("Unable to match material to "
						+ tName + ". Defaulting to " + defaultMaterial.name());
				type = defaultMaterial;
			}
		} else {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Unable to find type key in dynamicloot. "
					+ "Defaulting to " + defaultMaterial.name());
			type = defaultMaterial;
		}
		
		return new DynamicLoot(gen, type, weight);
		
		
	}
	
	private DynamicGenerator generator;
	
	private Material type;
	
	public DynamicLoot(DynamicGenerator generator, Material type, double weight) {
		super(null, weight);
		this.type = type;
		this.generator = generator;
	}
	
	@Override
	public ItemStack getItem() {
		return generator.generate(type);
	}
}
