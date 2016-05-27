package com.SkyIsland.QuestManager.Magic.Spell.Effect;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.SkyIsland.QuestManager.Magic.MagicUser;
import com.SkyIsland.QuestManager.Magic.SpellPylon;

/**
 * Creates in the world a pylon ties to the given player of a certain type
 * @author Skyler
 *
 */
public class CastPylonEffect extends SpellEffect {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(CastPylonEffect.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(CastPylonEffect.class);
	}
	

	private enum aliases {
		DEFAULT(CastPylonEffect.class.getName()),
		LONGI("SpellPylon"),
		LONG("PylonSpell"),
		SHORT("SPylon");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	private String type;
	
	private ItemStack icon;
	
	public static CastPylonEffect valueOf(Map<String, Object> map) {
		return new CastPylonEffect((String) map.get("type"), (ItemStack) map.get("icon"));
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		map.put("type", this.type);
		map.put("icon", icon);
		
		return map;
	}
	
	private CastPylonEffect(String name, ItemStack icon) {
		this.type = name;
		this.icon = icon;
	}

	@Override
	public void apply(Entity e, MagicUser cause) {
		//cast it at location of entity?
		SpellPylon pylon = new SpellPylon(type, icon, e.getLocation());
		
		cause.addSpellPylon(pylon);
		
	}

	@Override
	public void apply(Location loc, MagicUser cause) {
		SpellPylon pylon = new SpellPylon(type, icon, loc);
		
		cause.addSpellPylon(pylon);
	}
	
	
	
}
