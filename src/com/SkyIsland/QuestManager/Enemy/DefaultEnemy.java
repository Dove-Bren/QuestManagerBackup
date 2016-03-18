package com.SkyIsland.QuestManager.Enemy;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.EntityType;

/**
 * Default enemy type for default mobs in minecraft.<br />
 * Wrapper for QM enemies
 * @author Skyler
 *
 */
public class DefaultEnemy extends Enemy {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(DefaultEnemy.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(DefaultEnemy.class);
	}
	

	private enum aliases {
		DEFAULT(DefaultEnemy.class.getName()),
		SIMPLE("DefaultEnemy");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	public DefaultEnemy(String name, EntityType type) {
		super(name, type);
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		map.put("type", type.name());
		map.put("name", name);
		
		return map;
	}
	
	public static DefaultEnemy valueOf(Map<String, Object> map) {
		
		String type = (String) map.get("type");
		EntityType et;
		try {
			et = EntityType.valueOf(type.toUpperCase());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Invalid entity type: " + type + "! Defaulting to Zombie!");
			et = EntityType.ZOMBIE;
		}
		
		String name = (String) map.get("name");
		
		return new DefaultEnemy(name, et);
	}
	
}
