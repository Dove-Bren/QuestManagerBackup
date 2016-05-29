package com.SkyIsland.QuestManager.Magic.Spell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Effects.ChargeEffect;
import com.SkyIsland.QuestManager.Effects.QuestEffect;
import com.SkyIsland.QuestManager.Magic.MagicUser;
import com.SkyIsland.QuestManager.Magic.Spell.Effect.SpellEffect;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.Event.MagicCastEvent;

public class SimpleSelfSpell extends SelfSpell {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(SimpleSelfSpell.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(SimpleSelfSpell.class);
	}
	

	private enum aliases {
		DEFAULT(SimpleSelfSpell.class.getName()),
		LONG("SimpleSelfSpell"),
		SHORT("SSelfSpell");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}

	public static SimpleSelfSpell valueOf(Map<String, Object> map) {
		if (!map.containsKey("cost") || !map.containsKey("name") || !map.containsKey("description")
				|| !map.containsKey("effects") || !map.containsKey("difficulty")) {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning(
					"Unable to load spell " 
						+ (map.containsKey("name") ? (String) map.get("name") : "")
						+ ": Missing some keys!"
					);
			return null;
		}
		
		SimpleSelfSpell spell = new SimpleSelfSpell(
				(int) map.get("cost"),
				(int) map.get("difficulty"),
				(String) map.get("name"),
				(String) map.get("description")
				);
		
		@SuppressWarnings("unchecked")
		List<SpellEffect> effects = (List<SpellEffect>) map.get("effects");
		for (SpellEffect effect : effects) {
			spell.addSpellEffect(effect);
		}
		
		if (map.containsKey("casteffect")) {
			spell.setCastEffect(Effect.valueOf((String) map.get("casteffect")));
		}
		if (map.containsKey("castsound")) {
			spell.setCastSound(Sound.valueOf((String) map.get("castsound")));
		}
		
		return spell;
	}
	
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("cost", getCost());
		map.put("name", getName());
		map.put("description", getDescription());
		
		map.put("effects", getSpellEffects());
		
		if (castEffect != null) {
			map.put("casteffect", castEffect.name());
		}
		if (castSound != null) {
			map.put("castsound", castSound.name());
		}
		
		return map;
	}
	
	protected Effect castEffect;
	
	protected Sound castSound;
	
	/**
	 * Creates a simple spell made to be cast on the self.<br />
	 * This spell will have no effects until added using 
	 * {@link #addSpellEffect(com.SkyIsland.QuestManager.Magic.Spell.Effect.SpellEffect)}
	 * @param cost
	 * @param name
	 * @param description
	 */
	public SimpleSelfSpell(int cost, int difficulty, String name, String description) {
		super(cost, difficulty, name, description);
		castEffect = null;
		castSound = null;
	}
	
	public void setCastEffect(Effect castEffect) {
		this.castEffect = castEffect;
	}

	public void setCastSound(Sound castSound) {
		this.castSound = castSound;
	}

	@Override
	public void cast(MagicUser caster) {
		
		if (caster instanceof QuestPlayer) {
			QuestPlayer player = (QuestPlayer) caster;
			MagicCastEvent event = new MagicCastEvent(player,
									MagicCastEvent.MagicType.MAGERY,
									this
							);
			Bukkit.getPluginManager().callEvent(event);
			
			if (event.isFail()) {
				fail(caster);
				return;
			}
			
		}
		
		Entity e = caster.getEntity();
		
		QuestEffect ef = new ChargeEffect(Effect.WITCH_MAGIC);
		ef.play(e, null);
			
		for (SpellEffect effect : getSpellEffects()) {
			effect.apply(e, caster);
		}
		
		if (castEffect != null) {
			e.getWorld().playEffect(e.getLocation().clone().add(0,1.5,0), castEffect, 0);
		}
		if (castSound != null) {
			e.getWorld().playSound(e.getLocation().clone().add(0,1.5,0), castSound, 1, 1);
		}
	}
	
}
