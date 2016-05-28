package com.SkyIsland.QuestManager.Player.Skill.Event;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import com.SkyIsland.QuestManager.Player.PlayerOptions;
import com.SkyIsland.QuestManager.Player.QuestPlayer;

/**
 * Event called when some sort of combat action is happening in a 
 * {@link com.SkyIsland.QuestManager.Configuration.PluginConfiguration#getWorlds() QuestWorld}
 * by a {@link com.SkyIsland.QuestManager.Player.QuestPlayer QuestPlayer}
 * @author Skyler
 *
 */
public class CombatEvent extends Event {
	
	public static final String missMessage = ChatColor.YELLOW + "You missed!" + ChatColor.RESET;
	
	public static final String noDamageMessage = ChatColor.YELLOW + "Your attack had no effect!" + ChatColor.RESET;
	
	public static final String damageMessage = ChatColor.DARK_GRAY + "Your attack did "
			+ ChatColor.RED + "%.2f damage" + ChatColor.DARK_GRAY + " to "
			+ ChatColor.GRAY + "%s" + ChatColor.RESET;
	
	public static void doMiss(QuestPlayer misser, Location targetLocation) {
		if (misser.getOptions().getOption(PlayerOptions.Key.CHAT_COMBAT_RESULT))
			misser.getPlayer().getPlayer().sendMessage(missMessage);
		targetLocation.getWorld().spigot().playEffect(targetLocation, Effect.SMOKE);
		targetLocation.getWorld().playSound(targetLocation, Sound.ENTITY_ENDERDRAGON_FLAP, 1f, 1.85f);
	}

	public static void doNoDamage(QuestPlayer misser, Location targetLocation) {
		if (misser.getOptions().getOption(PlayerOptions.Key.CHAT_COMBAT_RESULT))
			misser.getPlayer().getPlayer().sendMessage(noDamageMessage);
		targetLocation.getWorld().spigot().playEffect(targetLocation, Effect.VILLAGER_THUNDERCLOUD);
		targetLocation.getWorld().playSound(targetLocation, Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
	}
	
	public static void doHit(QuestPlayer misser, Location targetLocation, double damage, String target) {
		if (misser.getOptions().getOption(PlayerOptions.Key.CHAT_COMBAT_DAMAGE)) {
			String modmsg = String.format(damageMessage, damage, target);
			
			misser.getPlayer().getPlayer().sendMessage(modmsg);
		}
		
		//TODO effects?
	}

	private static final HandlerList handlers = new HandlerList();
		
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private QuestPlayer player;
	
	private LivingEntity target;
	
	private ItemStack weapon;
	
	private ItemStack otheritem;
	
	/**
	 * The base amount of damage the player is going to do
	 */
	private double damage;
	
	/**
	 * Damage modification from skills
	 */
	private double modifiedDamage;
	
	/**
	 * Multiplicitive damage modification from skills
	 */
	private double efficiency;
	
	private boolean isMiss;
	
	public CombatEvent(QuestPlayer player, LivingEntity target, ItemStack weapon, ItemStack otheritem, double damage) {
		this.player = player;
		this.target = target;
		this.damage = damage;
		this.weapon = weapon;
		this.otheritem = otheritem;
		this.modifiedDamage = 0.0;
		this.efficiency = 1.0;
		isMiss = false;
	}

	public QuestPlayer getPlayer() {
		return player;
	}

	public void setPlayer(QuestPlayer player) {
		this.player = player;
	}

	public LivingEntity getTarget() {
		return target;
	}
	
	public ItemStack getWeapon() {
		return weapon;
	}
	
	public ItemStack getOtherItem() {
		return otheritem;
	}

	public void setTarget(LivingEntity target) {
		this.target = target;
	}

	/**
	 * Gets the base amount of damage being done. 
	 * @return
	 */
	public double getDamage() {
		return damage;
	}

	/**
	 * Sets the base amount of damage being done.<br />
	 * This is not intended to be changed for simple 'bonus damage' adjustments. For that, see
	 * {@link #setModifiedDamage(double)}
	 * @param damage
	 * @return
	 */
	public void setDamage(double damage) {
		this.damage = damage;
	}

	/**
	 * Returns the current amount of bonus damage being dealt to the target. This includes negative amounts
	 * for damage penalties.
	 * @return
	 */
	public double getModifiedDamage() {
		return modifiedDamage;
	}

	/**
	 * Sets the damage modifier for the event. This includes bonuses and penalties to damage
	 * @param modifiedDamage
	 */
	public void setModifiedDamage(double modifiedDamage) {
		this.modifiedDamage = modifiedDamage;
	}

	/**
	 * Gets the current efficiency of the event.<br />
	 * This is a multiplicitive bonus/penalty done after modifications
	 * @return
	 */
	public double getEfficiency() {
		return efficiency;
	}

	/**
	 * Sets the efficiency the event will be executed at.<br />
	 * This is the final modification done, and is multiplicitive (e.g. efficiency 1 does 100% of damage)
	 * @param efficiency
	 */
	public void setEfficiency(double efficiency) {
		this.efficiency = efficiency;
	}

	public boolean isMiss() {
		return isMiss;
	}

	public void setMiss(boolean isMiss) {
		this.isMiss = isMiss;
	}
	
	/**
	 * Performs all calculations with given parameters to determine the final damage dealt.<br />
	 * Damage will not be negative. This calculation also does not consider whether or not the attack missed.
	 * @return the damage that would be dealt, or 0 if the damage would be negative
	 */
	public double getFinalDamage() {
		double calc = damage + modifiedDamage;
		calc *= efficiency;
		
		return Math.max(calc, 0.0);
	}
	
}
