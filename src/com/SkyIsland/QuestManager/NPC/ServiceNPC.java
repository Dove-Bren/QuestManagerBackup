package com.SkyIsland.QuestManager.NPC;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.EquipmentConfiguration;
import com.SkyIsland.QuestManager.Configuration.Utils.LocationState;
import com.SkyIsland.QuestManager.Fanciful.FancyMessage;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.UI.ChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.BioptionChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.Action.OpenInventoryGuiAction;
import com.SkyIsland.QuestManager.UI.Menu.Inventory.ServiceInventory;
import com.SkyIsland.QuestManager.UI.Menu.Message.BioptionMessage;

/**
 * NPC which offers to and repairs a players equipment, for a fee
 * @author Skyler
 *
 */
public class ServiceNPC extends SimpleStaticBioptionNPC {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(ServiceNPC.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(ServiceNPC.class);
	}
	

	private enum aliases {
		FULL("com.SkyIsland.QuestManager.NPC.ServiceNPC"),
		DEFAULT(ServiceNPC.class.getName()),
		SHORT("ServiceNPC"),
		INFORMAL("SERVICE");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	private ServiceNPC(Location startingLoc) {
		super(startingLoc);
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>(4);
		
		map.put("name", name);
		map.put("type", getEntity().getType());
		map.put("location", new LocationState(getEntity().getLocation()));
		map.put("services", inventory);
		
		EquipmentConfiguration econ;
		
		if (getEntity() instanceof LivingEntity) {
			econ = new EquipmentConfiguration(
					((LivingEntity) getEntity()).getEquipment()
					);
		} else {
			econ = new EquipmentConfiguration();
		}
		
		map.put("equipment", econ);
		
		map.put("message", chat);
	
		
		return map;
	}
	
	public static ServiceNPC valueOf(Map<String, Object> map) {
		if (map == null || !map.containsKey("name") || !map.containsKey("type") 
				 || !map.containsKey("location") || !map.containsKey("equipment")
				  || !map.containsKey("message") || !map.containsKey("services")) {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Invalid NPC info! "
					+ (map.containsKey("name") ? ": " + map.get("name") : ""));
			return null;
		}
		
		
		EquipmentConfiguration econ = new EquipmentConfiguration();
		try {
			YamlConfiguration tmp = new YamlConfiguration();
			tmp.createSection("key",  (Map<?, ?>) map.get("equipment"));
			econ.load(tmp.getConfigurationSection("key"));
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LocationState ls = (LocationState) map.get("location");
		Location loc = ls.getLocation();
		
		ServiceNPC npc = new ServiceNPC(loc);
		EntityType type = EntityType.valueOf((String) map.get("type"));
		
		npc.name = (String) map.get("name");
		
		npc.inventory = (ServiceInventory) map.get("services");
		

		loc.getChunk();
		npc.setEntity(loc.getWorld().spawnEntity(loc, type));
		npc.getEntity().setCustomName((String) map.get("name"));

		if (npc.getEntity() instanceof LivingEntity) {
			EntityEquipment equipment = ((LivingEntity) npc.getEntity()).getEquipment();
			equipment.setHelmet(econ.getHead());
			equipment.setChestplate(econ.getChest());
			equipment.setLeggings(econ.getLegs());
			equipment.setBoots(econ.getBoots());
			equipment.setItemInMainHand(econ.getHeldMain());
			equipment.setItemInOffHand(econ.getHeldOff());
			
		}
		
		npc.chat = (BioptionMessage) map.get("message");		
		
		//provide our npc's name, unless we don't have one!
		if (npc.name != null && !npc.name.equals("")) {
			FancyMessage label = new FancyMessage(npc.name);
			npc.chat.setSourceLabel(label);			
		}
		
		return npc;
	}
	
	private ServiceInventory inventory;
		
	@Override
	protected void interact(Player player) {
		
		QuestPlayer qp = QuestManagerPlugin.questManagerPlugin.getPlayerManager()
				.getPlayer(player.getUniqueId());
		
		
		ChatMenu messageChat = new BioptionChatMenu(chat, 
					new OpenInventoryGuiAction(qp, inventory)
		, null);			

		messageChat.show(player);
	}
	
}
