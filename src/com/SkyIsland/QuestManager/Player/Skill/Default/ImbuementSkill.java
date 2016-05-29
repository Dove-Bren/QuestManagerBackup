package com.SkyIsland.QuestManager.Player.Skill.Default;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.Utils.YamlWriter;
import com.SkyIsland.QuestManager.Magic.ImbuementHandler;
import com.SkyIsland.QuestManager.Magic.ImbuementSet;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.LogSkill;
import com.SkyIsland.QuestManager.Player.Skill.Skill;
import com.google.common.collect.Lists;

/**
 * Dictates the difficulty and potential of spell weaving spells
 * @author Skyler
 *
 */
public class ImbuementSkill extends LogSkill implements Listener {
	
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
		ret += "\n\n" + ChatColor.GOLD + "Imbuement Slots: " + handler.getImbuementSlots(player);
		
		ret += "\n" + ChatColor.GREEN + "Imbuement Potency: " 
				+ ((int) (100 + (100 * handler.getPotencyBonus(player)))) + "%" + ChatColor.RESET;
		ret += "\n" + ChatColor.DARK_BLUE + "Imbue Time: " + ((float) this.getApplyTime(player));
		ret += "\n" + ChatColor.DARK_BLUE + "Mana Discount: " 
				+ ((float) (100 * player.getSkillLevel(this) * slashDiscountRate) + "%");
		
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
	
	/**
	 * How much, per level, is taken off of the time to apply
	 */
	private double applyDiscountRate;
	
	/**
	 * The base amount of time it takes to apply
	 */
	private double applyTime;
	
	private double applyCost;
	
	private double slashCost;
	
	private double slashAspectPenalty;
	
	private boolean enabled;
	
	public ImbuementSkill() {
		File configFile = new File(QuestManagerPlugin.questManagerPlugin.getDataFolder(), 
				QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillPath() + configName);
		YamlConfiguration config = createConfig(configFile);

		
		if (!config.getBoolean("enabled", true)) {
			enabled = false;
			return;
		}
		enabled = true;
		
		this.startingLevel = config.getInt("startingLevel", 0);
		this.applyTime = config.getDouble("applyTime", 5.0);
		this.applyDiscountRate = config.getDouble("applyDiscountRate", 0.0075);
		this.applyCost = config.getDouble("applyCost", 0.0);
		this.slashCost = config.getDouble("slashBaseCost", 10.0);
		this.slashAspectPenalty = config.getDouble("slashAspectPenalty", 1.25);
		this.slashDiscountRate = config.getDouble("slashDiscountRate", 0.008);
		
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
		QuestManagerPlugin.questManagerPlugin.getImbuementHandler().setImbuementSkill(this);
	}
	
	private YamlConfiguration createConfig(File configFile) {
		if (!configFile.exists()) {
			YamlWriter writer = new YamlWriter();
			
			writer.addLine("enabled", true, Lists.newArrayList("Whether or not this skill is allowed to be used.", "Note: To turn off imbueing altogether, see the imbument config", "in folder defaultly one up from here: imbuement.yml", "true | false"))
				.addLine("startingLevel", 0, Lists.newArrayList("The level given to players who don't have this skill yet", "[int]"))
				.addLine("applyTime", 5.0, Lists.newArrayList("How long it takes for an imbuement to be applied and ready", "to use. This is in seconds. Rounded to nearest 0.05 seconds (tick)", "[double]"))
				.addLine("applyDiscountRate", 0.0075, Lists.newArrayList("How much of the apply time is taken off per level", "[double] 0.01 is 1%"))
				.addLine("applyCost", 0.0, Lists.newArrayList("Decides how much it is to apply an imbuement. If set to 0, the", "cost is calculated as the cost per slash. If greater then", "0, it is multiplied by the number of aspects to get the cost", "[double] 0, or some positive number"))
				.addLine("slashBaseCost", 10.0, Lists.newArrayList("Base cost per mana cost per imbuement slash. This is multiplied", "by the potency of each effect per effect on the imbuement.", "so an imbuement at potency 0.45 would contribute (0.45*base) to the slash cost", "[double]"))
				.addLine("slashAspectPenalty", 1.25, Lists.newArrayList("Penalty multiplied in after getting a total from the", "base cost calculation. Each effect beyond 1 results in", "the cost being multiplied by this number. In other words,", "total cost = (base total) * (penalty ^ (#effects - 1))", "[double] 1 is no penalty. 0 through 1 decrease cost. Neg unsupported"))
				.addLine("slashDiscountRate", 0.008, Lists.newArrayList("The penalty per level under apprentiveLevel given to the", "chance to hit. Penalty is", "(  ([calculated spell level] + levelGrace) * hitchancePenalty )", "[double]"));
			
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
	
	/**
	 * returns the configed apply time<br />
	 * If not enabled, returns 0.
	 * @return
	 */
	public double getApplyTime(QuestPlayer player) {
		if (!enabled) {
			return 0;
		}
		
		double bonus = 1 - (player.getSkillLevel(this) * applyDiscountRate);
		bonus = Math.max(0, bonus);

		return applyTime * bonus;
	}
	
	/**
	 * Calculates the apply cost from config variables.<br />
	 * If not enabled, returns 0.
	 * @return
	 */
	public double getApplyCost(QuestPlayer player, ImbuementSet effects) {
		if (!enabled || effects == null || effects.getEffectMap().isEmpty()) {
			return 0;
		}
		
		if (applyCost <= 0) {
			return getSlashCost(player, effects);
		}
		
		return applyCost * effects.getEffectMap().size();
	}
	
	/**
	 * Calculates the per-slash cost from config variables. This includes calculating any player 
	 * bonuses<br />
	 * If not enabled, returns 0.
	 * @return
	 */
	public double getSlashCost(QuestPlayer player, ImbuementSet effects) {
		if (!enabled || effects == null || effects.getEffectMap().isEmpty()) {
			return 0;
		}
		
		double total = 0;
		
		for (Double potency : effects.getEffectMap().values()) {
			total += (potency * slashCost);
		}
		
		total *= Math.pow(slashAspectPenalty, effects.getEffectMap().size() - 1);
		
		int lvl = player.getSkillLevel(this);
		double bonus = 1 - (lvl * slashDiscountRate);
		
		bonus = Math.max(0, bonus);
		
		return total * bonus;
	}
	
	
	
}
