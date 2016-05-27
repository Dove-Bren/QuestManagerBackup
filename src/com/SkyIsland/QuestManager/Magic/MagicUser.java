package com.SkyIsland.QuestManager.Magic;

import java.util.List;

import org.bukkit.entity.Entity;

public interface MagicUser {
	
	public Entity getEntity();
	
	public int getMP();
	
	public void addMP(int amount);
	
	public void addSpellPylon(SpellPylon pylon);
	
	public List<SpellPylon> getSpellPylons();
	
	public void clearSpellPylons();
	
}
