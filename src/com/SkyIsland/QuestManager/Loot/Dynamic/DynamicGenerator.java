package com.SkyIsland.QuestManager.Loot.Dynamic;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public interface DynamicGenerator {
	
	public ItemStack generate(Material type);
	
}
