package com.SkyIsland.QuestManager.Player.Skill.Default;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.Skill;
import com.SkyIsland.QuestManager.Player.Skill.Event.MagicApplyEvent;
import com.SkyIsland.QuestManager.Player.Skill.Event.MagicCastEvent;

public class MagerySkill extends Skill implements Listener {
	
	public static final String configName = "Magery.yml";

	public Type getType() {
		return Skill.Type.COMBAT;
	}
	
	public String getName() {
		return "Magery";
	}
	
	public String getDescription(QuestPlayer player) {
		String ret = ChatColor.WHITE + "The Magery skill governs the player's ability to cast spells, and "
				+ "produce meaningful results. Magery skill combats spell difficulty, and is the main "
				+ "determining factor in spell success or failure.";
		
		int lvl = player.getSkillLevel(this);
		int mastery = Math.max(0, (int) ((difficultyRatio * lvl) - levelGrace));
		ret += ChatColor.GOLD + "\nCurrent Mastery Level: " + mastery;
		
		ret += "\n" + ChatColor.GREEN + "Spell Efficiency: " + (lvl * levelRate) + ChatColor.RESET;
		
		return ret;
	}

	@Override
	public int getStartingLevel() {
		return startingLevel;
	}
	
	@Override
	public String getConfigKey() {
		return "Magery";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof MagerySkill);
	}
	
	private int startingLevel;
	
	/**
	 * Magery damage increase. Multiplicitive. What bonus % to add (.15 is 15%, or 115% damage done with magery alone)
	 */
	private double levelRate;
	
	/**
	 * How many player magery levels correspond to 1 difficulty level?
	 * In other words, at what magery level should a player be able to cast a level x spell? n*x, where n
	 * is the ratio.
	 * If magery goes from 0-100 and difficulty goes from 0-100, then 1 is perfect. if magery goes from
	 * 0-100 and difficulty goes from 0-10, a ratio of 10 is perfect. (10 magery levels per difficulty)
	 */
	private double difficultyRatio;
		
	/**
	 * How many levels over the appropriate level (according to the difficulty ratio) a player must be to
	 * no longer make checks on spell failure
	 */
	private int levelGrace;
	
	public MagerySkill() {
		File configFile = new File(QuestManagerPlugin.questManagerPlugin.getDataFolder(), 
				QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillPath() + configName);
		YamlConfiguration config = createConfig(configFile);
		
		if (!config.getBoolean("enabled", true)) {
			return;
		}
		
		this.startingLevel = config.getInt("startingLevel", 0);
		this.levelRate = config.getDouble("bonusDamagePerLevel", 0.01);
		this.difficultyRatio = config.getDouble("difficultyRatio", 1.0);
		this.levelGrace = config.getInt("levelGrace", 5);
		
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			YamlConfiguration defaultConfig = new YamlConfiguration();
			
			defaultConfig.set("enabled", true);
			defaultConfig.set("startingLevel", 0);
			defaultConfig.set("bonusDamagePerLevel", 0.01);
			defaultConfig.set("difficultyRatio", 1.0);
			defaultConfig.set("levelGrace", 5);
			
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
	public void onMagicCast(MagicCastEvent e) {
		QuestPlayer player = e.getPlayer();
		
		if (e.getCastSpell() == null) {
			return;
		}
		
		int difficultylevel = (int) (e.getCastSpell().getDifficulty() * this.difficultyRatio);
		int levelDifference = difficultylevel - player.getSkillLevel(this);
		boolean causeMiss = false;
		
		if (levelDifference > -levelGrace) {
			int chance = levelDifference * 2, 
					roll = Skill.random.nextInt(100);
			if (roll < chance) {
				e.setFail(true);
				causeMiss = true;
			}
			
		}
		
		//give xp for the cast (success, fail)
		this.perform(player, causeMiss);
	}
	
	@EventHandler
	public void onMagicHit(MagicApplyEvent e) {
		QuestPlayer player = e.getPlayer();
		
		double adjustment = this.levelRate * player.getSkillLevel(this);
		
		e.setEfficiency(e.getEfficiency() + adjustment);
		
		//don't do perform, as that's handled on-cast
		
		
	}
	
}
