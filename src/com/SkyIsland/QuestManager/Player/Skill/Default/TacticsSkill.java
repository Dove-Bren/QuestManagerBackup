package com.SkyIsland.QuestManager.Player.Skill.Default;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.Utils.YamlWriter;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.LogReducedSkill;
import com.SkyIsland.QuestManager.Player.Skill.Skill;
import com.SkyIsland.QuestManager.Player.Skill.Event.CombatEvent;
import com.google.common.collect.Lists;

public class TacticsSkill extends LogReducedSkill implements Listener {
	
	public static final String configName = "Tactics.yml";

	public Type getType() {
		return Skill.Type.COMBAT;
	}
	
	public String getName() {
		return "Tactics";
	}
	
	public String getDescription(QuestPlayer player) {
		String ret = ChatColor.WHITE + "Better tactics let you do better in fights, increasing melee damage";
		
		int lvl = player.getSkillLevel(this);
				
		ret += "\n\n" + ChatColor.GREEN + "Bonus Damage: " + (lvl / levelRate) + ChatColor.RESET;
		
		return ret;
	}

	@Override
	public int getStartingLevel() {
		return startingLevel;
	}
	
	@Override
	public String getConfigKey() {
		return "Tactics";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof TacticsSkill);
	}
	
	private int startingLevel;
	
	private int levelRate;
	
	public TacticsSkill() {
		File configFile = new File(QuestManagerPlugin.questManagerPlugin.getDataFolder(), 
				QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillPath() + configName);
		YamlConfiguration config = createConfig(configFile);
		
		if (!config.getBoolean("enabled", true)) {
			return;
		}
		
		this.startingLevel = config.getInt("startingLevel", 0);
		this.levelRate = config.getInt("levelsperdamageincrease", 20);
		
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			
			YamlWriter writer = new YamlWriter();
			
			writer.addLine("enabled", true, Lists.newArrayList("Whether or not this skill is allowed to be used.", "true | false"))
				.addLine("startingLevel", 0, Lists.newArrayList("The level given to players who don't have this skill yet", "[int]"))
				.addLine("levelsperdamageincrease", 20, Lists.newArrayList("How many levels are needed to gain an additional bonus damage", "[int], greater than 0"));
			
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
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onCombat(CombatEvent e) {		
		if (e.isMiss()) {
			return;
		}
		
		int lvl = e.getPlayer().getSkillLevel(this);
				
		//just increase damage based on level
		//every n levels, one more damage
		e.setModifiedDamage(e.getModifiedDamage() + (lvl / levelRate));
		
		this.perform(e.getPlayer()); //only get a 'cause miss' if this skill caused it 
		
	}
	
}
