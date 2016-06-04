package com.SkyIsland.QuestManager.Player.Skill.Default;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.Utils.YamlWriter;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.LogSkill;
import com.SkyIsland.QuestManager.Player.Skill.QualityItem;
import com.SkyIsland.QuestManager.Player.Skill.Skill;
import com.SkyIsland.QuestManager.Player.Skill.Event.MineEvent;
import com.SkyIsland.QuestManager.UI.Menu.ActiveInventoryMenu;
import com.SkyIsland.QuestManager.UI.Menu.Action.CollectOreAction;
import com.SkyIsland.QuestManager.UI.Menu.Inventory.Minigames.MiningGui;
import com.google.common.collect.Lists;

public class MiningSkill extends LogSkill implements Listener {
	
	public static final String configName = "Mining.yml";
	
	public static final String badRangeMessage = ChatColor.RED + "Despite your efforts, you were unable to find suitable ore";
	
	public static final String notOreMessage = ChatColor.DARK_GRAY + "There doesn't appear to be any ore near that area";
	
	private static final class OreRecord {
		
		private int difficulty;
		
		private ItemStack icon;
		
		private int oreCount;
		
		private int iconCount;
		
		private int rows;
		
		public OreRecord(int difficulty, ItemStack icon, int oreCount, int iconCount, int rows) {
			this.difficulty = difficulty;
			this.icon = icon;
			this.oreCount = oreCount;
			this.iconCount = iconCount;
			this.rows = rows;
		}
		
	}

	public Type getType() {
		return Skill.Type.TRADE;
	}
	
	public String getName() {
		return "Mining";
	}
	
	public String getDescription(QuestPlayer player) {//proficient
		String ret = ChatColor.WHITE + "Those proficient in mining can mine more delicate ores and do so with more"
				+ " finesse. Additionally, ores mined yield better results of increasing quantity.";
		
		int level = player.getSkillLevel(this);
		
		ret += "\n\n" + ChatColor.GOLD + "Ore Range: " 
				+ Math.max(0, level - maxDifficultyRange) + " - " + (level + maxDifficultyRange);
		ret += "\n" + ChatColor.GOLD + "Ore Quality: +" + (level * qualityRate);
		
		ret += "\n" + ChatColor.GREEN + "Bonus Hits: " 
				+ ((int) (level * hitBonus)) + ChatColor.RESET;
		
		return ret;
	}

	@Override
	public int getStartingLevel() {
		return startingLevel;
	}
	
	@Override
	public String getConfigKey() {
		return "Mining";
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof MiningSkill);
	}
	
	private int startingLevel;
	
	private float baseHardness;
	
	private float hardnessDeviation;
	
	private float hardnessDifficultyRate;
	
	private int baseStartingSpots;
	
	private double startingSpotRate;
	
	private double startingSpotBonus;
	
	private int baseHits;
	
	private double hitRate;
	
	private double hitBonus;
	
	private double extraOrePerLevel;
	
	private int maxDifficultyRange;
	
	private double qualityRate;
	
	private double smeltPenalty;
	
	private boolean smeltingEnabled;
	
	private Map<Material, List<OreRecord>> oreRecords;
	
	public MiningSkill() {
		File configFile = new File(QuestManagerPlugin.questManagerPlugin.getDataFolder(), 
				QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillPath() + configName);
		YamlConfiguration config = createConfig(configFile);

		
		if (!config.getBoolean("enabled", true)) {
			return;
		}
		
		this.startingLevel = config.getInt("startingLevel", 0);
		this.baseHardness = (float) config.getDouble("baseHardness", 1);
		this.hardnessDeviation = (float) config.getDouble("hardnessDeviation", .5);
		this.hardnessDifficultyRate = (float) config.getDouble("hardnessDifficultyRate", .02);
		this.baseStartingSpots = config.getInt("baseStartingSpots", 2);
		this.startingSpotRate = config.getDouble("startingSpotRate", .01);
		this.startingSpotBonus = config.getDouble("startingSpotBonus", 0.02);
		this.baseHits = config.getInt("baseHits", 10);
		this.hitRate = config.getDouble("hitRate", .0675);
		this.hitBonus = config.getDouble("hitBonus", .25);
		this.extraOrePerLevel = config.getDouble("extraOrePerLevel", 0.05);
		this.maxDifficultyRange = config.getInt("maxDifficultyRange", 20);
		this.qualityRate = config.getDouble("qualityRate", 0.01);
		this.smeltingEnabled = config.getBoolean("smeltingEnabled", true);
		this.smeltPenalty = config.getDouble("smeltPenalty", 0.05);
		
		this.oreRecords = new HashMap<>();
		if (!config.contains("ore")) {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Didn't find any ore table"
					+ "for MiningSkill even though it's enabled!");
			return;
		} else {
			ConfigurationSection sex = config.getConfigurationSection("ore"), subsex;
			Material type = null;
			List<OreRecord> list;
			for (String key : sex.getKeys(false)) {
				if (key.startsWith("==")) {
					continue;
				}
				
				try {
					type = Material.valueOf(key.toUpperCase());
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Unable to find matching Material. See Bukkit's Material enum");
				}
				
				subsex = sex.getConfigurationSection(key);
				list = new LinkedList<>();
				for (String name : subsex.getKeys(false)) {
					try {
						list.add(new OreRecord(
								subsex.getInt(name + ".difficulty"), subsex.getItemStack(name + ".item", new ItemStack(Material.COAL_ORE)),
								subsex.getInt(name + ".oreCount"), subsex.getInt(name + ".iconCount"), subsex.getInt(name + ".rows")
								));
					} catch (Exception e) {
						e.printStackTrace();
						QuestManagerPlugin.questManagerPlugin.getLogger().warning("Skipping that one! ^");
					}
				}
				
				oreRecords.put(type, list);
				
			}
		}
				
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
		MiningGui.setMiningSkill(this);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			YamlWriter writer = new YamlWriter();
			
			writer.addLine("enabled", true, Lists.newArrayList("Whether or not this skill is allowed to be used.", "Note: To turn off imbueing altogether, see the imbument config", "in folder defaultly one up from here: imbuement.yml", "true | false"))
				.addLine("startingLevel", 0, Lists.newArrayList("The level given to players who don't have this skill yet", "[int]"))
				.addLine("baseHardness", 1, Lists.newArrayList("Base toughness of mining materials.", "[double] ranges from 0 to 2"))
				.addLine("hardnessDeviation", 0.5, Lists.newArrayList("Standard deviation on material type", "from the above average", "[double] Hardness is a scale from 0-2"))
				.addLine("hardnessDifficultyRate", 0.02, Lists.newArrayList("How much hardness is added to average", "per difficulty of the ore", "[double] Hardness goes from 0-2, so..."))
				.addLine("baseStartingSpots", 2, Lists.newArrayList("Base number of starting spots to give", "players. More spots makes for easier games", "[int] number of spots"))
				.addLine("startingSpotRate", .01, Lists.newArrayList("Negative spots per difficulty. Regardless,", "players will always have at least 1 spot", "[double] number of spots per level"))
				.addLine("startingSpotBonus", 0.02, Lists.newArrayList("Extra slots given per skill level", "[double] Slots per level added"))
				.addLine("baseHits", 20, Lists.newArrayList("Base number of hits a player can do", "without failing the game", "[int] block hits"))
				.addLine("hitRate", .0675, Lists.newArrayList("Number of hits deducted per difficulty", "over the player's level", "[double] hits per difficulty"))
				.addLine("hitBonus", .25, Lists.newArrayList("Extra hits given per skill level", "[double] hits per level"))
				.addLine("extraOrePerLevel", 0.05, Lists.newArrayList("Extra pieces of ore given to a player", "per level over difficulty level", "[double] 1.0 is a whole extra piece"))
				.addLine("maxDifficultyRange", 20, Lists.newArrayList("Biggest gap between player and ore difficulty", "that will be allowed through random ore", "algorithm", "[int] larger than 0"))
				.addLine("qualityRate", 0.01, Lists.newArrayList("Bonus to quality per mining skill level", "[double] .01 is 1%"))
				.addLine("smeltingEnabled", true, Lists.newArrayList("Can players use ores on lava to combine it with another", "and get a single item stack of average quality", "[true|false]"))
				.addLine("smeltPenalty", 0.05, Lists.newArrayList("If smelting two items, how much of the sum quality", "is lost in the process?", "[double] .01 is 1%"));
			
			
			Map<String, Map<String, Map<String, Object>>> map = new HashMap<>();
			Map<String, Map<String, Object>> typeList = new HashMap<>();
			Map<String, Object> sub = new HashMap<>();
			
			sub.put("difficulty", 10);
			ItemStack item = new ItemStack(Material.COAL_ORE);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName("Coal Ore");
			meta.setLore(Lists.newArrayList("Low quality ore with", "many common uses"));
			item.setItemMeta(meta);
			sub.put("item", item);
			sub.put("oreCount", 2);
			sub.put("iconCount", 6);
			sub.put("rows", 6);
			typeList.put("Coal", sub);
			sub = new HashMap<>();
			sub.put("difficulty", 15);
			item = new ItemStack(Material.COAL_ORE);
			meta = item.getItemMeta();
			meta.setDisplayName("Miner's Coal Ore");
			meta.setLore(Lists.newArrayList("Tougher than regular coal", "but burns less bright"));
			item.setItemMeta(meta);
			sub.put("item", item);
			sub.put("oreCount", 1);
			sub.put("iconCount", 4);
			sub.put("rows", 4);
			typeList.put("MinersCoal", sub);
			
			map.put("COAL_ORE", typeList);
			
			typeList = new HashMap<>();
			sub = new HashMap<>();
			sub.put("difficulty", 25);
			item = new ItemStack(Material.IRON_ORE);
			meta = item.getItemMeta();
			meta.setDisplayName("Poor Iron Ore");
			meta.setLore(Lists.newArrayList("Low quality iron ore"));
			item.setItemMeta(meta);
			sub.put("item", item);
			sub.put("oreCount", 1);
			sub.put("iconCount", 4);
			sub.put("rows", 5);
			typeList.put("Iron Ore", sub);
			map.put("IRON_ORE", typeList);
			
			writer.addLine("ore", map, Lists.newArrayList("List of ore and their difficulties", "Note: IconCount is how many pieces", "of ore are in the game; oreCount is", "the amount of the ore the player gets", "Plan difficulties carefully, as players that are", "at a level with no ore in range (maxDifficultyRange)", "are stuck forever!", "[Material]: -name: {difficulty: [int], icon: [itemstack], iconCount: [int], oreCount: [int], rows: [int]}"));
			
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
	public void onPlayerMine(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			playerSmeltEvent(e);
			return;
		}
		
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getWorlds()
				.contains(e.getPlayer().getWorld().getName())) {
			return;
		}
		
		if (e.getItem() == null || !e.getItem().getType().name().contains("PICKAXE")) {
			playerSmeltEvent(e);
			return;
		}
		
		if (!oreRecords.containsKey(e.getClickedBlock().getType())) {
			e.getPlayer().sendMessage(notOreMessage);
			return;
		}

		//e.setCancelled(true);
		
		QuestPlayer qp = QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer(e.getPlayer());
		int level = qp.getSkillLevel(this);
		
		OreRecord record = getOre(e.getClickedBlock().getType(), level);
		if (record == null) {
			e.getPlayer().sendMessage(badRangeMessage);
			return;
		}
		
		MineEvent event = new MineEvent(qp, new QualityItem(record.icon), record.difficulty);
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isCancelled()) {
			return;
		}
		
		int deltaDifficulty = Math.max(0, record.difficulty - level);
		float averageHardness;
		int rows, amount, startingSlots, hits, iconCount;
		float hardnessDeviation;
		
		
		averageHardness = baseHardness + (record.difficulty * hardnessDifficultyRate);
		hardnessDeviation = this.hardnessDeviation;
		hits = (int) Math.round(baseHits - (hitRate* deltaDifficulty));
		startingSlots = (int) Math.round(baseStartingSpots - (startingSpotRate * record.difficulty));
		rows = record.rows;
		amount = record.oreCount;
		iconCount = record.iconCount;
		
		////////Modifer Code - Move to eventhandler if mechs moved out of skill/////////
		
		event.setOpenSlotsModifier(event.getOpenSlotsModifier()
				+ (startingSpotBonus * level));
		event.setHitsModifier(event.getHitsModifier() + (hitBonus * level));
		event.setAmountModifier(event.getAmountModifier() + (extraOrePerLevel * deltaDifficulty));
		event.setQualityModifier(event.getQualityModifier() + (level * qualityRate));
		
		////////////////////////////////////////////////////////////////////////////////
		
		//apply modifiers
		averageHardness *= event.getHardnessModifier();
		hits *= event.getHitsModifier();
		startingSlots *= event.getOpenSlotsModifier();
		amount *= event.getAmountModifier();
		
		QualityItem reward = new QualityItem(record.icon.clone());
		reward.getUnderlyingItem().setAmount(amount);
		reward.setQuality(reward.getQuality() * event.getQualityModifier());
		
		//Player player, QualityItem result, int skillLevel, int oreCount, int depth, int blockHits,
		//double averageHardness,	double hardnessDeviation, int startingSpots, double bonusQuality, ItemStack oreIcon
		MiningGui gui = new MiningGui(e.getPlayer(), reward, record.difficulty, iconCount, rows,
				hits, averageHardness, hardnessDeviation, startingSlots, .2, record.icon);
		ActiveInventoryMenu menu = new ActiveInventoryMenu(qp, gui, new CollectOreAction(qp));
		QuestManagerPlugin.questManagerPlugin.getInventoryGuiHandler().showMenu(e.getPlayer(), menu);
		gui.start();
		
	}

	/**
	 * Finds and returns a fish (if one exists) within {@link #maxDifficultyRange} of the provided
	 * difficulty.
	 * @param difficulty
	 * @return A fish record within the provided limits, or null if none were found
	 */
	private OreRecord getOre(Material type, int difficulty) {
		if (oreRecords.isEmpty()) {
			return null;
		}
		
		List<OreRecord> list = oreRecords.get(type);
		if (list == null || list.isEmpty()) {
			return null;
		}
		
		Collections.shuffle(list);
		for (OreRecord record : list) {
			if (Math.abs(record.difficulty - difficulty) <= maxDifficultyRange) {
				return record;
			}
		}
		
		return null;
	}
	
	private void playerSmeltEvent(PlayerInteractEvent e) {
		if (!smeltingEnabled) {
			return;
		}
		
		Set<Material> s = null;
		List<Block> sight = e.getPlayer().getLineOfSight(s, 4);
		boolean trip = false;
		for (Block block : sight) {
			if (block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA) {
				trip = true;
				break;
			}
		}
		
		
		if (!trip) {
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
			e.getPlayer().sendMessage(ChatColor.YELLOW + "Unable to find matching ore in inventory to combine with");
			return;
		}
		
		//same item,both have quality
		double sum = 0, quality;
		String cache = ChatColor.stripColor(match.getItemMeta().getLore().get(0));
		quality = Double.valueOf(cache.substring(cache.indexOf(":") + 1).trim());
		sum += (quality * match.getAmount());
		
		
		cache = ChatColor.stripColor(smeltItem.getItemMeta().getLore().get(0));
		quality = Double.valueOf(cache.substring(cache.indexOf(":") + 1).trim());
		sum += (quality * smeltItem.getAmount());
		
		sum *= 1 - smeltPenalty;
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
	
}
