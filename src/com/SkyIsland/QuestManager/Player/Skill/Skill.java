package com.SkyIsland.QuestManager.Player.Skill;

import java.util.Random;

import com.SkyIsland.QuestManager.Player.QuestPlayer;

/**
 * A player skill. Skills can pertain to any aspect of the game, from combat to crafting.<br />
 * Implementations are responsible for catching events and acting on them. Implementations are <b>heavily
 * encouraged</b> to use the pre-built skill events rather than the bukkit ones, as they include the ability
 * to transfer information about success/failure between the potential many different skills that may
 * influence an action.<br />
 * While an implementation may override the level-up and experience-gain mechanics of a skill, prebuilt ones
 * are included in this class to allow a uniform config-specified skill experience. For more information, see
 * the {@link #perform()} method.
 * @author Skyler
 *
 */
public abstract class Skill {
	
	public enum Type {
		COMBAT,
		TRADE,
		OTHER
	}
	
	//Courtesy random
	public static final Random random = new Random();
	
	/**
	 * Adds experience to the skill, standardized as the specified amount for performing an action of
	 * level <i>actionLevel</i> and either succeeding or failing, as determiend by <i>fail</i>
	 * @param actionLevel The level of the action that was performed. Maybe cooking salmon is a lvl 30 task, for example
	 * @param fail whether or not the action was failed. May or not award xp on failure, as determined by config
	 */
	public void perform(QuestPlayer participant, int actionLevel, boolean fail) {
		//TODO
	}
	
	/**
	 * Awards experience equivalent of performing an action of the same level as the skill and either
	 * succeeding or failing.<br />
	 * In other words, {@link #perform(int, boolean) perform(actionLevel, false)};
	 * @param actionLevel
	 */
	public void perform(QuestPlayer participant, int actionLevel) {
		perform(participant, actionLevel, false);
	}
	
	/**
	 * Awards experience equivalent of performing an action of the same level as the skill and either
	 * succeeding or failing.<br />
	 * In other words, {@link #perform(int, boolean) perform(this.level, fail)};
	 * @param fail
	 */
	public void perform(QuestPlayer participant, boolean fail) {
		perform(participant, participant.getSkillLevel(this), fail);
	}
	
	/**
	 * Awards experience equivalent of performing an action of the same level as the skill and succeeding.<br />
	 * In other words, {@link #perform(int, boolean) perform(this.level, false)};
	 */
	public void perform(QuestPlayer participant) {
		perform(participant, participant.getSkillLevel(this), false);
	}
	
	public abstract Type getType();
	
	public abstract int getStartingLevel();
	
	@Override
	public abstract boolean equals(Object o);
	
	/**
	 * Returns a key that can be used to figure out what skill information is being looked at in a player config file.<br />
	 * The key can be simple, but should try to be unique. For example, two-handed skill could have "two_handed"
	 * @return
	 */
	public abstract String getConfigKey();
}
