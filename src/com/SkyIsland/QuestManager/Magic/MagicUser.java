package com.SkyIsland.QuestManager.Magic;

import java.util.List;

import org.bukkit.entity.Entity;

public interface MagicUser {
	
	public Entity getEntity();
	
	public double getMP();
	
	public void addMP(double amount);
	
	public void addSpellPylon(SpellPylon pylon);
	
	public List<SpellPylon> getSpellPylons();
	
	public void clearSpellPylons();
	
	/**
	 * Cast the currently set up spell weaving spell. Spell cast, cost, difficulty, etc
	 * are determined by the spell the player has created with their current pylons.<br />
	 * Pylons should be destroyed at this time, and checks should be made to player
	 * mana, skill level, etc
	 */
	public void castSpellWeavingSpell();
	
}
