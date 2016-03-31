package com.SkyIsland.QuestManager.Player.Skill.Event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.SkyIsland.QuestManager.Player.QuestPlayer;

/**
 * Thrown when a {@link com.SkyIsland.QuestManager.Player.QuestPlayer QuestPlayer}
 * is casting a spell.
 * @author Skyler
 *
 */
public class MagicCastEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
		
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private QuestPlayer player;
	
	private int difficulty;
	
	private boolean isFail;
	
	public MagicCastEvent(QuestPlayer player, int difficulty) {
		this.player = player;
		this.difficulty = difficulty;
		isFail = false;
	}

	public QuestPlayer getPlayer() {
		return player;
	}

	public void setPlayer(QuestPlayer player) {
		this.player = player;
	}

	public int getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(int difficulty) {
		this.difficulty = difficulty;
	}

	public boolean isFail() {
		return isFail;
	}

	public void setFail(boolean isFail) {
		this.isFail = isFail;
	}
	
}
