package com.SkyIsland.QuestManager.Player.Skill.Default;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.Utils.YamlWriter;
import com.SkyIsland.QuestManager.Magic.ImbuementHandler;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.Skill;
import com.SkyIsland.QuestManager.Player.Skill.Event.MagicApplyEvent;
import com.SkyIsland.QuestManager.Player.Skill.Event.MagicCastEvent;
import com.SkyIsland.QuestManager.Player.Skill.Event.MagicCastEvent.MagicType;
import com.google.common.collect.Lists;

/**
 * Dictates the difficulty and potential of spell weaving spells
 * @author Skyler
 *
 */
public class ImbuementSkill extends Skill implements Listener {
	
	public static final String configName = "Imbuement.yml";

	public Type getType() {
		return Skill.Type.COMBAT;
	}
	
	public String getName() {
		return "Imbuement";
	}
	
	public String getDescription(QuestPlayer player) {
		String ret = ChatColor.WHITE + "The imbuement skill governs a player's ability to imbue items at an "
				+ "imbuement altar. More skill means better results and potentially more imbuement slots";
		
		ImbuementHandler handler = QuestManagerPlugin.questManagerPlugin.getImbuementHandler();
		ret += ChatColor.GOLD + "\n\nImbuement Slots: " + handler.getImbuementSlots(player);
		
		ret += "\n" + ChatColor.GREEN + "Spell Efficiency: " 
				+ ((int) (100 + (100 * handler.getPotencyBonus(player)))) + "%" + ChatColor.RESET;
		
		return ret;
	}

	@Override
	public int getStartingLevel() {
		return startingLevel;
	}
	
	@Override
	public String getConfigKey() {
		return "Imbuement";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof ImbuementSkill);
	}
	
	private int startingLevel;
	
	/**
	 * How much, per level, the price of a slash is reduced
	 */
	private double slashDiscountRate;
	
	private double castingTime;////////////////
	
	public ImbuementSkill() {
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
		this.rateDecrease = config.getDouble("hitchancePenalty", 3.0);
		
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			YamlWriter writer = new YamlWriter();
			
			writer.addLine("enabled", true, Lists.newArrayList("Whether or not this skill is allowed to be used.", "true | false"))
				.addLine("startingLevel", 0, Lists.newArrayList("The level given to players who don't have this skill yet", "[int]"))
				.addLine("bonusDamagePerLevel", 0.01, Lists.newArrayList("How many damage is added per level, as a % (0.5 is 50%)", "[double]"))
				.addLine("difficultyRatio", 1.0, Lists.newArrayList("How many player spellwaving levels correspond to 1 difficulty level?", "In other words, at what spellweaving level should a player be able to cast a", "level x spell? n*x, where n is the ratio. If spellweaving goes from 0-100", "and difficulty goes from 0-100, then 1 is perfect. If spellweaving goes from 0-100 and difficulty", "goes from 0-10, a ratio of 10 is perfect. (10 spellweaving levels per difficulty)", "[double] "))
				.addLine("levelGrace", 5, Lists.newArrayList("How many levels over the appropriate level (according to the", "difficulty ratio) a player must be to no longer make", "checks on spell failure", "[int]"))
				.addLine("hitchancePenalty", 3.0, Lists.newArrayList("The penalty per level under apprentiveLevel given to the", "chance to hit. Penalty is", "(  ([calculated spell level] + levelGrace) * hitchancePenalty )", "[double]"));
			
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
	public void onMagicCast(MagicCastEvent e) {
		if (e.getType() != MagicType.SPELLWEAVING) {
			return;
		}
		
		QuestPlayer player = e.getPlayer();
		
		if (e.getCastSpell() == null) {
			return;
		}
		
		int difficultylevel = (int) (e.getCastSpell().getDifficulty() * this.difficultyRatio);
		int levelDifference = difficultylevel - player.getSkillLevel(this);
		boolean causeMiss = false;
		
		if (levelDifference > -levelGrace) {
			int chance = (int) (levelDifference * rateDecrease), 
					roll = Skill.random.nextInt(100);
			if (roll < chance) {
				e.setFail(true);
				causeMiss = true;
			}
			
		}
		
		//give xp for the cast (success, fail)
		this.perform(player, difficultylevel, causeMiss);
	}
	
	@EventHandler
	public void onMagicHit(MagicApplyEvent e) {
		
		QuestPlayer player = e.getPlayer();
		
		double adjustment = this.levelRate * player.getSkillLevel(this);
		
		e.setEfficiency(e.getEfficiency() + adjustment);
		
		//don't do perform, as that's handled on-cast
		
		
	}
	
	public ImbuementSkill() {
		
	}
}
