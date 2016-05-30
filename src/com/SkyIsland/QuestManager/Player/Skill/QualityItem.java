package com.SkyIsland.QuestManager.Player.Skill;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Item with some sort of given quality
 * @author Skyler
 *
 */
public class QualityItem {

	public static final double normalQuality = 1.0;
	
	private ItemStack item;
	
	private double quality;
	
	public QualityItem(ItemStack item) {
		this(item, normalQuality);
	}
	
	public QualityItem(ItemStack item, double quality) {
		this.item = item;
		this.quality = quality;
	}

	public ItemStack getUnderlyingItem() {
		return item;
	}
	
	/**
	 * Returns a formatted item
	 * @return
	 */
	public ItemStack getItem() {
		if (item == null) {
			return item;
		}

		ItemStack ret = item.clone();
		String line = ChatColor.DARK_GRAY + "Quality: " + ChatColor.GOLD + String.format("%.2f", quality);
		
		ItemMeta meta = ret.getItemMeta();
		List<String> lore;
		if (meta.getLore() != null && !meta.getLore().isEmpty()) {
			lore = new ArrayList<String>(meta.getLore().size());
			lore.add(line);
			lore.addAll(meta.getLore());
		} else {
			lore = new ArrayList<String>(1);
			lore.add(line);
		}
		
		meta.setLore(lore);
		
		ret.setItemMeta(meta);
		
		return ret;
	}

	public void setItem(ItemStack item) {
		this.item = item;
	}

	public double getQuality() {
		return quality;
	}

	public void setQuality(double quality) {
		this.quality = quality;
	}
	
	@Override
	public QualityItem clone() {
		return new QualityItem(item, quality);
	}
	
	public boolean isSimilar(ItemStack item) {
		if (item == null && this.item == null)
			return true;
		
		if (item.getType() == this.item.getType())
		if (item.getData() == this.item.getData()) {
			ItemMeta d1, d2;
			d1 = item.getItemMeta();
			d2 = this.item.getItemMeta();
			if (d1 == null && d2 == null) {
				return true;
			}
			if (d1 == null || d2 == null) {
				return false; //after the and, this is XOR
			}
			
			if (d1.getDisplayName().equals(d2.getDisplayName()))
				return true;
		}
		
		return false;
			
	}
	
	public boolean isSimilar(QualityItem item) {
		if (this.quality == item.quality)
			return isSimilar(item.item);
		
		return false;
	}
	
}
