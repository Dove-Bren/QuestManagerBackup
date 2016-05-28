package com.SkyIsland.QuestManager.UI.Menu.Inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.UI.Menu.Action.MenuAction;

import net.md_5.bungee.api.ChatColor;

/**
 * An item used in an inventory menu.<br />
 * This is composed of an actual item stack, a display item type, and the cost & fame requirement
 * of the item.
 * @author Skyler
 *
 */
public class BasicInventoryItem extends InventoryItem {
	
	private ItemStack item;
	
	//private List<String> tooltip;
	
	private MenuAction action;
	
	/**
	 * Makes a simple wrapper item that runs the provided action when clicked. Tooltip can be null.
	 * @param icon
	 * @param tooltip The tooltip to display. If null, no tooltip is displayed
	 * @param action
	 */
	public BasicInventoryItem(ItemStack icon, List<String> tooltip, MenuAction action) {
		super(icon);
		
		//set up tooltip information
		ItemMeta meta = icon.getItemMeta();
		if (tooltip == null) {
			meta.setDisplayName("");
		} else {
			meta.setDisplayName(ChatColor.GRAY + tooltip.get(0) + ChatColor.RESET);
		}
		
		if (tooltip.size() > 1) {
			List<String> lore = new ArrayList<>(tooltip.size() - 1);
			for (int i = 1; i < tooltip.size(); i++) {
				lore.add(ChatColor.GRAY + tooltip.get(i));
			}
			meta.setLore(lore);
		}
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_ENCHANTS,
				ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);
		
		icon.setItemMeta(meta);
		
		
		this.item = icon;
		this.action = action;
	}
	
	/**
	 * Returns the item that should be used to display the item to the given player.<br />
	 * This method formats the lore, etc to display correctly (and with correct colors) to
	 * the provided player given their fame and money.<br /><br />
	 * 
	 * If the passed player is null, the item without lore is returned.
	 * @param player
	 * @return
	 */
	@Override
	public ItemStack getDisplay(QuestPlayer player) {
		return item;
	}
	
	@Override
	public MenuAction getAction(QuestPlayer player) {
		return action;
	}	
	
}
