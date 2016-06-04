package com.SkyIsland.QuestManager.Player.Skill.Default;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.Utils.YamlWriter;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.LogSkill;
import com.SkyIsland.QuestManager.Player.Skill.QualityItem;
import com.SkyIsland.QuestManager.Player.Skill.Skill;
import com.SkyIsland.QuestManager.Player.Skill.Event.FishEvent;
import com.SkyIsland.QuestManager.UI.Menu.ActiveInventoryMenu;
import com.SkyIsland.QuestManager.UI.Menu.InventoryMenu;
import com.SkyIsland.QuestManager.UI.Menu.Action.CollectFishAction;
import com.SkyIsland.QuestManager.UI.Menu.Inventory.Minigames.FishingGui;
import com.google.common.collect.Lists;

public class FishingSkill extends LogSkill implements Listener {
	
	public static final String configName = "Fishing.yml";
	
	public static final String badRangeMessage = ChatColor.RED + "There doesn't seem to be any fish in your skill range...";
	
	private static final class FishRecord {
		
		private int difficulty;
		
		private ItemStack icon;
		
		public FishRecord(int difficulty, ItemStack icon) {
			this.difficulty = difficulty;
			this.icon = icon;
		}
		
	}

	public Type getType() {
		return Skill.Type.TRADE;
	}
	
	public String getName() {
		return "Fishing";
	}
	
	public String getDescription(QuestPlayer player) {
		String ret = ChatColor.WHITE + "Fishing skill determines the types of fish caught and how difficulty it"
				+ " is to catch them.";
		
		int level = player.getSkillLevel(this);
		
		ret += "\n\n" + ChatColor.GOLD + "Fish Range: " 
				+ Math.max(0, level - maxDifficultyRange) + " - " + (level + maxDifficultyRange);
		ret += "\n" + ChatColor.GOLD + "Catch Quality: +" + (level * qualityRate);
		
		ret += "\n" + ChatColor.GREEN + "Difficulty: " 
				+ ((int) (100 - (100 * (level * reelDifficultyDiscount)))) + "%" + ChatColor.RESET;
		ret += "\n" + ChatColor.DARK_BLUE + "Reeling Time: "
				+ ((int) (100 - (100 * (level * timeDiscount))) + "%"); 
		
		return ret;
	}

	@Override
	public int getStartingLevel() {
		return startingLevel;
	}
	
	@Override
	public String getConfigKey() {
		return "Fishing";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof FishingSkill);
	}
	
	private int startingLevel;
	
	private double difficultyPerRow;
	
	private float baseReelDifficulty;
	
	private float baseReelDeviation;
	
	private float reelDifficultyRate;
	
	private double reelDifficultyDiscount;
	
	private double baseObstacleDifficulty;
	
	private double baseObstacleDeviation;
	
	private double obstacleDifficultyRate;
	
	private double obstacleDifficultyDiscount;
	
	private double baseTimePerDifficulty;
	
	private double timeDiscount;
	
	private double extraFishPerLevel;
	
	private int maxDifficultyRange;
	
	private double qualityRate;
	
	private List<FishRecord> fishRecords;
	
	public FishingSkill() {
		File configFile = new File(QuestManagerPlugin.questManagerPlugin.getDataFolder(), 
				QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillPath() + configName);
		YamlConfiguration config = createConfig(configFile);

		
		if (!config.getBoolean("enabled", true)) {
			return;
		}
		
		this.startingLevel = config.getInt("startingLevel", 0);
		this.difficultyPerRow = config.getDouble("difficultyPerRow", 15);
		this.baseReelDifficulty = (float) config.getDouble("baseReelDifficulty", .03);
		this.baseReelDeviation = (float) config.getDouble("baseReelDeviation", .02);
		this.reelDifficultyRate = (float) config.getDouble("reelDifficultyRate", .001);
		this.reelDifficultyDiscount = config.getDouble("reelDifficultyDiscount", .003);
		this.baseObstacleDifficulty = config.getDouble("baseObstacleDifficulty", 2.0);
		this.baseObstacleDeviation = config.getDouble("baseObstacleDeviation", 1.0);
		this.obstacleDifficultyRate = config.getDouble("obstacleDifficultyRate", .05);
		this.obstacleDifficultyDiscount = config.getDouble("obstacleDifficultyDiscount", 0.0);
		this.baseTimePerDifficulty = config.getDouble("baseTimePerDifficulty", 2.0);
		this.maxDifficultyRange = config.getInt("maxDifficultyRange", 20);
		this.timeDiscount = config.getDouble("timeDiscount", 0.025);
		this.extraFishPerLevel = config.getDouble("extraFishPerLevel", 0.2);
		this.qualityRate = config.getDouble("qualityRate", 0.01);
		
		this.fishRecords = new LinkedList<>();
		if (!config.contains("fish")) {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Didn't find any fish table"
					+ "for FishingSkill even though it's enabled!");
			return;
		} else {
			ConfigurationSection sex = config.getConfigurationSection("fish");
			for (String key : sex.getKeys(false)) {
				if (key.startsWith("==")) {
					continue;
				}
				
				try {
					fishRecords.add(new FishRecord(
							sex.getInt(key + ".difficulty"), sex.getItemStack(key + ".icon", new ItemStack(Material.RAW_FISH))
							));
				} catch (Exception e) {
					e.printStackTrace();
					QuestManagerPlugin.questManagerPlugin.getLogger().warning("Skipping that one! ^");
				}
			}
		}
				
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
		FishingGui.setFishingSkill(this);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			YamlWriter writer = new YamlWriter();
			
			writer.addLine("enabled", true, Lists.newArrayList("Whether or not this skill is allowed to be used.", "Note: To turn off imbueing altogether, see the imbument config", "in folder defaultly one up from here: imbuement.yml", "true | false"))
				.addLine("startingLevel", 0, Lists.newArrayList("The level given to players who don't have this skill yet", "[int]"))
				.addLine("difficultyPerRow", 15, Lists.newArrayList("How many difficulty levels are required to add", "another row of water to the minigame", "[int] greater than 0", "Will enforce cap of 5"))
				.addLine("baseReelDifficulty", 0.03, Lists.newArrayList("Average reel change (out of 1) per half second.", "higher levels are harder", "[double] ratio out of 1. Game fails when", "the total number goes above 1 or below 0"))
				.addLine("baseReelDeviation", 0.02, Lists.newArrayList("Standard deviation of change in reel change. Larger", "values make for more sporadic, harder games", "[double] ratio out of 1. Google [Std. Deviation]"))
				.addLine("reelDifficultyRate", 0.001, Lists.newArrayList("Amount added per level under fish difficulty to", "reel difficulty", "[double] ratio out of 1"))
				.addLine("reelDifficultyDiscount", .003, Lists.newArrayList("Discount per level given to reel difficulty", "[double] .01 is 1% total discount"))
				.addLine("baseObstacleDifficulty", 2.0, Lists.newArrayList("Average time between obstacle creation. Smaller", "values make for harder games ", "[double] time in seconds, rounds to nearest 0.05"))
				.addLine("baseObstacleDeviation", 1.0, Lists.newArrayList("Standard deviation on time between obstacles", "[double] time in seconds. Google [Std. Deviation]"))
				.addLine("obstacleDifficultyRate", .05, Lists.newArrayList("Time deducted from average time between obstacles", "per level under the fish difficulty", "[double] time in seconds"))
				.addLine("obstacleDifficultyDiscount", 0.0, Lists.newArrayList("Discount per level given to obstacle time", "[double] .01 is 1%"))
				.addLine("baseTimePerDifficulty", 2.0, Lists.newArrayList("Time per difficulty of the fish the player must", "play the minigame and hold it for", "[double] time in seconds"))
				.addLine("maxDifficultyRange", 20, Lists.newArrayList("Biggest gap between player and fish difficulty", "that will be allowed through random catch", "algorithm", "[int] larger than 0"))
				.addLine("timeDiscount", 0.025, Lists.newArrayList("Discount taken off total time per skill level", "[double] .01 is 1%"))
				.addLine("extraFishPerLevel", 0.2, Lists.newArrayList("How many extra fish a level over fish", "difficulty gives. Expected to be a fraction.", "extra fish rounds down. So .8 extra fish is 0", "[double] fish per level. .1 is 1/10 a fish"))
				.addLine("qualityRate", 0.01, Lists.newArrayList("Bonus to quality per fishing skill level", "[double] .01 is 1%"));
			
			
			Map<String, Map<String, Object>> map = new HashMap<>();
			Map<String, Object> sub = new HashMap<>();
			
			sub.put("difficulty", 10);
			ItemStack item = new ItemStack(Material.RAW_FISH);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("Trout");
			meta.setLore(Lists.newArrayList("Moderate fish of low rarity.", "Good for cooking"));
			item.setItemMeta(meta);
			sub.put("icon", item);
			map.put("Trout", sub);
			
			sub = new HashMap<>();
			sub.put("difficulty", 20);
			item = new ItemStack(Material.RAW_FISH, 1, (short) 1);
			meta = item.getItemMeta();
			meta.setDisplayName("Salmon");
			meta.setLore(Lists.newArrayList("Smaller fish popular", "in Western Culture"));
			item.setItemMeta(meta);
			sub.put("icon", item);
			map.put("Salmon", sub);
			
			writer.addLine("fish", map, Lists.newArrayList("List of fish and their difficulties", "Plan difficulties carefully, as players that are", "at a level with no fish in range (maxDifficultyRange)", "are stuck forever!", "name: {difficulty: [int], icon: [itemstack]}"));
			
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
	public void onPlayerFish(PlayerFishEvent e) {
		if (e.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
			return;
		}
		
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getWorlds()
				.contains(e.getPlayer().getWorld().getName())) {
			return;
		}
		

		//e.setCancelled(true);
		e.getCaught().remove();
		
		QuestPlayer qp = QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer(e.getPlayer());
		int level = qp.getSkillLevel(this);
		
		FishRecord record = getFish(level);
		if (record == null) {
			e.getPlayer().sendMessage(badRangeMessage);
			return;
		}
		
		FishEvent event = new FishEvent(qp, new QualityItem(record.icon), record.difficulty);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled()) {
			return;
		}
		
		int deltaDifficulty = Math.max(0, record.difficulty - level);
		double obstacleTime, obstacleDeviation, completionTime;
		int rows, amount;
		float reelDifficulty, reelDeviation;
		
		
		obstacleTime = baseObstacleDifficulty + (deltaDifficulty * obstacleDifficultyRate);
		obstacleDeviation = baseObstacleDeviation;
		completionTime = baseTimePerDifficulty * record.difficulty;
		rows = (int) Math.min(5, Math.max(1, 1 + ((int) record.difficulty / (int) difficultyPerRow)));
		amount = 1 + (int) Math.floor(extraFishPerLevel * Math.max(level - record.difficulty, 0));
		reelDifficulty = baseReelDifficulty + (reelDifficultyRate * deltaDifficulty);
		reelDeviation = baseReelDeviation;
		
		////////Modifer Code - Move to eventhandler if mechs moved out of skill/////////
		
		event.setObstacleDifficultyModifier(event.getObstacleDifficultyModifier()
				- (obstacleDifficultyDiscount * level));
		event.setReelDifficultyModifier(event.getReelDifficultyModifier()
				- (reelDifficultyDiscount * level));
		event.setTimeModifier(event.getTimeModifier() - (timeDiscount * level));
		event.setQualityModifier(event.getQualityModifier() + (level * qualityRate));
		
		////////////////////////////////////////////////////////////////////////////////
		
		//apply modifiers
		obstacleTime *= event.getObstacleDifficultyModifier();
		obstacleDeviation *= event.getObstacleDeviationModifier();
		completionTime *= event.getTimeModifier();
		reelDifficulty *= event.getReelDifficultyModifier();
		reelDeviation *= event.getReelDeviationModifier();
		
		QualityItem reward = new QualityItem(record.icon.clone());
		reward.getUnderlyingItem().setAmount(amount);
		reward.setQuality(reward.getQuality() * event.getQualityModifier());
		
		FishingGui gui = new FishingGui(e.getPlayer(), reward, record.difficulty, rows,
				reelDifficulty, reelDeviation, obstacleTime, obstacleDeviation, completionTime);
		InventoryMenu menu = new ActiveInventoryMenu(qp, gui, new CollectFishAction(qp));
		QuestManagerPlugin.questManagerPlugin.getInventoryGuiHandler().showMenu(e.getPlayer(), menu);
		gui.start();
		
	}

	/**
	 * Finds and returns a fish (if one exists) within {@link #maxDifficultyRange} of the provided
	 * difficulty.
	 * @param difficulty
	 * @return A fish record within the provided limits, or null if none were found
	 */
	private FishRecord getFish(int difficulty) {
		if (fishRecords.isEmpty()) {
			return null;
		}
		
		Collections.shuffle(fishRecords);
		for (FishRecord record : fishRecords) {
			if (Math.abs(record.difficulty - difficulty) <= maxDifficultyRange) {
				return record;
			}
		}
		
		return null;
	}
	
}
