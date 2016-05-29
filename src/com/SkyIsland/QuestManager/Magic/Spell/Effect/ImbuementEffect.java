package com.SkyIsland.QuestManager.Magic.Spell.Effect;

import org.bukkit.entity.Entity;

import com.SkyIsland.QuestManager.Magic.MagicUser;

/**
 * This effect can be used for imbuement.<br />
 * Compatable effects are able to be scaled up or down on a whim -- as is expected for combining
 * effects with some generic scale. These effects are expected to scale up or down based on their given
 * potency -- A value where 1 signifies a normal, 100% effective effect. 
 * @author Skyler
 *
 */
public interface ImbuementEffect {

	/**
	 * Returns the current set potency of the effect. A value of 1.0 determines a full effect (100%). Values
	 * over 1 indicate a super-charged effect. 
	 * @return
	 */
	public double getPotency();
	
	/**
	 * Sets the potency. A value of 1.0 determines a full effect (100%). Values
	 * over 1 indicate a super-charged effect. 
	 */
	public void setPotency(double potency);
	
	/**
	 * Returns a 'copy' of this effect with parameters tweaked to be at <i>potency</i> potency.<br />
	 * Values of potency are not bounded, but are defined to be 100% at 1.0. In other words, at 1.0, the
	 * effect returned should be exactly the same as the original effect.
	 * @param potency
	 * @return
	 */
	public ImbuementEffect getCopyAtPotency(double potency);
	
	public abstract void apply(Entity e, MagicUser cause);
	
}
