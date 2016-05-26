package com.SkyIsland.QuestManager.Magic.Spell.Effect;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Magic.MagicUser;
import com.SkyIsland.QuestManager.Magic.Summon;
import com.SkyIsland.QuestManager.Magic.SummonManager;

import net.md_5.bungee.api.ChatColor;

/**
 * Summons a tamed creature for the caster 
 * @author Skyler
 *
 */
public class SummonTamedEffect extends SpellEffect {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(SummonTamedEffect.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(SummonTamedEffect.class);
	}
	

	private enum aliases {
		DEFAULT(SummonTamedEffect.class.getName()),
		LONGI("SpellSummonTamed"),
		LONG("SummonTamedSpell"),
		SHORT("SSummonTamed");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	public static SummonTamedEffect valueOf(Map<String, Object> map) {
		
		int hp = -1;
		if (map.containsKey("hp")) {
			hp = (Integer) map.get("hp");
		}
		
		return new SummonTamedEffect(
				(Integer) map.get("duration"),
				EntityType.valueOf((String) map.get("type")),
				(String) map.get("name"),
				(Integer) map.get("count"),
				hp);
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("type", type.name());
		map.put("duration", duration);
		map.put("count", count);
		map.put("hp", hp);
		map.put("name", name);
		
		return map;
	}
	
	private static boolean isTameable(EntityType type) {
		switch (type) {
		case HORSE:
		case OCELOT:
		case WOLF:
			return true;
		default:
			return false;
		}
	}
	
	public static final String summonDenial = ChatColor.YELLOW + "You cannot summon this, as you already have too many summons!";
	
	private int duration;
	
	private EntityType type;
	
	private int hp;
	
	private int count;
	
	private String name;
	
	public SummonTamedEffect(int duration, EntityType type, String name, int count) {
		this(duration, type, name, count, -1);
	}
	
	public SummonTamedEffect(int duration, EntityType type, String name, int count, int hp) {
		this.duration = duration;
		this.type = type;
		this.count = count;
		this.hp = hp;
		this.name = name;
		if (!isTameable(type)) {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning(
					"WARNING! Summon'ed type [" + type + "] may not be tameable, and could "
					+ "result in exceptions further on. Please review tameable mobs and "
					+ "use one of those instead!");
		}
		
	}
	
	@Override
	public void apply(Entity e, MagicUser cause) {
		if (!(cause.getEntity() instanceof AnimalTamer)) {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Unable to summon tamed "
					+ "entity to caster, because they aren't an AnimalTamer: " + cause.getEntity().getCustomName());
			return;
		}
		
		SummonManager manager = QuestManagerPlugin.questManagerPlugin.getSummonManager();
		
		Location tmp = e.getLocation().clone();
		tmp.add(0, 1.5, 0);
		tmp.add(e.getLocation().getDirection().normalize().multiply(2));
		Entity ent = tmp.getWorld().spawnEntity(tmp, type);
		if (!(ent instanceof Tameable)) {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Unable to summon tamed"
					+ " entity, as entity type is not tameable: [" + type + "]");
			ent.remove();
			return;
		}
		Summon s = new Summon(cause.getEntity().getUniqueId(), ent, duration);
		
		if (cause instanceof Player) {
			if (!manager.registerSummon((Player) cause, s)) {
				s.remove();
				cause.getEntity().sendMessage(summonDenial);
				return;
			}
		} else {
			manager.registerSummon(s);
		}
		
		Tameable tame = (Tameable) ent;
		tame.setTamed(true);
		tame.setOwner((AnimalTamer) cause.getEntity());
		
		ent.setCustomName(cause.getEntity().getName() + "'s " + name);
		ent.setCustomNameVisible(true);
		
		
		if (ent instanceof LivingEntity) {
			LivingEntity live = (LivingEntity) ent;
			live.setRemoveWhenFarAway(false);
			if (hp > 0) {
					live.setMaxHealth(hp);
					live.setHealth(hp);
			}

		}
	}
	
	@Override
	public void apply(Location loc, MagicUser cause) {
		; //do nothing
	}
	
	
	
}
