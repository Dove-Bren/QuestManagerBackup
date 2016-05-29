package com.SkyIsland.QuestManager.Magic.Spell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;

import com.SkyIsland.QuestManager.Effects.ChargeEffect;
import com.SkyIsland.QuestManager.Effects.QuestEffect;
import com.SkyIsland.QuestManager.Magic.MagicUser;
import com.SkyIsland.QuestManager.Magic.Spell.Effect.SpellEffect;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.Event.MagicCastEvent;

/**
 * Holds on to both the invocation recipe and the result of a spell weaving spell
 * @author Skyler
 *
 */
public class SpellWeavingSpell extends Spell implements ConfigurationSerializable {
	
	public enum SpellTarget {
		ENTITY,
		BLOCK,
		BOTH;
	}

	public static class SpellWeavingRecipe {
		private List<String> components;
		
		private boolean isOrdered;
		
		/**
		 * Creates a recipe out of the given components. If the recipe is ordered, the order
		 * of the list is the order of the components.
		 * @param componenets
		 * @param isOrdered
		 */
		public SpellWeavingRecipe(List<String> componenets, boolean isOrdered) {
			this.components = componenets;
			this.isOrdered = isOrdered;
		}
		
		/**
		 * Makes a new recipe with the given components. The recipe defaults to <i>not ordered</i>
		 * @param components
		 */
		public SpellWeavingRecipe(List<String> components) {
			this(components, false);
		}
		
		/**
		 * Creates new unordered blank recipe. Good for building
		 */
		public SpellWeavingRecipe() {
			this(new LinkedList<String>(), false);
		}
		
		public SpellWeavingRecipe addComponent(String type) {
			this.components.add(type);
			return this;
		}
		
		public void setOrdered(boolean isOrdered) {
			this.isOrdered = isOrdered;
		}
		
		public boolean isOrdered() {
			return isOrdered;
		}
		
		public List<String> getComponents() {
			return components;
		}
		
		public boolean matches(List<String> typeList) {
			if (typeList == null) {
				return false;
			}
			
			if (typeList.isEmpty()) {
				return components.isEmpty(); //empty == empty
			}
			
			if (typeList.size() != components.size()) {
				return false;
			}
			
			if (isOrdered) {
				//simple. Go through both lists, check a[i] == b[i]
				int index = 0;
				while (index < components.size()) {
					if (!components.get(index).equals(typeList.get(index))) {
						return false;
					}
				}
				
				return true;
			} else {
				//harder. Duplicate list, remove as we go through one and check if it's empty at the end
				List<String> copy = new ArrayList<>(components.size());
				for (String key : components) {
					copy.add(key);
				}
				
				for (String key : typeList) {
					if (!copy.remove(key)) {
						return false;
					}
				}
				
				return true;
			}
		}
	}
	
	private SpellWeavingRecipe recipe;
	
	private SpellTarget target;
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(SpellWeavingSpell.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(SpellWeavingSpell.class);
	}
	

	private enum aliases {
		DEFAULT(SpellWeavingSpell.class.getName()),
		LONGI("SpellWeavingSpell"),
		SHORT("WeaveSpell");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	public SpellWeavingSpell(int cost, int difficulty, String name, String description, SpellTarget target) {
		super(cost, difficulty, name, description);
		
		this.recipe = null;
		this.target = target;
	}
	
	public void setSpellRecipe(SpellWeavingRecipe recipe) {
		this.recipe = recipe;
	}
	
	public SpellWeavingRecipe getSpellRecipe() {
		return this.recipe;
	}
	
	public SpellTarget getTargetType() {
		return this.target;
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		map.put("name", this.getName());
		map.put("cost", getCost());
		map.put("description", getDescription());
		map.put("difficulty", getDifficulty());
		map.put("targetType", target.name());
		map.put("recipe.types", recipe.components);
		map.put("recipe.isOrdered", recipe.isOrdered);
		map.put("effects", getSpellEffects());
		
		return map;
	}
	
	@SuppressWarnings("unchecked")
	public static SpellWeavingSpell valueOf(Map<String, Object> map) {
		List<SpellEffect> effects = (List<SpellEffect>) map.get("effects");
		List<String> types = (List<String>) map.get("recipe.types");
		boolean isOrdered = (Boolean) map.get("recipe.isOrdered");
		
		SpellWeavingSpell spell = new SpellWeavingSpell(
				(Integer) map.get("cost"),
				(Integer) map.get("difficulty"),
				(String) map.get("name"),
				(String) map.get("description"),
				SpellTarget.valueOf((String) map.get("targetType"))
				);
		
		spell.getSpellEffects().addAll(effects);
		spell.setSpellRecipe(new SpellWeavingRecipe(types, isOrdered));
				
		return spell;
	}
	
	/**
	 * Casts the spell with all provided entities as targets.<br />
	 * This method handles XP awarding, so it should only be called when blocks will be unaffected. To
	 * instead apply an affect on a set of entities and a set of locations, use 
	 * {@link #castOnAll(MagicUser, Collection, Collection)} instead
	 * @param caster
	 * @param targets
	 */
	public void castOnEntities(MagicUser caster, Collection<Entity> targets) {
		if (caster instanceof QuestPlayer) {
			QuestPlayer player = (QuestPlayer) caster;
			MagicCastEvent event = new MagicCastEvent(player,
									MagicCastEvent.MagicType.SPELLWEAVING,
									this
							);
			Bukkit.getPluginManager().callEvent(event);
			
			if (event.isFail()) {
				fail(caster);
				return;
			}
			
		}
		
		applyEntities(caster, targets);
	}
	
	public void castOnLocations(MagicUser caster, Collection<Location> targetLocations) {
		if (caster instanceof QuestPlayer) {
			QuestPlayer player = (QuestPlayer) caster;
			MagicCastEvent event = new MagicCastEvent(player,
									MagicCastEvent.MagicType.SPELLWEAVING,
									this
							);
			Bukkit.getPluginManager().callEvent(event);
			
			if (event.isFail()) {
				fail(caster);
				return;
			}
			
		}
		
		applyLocations(caster, targetLocations);
	}
	
	public void castOnAll(MagicUser caster, Collection<Entity> targets, Collection<Location> targetLocations) {
		if (caster instanceof QuestPlayer) {
			QuestPlayer player = (QuestPlayer) caster;
			MagicCastEvent event = new MagicCastEvent(player,
									MagicCastEvent.MagicType.SPELLWEAVING,
									this
							);
			Bukkit.getPluginManager().callEvent(event);
			
			if (event.isFail()) {
				fail(caster);
				return;
			}
			
		}
		
		applyEntities(caster, targets);
		applyLocations(caster, targetLocations);
	}
	
	private void applyEntities(MagicUser caster, Collection<Entity> targets) {
				
		System.out.println("casting on " + targets.size() + " entities");
		
		Entity entity = caster.getEntity();
		
		QuestEffect ef = new ChargeEffect(Effect.DRAGON_BREATH);
		ef.play(entity, null);
		
		for (Entity e : targets)
		for (SpellEffect effect : this.getSpellEffects()) {
			effect.apply(e, caster);
		}
	}
	
	private void applyLocations(MagicUser caster, Collection<Location> targetLocations) {
		Entity entity = caster.getEntity();
		
		QuestEffect ef = new ChargeEffect(Effect.DRAGON_BREATH);
		ef.play(entity, null);
		
		for (Location loc : targetLocations)
		for (SpellEffect effect : this.getSpellEffects()) {
			effect.apply(loc, caster);
		}
	}
	
	/**
	 * Does nothing. To cast a spell weaving spell, use
	 * {@link com.SkyIsland.QuestManager.Magic.MagicUser#castSpellWeavingSpell() MagicUser.castSpellWeavingSpell()}.
	 * <p>
	 * This is not a supported action because spell weaving spells are defined by the placement of pylons.
	 * </p>
	 * 
	 */
	@Override
	public void cast(MagicUser caster) {
		
	}
}
