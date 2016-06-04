package com.SkyIsland.QuestManager.Player.Skill.Default;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wood;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.Utils.LocationState;
import com.SkyIsland.QuestManager.Configuration.Utils.YamlWriter;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.LogSkill;
import com.SkyIsland.QuestManager.Player.Skill.QualityItem;
import com.SkyIsland.QuestManager.Player.Skill.Skill;
import com.SkyIsland.QuestManager.Player.Skill.Event.WoodChopEvent;
import com.SkyIsland.QuestManager.Region.Region;
import com.SkyIsland.QuestManager.Region.RegionManager;
import com.SkyIsland.QuestManager.UI.ActionSequence.LumberjackSequence;
import com.google.common.collect.Lists;

public class LumberjackSkill extends LogSkill implements Listener {
	
	public static final String configName = "Lumberjack.yml";
	
	public static final String badRangeMessage = ChatColor.RED + "Despite your efforts, you were unable to find suitable wood";
	
	public static final String notOreMessage = ChatColor.DARK_GRAY + "There doesn't appear to be any good wood near that area";
	
	public static final String tooSoonMessage = ChatColor.DARK_GRAY + "The wood has yet to regrow on this tree";
	
	private static final class TreeRecord {
		
		private int difficulty;
		
		private ItemStack reward;
		
		private int woodCount;
		
		private String name;
		
		private MaterialData woodType;
		
		private Region region;
		
		/**
		 * Creates a new tree record to dictate what kind of trees a player can find when attempting to
		 * chop down a tree.
		 * @param difficulty
		 * @param name
		 * @param reward
		 * @param woodCount
		 * @param woodType
		 * @param region The region this tree can be found in (regardless of if type matches), or null
		 * if no region is defined (anytime the tree type matches, this record is valid)
		 */
		public TreeRecord(int difficulty, String name, ItemStack reward, int woodCount,
				MaterialData woodType, Region region) {
			this.difficulty = difficulty;
			this.name = name;
			this.reward = reward;
			this.woodCount = woodCount;
			this.woodType = woodType;
			this.region = region;
		}
		
		public boolean isValid(Block block) {
			if (region != null)
			if (!region.isIn(block.getLocation()))
					return false;
			
			
			return block.getState().getData().equals(woodType);
		}
		
	}

	public Type getType() {
		return Skill.Type.TRADE;
	}
	
	public String getName() {
		return "Lumberjack";
	}
	
	public String getDescription(QuestPlayer player) {//proficient
		String ret = ChatColor.WHITE + "Lumberjacks fell trees and claim their valuable wood. More skilled"
				+ " lumberjacks do the work with fewer strikes, yet more reward.";
		
		int level = player.getSkillLevel(this);
		
		ret += "\n\n" + ChatColor.GOLD + "Tree Range: " 
				+ Math.max(0, level - maxDifficultyRange) + " - " + (level + maxDifficultyRange);
		ret += "\n" + ChatColor.GOLD + "Wood Quality: +" + (level * qualityRate);
		
		ret += "\n" + ChatColor.GREEN + "Hit Discount: " 
				+ ((float) (level * hitBonus)) + ChatColor.RESET;
		
		return ret;
	}

	@Override
	public int getStartingLevel() {
		return startingLevel;
	}
	
	@Override
	public String getConfigKey() {
		return "Lumberjack";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof LumberjackSkill);
	}
	
	private int startingLevel;
	
	private double timingBase;
	
	private double timingRate;
	
	private double baseDelay;
	
	private double delayDeviation;
	
	private int baseHits;
	
	private double hitRate;
	
	private double hitBonus;
	
	private double extraWoodPerLevel;
	
	private int maxDifficultyRange;
	
	private double qualityRate;
	
	private double millPenalty;
	
	private boolean millingEnabled;
	
	private List<TreeRecord> treeRecords;
	
	private Map<UUID, LumberjackSequence> activeSessions;
	
	public LumberjackSkill() {
		File configFile = new File(QuestManagerPlugin.questManagerPlugin.getDataFolder(), 
				QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillPath() + configName);
		YamlConfiguration config = createConfig(configFile);

		
		if (!config.getBoolean("enabled", true)) {
			return;
		}
		
		this.startingLevel = config.getInt("startingLevel", 0);
		this.timingBase = config.getDouble("timingBase", .3);
		this.timingRate = config.getDouble("timingRate", .002);
		this.baseDelay = config.getDouble("baseTime", 1.5);
		this.delayDeviation = config.getDouble("delayDeviation", .25);
		this.baseHits = config.getInt("baseHits", 5);
		this.hitRate = config.getDouble("hitRate", .1);
		this.hitBonus = config.getDouble("hitBonus", .005);
		this.extraWoodPerLevel = config.getDouble("extraWoodPerLevel", 0.05);
		this.maxDifficultyRange = config.getInt("maxDifficultyRange", 20);
		this.qualityRate = config.getDouble("qualityRate", 0.01);
		this.millingEnabled = config.getBoolean("millingEnabled", true);
		this.millPenalty = config.getDouble("millPenalty", 0.05);
		
		this.activeSessions = new HashMap<>();
		this.treeRecords = new LinkedList<>();
		if (!config.contains("trees")) {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Didn't find any tree table"
					+ "for LumberjackSkill even though it's enabled!");
			return;
		} else {
			/*
			 * trees:
			 *   Elm:
			 *     difficulty: 40
			 *     ...
			 *   Witchwood:
			 *     difficulty: 85
			 *     ...
			 */
			ConfigurationSection sex = config.getConfigurationSection("trees"), subsex;
			RegionManager rManager = QuestManagerPlugin.questManagerPlugin.getEnemyManager();
			for (String key : sex.getKeys(false)) {
				if (key.startsWith("==")) {
					continue;
				}
								
				subsex = sex.getConfigurationSection(key);
				try {
					Wood wood = new Wood(Material.LOG, TreeSpecies.valueOf(subsex.getString("treeType").toUpperCase()));
					treeRecords.add(new TreeRecord(
							subsex.getInt("difficulty"), key,
							subsex.getItemStack("reward"), subsex.getInt("woodCount"),
							wood, rManager.getRegion(subsex.contains("region") ? 
									(subsex.get("region") == null ? null : ((LocationState) subsex.get("region")).getLocation()) 
									: null)
							));
				} catch (Exception e) {
					e.printStackTrace();
					QuestManagerPlugin.questManagerPlugin.getLogger().warning("Skipping that one! ^");
				}
				
			}
		}
				
		LumberjackSequence.setSkillLink(this);
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			YamlWriter writer = new YamlWriter();
			
			writer.addLine("enabled", true, Lists.newArrayList("Whether or not this skill is allowed to be used.", "Note: To turn off imbueing altogether, see the imbument config", "in folder defaultly one up from here: imbuement.yml", "true | false"))
				.addLine("startingLevel", 0, Lists.newArrayList("The level given to players who don't have this skill yet", "[int]"))
				.addLine("timingBase", .3, Lists.newArrayList("Base time a player can be early or late", "and still count as having hit", "[double] number of seconds"))
				.addLine("timingRate", .002, Lists.newArrayList("How many fewer seconds per difficulty", "that a player can hit and have it count", "[double] number of seconds"))
				.addLine("baseDelay", 1.5, Lists.newArrayList("Average time it takes to wind up a good", "swing. Shorter times are harder", "[double] number of seconds"))
				.addLine("delayDeviation", .25, Lists.newArrayList("Standard deviation on time for a", "full swing. Google [Std Deviation] for info", "[double] number of seconds"))
				.addLine("baseHits", 5, Lists.newArrayList("Base number of hits a player must make", "to fell the tree and get the wood.", "Larger numbers mean longer cutting times", "[int] larger than 0"))
				.addLine("hitRate", 0.1, Lists.newArrayList("Hits added to total hit count per", "difficulty of the wood", "[double] number of hits"))
				.addLine("hitBonus", .25, Lists.newArrayList("How much of the hits to subtract per", "skill level", "[double] .01 is 1%"))
				.addLine("extraWoodPerLevel", 0.05, Lists.newArrayList("Extra pieces of wood given to a player", "per level over difficulty level", "[double] 1.0 is a whole extra log"))
				.addLine("maxDifficultyRange", 20, Lists.newArrayList("Biggest gap between player and ore difficulty", "that will be allowed through random ore", "algorithm", "[int] larger than 0"))
				.addLine("qualityRate", 0.01, Lists.newArrayList("Bonus to quality per mining skill level", "[double] .01 is 1%"))
				.addLine("millingEnabled", true, Lists.newArrayList("Can players use logs on crafting tables", "and get a single item stack of average quality", "[true|false]"))
				.addLine("millPenalty", 0.05, Lists.newArrayList("If milling two items, how much of the sum quality", "is lost in the process?", "[double] .01 is 1%"));
			
			
			Map<String, Map<String, Object>> map = new HashMap<>();
			Map<String, Object> sub = new HashMap<>();
			
			sub.put("difficulty", 10);
			ItemStack item = (new Wood(Material.LOG, TreeSpecies.GENERIC)).toItemStack();
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("Oak Wood");
			meta.setLore(Lists.newArrayList("Common wood of common", "quality. Very useful"));
			item.setItemMeta(meta);
			sub.put("reward", item);
			sub.put("treeType", TreeSpecies.GENERIC.name());
			sub.put("woodCount", 2);
			sub.put("region", null);
			map.put("Oak", sub);
			
			
			sub = new HashMap<>();
			sub.put("difficulty", 15);
			item = (new Wood(Material.LOG, TreeSpecies.GENERIC)).toItemStack();
			meta = item.getItemMeta();
			meta.setDisplayName("WarmOak Wood");
			meta.setLore(Lists.newArrayList("Specialty oak prized over", "regular oak for it's color"));
			item.setItemMeta(meta);
			sub.put("reward", item);
			sub.put("treeType", TreeSpecies.GENERIC.name());
			sub.put("woodCount", 1);
			sub.put("region", new Location(Bukkit.getWorld("QuestWorld"), -400, 55, -855));
			map.put("WarmOak", sub);
			
			
			sub = new HashMap<>();
			sub.put("difficulty", 20);
			item = (new Wood(Material.LOG, TreeSpecies.BIRCH)).toItemStack();
			meta = item.getItemMeta();
			meta.setDisplayName("Birch Wood");
			meta.setLore(Lists.newArrayList("Light, eyed wood with little", "strength. It's value comes from", "it's color"));
			item.setItemMeta(meta);
			sub.put("reward", item);
			sub.put("treeType", TreeSpecies.BIRCH.name());
			sub.put("woodCount", 2);
			sub.put("region", null);
			map.put("Birch", sub);
			
			
			writer.addLine("trees", map, Lists.newArrayList("List of wood types and trees/regions", "they can be found at. If region is", "null, only the wood type", "and player level is taken into account.", "Plan difficulties carefully, as players that are", "at a level with no wood in range (maxDifficultyRange)", "are stuck forever!", "name: {difficulty: [int], reward: [itemstack], woodCount: [int], treeType: [TreeSpecies], region: [null or Location]}"));
			
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
	public void onPlayerChop(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		if (activeSessions.containsKey(e.getPlayer().getUniqueId())) {
			//e.getPlayer().sendMessage(ChatColor.YELLOW + "You're already involved in a wood chopping sequence");
			return;
		}

		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getWorlds()
				.contains(e.getPlayer().getWorld().getName())) {
			return;
		}

		if (e.getItem() == null || !e.getItem().getType().name().contains("AXE")) {
			playerMillEvent(e);
			return;
		}

		if (!e.getClickedBlock().getType().name().contains("LOG")) {
			return;
		}

		//e.setCancelled(true);
		
		QuestPlayer qp = QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer(e.getPlayer());
		int level = qp.getSkillLevel(this);
		
		TreeRecord record = getTree(e.getClickedBlock(), level);
		if (record == null) {
			e.getPlayer().sendMessage(badRangeMessage);
			return;
		}
		
		WoodChopEvent event = new WoodChopEvent(qp, new QualityItem(record.reward), record.difficulty);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled()) {
			return;
		}
		
		int deltaDifficulty = Math.max(0, record.difficulty - level);
		int amount, hits;
		double timing, averageSwing, swingDeviation;
		
		
		timing = timingBase + (record.difficulty * timingRate);
		averageSwing = baseDelay;
		swingDeviation = delayDeviation;
		hits = (int) Math.round(baseHits + (hitRate * deltaDifficulty));
		amount = record.woodCount;
		
		////////Modifer Code - Move to eventhandler if mechs moved out of skill/////////
		
		event.setHitsModifier(event.getHitsModifier() - (hitBonus * level));
		event.setAmountModifier(event.getAmountModifier() + (extraWoodPerLevel * deltaDifficulty));
		event.setQualityModifier(event.getQualityModifier() + (level * qualityRate));
		
		////////////////////////////////////////////////////////////////////////////////
		
		//apply modifiers
		timing *= event.getTimingModifier();
		averageSwing *= event.getSwingTimeModifier();
		hits *= event.getHitsModifier();
		amount *= event.getAmountModifier();
		
		QualityItem reward = new QualityItem(record.reward.clone());
		reward.getUnderlyingItem().setAmount(amount);
		reward.setQuality(reward.getQuality() * event.getQualityModifier());
		
		//QuestPlayer player, Vector treeLocation, QualityItem input, double averageSwingTime,
		//double swingTimeDeviation, double reactionTime, int hits, String displayName
		LumberjackSequence sequence = new LumberjackSequence(qp, e.getClickedBlock().getLocation().toVector(),
				reward, averageSwing, swingDeviation, timing, hits, record.name, record.difficulty);
		activeSessions.put(e.getPlayer().getUniqueId(), sequence);
		sequence.start();
		
	}

	/**
	 * Finds and returns a fish (if one exists) within {@link #maxDifficultyRange} of the provided
	 * difficulty.
	 * @param difficulty
	 * @return A fish record within the provided limits, or null if none were found
	 */
	private TreeRecord getTree(Block clickedBlock, int difficulty) {
		if (treeRecords.isEmpty()) {
			return null;
		}
		
		List<TreeRecord> list = new LinkedList<>();
		for (TreeRecord record : treeRecords) {
			if (record.isValid(clickedBlock))
				list.add(record);
		}
		
		if (list == null || list.isEmpty()) {
			return null;
		}
		
		Collections.shuffle(list);
		for (TreeRecord record : list) {
			if (Math.abs(record.difficulty - difficulty) <= maxDifficultyRange) {
				return record;
			}
		}
		
		return null;
	}
	
	private void playerMillEvent(PlayerInteractEvent e) {
		if (!millingEnabled) {
			return;
		}
		
		if (e.getClickedBlock() == null)
			return;
		
		
		if (e.getClickedBlock().getType() != Material.WORKBENCH) {
			System.out.println("Material: " + e.getClickedBlock().getType().name());
			return;
		}
		
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getWorlds()
				.contains(e.getPlayer().getWorld().getName())) {
			return;
		}
		
		ItemStack smeltItem = e.getItem();
		if (smeltItem == null || !smeltItem.hasItemMeta()) {
			return;
		}

		if (smeltItem.getItemMeta().getLore() == null
				|| !smeltItem.getItemMeta().getLore().get(0).contains("Quality: ")) {
			return;
		}
		
		e.setCancelled(true);
		
		ItemStack match = null;
		int slot = -1;
		for (Entry<Integer, ? extends ItemStack> item : e.getPlayer().getInventory().all(smeltItem.getType()).entrySet()) {
			
			if (!item.getValue().hasItemMeta())
				continue;
			
			ItemMeta meta = item.getValue().getItemMeta();
			if (meta.getLore() == null || !meta.getLore().get(0).contains("Quality: ")) {
				continue;
			}
			
			if (item.getValue().getDurability() != smeltItem.getDurability()
					|| !meta.getDisplayName().equals(smeltItem.getItemMeta().getDisplayName()))
				continue;
			
			if (item.getValue().equals(smeltItem)) 
				continue;
			
			match = item.getValue();
			slot = item.getKey();
			break;
		}
		
		if (match == null) {
			e.getPlayer().sendMessage(ChatColor.YELLOW + "Unable to find matching logs in inventory to combine with");
			return;
		}
		
		e.setCancelled(true);
		
		//same item,both have quality
		double sum = 0, quality;
		String cache = ChatColor.stripColor(match.getItemMeta().getLore().get(0));
		quality = Double.valueOf(cache.substring(cache.indexOf(":") + 1).trim());
		sum += (quality * match.getAmount());
		
		
		cache = ChatColor.stripColor(smeltItem.getItemMeta().getLore().get(0));
		quality = Double.valueOf(cache.substring(cache.indexOf(":") + 1).trim());
		sum += (quality * smeltItem.getAmount());
		
		sum *= 1 - millPenalty;
		int quantity = match.getAmount() + smeltItem.getAmount();
		sum /= quantity;
		
		
		e.getPlayer().getInventory().setItem(e.getPlayer().getInventory().getHeldItemSlot(), null);
		e.getPlayer().getInventory().setItem(slot, null);
		smeltItem.setAmount(quantity);
		List<String> lore = smeltItem.getItemMeta().getLore();
		lore.remove(0);
		ItemMeta meta = smeltItem.getItemMeta();
		meta.setLore(lore);
		smeltItem.setItemMeta(meta);
		QualityItem result = new QualityItem(smeltItem.clone(), sum);
		e.getPlayer().getInventory().addItem(result.getItem());
	}
	
	public void playerFinish(QuestPlayer player) {
		activeSessions.remove(player.getPlayer().getUniqueId());
	}
	
}
