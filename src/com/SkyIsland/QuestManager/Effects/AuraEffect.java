package com.SkyIsland.QuestManager.Effects;

import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import com.SkyIsland.QuestManager.Scheduling.Alarm;
import com.SkyIsland.QuestManager.Scheduling.Alarmable;

/**
 * Effect where particles are constantly created around a player, as if an aura.
 * @author Skyler
 *
 */
public class AuraEffect extends EntityEffect implements ConstantEffect, Alarmable<Integer> {

	public static Random rand = new Random();
	
	/**
	 * The particle to create
	 */
	private Effect effect;
	
	private int data;
	
	/**
	 * How many per tick to create
	 */
	private int count; 
	
	private Entity entity;
	
	/**
	 * AuraEffect that creates the given particle effect.
	 * @param effect The particle to create
	 * @param count How many to create per 5 ticks (.25 seconds)
	 * @param optional data to be passed to the effect. See {@link org.bukkit.Effect}. Defaults to 0
	 */
	public AuraEffect(Effect effect, int count, int data) {
		this.effect = effect;
		this.count = count;
		this.data = data;
	}
	
	public AuraEffect(Effect effect, int count) {
		this(effect, count, 0);
	}
	
	public AuraEffect(Effect effect) {
		this(effect, 5);
	}
	
	@Override
	public void play(Entity entity) {
		Alarm.getScheduler().schedule(this, 1, .25);
		this.entity = entity;
	}

	@Override
	public void stop() {
		Alarm.getScheduler().unregister(this);
	}

	@Override
	public void alarm(Integer reference) {
		//disregard reference. We only have one alarm
		Location anchor = entity.getLocation().clone().add(0, 1, 0), tmp;
		float dir;
		for (int i = 0; i < count; i++) {
			tmp = anchor.clone();
			dir = (float) (rand.nextFloat() * (2 * Math.PI));
			tmp.add(Math.cos(dir), 0, Math.sin(dir));
			
			tmp.getWorld().playEffect(tmp, effect, data);
		}
		
		Alarm.getScheduler().schedule(this, 1, .25);
	}
	
}
