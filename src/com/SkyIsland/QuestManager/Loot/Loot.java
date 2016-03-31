package com.SkyIsland.QuestManager.Loot;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;

import com.SkyIsland.QuestManager.QuestManagerPlugin;

/**
 * Holds information about a possible piece of loot.
 * Information stored included what the loot item will be, and the chance it'll be obtained.
 * <p>
 * Implementations of loot generation may vary, but the weight of the loot should universally be held
 * as the relative ratio of how often a piece of loot will show up, compared to a piece of loot with weight 1.
 * Specific drop chances per loot generation depends on how many pieces of loot are being generated, and how
 * big the pool is.
 * </p>
 * <p>
 * To better illustrate weight, consider a loot pool with only two possible items with weights <i>1.0</i> and 
 * <i>2.0</i>. For every attempt to generate a piece of loot, the object with weight <i>2.0</i> should have
 * <b>double</b> the chance of being selected compared to the other object. In this situation, the piece of 
 * loot would have a 66.6% chance of being drawn. 
 * </p>
 * <p>
 * The exact probability per loot generation is given as <br />
 * &nbsp;&nbsp;&nbsp;&nbsp;(<i>weight</i>) / (<i>Pool weight total</i>)
 * </p>
 * @author Skyler
 *
 */
public class Loot implements ConfigurationSerializable {
	
	public static final Material defaultMaterial = Material.STONE;
	
	public static final double defaultWeight = 1.0;
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(Loot.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(Loot.class);
	}
	

	private enum aliases {
		UPPER("LOOT"),
		LOWER("loot"),
		FORMAL("Loot"),
		DEFAULT(Loot.class.getName());
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		map.put("weight", weight);
		map.put("item", item);
		
		return map;
	}
	
	public static Loot valueOf(Map<String, Object> map) {
		if (map == null) {
			return null;
		}
		
		double weight = defaultWeight;
		ItemStack item;
		
		if (map.containsKey("weight")) {
			weight = (Double) map.get("weight");
		}
		
		if (map.containsKey("item")) {
			//either itemstack or material type. We'll take both
			Object o = map.get("item");
			if (o instanceof String) {
				Material type;
				try {
					type = Material.matchMaterial((String) o);
				} catch (Exception e) {
					QuestManagerPlugin.questManagerPlugin.getLogger().warning("Unable to match material to "
							+ (String) o + ". Defaulting to " + defaultMaterial.name());
					type = defaultMaterial;
				}
				
				item = new ItemStack(type);
			} else {
				try {
					item = (ItemStack) o;
				} catch (Exception e) {
					e.printStackTrace();
					QuestManagerPlugin.questManagerPlugin.getLogger().warning("Unable to get item from the "
							+ "provided object: "+ o.toString() + ". Defaulting to " + defaultMaterial.name());
					item = new ItemStack(defaultMaterial);
				}
			}
		} else {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Unable to find item key in loot. "
					+ "Defaulting to " + defaultMaterial.name());
			item = new ItemStack(defaultMaterial);
		}
		
		return new Loot(item, weight);
		
		
	}
	
	private double weight;
	
	private ItemStack item;
	
	/**
	 * Creates a piece of loot with the given item and weight.<br />
	 * If weight is <= 0, 1.0 is taken as weight instead. 
	 * @param item
	 * @param weight
	 */
	public Loot(ItemStack item, double weight) {
		this.item = item;
		this.weight = weight;
		
		if (weight <= 0) {
			this.weight = 1.0;
		}
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public ItemStack getItem() {
		return item;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}

}
