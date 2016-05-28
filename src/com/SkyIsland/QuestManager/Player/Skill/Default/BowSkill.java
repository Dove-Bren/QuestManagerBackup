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
import com.SkyIsland.QuestManager.Player.PlayerOptions;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.LogSkill;
import com.SkyIsland.QuestManager.Player.Skill.Skill;
import com.SkyIsland.QuestManager.Player.Skill.Event.CombatEvent;
import com.google.common.collect.Lists;

public class BowSkill extends LogSkill implements Listener {
	
	public static final String configName = "Bow.yml";
	
	public static final String criticalMessage = ChatColor.DARK_GREEN + "You landed a critical hit" + ChatColor.RESET;

	public Type getType() {
		return Skill.Type.COMBAT;
	}
	
	public String getName() {
		return "Bow";
	}
	
	public String getDescription(QuestPlayer player) {
		String ret = ChatColor.WHITE + "The Bow Skill increases accuracy and precision when using a bow.";
		if (doCrits)
			ret += " The Bow Skill also increases the likelihood of landing a critical shot.";
		int lvl = player.getSkillLevel(this);
		if (lvl < apprenticeLevel) {
			ret += "\n\n" + ChatColor.RED + "Chance to hit: " + (int) (-(rateDecrease) * (apprenticeLevel - lvl)) + "%";
		} else if (doCrits) {
			ret += "\n" + ChatColor.GREEN + "Critical Chance: " + ((int) (100 * lvl * levelRate)) + "%" + ChatColor.RESET;
		}
		
		return ret;
	}

	@Override
	public int getStartingLevel() {
		return startingLevel;
	}
	
	@Override
	public String getConfigKey() {
		return "Bow";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof BowSkill);
	}
	
	private int startingLevel;
	
	private double levelRate;
	
	private int apprenticeLevel;
	
	private double rateDecrease;
	
	private boolean doCrits;
	
	private double critDamage;
	
	public BowSkill() {
		File configFile = new File(QuestManagerPlugin.questManagerPlugin.getDataFolder(), 
				QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillPath() + configName);
		YamlConfiguration config = createConfig(configFile);
		
		if (!config.getBoolean("enabled", true)) {
			return;
		}
		
		this.startingLevel = config.getInt("startingLevel", 0);
		this.levelRate = config.getDouble("critRate", 0.003);
		this.apprenticeLevel = config.getInt("apprenticeLevel", 15);
		this.rateDecrease = config.getDouble("hitchancePenalty", 3.0);
		this.doCrits = config.getBoolean("doCrits", true);
		this.critDamage = config.getDouble("critDamage", .5);
		
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			
			YamlWriter writer = new YamlWriter();
			
			writer.addLine("enabled", true, Lists.newArrayList("Whether or not this skill is allowed to be used.", "true | false"))
				.addLine("startingLevel", 0, Lists.newArrayList("The level given to players who don't have this skill yet", "[int]"))
				.addLine("apprenticeLevel", 15, Lists.newArrayList("The level at which the player's chance to hit is no", "longer is penalized", "[int], greater than 0"))
				.addLine("hitchancePenalty", 3.0, Lists.newArrayList("The penalty per level under apprentiveLevel given to the", "chance to hit. Maximum penalty is (apprenticeLevel * hitchancePenalty)", "[double]"))
				.addLine("doCrits", true, Lists.newArrayList("Whether bow shots should do custom critical hits"))
				.addLine("critRate", 0.01, Lists.newArrayList("Chance per level over apprentice to land a critical shot.", "Only in effect if doCrits is true", "[double], greater than 0. 0.01 is 1%"))
				.addLine("critDamage", 0.5, Lists.newArrayList("Critical hit damage multiplier. This is added to 1 and", "multiplied by the base damage of the attack." , "Only in effect if doCrits is true", "[double], greater than 0. 0.01 is 1%"));
			
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
		
		if (!e.getWeapon().getType().name().toLowerCase().contains("bow")) {
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
		
		//if it's a hit, try for a crit
		if (doCrits) {
			int land = Math.max(0, (int) levelRate * (lvl - apprenticeLevel));
			int roll = Skill.random.nextInt(100);
			if (roll <= land) {
				if (QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer(p)
						.getOptions().getOption(PlayerOptions.Key.CHAT_COMBAT_RESULT)) {
					p.sendMessage(criticalMessage);
				}
				
				e.setEfficiency(e.getEfficiency() + critDamage);
			}
		}
		
		this.perform(e.getPlayer(), causeMiss); //only get a 'cause miss' if this skill caused it 
		
	}
	
}
