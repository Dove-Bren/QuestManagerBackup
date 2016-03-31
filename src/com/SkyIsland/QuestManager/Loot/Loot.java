package com.SkyIsland.QuestManager.Loot;

import org.bukkit.inventory.ItemStack;

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
public class Loot {
	
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
