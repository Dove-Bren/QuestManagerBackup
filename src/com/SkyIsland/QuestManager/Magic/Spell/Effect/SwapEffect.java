package com.SkyIsland.QuestManager.Magic.Spell.Effect;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;

import com.SkyIsland.QuestManager.Magic.MagicUser;

/**
 * Swaps location of the caster and an entity target. Does nothing on contact with a solid. 
 * @author Skyler
 *
 */
public class SwapEffect extends SpellEffect {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(SwapEffect.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(SwapEffect.class);
	}
	

	private enum aliases {
		DEFAULT(SwapEffect.class.getName()),
		LONGI("SpellTeleport"),
		LONG("TeleportSpell"),
		SHORT("STeleport");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	public static SwapEffect valueOf(Map<String, Object> map) {
		return new SwapEffect();
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		return map;
	}
	
	
	public SwapEffect() {
	}
	
	@Override
	public void apply(Entity e, MagicUser cause) {
		Location tmp = e.getLocation().clone();
		e.teleport(cause.getEntity());
		cause.getEntity().teleport(tmp);
	}
	
	@Override
	public void apply(Location loc, MagicUser cause) {
		; //do nothing
	}
	
	
	
}
