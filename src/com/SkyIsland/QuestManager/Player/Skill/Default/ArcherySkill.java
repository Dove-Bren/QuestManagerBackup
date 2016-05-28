package com.SkyIsland.QuestManager.Player.Skill.Default;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.Utils.YamlWriter;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.LogSkill;
import com.SkyIsland.QuestManager.Player.Skill.Skill;
import com.SkyIsland.QuestManager.Player.Skill.Event.CombatEvent;
import com.google.common.collect.Lists;

public class ArcherySkill extends LogSkill implements Listener {
	
	public static final String configName = "Archery.yml";

	public Type getType() {
		return Skill.Type.COMBAT;
	}
	
	public String getName() {
		return "Archery";
	}
	
	public String getDescription(QuestPlayer player) {
		String ret = ChatColor.WHITE + "A higher skill in Archery allows the player to shoot arrows further. "
				+ "Archers do not fire with anything other than arrows in their offhand.";
		
		int lvl = player.getSkillLevel(this);
		
		ret += "\n\n" + ChatColor.GREEN + "Arrow Speed: " + (100 * (1 + lvl * levelRate)) + "%" + ChatColor.RESET;
		
		return ret;
	}

	@Override
	public int getStartingLevel() {
		return startingLevel;
	}
	
	@Override
	public String getConfigKey() {
		return "Archery";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof ArcherySkill);
	}
	
	private int startingLevel;
	
	private double levelRate;
	
	public ArcherySkill() {
		File configFile = new File(QuestManagerPlugin.questManagerPlugin.getDataFolder(), 
				QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillPath() + configName);
		YamlConfiguration config = createConfig(configFile);
		
		if (!config.getBoolean("enabled", true)) {
			return;
		}
		
		this.startingLevel = config.getInt("startingLevel", 0);
		this.levelRate = config.getDouble("arrowSpeedPerLevel", 0.005);
		
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			
			YamlWriter writer = new YamlWriter();
			
			writer.addLine("enabled", true, Lists.newArrayList("Whether or not this skill is allowed to be used.", "true | false"))
				.addLine("startingLevel", 0, Lists.newArrayList("The level given to players who don't have this skill yet", "[int]"))
				.addLine("arrowSpeedPerLevel", 0.005, Lists.newArrayList("Bonus to arrow speed per level. Multiplicitive bonus", "[double], 0.01 is 1%"));
			
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
	public void onFire(ProjectileLaunchEvent e) {
		if (e.getEntity().getShooter() instanceof Player) {
			QuestPlayer qp = QuestManagerPlugin.questManagerPlugin.getPlayerManager()
					.getPlayer((Player) e.getEntity().getShooter());
			int level = qp.getSkillLevel(this);
			
			double bonus = level * levelRate;
			if (e.getEntity() instanceof Arrow) {
				e.getEntity().setVelocity(e.getEntity().getVelocity().multiply(1 + bonus));
				this.perform(qp);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onHitLand(CombatEvent e) {
		if (e.getWeapon() == null || !e.getWeapon().getType().name().toLowerCase().contains("bow")) {
			return;
		}
		
		if (e.getOtherItem() != null && e.getOtherItem().getType() != Material.AIR) {
			return;
		}
		
		if (e.isMiss()) {
			return;
		}
		
		this.perform(e.getPlayer());
	}
	
}
