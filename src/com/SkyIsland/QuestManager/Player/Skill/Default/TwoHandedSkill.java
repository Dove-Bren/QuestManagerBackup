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
import com.SkyIsland.QuestManager.Player.Skill.LogSkill;
import com.SkyIsland.QuestManager.Player.Skill.Skill;
import com.SkyIsland.QuestManager.Player.Skill.Event.CombatEvent;
import com.SkyIsland.QuestManager.UI.Menu.Action.ForgeAction;
import com.google.common.collect.Lists;

public class TwoHandedSkill extends LogSkill implements Listener {
	
	public static final String configName = "TwoHanded.yml";

	public Type getType() {
		return Skill.Type.COMBAT;
	}
	
	public String getName() {
		return "Two Handed";
	}
	
	public String getDescription(QuestPlayer player) {
		String ret = ChatColor.WHITE + "The Two Handed skill involves a player using a single weapon to attack, with nothing"
				+ " in their offhand.";
		
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
		return "Two_Handed";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof TwoHandedSkill);
	}
	
	private int startingLevel;
	
	private int levelRate;
	
	public TwoHandedSkill() {
		File configFile = new File(QuestManagerPlugin.questManagerPlugin.getDataFolder(), 
				QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillPath() + configName);
		YamlConfiguration config = createConfig(configFile);
		
		if (!config.getBoolean("enabled", true)) {
			return;
		}
		
		this.startingLevel = config.getInt("startingLevel", 0);
		this.levelRate = config.getInt("levelsperdamageincrease", 10);
		
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			
			YamlWriter writer = new YamlWriter();
			
			writer.addLine("enabled", true, Lists.newArrayList("Whether or not this skill is allowed to be used.", "true | false"))
				.addLine("startingLevel", 0, Lists.newArrayList("The level given to players who don't have this skill yet", "[int]"))
				.addLine("levelsperdamageincrease", 10, Lists.newArrayList("How many levels are needed to gain an additional bonus damage", "[int], greater than 0"));
			
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
				|| (p.getInventory().getItemInOffHand() != null && p.getInventory().getItemInOffHand().getType() != Material.AIR)) {
			return;
		}
		
		int lvl = e.getPlayer().getSkillLevel(this);
		
		//just increase damage based on level
		//every n levels, one more damage
		e.setModifiedDamage(e.getModifiedDamage() + (lvl / levelRate));
		
		this.perform(e.getPlayer(), e.isMiss());
		
	}
	
}
