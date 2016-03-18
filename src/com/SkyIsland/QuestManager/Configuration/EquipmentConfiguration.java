package com.SkyIsland.QuestManager.Configuration;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/**
 * Convenience class for storing equipment configuration.<br />
 * @author Skyler
 *
 */
public class EquipmentConfiguration {

	private ItemStack head;
	
	private ItemStack chest;
	
	private ItemStack legs;
	
	private ItemStack boots;
	
	private ItemStack heldMain;
	
	private ItemStack heldOff;
	
	public EquipmentConfiguration() {
		; //default everything to null;
	}
	
	public EquipmentConfiguration(ItemStack head, ItemStack chest, ItemStack legs, 
			ItemStack boots, ItemStack held) {
		this(head, chest, legs, boots, held, null);
	}
	
	public EquipmentConfiguration(ItemStack head, ItemStack chest, ItemStack legs,
			ItemStack boots, ItemStack mainhand, ItemStack offhand) {
		this.head = head;
		this.chest = chest;
		this.legs = legs;
		this.boots = boots;
		this.heldMain = mainhand;
		this.heldOff = offhand;
	}
	
	public EquipmentConfiguration(EntityEquipment equips) {
		this.head = equips.getHelmet();
		this.chest = equips.getChestplate();
		this.legs = equips.getLeggings();
		this.boots = equips.getBoots();
		this.heldMain = equips.getItemInMainHand();
		this.heldOff = equips.getItemInOffHand();
	}
	
	public void save(File file) throws IOException {
		
		YamlConfiguration state = new YamlConfiguration();
		
		//unique identification
		state.set("type", "ecnf");
		
		state.set("head", head);
		state.set("chest", chest);
		state.set("legs", legs);
		state.set("boots", boots);
		state.set("main", heldMain);
		state.set("offhand", heldOff);
		
		state.save(file);
	}
	
	public YamlConfiguration getConfiguration() {
		
		YamlConfiguration state = new YamlConfiguration();
		
		//unique identification
		state.set("type", "ecnf");
		
		state.set("head", head);
		state.set("chest", chest);
		state.set("legs", legs);
		state.set("boots", boots);
		state.set("main", heldMain);
		state.set("offhand", heldOff);
		
		return state;
	}
	
	public void load(ConfigurationSection config) throws InvalidConfigurationException {
		
		if (!config.contains("type") || !config.getString("type").equals("ecnf")) {
			throw new InvalidConfigurationException();
		}
		
		head = config.getItemStack("head");
		chest = config.getItemStack("chest");
		legs = config.getItemStack("legs");
		boots = config.getItemStack("boots");
		
		if (config.contains("held")) {
			heldMain = config.getItemStack("held");
			heldOff = null;
		} else {
			heldMain = config.getItemStack("main");
			heldOff = config.getItemStack("offhand");
		}
		
		
	}

	/**
	 * @return the head
	 */
	public ItemStack getHead() {
		return head;
	}

	/**
	 * @param head the head to set
	 */
	public void setHead(ItemStack head) {
		this.head = head;
	}

	/**
	 * @return the chest
	 */
	public ItemStack getChest() {
		return chest;
	}

	/**
	 * @param chest the chest to set
	 */
	public void setChest(ItemStack chest) {
		this.chest = chest;
	}

	/**
	 * @return the legs
	 */
	public ItemStack getLegs() {
		return legs;
	}

	/**
	 * @param legs the legs to set
	 */
	public void setLegs(ItemStack legs) {
		this.legs = legs;
	}

	/**
	 * @return the boots
	 */
	public ItemStack getBoots() {
		return boots;
	}

	/**
	 * @param boots the boots to set
	 */
	public void setBoots(ItemStack boots) {
		this.boots = boots;
	}

	public ItemStack getHeldMain() {
		return heldMain;
	}

	public void setHeldMain(ItemStack heldMain) {
		this.heldMain = heldMain;
	}

	public ItemStack getHeldOff() {
		return heldOff;
	}

	public void setHeldOff(ItemStack heldOff) {
		this.heldOff = heldOff;
	}
	
	
}
