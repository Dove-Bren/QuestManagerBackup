package com.SkyIsland.QuestManager.Magic.Spell;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.SkyIsland.QuestManager.Magic.MagicUser;

public abstract class TargetSpell extends Spell {
	
	protected TargetSpell(int cost, int difficulty, String name, String description) {
		super(cost, difficulty, name, description);
	}
	
	public abstract void cast(MagicUser caster, Vector direction);
	
	protected abstract void onBlockHit(MagicUser caster, Location loc);
	
	protected abstract void onEntityHit(MagicUser caster, LivingEntity target);
	
}
