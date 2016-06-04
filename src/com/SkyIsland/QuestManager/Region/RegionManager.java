package com.SkyIsland.QuestManager.Region;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Enemy.Enemy;
import com.SkyIsland.QuestManager.Enemy.EnemyAlarms;
import com.SkyIsland.QuestManager.Scheduling.Alarm;
import com.SkyIsland.QuestManager.Scheduling.Alarmable;
import com.SkyIsland.QuestManager.util.WeightedList;

public final class RegionManager implements Alarmable<EnemyAlarms> {
	
	/**
	 * Holds the enemy list and the music to play for the region
	 * @author Skyler
	 *
	 */
	private static class RegionRecord {
		
		private Material record;
		
		private WeightedList<Enemy> enemies;
		
		public RegionRecord(Material sound, WeightedList<Enemy> enemies) {
			this.record = sound;
			this.enemies = enemies;
		}
		
		public Material getSound() {
			return record;
			
		}
		
		public WeightedList<Enemy> getEnemies() {
			return enemies;
		}
	}
	
	private Map<Region, RegionRecord> regionMap;
	
	private double spawnrate;
	
	/**
	 * Creates an empty enemy manager with a default spawnrate of 3 seconds
	 */
	public RegionManager() {
		this(3.0);
	}
	
	/**
	 * Creates an enemy manager with the provided spawn rate
	 * @param spawnrate
	 */
	public RegionManager(double spawnrate) {
		regionMap = new HashMap<Region, RegionRecord>();
		this.spawnrate = spawnrate;
		
		Alarm.getScheduler().schedule(this, EnemyAlarms.SPAWN, spawnrate);
	}
	
	/**
	 * Creates a new Enemy Manager using the provided file or files in the provided directory.<br />
	 * Spawnrate defaults to 3.0
	 * @param target The file to load or the directory to search for files to load
	 */
	public RegionManager(File target) {
		this(target, 3.0);
	}
	
	/**
	 * Creates a new Enemy Manager using the provided file or files in the provided directory.<br />
	 * @param target The file to load or the directory to search for files to load
	 * @param spawnrate
	 */
	public RegionManager(File target, double spawnrate) {
		this(spawnrate);
		load(target);
	}
	
	/**
	 * Registers the region with the manager.<br />
	 * Regions must be registered before they can start being associated with enemy types.
	 * @param region
	 * @return false if the region is null or already in the map, true otherwise
	 */
	public boolean registerRegion(Region region) {
		if (region == null || regionMap.containsKey(region)) {
			return false;
		}
		
		regionMap.put(region, new RegionRecord(null, new WeightedList<Enemy>()));
		
		return true;
	}
	
	/**
	 * Adds the enemy to the list of enemies for a region.<br />
	 * The underlying list does not make any checks against duplicates. Duplicate adds/inserts
	 * will result in duplicate entries.
	 * @param key
	 * @param enemy
	 * @param weight
	 * @return false if the region is not in the map, true otherwise
	 */
	public boolean addEnemy(Region key, Enemy enemy, double weight) {
		if (!regionMap.containsKey(key)) {
			return false;
		}
		
		WeightedList<Enemy> list = (regionMap.get(key)).enemies;
		list.add(enemy, weight);
		
		return true;
	}
	
	/**
	 * Adds the enemy to the region with a default weight 1
	 * @param key
	 * @param enemy
	 * @return
	 * @see #addEnemy(Region, Enemy, double)
	 */
	public boolean addEnemy(Region key, Enemy enemy) {
		return addEnemy(key, enemy, 1.0);
	}
	
	public void clear() {
		if (regionMap.isEmpty()) {
			return;
		}
		
		for (RegionRecord r : regionMap.values()) {
			r.getEnemies().clear();
		}
		
		regionMap.clear();
	}

	public double getSpawnrate() {
		return spawnrate;
	}

	public void setSpawnrate(double spawnrate) {
		this.spawnrate = spawnrate;
	}

	@Override
	public void alarm(EnemyAlarms reference) {
		switch (reference) {
		case SPAWN:
			spawnEnemies();
			Alarm.getScheduler().schedule(this, EnemyAlarms.SPAWN, spawnrate);
			break;
		}
	}
	
	/**
	 * Goes through all players in a quest world and spawns enemies if they are in a region.
	 */
	private void spawnEnemies() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getWorlds().contains(
					player.getWorld().getName())) {
				//is in a quest world
				for (Region r : regionMap.keySet()) {
					if (r.isIn(player)) {
						spawnInRegion(r);
						
						if (regionMap.get(r).getSound() != null) {
							player.playEffect(player.getLocation(), Effect.RECORD_PLAY,
									regionMap.get(r).getSound());
						}
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Spawns an enemy in the provided region from that region's list of enemies
	 * @param region
	 */
	private void spawnInRegion(Region region) {
		if (regionMap.get(region).enemies == null || regionMap.get(region).enemies.isEmpty()) 
			return;
		Enemy e;
		WeightedList<Enemy> l = (regionMap.get(region)).enemies;
		
		e = l.getRandom();
		
		e.spawn(region.randomLocation(true));
	}
	
	/**
	 * Loads regions and enemies from the provided config file.<br />
	 * Does not clear the current map before adding what's found in the config
	 * @param config
	 */
	private void load(File target) {
		/*
		 * Is a file? If so, load it. If not, get all files and load them
		 */
		if (target == null || !target.exists()) {
			return;
		}
		
		if (!target.isDirectory()) {
			loadFile(target);
		} else {
			for (File file : target.listFiles()) {
				if (file.isDirectory()) {
					load(file);
				}
				
				String ln = file.getName().toLowerCase();
				
				if (ln.endsWith(".yml") || ln.endsWith(".yaml")) {
					loadFile(file);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadFile(File file) {
		/*
		 * Load the config. There should be regions and enemies associated with them, like
		 * region1:
		 *  type:	==: Cuboid
		 * 			etc
		 *  enemies:
		 *    - ==: enemy
		 */
		
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		for (String key : config.getKeys(false)) {
			ConfigurationSection regionSection = config.getConfigurationSection(key);
			Region region = (Region) regionSection.get("region");
			
			List<Enemy> enemies = null;

			if (regionSection.contains("enemies")) {
				//load enemies
				enemies = (List<Enemy>) regionSection.getList("enemies");
			}
			
			registerRegion(region);
			if (enemies != null && !enemies.isEmpty())
			for (Enemy e : enemies) {
				addEnemy(region, e);
			}
			

			if (regionSection.contains("music")) {
				regionMap.get(region).record = Material.valueOf((String) regionSection.get("music"));
				
			}
			//TODO add enemy weights?
		}
	}
	
	/**
	 * Attempts to find a registered region that the given location falls in
	 * @param location
	 * @return The region the location lies in, or null if none were found
	 */
	public Region getRegion(Location location) {
		if (regionMap.isEmpty())
			return null;
		
		if (location == null) {
			return null;
		}
		
		for (Region region : regionMap.keySet()) {
			if (region.isIn(location)) {
				return region;
			}
		}
		
		return null;
	}
	
}
