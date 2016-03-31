package com.SkyIsland.QuestManager.Player.Skill;

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
	
	/**
	 * The current level of the skill. This level is relative to the {@link SkillManager#skillLevelCap skillLevelCap}
	 */
	protected int level;
	
	/**
	 * Current progress to next level. This should be [0-1)
	 */
	protected float experience;
	
	protected Skill(int startingLevel) {
		this(startingLevel, 0f);
	}
	
	protected Skill(int startingLevel, float experience) {
		this.level = startingLevel;
		this.experience = experience;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public float getExperience() {
		return experience;
	}

	public void setExperience(float experience) {
		this.experience = experience;
	}
	
	/**
	 * Adds experience to the skill, standardized as the specified amount for performing an action of
	 * level <i>actionLevel</i> and either succeeding or failing, as determiend by <i>fail</i>
	 * @param actionLevel The level of the action that was performed. Maybe cooking salmon is a lvl 30 task, for example
	 * @param fail whether or not the action was failed. May or not award xp on failure, as determined by config
	 */
	public void perform(int actionLevel, boolean fail) {
		//TODO
	}
	
	/**
	 * Awards experience equivalent of performing an action of the same level as the skill and either
	 * succeeding or failing.<br />
	 * In other words, {@link #perform(int, boolean) perform(actionLevel, false)};
	 * @param actionLevel
	 */
	public void perform(int actionLevel) {
		perform(actionLevel, false);
	}
	
	/**
	 * Awards experience equivalent of performing an action of the same level as the skill and either
	 * succeeding or failing.<br />
	 * In other words, {@link #perform(int, boolean) perform(this.level, fail)};
	 * @param fail
	 */
	public void perform(boolean fail) {
		perform(level, fail);
	}
	
	/**
	 * Awards experience equivalent of performing an action of the same level as the skill and succeeding.<br />
	 * In other words, {@link #perform(int, boolean) perform(this.level, false)};
	 */
	public void perform() {
		perform(level, false);
	}
}
