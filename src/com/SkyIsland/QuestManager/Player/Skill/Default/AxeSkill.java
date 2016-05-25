package com.SkyIsland.QuestManager.Player.Skill.Default;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.Utils.YamlWriter;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.Skill;
import com.SkyIsland.QuestManager.Player.Skill.Event.CombatEvent;
import com.google.common.collect.Lists;

public class AxeSkill extends Skill implements Listener {
	
	public static final String configName = "Axe.yml";

	public Type getType() {
		return Skill.Type.COMBAT;
	}
	
	public String getName() {
		return "Axe";
	}
	
	public String getDescription(QuestPlayer player) {
		String ret = ChatColor.WHITE + "Axe Skill governs the player's ability to hit their target while "
				+ "using an axe.";
		
		int lvl = player.getSkillLevel(this);
		if (lvl < apprenticeLevel) {
			ret += "\n\n" + ChatColor.RED + "Chance to hit: " + (int) (-(rateDecrease) * (apprenticeLevel - lvl)) + "%";
		}
		
		ret += "\n" + ChatColor.GREEN + "Bonus Damage: " + (Math.max(0, lvl - apprenticeLevel) / levelRate) + ChatColor.RESET;
		
		return ret;
	}

	@Override
	public int getStartingLevel() {
		return startingLevel;
	}
	
	@Override
	public String getConfigKey() {
		return "Axe";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof AxeSkill);
	}
	
	private int startingLevel;
	
	private int levelRate;
	
	private int apprenticeLevel;
	
	private double rateDecrease;
	
	public AxeSkill() {
		File configFile = new File(QuestManagerPlugin.questManagerPlugin.getDataFolder(), 
				QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillPath() + configName);
		YamlConfiguration config = createConfig(configFile);
		
		if (!config.getBoolean("enabled", true)) {
			return;
		}
		
		this.startingLevel = config.getInt("startingLevel", 0);
		this.levelRate = config.getInt("levelsperdamageincrease", 10);
		this.apprenticeLevel = config.getInt("apprenticeLevel", 15);
		this.rateDecrease = config.getDouble("hitchancePenalty", 3.0);
		
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			
			YamlWriter writer = new YamlWriter();
			
			writer.addLine("enabled", true, Lists.newArrayList("Whether or not this skill is allowed to be used.", "true | false"))
				.addLine("startingLevel", 0, Lists.newArrayList("The level given to players who don't have this skill yet", "[int]"))
				.addLine("levelsperdamageincrease", 10, Lists.newArrayList("How many levels are needed to gain an additional bonus damage,", "over apprentice level", "[int], greater than 0"))
				.addLine("apprenticeLevel", 15, Lists.newArrayList("The level at which the player's chance to hit is no", "longer is penalized", "[int], greater than 0"))
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
		
		if (!p.getInventory().getItemInMainHand().getType().name().toLowerCase().contains("axe")) {
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
		
		//just increase damage based on level
		//every n levels, one more damage
		e.setModifiedDamage(e.getModifiedDamage() + (Math.max(0, lvl - apprenticeLevel) / levelRate));
		
		this.perform(e.getPlayer(), causeMiss); //only get a 'cause miss' if this skill caused it 
		
	}
	
}
