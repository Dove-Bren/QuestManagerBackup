package com.SkyIsland.QuestManager.Player.Skill;

import java.util.Collection;
import java.util.Random;

import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.PluginConfiguration;
import com.SkyIsland.QuestManager.Player.QuestPlayer;

/**
 * A player skill. Skills can pertain to any aspect of the game, from combat to crafting.<br />
 * Implementations are responsible for catching events and acting on them. Implementations are <b>heavily
 * encouraged</b> to use the pre-built skill events rather than the bukkit ones, as they include the ability
 * to transfer information about success/failure between the potential many different skills that may
 * influence an action.<br />
 * While an implementation may override the level-up and experience-gain mechanics of a skill, prebuilt ones
 * are included in this class to allow a uniform config-specified skill experience. For more information, see
 * the {@link #perform(QuestPlayer, int, boolean)} method.
 * @author Skyler
 *
 */
public abstract class Skill implements Comparable<Skill> {
	
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
		//XP = (base * (1-remaining))
		
		PluginConfiguration config = QuestManagerPlugin.questManagerPlugin.getPluginConfiguration();
		
		int levelDifference = participant.getSkillLevel(this) - actionLevel;
		//negative means the skill's a higher level than the player
		
		if (levelDifference > config.getSkillCutoff()) {
			return; //no xp, too far below the player's level!
		}
		
		if (fail && levelDifference < -config.getSkillUpperCutoff()) {
			//skill level is higher than the config's upper skill fail limit cutoff
			return;
		}
		
		double base;
		if (fail) {
			base = config.getSkillGrowthOnFail();
		} else {
			base = config.getSkillGrowthOnSuccess();
		}
		
		//every level difference between skill and player decreases xp. Each level differene is 
		//1/[cutoff] reduction. That way, we get a nice approach towards 0 at the cutoff
		float xp = (float) (base * (1-(Math.abs((double) levelDifference) / (double) config.getSkillCutoff())));
		
		//lulz now just add it to player xp
		participant.setSkillExperience(this, participant.getSkillExperience(this) + xp);
		
		
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
	
	public abstract String getName();
	
	/**
	 * Get a pre-formated description that can be sent to the user.<br />
	 * Message should contain a brief description followed by any specifics defined by the
	 * Skill. For example, the {@link com.SkyIsland.QuestManager.Player.Skill.Default.TwoHandedSkill TwoHandedSkill}
	 * should have details about what the current hit chance and bonus damage is. 
	 * @return
	 */
	public abstract String getDescription(QuestPlayer player);
	
	public abstract int getStartingLevel();
	
	@Override
	public abstract boolean equals(Object o);
	
	/**
	 * Returns a key that can be used to figure out what skill information is being looked at in a player config file.<br />
	 * The key can be simple, but should try to be unique. For example, two-handed skill could have "two_handed"
	 * @return
	 */
	public abstract String getConfigKey();
	
	/**
	 * Sets the provided attribute up with a modifier by the given name (overwriting one if it exists)
	 * for the given amount. <br />
	 * <b>This is done</b> under the {@link AttributeModifier.Operation#ADD_SCALAR} operation!
	 * @param attribute
	 * @param name
	 * @param amt
	 */
	public static void setAttributeModifier(AttributeInstance attribute, String name, double amt) {
		Collection<AttributeModifier> mods = attribute.getModifiers();
		
		for (AttributeModifier mod : mods) {
			if (mod.getName().equals(name)) {
				attribute.removeModifier(mod);
				break;
			}
		}
		
		attribute.addModifier(new AttributeModifier(name, amt, AttributeModifier.Operation.ADD_SCALAR));
	}
	
	@Override
	public int compareTo(Skill o) {
		return getName().compareTo(o.getName());
	}
}
