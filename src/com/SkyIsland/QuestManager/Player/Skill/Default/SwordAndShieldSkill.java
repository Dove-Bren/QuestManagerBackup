package com.SkyIsland.QuestManager.Player.Skill.Default;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.Skill;
import com.SkyIsland.QuestManager.Player.Skill.Event.CombatEvent;
import com.SkyIsland.QuestManager.UI.Menu.Action.ForgeAction;

/**
 * Skill governing combat with a weapon in main hand, shield in offhand
 * @author Skyler
 *
 */
public class SwordAndShieldSkill extends Skill implements Listener {
	
	public static final String configName = "SwordAndShield.yml";

	public Type getType() {
		return Skill.Type.COMBAT;
	}
	
	public String getName() {
		return "Sword & Shield";
	}
	
	public String getDescription(QuestPlayer player) {
		String ret = ChatColor.WHITE + "The Sword and Shield skill determines a player's offensive and "
				+ "defensive abilities while they have a weapon in their mainhand and a shield in their offhand";
		
		int lvl = player.getSkillLevel(this);
		if (lvl < apprenticeLevel) {
			ret += "\n\n" + ChatColor.RED + "Chance to hit: " + (-3 * (apprenticeLevel - lvl)) + "%";
		}
		
		ret += "\n" + ChatColor.GREEN + "Bonus Defense: " + (lvl / levelRate) + ChatColor.RESET;
		
		return ret;
	}

	@Override
	public int getStartingLevel() {
		return startingLevel;
	}
	
	@Override
	public String getConfigKey() {
		return "Sword_And_Shield";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof SwordAndShieldSkill);
	}
	
	private int startingLevel;
	
	private int levelRate;
	
	private int apprenticeLevel;
	
	public SwordAndShieldSkill() {
		File configFile = new File(QuestManagerPlugin.questManagerPlugin.getDataFolder(), 
				QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillPath() + configName);
		YamlConfiguration config = createConfig(configFile);
		
		if (!config.getBoolean("enabled", true)) {
			return;
		}
		
		this.startingLevel = config.getInt("startingLevel", 0);
		this.levelRate = config.getInt("levelsperdefenseincrease", 10);
		this.apprenticeLevel = config.getInt("apprenticeLevel", 20);
		
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			YamlConfiguration defaultConfig = new YamlConfiguration();
			
			defaultConfig.set("enabled", true);
			defaultConfig.set("startingLevel", 0);
			defaultConfig.set("levelsperdefenseincrease", 10);
			defaultConfig.set("apprenticeLevel", 20);
			
			try {
				defaultConfig.save(configFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return defaultConfig;
		}
		
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		return config;
	}
	
	@EventHandler
	public void onCombat(CombatEvent e) {
		Player p = e.getPlayer().getPlayer().getPlayer();
		
		if (!ForgeAction.Repairable.isRepairable(p.getInventory().getItemInMainHand().getType())
				|| (p.getInventory().getItemInOffHand() == null || p.getInventory().getItemInOffHand().getType() != Material.SHIELD)) {
			return;
		}
		
		int lvl = e.getPlayer().getSkillLevel(this);
		
		//reduce chance to hit if level under apprentice level
		boolean causeMiss = false;
		if (lvl < apprenticeLevel) {
			//3% per level under apprentice -- up to 45%
			int miss = 3 * (apprenticeLevel - lvl); 
			int roll = Skill.random.nextInt(100);
			if (roll <= miss) {
				e.setMiss(true);
				causeMiss = true;
			}
		}
		
		//just increase defense based on level
		//every n levels, one more defense point
		e.setModifiedDamage(e.getModifiedDamage() - (lvl / levelRate));
		
		this.perform(e.getPlayer(), causeMiss); //only get a 'cause miss' if this skill caused it 
		
	}
	
}
