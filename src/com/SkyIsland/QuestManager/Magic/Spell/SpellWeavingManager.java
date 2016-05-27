package com.SkyIsland.QuestManager.Magic.Spell;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import com.SkyIsland.QuestManager.QuestManagerPlugin;

/**
 * <p>Spell weaving, in the scope of this framework, refers to the use of pylons in specific orders or
 * combinations to trigger different spells. The mechanic behind it involves creation of pylons (which
 * are set up to be done through a {@link com.SkyIsland.QuestManager.Magic.Spell.Effect.CastPylonEffect
 * CastPylonEffect}) and then triggered. Trigger mechanics may differ in implementation, but were provided
 * through the framework through either the {@link com.SkyIsland.QuestManager.Magic.Spell.Effect.TriggerWeaveEffect
 * TriggerWeaveEffect} spell effect or the {@link com.SkyIsland.QuestManager.Player.Utils.SpellWeavingTrigger
 * SpellWeavingTrigger} defined object. Both can be used, or a single one. Both casting implementations are
 * the same.</p>
 * <p>
 * The manager keeps records of recipes and deals with configuration of spells. 
 * </p>
 * 
 * @author Skyler
 * @see {@link SpellWeavingSpell}
 *
 */
public class SpellWeavingManager {

	public static final String badRecipeMessage = ChatColor.YELLOW + "Your woven spell energies failed to create"
			+ " any meaningful effects" + ChatColor.RESET;
	
	private List<SpellWeavingSpell> spells;
	
	private List<SpellWeavingSpell> orderedSpells;
	
	@SuppressWarnings("unchecked")
	public SpellWeavingManager(File configFile) {
		spells = new LinkedList<>();
		orderedSpells = new LinkedList<>();
		
		if (configFile == null || !configFile.exists()) {
			return;
		}
		
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		
		for (SpellWeavingSpell spell : (List<SpellWeavingSpell>) config.getList("spells")) {
			if (spell == null || spell.getSpellRecipe() == null
					|| spell.getSpellRecipe().getComponents().size() < 3) {
				QuestManagerPlugin.questManagerPlugin.getLogger().warning(
						"Unable to register spell-weaving spell " + spell.getName()
						+ " (does it have 3 or more recipe components?)");
				continue;
			}
			
			if (spell.getSpellRecipe().isOrdered()) {
				this.orderedSpells.add(spell);
			} else {
				this.spells.add(spell);
			}
		}
	}
	
	public void save(File outfile) {
		if (outfile == null) {
			return;
		}
		
		YamlConfiguration config = new YamlConfiguration();
		
		List<SpellWeavingSpell> list = new LinkedList<>(spells);
		list.addAll(orderedSpells);
		
		config.set("spells", list);
		
		try {
			config.save(outfile);
		} catch (Exception e) {
			e.printStackTrace();
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Ditch message: [" 
					+ config.saveToString() + "]");
		}
			
	}
	
	/**
	 * Registers the spell to the manager. This allows the spell to be looked up and matches when a user
	 * attempts to cast a spell-weaving spell.
	 * <p>
	 * Because spell weaving spells perform in a defined geometry, there must be at least three runes
	 * per spell recipe. There can be more, but each must have at least three.
	 * </p>
	 * <p>
	 * Once a spell is registered, it should <b>not</b> be altered. While most alterations will not cause
	 * any discrepency, changing whether the spell is ordered or not will cause it to potentially be
	 * evaluated in the wrong order and invalidate the preference promised in {@link #getSpell(List)}.
	 * </p>
	 * @param spell
	 * @return Whether the spell was added or not. False if the spell is null or has less than 3 components.
	 */
	public boolean registerSpell(SpellWeavingSpell spell) {
		if (spell == null || spell.getSpellRecipe() == null
				|| spell.getSpellRecipe().getComponents().size() < 3) {
			return false;
		}
		
		if (spell.getSpellRecipe().isOrdered()) {
			this.orderedSpells.add(spell);
		} else {
			this.spells.add(spell);
		}
		
		return true;
	}
	
	/**
	 * Runs through the registered spells and checks for a match with the given type list. If no registered
	 * spell matches the provided list, null is returned. Spells with ordered recipes are considered
	 * before spells with no ordering.<br />
	 * <b>Note:</b> order is not internally established. Spells are checked in the order they were added,
	 * so typeLists that match multiple recipes will return the matching spell that was registered first
	 * @param typeList
	 * @return
	 */
	public SpellWeavingSpell getSpell(List<String> typeList) {
		//searched for matches in ordered spells first
		
		for (SpellWeavingSpell s : orderedSpells) {
			if (s.getSpellRecipe().matches(typeList)) {
				return s;
			}
		}
		
		for (SpellWeavingSpell s : spells) {
			if (s.getSpellRecipe().matches(typeList)) {
				return s;
			}
		}
		
		return null;
	}
	
}
