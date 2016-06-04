package com.SkyIsland.QuestManager.Player.Skill;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.PluginConfiguration;
import com.SkyIsland.QuestManager.Player.QuestPlayer;

/**
 * Same as a regular Skill, except the xp gain is done on a log scale;
 * The higher level you are, the less xp you get for doing an 'at-level' skill.
 * @author Skyler
 * @see {@link Skill}
 * @see {@link #perform(QuestPlayer, int, boolean)}
 */
public abstract class LogSkill extends Skill implements StagedIncreaseSkill {

	/**
	 * Adds experience to the skill, standardized as the specified amount for performing an action of
	 * level <i>actionLevel</i> and either succeeding or failing, as determined by <i>fail</i><br />
	 * This implementation differs from the standard one in that xp gains generally decrease as the
	 * skill level increases. Performing an action with action-level matching skill level at level one gives
	 * much more xp than doing the same at a high level.
	 * @param actionLevel The level of the action that was performed. Maybe cooking salmon is a lvl 30 task, for example
	 * @param fail whether or not the action was failed. May or not award xp on failure, as determined by config
	 */
	@Override
	public void perform(QuestPlayer participant, int actionLevel, boolean fail) {
		//XP = (base * (1-remaining))
		
		PluginConfiguration config = QuestManagerPlugin.questManagerPlugin.getPluginConfiguration();
		
		int levelDifference = participant.getSkillLevel(this) - actionLevel;
		//negative means the skill's a higher level than the player
		
		if (levelDifference > config.getSkillCutoff()) {
			System.out.println("Cutoff: difference of " + levelDifference);
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
		float xp = (float) (base * Math.max(1, 1-(Math.abs((double) levelDifference) / (double) config.getSkillCutoff())));
		
		//Apply log dropoff as skill level increases
		xp = (float) (xp / (Math.max(1, Math.log10(Math.max(1, participant.getSkillLevel(this))))));
		
		//lulz now just add it to player xp
		participant.setSkillExperience(this, participant.getSkillExperience(this) + xp);
	}	

	@Override
	public void performMinor(QuestPlayer participant, int actionLevel, boolean fail) {
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
		float xp = (float) (base * Math.max(1, (1-(Math.abs((double) levelDifference) / (double) config.getSkillCutoff()))));
		
		//Apply log dropoff as skill level increases
		xp = (float) (xp / (Math.max(1, Math.log10(Math.max(1, participant.getSkillLevel(this))))));
		
		//further reduce xp
		xp /= 4f;
		participant.setSkillExperience(this, participant.getSkillExperience(this) + xp);
	}

	@Override
	public void performMajor(QuestPlayer participant, int actionLevel, boolean fail) {
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
		float xp = (float) (4f * base * Math.max(1, (1-(Math.abs((double) levelDifference) / (double) config.getSkillCutoff()))));
		
		//Apply log dropoff as skill level increases
		xp = (float) (xp / (Math.max(1, Math.log10(Math.max(1, participant.getSkillLevel(this))))));
		
		participant.setSkillExperience(this, participant.getSkillExperience(this) + xp);
	}
}
