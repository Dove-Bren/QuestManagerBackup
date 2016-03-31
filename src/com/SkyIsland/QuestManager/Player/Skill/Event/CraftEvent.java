package com.SkyIsland.QuestManager.Player.Skill.Event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.SkyIsland.QuestManager.Player.QuestPlayer;

/**
 * Called when a {@link com.SkyIsland.QuestManager.Player.QuestPlayer QuestPlayer} is crafting
 * an object. Specifically, called when the player is using a non-standard crafting interface.
 * @author Skyler
 *
 */
public class CraftEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
		
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private QuestPlayer player;
	
	private boolean isFail;
	
	private Recipe recipe;
	
	private int difficulty;
	
	private ItemStack outcome;
	
	public CraftEvent(QuestPlayer player, Recipe recipe, int difficulty, ItemStack outcome) {
		this.player = player;
		this.isFail = false;
		this.recipe = recipe;
		this.difficulty = difficulty;
		this.outcome = outcome;
	}

	public QuestPlayer getPlayer() {
		return player;
	}

	public void setPlayer(QuestPlayer player) {
		this.player = player;
	}

	public boolean isFail() {
		return isFail;
	}

	public void setFail(boolean isFail) {
		this.isFail = isFail;
	}

	public Recipe getRecipe() {
		return recipe;
	}

	public void setRecipe(Recipe recipe) {
		this.recipe = recipe;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}

	public ItemStack getOutcome() {
		return outcome;
	}

	public void setOutcome(ItemStack outcome) {
		this.outcome = outcome;
	}	
}
