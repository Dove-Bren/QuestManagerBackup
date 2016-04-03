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
import com.SkyIsland.QuestManager.Configuration.Utils.YamlWriter;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.Skill;
import com.SkyIsland.QuestManager.Player.Skill.Event.CombatEvent;
import com.SkyIsland.QuestManager.UI.Menu.Action.ForgeAction;
import com.google.common.collect.Lists;

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
		return "Sword&Shield";
	}
	
	public String getDescription(QuestPlayer player) {
		String ret = ChatColor.WHITE + "The Sword and Shield skill determines a player's offensive and "
				+ "defensive abilities while they have a weapon in their mainhand and a shield in their offhand";
		
		int lvl = player.getSkillLevel(this);
		if (lvl < apprenticeLevel) {
			ret += "\n\n" + ChatColor.RED + "Chance to hit: " + (int) (-rateDecrease * (apprenticeLevel - lvl)) + "%";
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
	
	private double rateDecrease;
	
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
		this.rateDecrease = config.getDouble("hitchancePenalty", 3.0);
		
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			YamlWriter writer = new YamlWriter();
			
			writer.addLine("enabled", true, Lists.newArrayList("Whether or not this skill is allowed to be used.", "true | false"))
				.addLine("startingLevel", 0, Lists.newArrayList("The level given to players who don't have this skill yet", "[int]"))
				.addLine("levelsperdefenseincrease", 10, Lists.newArrayList("How many levels are required to gain an additional", "point in defense", "[int], greater than 0"))
				.addLine("apprenticeLevel", 20, Lists.newArrayList("The level at which the player's chance to hit is no", "longer is penalized", "[int], greater than 0"))
				.addLine("hitchancePenalty", 3.0, Lists.newArrayList("The penalty per level under apprentiveLevel given to the", "chance to hit. Maximum penalty is (apprenticeLevel * hitchancePenalty)", "[double]"));
			
			try {
				writer.save(configFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return writer.buildYaml();
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
			int miss = (int) (rateDecrease * (apprenticeLevel - lvl)); 
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
