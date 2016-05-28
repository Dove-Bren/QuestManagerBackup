package com.SkyIsland.QuestManager.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import com.SkyIsland.QuestManager.QuestManagerPlugin;

/**
 * Stores player options for easier access and modification
 * @author Skyler
 * @see {@link QuestPlayer}
 */
public class PlayerOptions implements ConfigurationSerializable {
	
	public enum Key {
		CHAT_COMBAT_DAMAGE("chat.combat.damage"),
		CHAT_COMBAT_RESULT("chat.combat.result"),
		CHAT_PET_DISMISSAL("chat.summon.dismissal");
		
		private String key;
		
		private Boolean def;
		
		private Key(String key, Boolean def) {
			this.key = key;
			this.def = def;
		}
		
		private Key(String key) {
			this(key, true);
		}
		
		public String getKey() {
			return key;
		}
		
		@Override
		public String toString() {
			return key;
		}
		
		public Boolean getDefault() {
			return def;
		}
		
	}
	
	private Map<Key, Boolean> opts;
	
	protected PlayerOptions() {
		opts = new HashMap<>();
		
		for (Key key : Key.values()) {
			opts.put(key, key.getDefault());
		}
	}
	
	/**
	 * Returns the currently registered option for the provided key.
	 * @param key
	 * @return True or false, depending on the users settings
	 */
	public boolean getOption(Key key) {
		return opts.get(key);
	}
	
	/**
	 * Sets the specified option to be set to <i>value</i>.
	 * @param key The option to set
	 * @param value The value to set
	 * @return The old value in the provided option
	 */
	public boolean setOption(Key key, boolean value) {
		return opts.put(key,  value);
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new TreeMap<String, Object>();
		
		for (Key key : Key.values()) {
			map.put(key.getKey(), opts.get(key));
		}
		
		return map;		
	}
	
	public static PlayerOptions valueOf(Map<String, Object> map) {
		PlayerOptions po = new PlayerOptions();
		
		for (Key key : Key.values()) {
			if (map.containsKey(key.getKey())) {
				Object o = map.get(key.getKey());
				
				if (!(o instanceof Boolean)) {
					QuestManagerPlugin.questManagerPlugin.getLogger().info("Wrong data type for player"
							+ " options: " + o);
					continue;
				}
				
				po.setOption(key, (boolean) map.get(key.getKey()));
			}
		}
		
		return po;
	}
	
}
