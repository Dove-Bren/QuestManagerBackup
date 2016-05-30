package com.SkyIsland.QuestManager.Player.Skill.Event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.QualityItem;

public class FishEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private QuestPlayer player;
	
	private QualityItem fish;
	
	private int difficulty;
	
	private double reelDifficultyModifier;
	
	private double reelDeviationModifier;
	
	private double timeModifier;
	
	private double obstacleDifficultyModifier;
	
	private double obstacleDeviationModifier;
	
	private double qualityModifier;
	
	private boolean isCancelled;
	
	public FishEvent(QuestPlayer player, QualityItem result, int difficulty) {
		this.player = player;
		this.fish = result;
		this.difficulty = difficulty;
		this.isCancelled = false;
		
		this.reelDeviationModifier = this.reelDifficultyModifier = this.timeModifier = this.obstacleDifficultyModifier
				= this.obstacleDeviationModifier = this.qualityModifier = 1.0;
	}

	public double getReelDifficultyModifier() {
		return reelDifficultyModifier;
	}

	public void setReelDifficultyModifier(double reelDifficultyModifier) {
		this.reelDifficultyModifier = reelDifficultyModifier;
	}

	public double getReelDeviationModifier() {
		return reelDeviationModifier;
	}

	public void setReelDeviationModifier(double reelDeviationModifier) {
		this.reelDeviationModifier = reelDeviationModifier;
	}

	public double getTimeModifier() {
		return timeModifier;
	}

	public void setTimeModifier(double timeModifier) {
		this.timeModifier = timeModifier;
	}

	public double getObstacleDifficultyModifier() {
		return obstacleDifficultyModifier;
	}

	public void setObstacleDifficultyModifier(double obstacleDifficultyModifier) {
		this.obstacleDifficultyModifier = obstacleDifficultyModifier;
	}

	public double getObstacleDeviationModifier() {
		return obstacleDeviationModifier;
	}

	public void setObstacleDeviationModifier(double obstacleDeviationModifier) {
		this.obstacleDeviationModifier = obstacleDeviationModifier;
	}

	public double getQualityModifier() {
		return qualityModifier;
	}

	public void setQualityModifier(double qualityModifier) {
		this.qualityModifier = qualityModifier;
	}

	public QuestPlayer getPlayer() {
		return player;
	}

	/**
	 * Returns a copy of the fish item. <b>Note:</b> Changes to the quality here are not supported, and will
	 * not be carried over. To affect the quality of the result, use {@link #setQualityModifier(double)}
	 * @return
	 */
	public QualityItem getFish() {
		return fish;
	}

	public int getDifficulty() {
		return difficulty;
	}

	@Override
	public boolean isCancelled() {
		return this.isCancelled;
	}

	@Override
	public void setCancelled(boolean arg0) {
		this.isCancelled = arg0;
	}
	
}
