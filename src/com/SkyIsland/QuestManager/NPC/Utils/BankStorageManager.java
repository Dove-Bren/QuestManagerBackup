package com.SkyIsland.QuestManager.NPC.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Player.QuestPlayer;

/**
 * Manages NPC Bank storage methods<br />
 * Specifically, stores bank inventories against IDs intended to be unique per bank
 * NPC. This is not enforced -- which allows for global-type banks or aligned banks --
 * but is supported.
 * @author Skyler
 *
 */
public class BankStorageManager {

	public static class BankStorage implements ConfigurationSerializable {
		
		private Map<UUID, Inventory> invMap;
		
		private String storageKey;
		
		protected BankStorage(String key) {
			this.storageKey = key;
			this.invMap = new TreeMap<UUID, Inventory>();
		}
		
		protected void setInventory(UUID player, Inventory inv) {
			invMap.put(player, inv);
		}
		
		protected Inventory getInventory(UUID player) {
			return invMap.get(player);
		}
		
		@Override
		public Map<String, Object> serialize() {
			Map<String, Object> map = new HashMap<String, Object>();
			Map<Integer, ItemStack> items;
			int index;
			
			map.put("key", storageKey);
			
			for (UUID key : invMap.keySet()) {
				items = new TreeMap<>();
				index = 0;
				
				for (ItemStack item : invMap.get(key).getContents()) {
					items.put(index, item);
					index++;
				}
				
				map.put(key.toString(), items);
			}
			
			return map;
		}
		
		@SuppressWarnings("unchecked")
		public static BankStorage deserialize(Map<String, Object> map) {
			BankStorage storage = new BankStorage((String) map.get("key"));
						
			if (!map.keySet().isEmpty()) {
				UUID uuid;
				Inventory inv;
				Map<Integer, ItemStack> items;
				
				for (String id : map.keySet()) {
					if (id.startsWith("==") || id.startsWith("key")) {
						continue;
					}
					
					uuid = UUID.fromString(id);
					inv = Bukkit.createInventory(null, 54, "Bank");//"QM_bank_" + storage.storageKey + "_" + qp.getIDString());
					items = (Map<Integer, ItemStack>) map.get(id);
					for (Integer slot : items.keySet()) {
						inv.setItem(slot, items.get(slot));
						
					}
					
					storage.invMap.put(uuid, inv);
				}
			}
			
			return storage;
		}
		
	}
	
	public static void registerSerialization() {
		ConfigurationSerialization.registerClass(BankStorage.class);
	}
	
	private Map<String, BankStorage> storageMap;
	
	@SuppressWarnings("unchecked")
	public BankStorageManager(File bankDataFile) {
		this.storageMap = new HashMap<>();
		
		if (bankDataFile == null || !bankDataFile.exists())
			return;
		
		YamlConfiguration bankData = YamlConfiguration.loadConfiguration(bankDataFile);
		
		List<BankStorage> storages = (List<BankStorage>) bankData.getList("banks");
		if (storages == null || storages.isEmpty()) {
			return;
		}
		
		for (BankStorage storage : storages) {
			System.out.println("Storage named " + storage.storageKey);
			storageMap.put(storage.storageKey, storage);
		}
	}
	
	/**
	 * Returns a player's personal bank storage for the given bank (specified by key).
	 * @param bankKey A key signifying which bank to pull from. If the bank doesn't exist yet, it will be created
	 * @param player The player to look up in the specified bank
	 * @return An inventory for the player (a new one, if it didn't exist but the bank does),
	 * or null if either parameter is null
	 */
	public Inventory getInventory(String bankKey, QuestPlayer player) {
		if (bankKey == null || player == null) {
			return null;
		}
		
		if (!storageMap.containsKey(bankKey)) {
			storageMap.put(bankKey, new BankStorage(bankKey));
		}
		
		BankStorage store = storageMap.get(bankKey);
		
		Inventory inv = store.getInventory(player.getPlayer().getUniqueId());
		
		if (inv == null) {
			inv = Bukkit.createInventory(null, 54, "Bank");//"QM_bank_" + bankKey + "_" + player.getIDString());
			store.setInventory(player.getPlayer().getUniqueId(), inv);
		}
		
		return inv;
	}
	
	public void save(File file) {
		if (storageMap.isEmpty()) {
			return;
		}
		
		YamlConfiguration config = new YamlConfiguration();
		List<BankStorage> list = new LinkedList<>();
		
		for (BankStorage store : storageMap.values()) {
			list.add(store);
		}	
		
		config.set("banks", list);
		
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Ditch string: ["
					+ config.saveToString() + "]");
		}
	}
	
}
