package com.SkyIsland.QuestManager.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.google.common.collect.Lists;

/**
 * Stores player options for easier access and modification
 * @author Skyler
 * @see {@link QuestPlayer}
 */
public class PlayerOptions implements ConfigurationSerializable {
	
	public enum Key {
		CHAT_COMBAT_DAMAGE("chat.combat.damage", new ItemStack(Material.IRON_SWORD), 
				Lists.newArrayList("Displays damage information in chat,", "like how much damage you did")),
		CHAT_COMBAT_RESULT("chat.combat.result", new ItemStack(Material.BOOK), 
				Lists.newArrayList("Shows spell failures, melee", "misses, etc in chat")),
		CHAT_PET_DISMISSAL("chat.summon.dismissal", new ItemStack(Material.APPLE), 
				Lists.newArrayList("Lets you know when your", "summon has died or expired")),
		SKILL_REVEAL("skill.showAll", new ItemStack(Material.EMPTY_MAP),
				Lists.newArrayList("Displays all skills in the", "skill table regardless of if", 
						"you've experienced the skill yet", "(spoilers)"), false);
		
		private String key;
		
		private Boolean def;
		
		private ItemStack icon;
		
		private List<String> hint;
		
		private Key(String key, ItemStack icon, List<String> hint, Boolean def) {
			this.key = key;
			this.def = def;
			this.icon = icon;
			this.hint = hint;
		}
		
		private Key(String key, ItemStack icon, List<String> hint) {
			this(key, icon, hint, true);
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
		
		public ItemStack getIcon() {
			return icon;
		}
		
		public List<String> getHint() {
			return hint;
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
