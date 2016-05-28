package com.SkyIsland.QuestManager.NPC;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Quest.Quest;
import com.SkyIsland.QuestManager.Quest.History.HistoryEvent;
import com.SkyIsland.QuestManager.Scheduling.Tickable;

public abstract class NPC implements ConfigurationSerializable, Listener, Tickable {
	
	/**
	 * Cache value for saving lookup times for entities
	 */
	private Entity entity;
	
	/**
	 * The actual ID of the entity we're monitoring
	 */
	protected UUID id;
	
	protected String name;
	
	/**
	 * Which quests is this NPC associated? Meaning which quest's history should include this npc's dialogue?
	 */
	protected String questName;
	
	protected NPC() {
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	/**
	 * Returns the entity this NPC is attached to.<br />
	 * This method attempts to save cycles by caching the last known entity to
	 * represent our UUID'd creature. If the cache is no longer valid, an entire
	 * sweep of worlds and entities is performed to lookup the entity.
	 * @return The entity attached to our UUID, or NULL if none is found
	 */
	public Entity getEntity() {
		if (entity != null && entity.isValid() && !entity.isDead() && entity.getUniqueId().equals(id)) {
			//still cached
			return entity;
		}
		
		//try and load last chunk the entity was in
		if (entity != null) {
			entity.getLocation().getChunk().load();
			
		} else {
			System.out.println("entity is null: " + name);
		}
		
		//cache has expired (new entity ID, etc) so grab entity
		for (World w : Bukkit.getWorlds())
		for (Entity e : w.getEntities()) {
			if (e.getUniqueId().equals(id)) {
				entity = e;
				return e;
			}
		}
		
		//unable to find entity!
		return null;
		
	}
	
	/**
	 * Register an entity to this NPC. This method also updates the ID of this npc
	 * @param entity
	 */
	public void setEntity(Entity entity) {
		this.entity = entity;
		this.id = entity.getUniqueId();
		if (entity instanceof LivingEntity) {
			((LivingEntity) entity).setRemoveWhenFarAway(false);
		}
	}
	
	/**
	 * Specify the ID used for this entity
	 * @param id
	 */
	public void setID(UUID id) {
		this.id = id;
	}
	
	public UUID getID() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getQuestName() {
		return questName;
	}
	
	public void setQuestName(String questName) {
		this.questName = questName;
	}
	
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractAtEntityEvent e) {
		if (!e.isCancelled())
		if (e.getHand().equals(EquipmentSlot.HAND))
		if (e.getRightClicked().getUniqueId().equals(id)) {
			e.setCancelled(true);
			this.interact(e.getPlayer());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEntityEvent e){
		if (!e.isCancelled())
			if (e.getRightClicked().getUniqueId().equals(id)) {
				e.setCancelled(true);
			}
	}
	
	@EventHandler
	public void onEntityHurt(EntityDamageEvent e) {
		if (!e.getEntity().getUniqueId().equals(id)) {
			return;
		}
			
		//grab the damage. if it's gonna kill us, just take no damage? 
		
		e.setDamage(0);
//		Entity ent = getEntity();
//		if (!(ent instanceof LivingEntity)) {
//			return;
//		}
//		
//		LivingEntity l = (LivingEntity) ent;
//		
//		if (e.getDamage() >= l.getHealth()){
//			e.setDamage(0.0);
//		}
	}
	
	public void removeEntity(boolean now) {
		final Entity e = getEntity();
		
		e.getLocation().getChunk().load();
		
		if (now) {
			e.remove();
		} else {
			Bukkit.getScheduler().runTaskLater(QuestManagerPlugin.questManagerPlugin, 
					new Runnable(){
	
						@Override
						public void run() {
							e.remove();
						}
					
					}, 1
			);
		}
	}
	
	protected abstract void interact(Player player);
	
	protected void updateQuestHistory(QuestPlayer qp, String desc) {
		if (questName == null || qp == null) {
			return;
		}
		
		Quest quest = null;
		
		for (Quest q : qp.getCurrentQuests()) {
			if (q.getName().equals(questName)) {
				quest = q;
				break;
			}
		}
		
		if (quest == null) {
			return;
		}

		for (HistoryEvent event : quest.getHistory().events()) {
			if (ChatColor.stripColor(event.getDescription()).equals(ChatColor.stripColor(desc))) {
				return; //already in there
			}
		}
		
		//wasn't in there, so add one
		quest.addHistoryEvent(new HistoryEvent(desc));
		qp.addJournal();
	}
}
