package com.SkyIsland.QuestManager.Magic.Spell.Effect;

/**
 * This effect can be used for imbuement.<br />
 * Compatable effects are able to be scaled up or down on a whim -- as is expected for combining
 * effects with some generic scale. These effects are expected to scale up or down based on their given
 * potency -- A value where 1 signifies a normal, 100% effective effect. 
 * @author Skyler
 *
 */
public abstract class ImbuementEffect extends SpellEffect {

	private String displayName;
	
	/**
	 * Returns a 'copy' of this effect with parameters tweaked to be at <i>potency</i> potency.<br />
	 * Values of potency are not bounded, but are defined to be 100% at 1.0. In other words, at 1.0, the
	 * effect returned should be exactly the same as the original effect.
	 * @param potency
	 * @return
	 */
	public abstract ImbuementEffect getCopyAtPotency(double potency);
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
}
