package com.SkyIsland.QuestManager.Quest.Requirements;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.EquipmentConfiguration;
import com.SkyIsland.QuestManager.Configuration.State.RequirementState;
import com.SkyIsland.QuestManager.Configuration.State.StatekeepingRequirement;
import com.SkyIsland.QuestManager.Configuration.Utils.LocationState;
import com.SkyIsland.QuestManager.NPC.NPC;
import com.SkyIsland.QuestManager.NPC.QuestMonsterNPC;
import com.SkyIsland.QuestManager.Player.Utils.CompassTrackable;
import com.SkyIsland.QuestManager.Quest.Goal;
import com.SkyIsland.QuestManager.Quest.Requirements.Factory.RequirementFactory;

/**
 * Requirement that a given entity must be slain.<br />
 * This requirement purposely does not require the entity be slain by certain participants.
 * Instead it is simply required that the provided entity is defeated.
 * @author Skyler
 *
 */
public class VanquishRequirement extends Requirement implements Listener, StatekeepingRequirement, CompassTrackable {
	
	public static class VanquishFactory extends RequirementFactory<VanquishRequirement> {
		
		public VanquishRequirement fromConfig(Goal goal, ConfigurationSection config) {
			VanquishRequirement req = new VanquishRequirement(goal);
			try {
				req.fromConfig(config);
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
			return req;
		}
	}
	
	private LivingEntity foe;
	
	private NPC foeNPC;
	
	private RequirementState foeStateRecord;
	
	private UUID id;
	
	private VanquishRequirement(Goal goal) {
		super(goal);
	}
	
	public VanquishRequirement(Goal goal, LivingEntity foe) {
		this(goal, "", foe);
	}
	
	public VanquishRequirement(Goal goal, String description, LivingEntity foe) {
		super(goal, description);
		this.foe = foe;
		state = false;
		
	}

	@Override
	public void activate() {
		ConfigurationSection myState = foeStateRecord.getConfig();
		
		//get rid of any entities we already have
		if (foe != null && !foe.isDead()) {
			foe.remove();			
		}
		
		
		ConfigurationSection foeState =  myState.getConfigurationSection("foe");
		Location loc = ((LocationState) foeState.get("location")).getLocation();
		
		//load chunk before creating foe
		loc.getChunk();
		
		foe = (LivingEntity) loc.getWorld().spawnEntity(loc, EntityType.valueOf(foeState.getString("type")));
		this.id = foe.getUniqueId();
		foe.setMaxHealth(foeState.getDouble("maxhp"));
		foe.setHealth(foeState.getDouble("hp"));
		foe.setCustomName(foeState.getString("name"));
		
		foe.setRemoveWhenFarAway(false);
		
		EntityEquipment equipment = foe.getEquipment();
		EquipmentConfiguration econ = new EquipmentConfiguration();
		try {
			econ.load( foeState.getConfigurationSection("equipment"));
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		equipment.setHelmet(econ.getHead());
		equipment.setChestplate(econ.getChest());
		equipment.setLeggings(econ.getLegs());
		equipment.setBoots(econ.getBoots());
		equipment.setItemInMainHand(econ.getHeldMain());
		equipment.setItemInOffHand(econ.getHeldOff());
		
		//special checks
		if (foe instanceof Guardian) {
			if (foeState.getBoolean("elder", false)) {
				((Guardian) foe).setElder(true);
			}
		}
		
		if (foe instanceof Slime) {
			if (foeState.getInt("size", 1) > 1) {
				((Slime) foe).setSize(foeState.getInt("size"));
			}
		}
		
		if (foe instanceof Skeleton) {
			if (foeState.getBoolean("wither", false)) {
				((Skeleton) foe).setSkeletonType(SkeletonType.WITHER);
			}
		}
		
		if (desc == null) {
			desc = foeState.getString("description", "Slay " + foe.getCustomName());
		}
		
		//add as NPC for non-removal
		foeNPC = new QuestMonsterNPC();
		foeNPC.setEntity(foe);
		
		QuestManagerPlugin.questManagerPlugin.getManager().registerNPC(foeNPC);
		
		update();
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	/**
	 * @return the foe
	 */
	public LivingEntity getFoe() {
		for (World w : Bukkit.getWorlds())
		for (Entity e : w.getEntities()) {
			if (e.getUniqueId().equals(id)) {
				foe = (LivingEntity) e;
				return foe;
			}
		}
		
		return null;
	}
	
	/**
	 * Catches entity death events and changes state to reflect whether or not this requirement
	 * is satisfied
	 * @param e
	 */
	@EventHandler
	public void onVanquish(EntityDeathEvent e) {
		
		if (!state && foe.isDead()) {
			state = true;
			
			//remove NPC
			QuestManagerPlugin.questManagerPlugin.getManager().unregisterNPC(foeNPC);
			
			//unregister listen, as we'll never need to check again
			HandlerList.unregisterAll(this);
			updateQuest();
		}
		
	}
	
	/**
	 * Double checks current state information, updating incase somehow the entity death
	 * slipped through the cracks
	 * TODO what about reloading!?!?!?!?!?!?!?!???!?!
	 */
	@Override
	public void update() {
		if (state) {
			return;
		}
		
		if (!foe.isValid() || foe.isDead()) {
			foe = getFoe();
		}
		
		state = (foe == null || foe.isDead());
	}

	@Override
	public RequirementState getState() {
		YamlConfiguration myState = new YamlConfiguration();
		
		myState.set("type", "vr");
		
		ConfigurationSection foeSection = myState.createSection("foe");
		foeSection.set("type", foe.getType().name());
		foeSection.set("maxhp", foe.getMaxHealth());
		foeSection.set("hp", foe.getHealth());
		foeSection.set("name", foe.getCustomName());
		foeSection.set("location", foe.getLocation());
		
		if (foe instanceof Guardian) {
			foeSection.set("elder", ((Guardian) foe).isElder());
		}
		
		if (foe instanceof Slime) {
			foeSection.set("size", ((Slime) foe).getSize());
		}
		
		if (foe instanceof Skeleton) {
			foeSection.set("wither", ((Skeleton) foe).getSkeletonType() == SkeletonType.WITHER);
		}
		
		EquipmentConfiguration econ = new EquipmentConfiguration(foe.getEquipment());
		foeSection.set("equipment", econ.getConfiguration());
		
		return new RequirementState(myState);
	}

	@Override
	public void loadState(RequirementState reqState) throws InvalidConfigurationException {

		ConfigurationSection myState = reqState.getConfig();

		if (!myState.contains("type") || !myState.getString("type").equals("vr")) {
			throw new InvalidConfigurationException("\n  ---Invalid type! Expected 'vr' but got " + myState.get("type", "null"));
		}
		
		this.desc = myState.getString("description", "Vanquish " + myState.getString("foe.name", "the monster"));
		
		this.foeStateRecord = reqState;
	}

	@Override
	public void fromConfig(ConfigurationSection config) throws InvalidConfigurationException {
		//what we need to load is the type of foe and his states
		//this is pretty much loadState
		
		//for laziness imma just do the same thing
		loadState(new RequirementState(config));
		
//		RequirementState fakeState = new RequirementState();
//		fakeState.importConfig(config);
//		loadState(fakeState);
		
		
	}
	
	public void stop() {
		
		//cleanup NPC
		QuestManagerPlugin.questManagerPlugin.getManager().unregisterNPC(foeNPC);
		
		//load chunk
		Chunk chunk = foe.getLocation().getChunk();
		
		for (Entity e : chunk.getEntities()) {
			if (e.getUniqueId().equals(id)) {
				foe = (LivingEntity) e;
				break;
			}
		}
		foe.remove();
	}
	
	@Override
	public String getDescription() {
		return desc;
	}
	
	@Override
	public Location getLocation() {
		getFoe(); //update foe
		if (foe == null) {
			return null;
		} else {
			return foe.getLocation();
		}
		
	}
	
}
