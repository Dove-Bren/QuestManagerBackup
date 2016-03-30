package com.SkyIsland.QuestManager.Configuration.Utils;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AttributedItemStack_INVALID {
	
	public enum Key {
		ATTACKDAMAGE(Attribute.GENERIC_ATTACK_DAMAGE),
		MAXHEALTH(Attribute.GENERIC_MAX_HEALTH),
		FOLLOWRANGE(Attribute.GENERIC_FOLLOW_RANGE),
		MOVEMENTSPEED(Attribute.GENERIC_MOVEMENT_SPEED),
		KNOCKBACKRESISTANCE(Attribute.GENERIC_KNOCKBACK_RESISTANCE),
		ATTACKSPEED(Attribute.GENERIC_ATTACK_SPEED),
		LUCK(Attribute.GENERIC_LUCK),
		ARMOR(Attribute.GENERIC_ARMOR);
		
		Attribute attribute;
		
		private Key(Attribute attribute) {
			this.attribute = attribute;
		}
		
		public Attribute getAttribute() {
			return attribute;
		}
	}
	
	public static ItemStack deserialize(Map<String,Object> args) {
		ItemStack base = ItemStack.deserialize(args);
		
		int i;
		for (Key key : Key.values()) {
			;
		}
		
		return null;
	}
	
	private Material type;
	
	private byte data;
	
	private int amount;
	
	//Attribute Storage
	
	/**
	 * Creates a blank AttributedItemSTack from the given base.<br />
	 * Only the material type, data, and amount, name and lore are carried over.<br />
	 * Additional meta information (like dye color, etc!) are <b>not carried over</b>.
	 * @param base
	 */
	public AttributedItemStack_INVALID(ItemStack base) {
		
	}
	
	public void addToInventory(Inventory inv) {
		
	}
	
}
