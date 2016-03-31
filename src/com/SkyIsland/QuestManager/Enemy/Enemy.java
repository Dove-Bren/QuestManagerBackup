package com.SkyIsland.QuestManager.Enemy;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;

import com.SkyIsland.QuestManager.QuestManagerPlugin;

/*
 * Depicts a QM enemy, which can be created or destroyed as the world loads and unloads.<br />
 * Each enemy object represents a unique, spawnable enemy type. Each instance of an enemy is created from this
 * class when spawning things, but this class doesn't hold instances.
 */
public abstract class Enemy implements ConfigurationSerializable {
	
	protected EntityType type;
	
	protected String name;
	
	/**
	 * Unique ID used to figure out who spawned an enemy. For example, was it NormalEnemy A, or StandardEnemy E?
	 * If the buried {@link #Enemy(String, EntityType)} constructor is not used, this should be set manually.
	 */
	protected String enemyClassID;
	
	private static int enemyClassIDIndex;
	
	{
		enemyClassIDIndex = ((new Random()).nextInt(4000));
	}
	
	public static final String classMetaKey = "QMEnemySpawnClass";
	
	public Enemy(String name, EntityType type) {
		this.enemyClassID = generateNewEnemyClassID();
		this.name = name;
		this.type = type;
	}
	
	public void spawn(Location loc) {
		Entity e = loc.getWorld().spawnEntity(loc, type);
		e.setMetadata(Enemy.classMetaKey, new FixedMetadataValue(
				QuestManagerPlugin.questManagerPlugin,
				this.enemyClassID
				));
		e.setCustomName(name);
	}
	
	public String generateNewEnemyClassID() {
		return "_BASEenemyID_" + Enemy.enemyClassIDIndex++; 
	}
	
}
