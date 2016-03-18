package com.SkyIsland.QuestManager.Enemy;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import com.SkyIsland.QuestManager.QuestManagerPlugin;

/**
 * Enemy type with very limited, straightforward customization; namely attributes
 * @author Skyler
 *
 */
public class NormalEnemy extends Enemy {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(NormalEnemy.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(NormalEnemy.class);
	}
	

	private enum aliases {
		DEFAULT(NormalEnemy.class.getName()),
		SIMPLE("NormalEnemy");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	protected double hp;
	
	protected double attack;
	
//	protected String type;
	
	public NormalEnemy(String name, EntityType type, double hp, double attack) {
		super(name, type);
		this.hp = hp;
		this.attack = attack;
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		map.put("type", type.name());
		map.put("name", name);
		map.put("hp", hp);
		map.put("attack", attack);
		
		return map;
	}
	
	public static NormalEnemy valueOf(Map<String, Object> map) {
		
		EntityType type;
		try {
			type = EntityType.valueOf(((String) map.get("type")).toUpperCase());
		} catch (Exception e) {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Unable to get EntityType " + 
					(String) map.get("type") + ", so defaulting to ZOMBIE");
			type = EntityType.ZOMBIE;
		}
		String name = (String) map.get("name");
		Double hp = (Double) map.get("hp");
		Double attack = (Double) map.get("attack");
		
		return new NormalEnemy(name, type, hp, attack);
	}
	

	@Override
	public void spawn(Location loc) {
				
		Entity e = loc.getWorld().spawnEntity(loc, type);
		e.setCustomName(name);
		e.setCustomNameVisible(true);
		
		if (!(e instanceof LivingEntity)) {
			return;
		}
		
		LivingEntity entity = (LivingEntity) e;
		entity.setMaxHealth(hp);
		entity.setHealth(hp);
		entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(attack);
		
		entity.getEquipment().setItemInMainHandDropChance(0f);
		
		
	}
	
	//LEFT HERE CAUSE OMG WHAT LULZ THIS SUCKED
	//but now there's an Attribute API <3
//	@Override
//	public void spawn(Location loc) {
//		
//		String cmd = "summon "
//				+ this.type + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " "
//				+ "{CustomName:" + name + ",CustomNameVisible:1,Attributes:["
//				+ "{Name:generic.maxHealth,Base:" + hp + "},"
//				+ "{Name:generic.attackDamage,Base:" + attack + "}]}";
//		
//		//Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
//		System.out.println("normal...");
//		CommandBlock sender = QuestManagerPlugin.questManagerPlugin.getManager().getAnchor(loc.getWorld().getName());
//		//Entity sender = Bukkit.getPlayer("dove_bren");
//
//		if (sender == null) {
//			System.out.println("Null!");
//		}
//			
//		Location ol = sender.getLocation().clone().add(0,1,0);
//		sender.setCommand(cmd);
//		ol.getBlock().setType(Material.REDSTONE_BLOCK);
//		ol.getBlock().getState().update(true);
//		sender.update(true);
//		ol.getBlock().setType(Material.STONE);
//		
//	}
	
}
