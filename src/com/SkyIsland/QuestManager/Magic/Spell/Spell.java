package com.SkyIsland.QuestManager.Magic.Spell;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import com.SkyIsland.QuestManager.Magic.MagicUser;
import com.SkyIsland.QuestManager.Magic.Spell.Effect.SpellEffect;
import com.SkyIsland.QuestManager.Player.PlayerOptions;
import com.SkyIsland.QuestManager.Player.QuestPlayer;

public abstract class Spell implements ConfigurationSerializable {
	
	public static final String failMessage = ChatColor.RED + "You failed to cast the spell!" + ChatColor.RESET;
	
	private int cost;
	
	private String name;
	
	private String description;
	
	/**
	 * How hard is this spell to cast?
	 */
	private int difficulty;
	
	private List<SpellEffect> spellEffects;
	
	protected Spell(int cost, int difficulty, String name, String description) {
		this.cost = cost;
		this.difficulty = difficulty;
		this.name = name;
		this.description = description;
		this.spellEffects = new LinkedList<SpellEffect>();
	}

	public int getCost() {
		return cost;
	}
	
	public int getDifficulty() {
		return difficulty;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}
	
	public void addSpellEffect(SpellEffect effect) {
		this.spellEffects.add(effect);
	}
	
	public List<SpellEffect> getSpellEffects() {
		return spellEffects;
	}
	
	protected void fail(MagicUser caster) {
		if (caster instanceof QuestPlayer) {
			QuestPlayer player = (QuestPlayer) caster;
			if (player.getPlayer().isOnline()) {
				Player p = player.getPlayer().getPlayer();
				if (player.getOptions().getOption(PlayerOptions.Key.CHAT_COMBAT_RESULT))
					p.sendMessage(Spell.failMessage);
				p.getWorld().spigot().playEffect(p.getEyeLocation(), Effect.SMOKE);
				p.getWorld().playSound(p.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1.0f, 2.0f);
				p.getWorld().playSound(p.getLocation(), Sound.BLOCK_WATERLILY_PLACE, 1.0f, 0.5f);
			}
		}
	}
	
	public abstract void cast(MagicUser caster);
	
}
