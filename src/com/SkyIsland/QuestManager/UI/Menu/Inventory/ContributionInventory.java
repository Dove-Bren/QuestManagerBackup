package com.SkyIsland.QuestManager.UI.Menu.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.SkyIsland.QuestManager.Player.QuestPlayer;

/**
 * Gui used to 'receive items' from a player's inventory. Allows specification of a filter
 * for runtime awesomeness
 * @author Skyler
 *
 */
public class ContributionInventory extends ReturnGuiInventory {
	
//	private static final class ContributionAction implements MenuAction {
//
//		private ContributionInventory gui;
//		
//		private ItemStack item;
//		
//		private ContributionAction(ContributionInventory gui) {
//			this.gui = gui;
//		}
//		
//		@Override
//		public void onAction() {
//			int openpos = -1;
//			for (int pos : gui.items.keySet()) {
//				if (gui.items.get(pos) == null || gui.items.get(pos).getType() == Material.AIR) {
//					openpos = pos;
//					break;
//				}
//			}
//			
//			if (openpos == -1) {
//				//no space
//				return; //do nothing, already cancelled
//			}
//			
//			
//		}
//		
//	}
	
	private Map<Integer, ItemStack> items;
	
	private ItemFilter filter;
	
	private int maxItems;
	
	private String invName;
	
	private Player player;
	
	private Inventory inv;
	
	public ContributionInventory(Player player, int maxItems) {
		this(player, maxItems, null, "Give Items");
	}
	
	public ContributionInventory(Player player, int maxItems, ItemFilter filter) {
		this(player, maxItems, filter, "Give Items");
	}
	
	public ContributionInventory(Player player, int maxItems, ItemFilter filter, String name) {
		this.player = player;
		this.filter = filter;
		this.maxItems = maxItems;
		this.invName = name;
		
		this.items = new HashMap<Integer, ItemStack>();
		
		if (!player.getPlayer().isOnline()) {
			System.out.println("Cannot fetch inventory for offline player [GuiInventory@getFormattedInventory]");
			return;
		}
		
		Player p = player.getPlayer().getPlayer();
		
		int size = 9 * ((int) Math.ceil((double) maxItems / 9.0));
		this.inv = Bukkit.createInventory(p, size, invName);
		if (!items.isEmpty()) {
			for (Entry<Integer, ItemStack> e : items.entrySet()) {
				int val = e.getKey();
				inv.setItem(val, e.getValue());
			}
		}
		
		//pad with barrier blocks for clarity
		for (int i = maxItems; i < size; i++) {
			inv.setItem(i, new ItemStack(Material.BARRIER));
		}
	}
	
	@Override
	public Inventory getFormattedInventory(QuestPlayer player) {
				
		return inv;
	}

	@Override
	public Map<String, Object> serialize() {
		return null; //Runtime only, not serializable
	}

	@Override
	public InventoryItem getItem(int pos) {
		//real logic is here
		/*
		 * Check slot. If in player's inventory, try to take. If in top inventory, try and put back.
		 */
		int size = 9 * ((int) Math.ceil((double) maxItems / 9.0));
		
		if (pos > size) {
			//in player's inventory
			doContribute(pos);
		} else {
			doReturn(pos);
		}
		
		
		return null; //tell it to do nothing
	}
	
	private void doContribute(int pos) {
		ItemStack clicked = player.getOpenInventory().getItem(pos);
		if (clicked == null) {
			return;
		}
		
		//make sure we have room
		int slot = -1;
		for (int i = 0; i < maxItems; i++) {
			if (items.get(i) == null || items.get(i).getType() == Material.AIR) {
				slot = i;
				break;
			}
		}
		
		if (slot == -1) {
			//can't find space. just cancel.
			return;
		}
		
		//if we have a filter, apply it
		if (filter != null && filter.filterItem(clicked) == false) {
			return;
		}
		
		//else we got an item and a spot. now just make sure we don't already have one of that type
		for (int i = 0; i < maxItems; i++) {
			if (items.get(i) != null && items.get(i).getType() == clicked.getType()) {
				//already have one of this type
				return;
			}
		}
		
		//deduct one from inv, put it in slot
		if (clicked.getAmount() > 1) {
			clicked.setAmount(clicked.getAmount() - 1);
		} else {
			player.getOpenInventory().setItem(pos, null);
		}
		
		ItemStack newItem = clicked.clone();
		newItem.setAmount(1);
		items.put(slot, newItem);
		updateInventory();
		
	}
	
	private void doReturn(int pos) {
		if (pos >= maxItems) {
			//clicking a barrier block
			return;
		}
		
		ItemStack item = items.get(pos);
		if (item == null) {
			return;
		}
		
		items.put(pos, null);
		this.player.getInventory().addItem(item);
		updateInventory();
	}
	
	private void updateInventory() {
		for (int i = 0; i < maxItems; i++) {
			inv.setItem(i, items.get(i));
		}
	}

	@Override
	public ItemStack[] getResult() {
		ItemStack[] ret = new ItemStack[maxItems];
		for (int i = 0; i < maxItems; i++) {
			ret[i] = items.get(i);
		}
		
		return ret;
	}
}
