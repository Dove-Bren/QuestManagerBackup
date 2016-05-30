package com.SkyIsland.QuestManager.Player.Skill;

import com.SkyIsland.QuestManager.Player.QuestPlayer;

/**
 * A skill that doesn't just have one way to perform it -- and therefore, requires
 * more than one way to calculate xp gains.
 * @author Skyler
 *
 */
public interface StagedIncreaseSkill {

	/**
	 * Performs a small action of the skill, resulting in smaller awarded xp.
	 * Adds experience to the skill, standardized as the specified amount for performing an action of
	 * level <i>actionLevel</i> and either succeeding or failing, as determiend by <i>fail</i>
	 * @param actionLevel The level of the action that was performed. Maybe cooking salmon is a lvl 30 task, for example
	 * @param fail whether or not the action was failed. May or not award xp on failure, as determined by config
	 */
	public void performMinor(QuestPlayer participant, int actionLevel, boolean fail);
	
	/**
	 * Performs a large action of the skill, resulting in larger awarded xp.
	 * Adds experience to the skill, standardized as the specified amount for performing an action of
	 * level <i>actionLevel</i> and either succeeding or failing, as determiend by <i>fail</i>
	 * @param actionLevel The level of the action that was performed. Maybe cooking salmon is a lvl 30 task, for example
	 * @param fail whether or not the action was failed. May or not award xp on failure, as determined by config
	 */
	public void performMajor(QuestPlayer participant, int actionLevel, boolean fail);
	
}
