package com.SkyIsland.QuestManager.Magic.Spell.Effect;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;

import com.SkyIsland.QuestManager.Magic.MagicUser;

/**
 * Manipulates blocks. This effect replaces one type of block with another
 * @author Skyler
 *
 */
public class BlockEffect extends SpellEffect {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(BlockEffect.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(BlockEffect.class);
	}
	

	private enum aliases {
		DEFAULT(BlockEffect.class.getName()),
		LONGI("SpellBlock"),
		LONG("BlockSpell"),
		SHORT("SBlock");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	public static BlockEffect valueOf(Map<String, Object> map) {
		return new BlockEffect(
				Material.valueOf((String) map.get("typefrom")),
				Material.valueOf((String) map.get("typeto"))
				);
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("typefrom", typeFrom.name());
		map.put("typeto", typeTo.name());
		
		return map;
	}
	
	private Material typeFrom, typeTo;
	
	public BlockEffect(Material typeFrom, Material typeTo) {
		this.typeFrom = typeFrom;
		this.typeTo = typeTo;
	}
	
	@Override
	public void apply(Entity e, MagicUser cause) {
		; //do nothing
	}
	
	@Override
	public void apply(Location loc, MagicUser cause) {
		if (loc.getBlock().getType() == typeFrom) {
			loc.getBlock().setType(typeTo);
		}
	}
	
	
	
}
