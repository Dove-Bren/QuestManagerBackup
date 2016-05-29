package com.SkyIsland.QuestManager.Magic.Spell.Effect;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.Utils.YamlWriter;
import com.SkyIsland.QuestManager.Fanciful.FancyMessage;
import com.SkyIsland.QuestManager.Magic.MagicUser;
import com.SkyIsland.QuestManager.Player.PlayerOptions;
import com.SkyIsland.QuestManager.Player.QuestPlayer;

/**
 * Wrapper class for potion effects put into spells
 * @author Skyler
 *
 */
public class StatusEffect extends ImbuementEffect {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(StatusEffect.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(StatusEffect.class);
	}
	

	private enum aliases {
		DEFAULT(StatusEffect.class.getName()),
		LONGI("SpellStatus"),
		LONG("StatusSpell"),
		SHORT("SStatus");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	public static StatusEffect valueOf(Map<String, Object> map) {
		return new StatusEffect((PotionEffect) map.get("effect"));
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("effect", effect);
		
		return map;
	}
	
	private PotionEffect effect;
	
	public StatusEffect(PotionEffect effect) {
		this.effect = effect;
	}
	
	@Override
	public void apply(Entity e, MagicUser cause) {
		if (e instanceof LivingEntity) {
			LivingEntity targ = (LivingEntity) e;
			effect.apply(targ);
			
			if (e instanceof Player) {
				QuestPlayer qp = QuestManagerPlugin.questManagerPlugin.getPlayerManager()
						.getPlayer((Player) e);
				if (qp.getOptions().getOption(PlayerOptions.Key.CHAT_COMBAT_DAMAGE)) {
					
					String msg;
					if (cause instanceof QuestPlayer && ((QuestPlayer) cause).getPlayer().getUniqueId()
							.equals(qp.getPlayer().getUniqueId())) {
						//healed self
						msg = ChatColor.DARK_GRAY + "You gained the effect ";
					} else {
						String name = cause.getEntity().getCustomName();
						if (name == null) {
							name = cause.getEntity().getType().toString();
						}
						msg = ChatColor.GRAY + cause.getEntity().getCustomName() + ChatColor.DARK_GRAY 
								+ " gave you the effect ";
					}
					
					String name = YamlWriter.toStandardFormat(effect.getType().getName());
					
					FancyMessage message = new FancyMessage(msg);
					message.then(name)
						.color(ChatColor.DARK_PURPLE)
						.tooltip(getEffectTooltip(effect, name));

					message.send(qp.getPlayer().getPlayer());
					
				}
			} else {
				//non player
				//damage them for 0 if it's negative effect? 
				if (isEffectHarmful(effect.getType())) {
					targ.setMetadata(DamageEffect.damageMetaKey, new FixedMetadataValue
							(QuestManagerPlugin.questManagerPlugin, true));
					targ.damage(0.0, cause.getEntity());
					targ.setMetadata(DamageEffect.damageMetaKey, new FixedMetadataValue
							(QuestManagerPlugin.questManagerPlugin, true));
				}
				
			}
		}
	}
	
	@Override
	public void apply(Location loc, MagicUser cause) {
		//can't damage a location
		//do nothing 
		;
	}
	
	public static List<String> getEffectTooltip(PotionEffect effect, String displayName) {
		List<String> list = new LinkedList<>();
		
		if (effect == null)
			return list;
		
		list.add(ChatColor.AQUA + displayName);
		
		if (effect.getType().equals(PotionEffectType.ABSORPTION)) {
			list.add(ChatColor.WHITE + "Grants temporary health that cannot be");
			list.add(ChatColor.WHITE + "regenerated, but will be deducted from");
			list.add(ChatColor.WHITE + "first");
			list.add(ChatColor.GOLD + " Bonus Health: " + ChatColor.GREEN + (4 * effect.getAmplifier())
					+ ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) {
			list.add(ChatColor.WHITE + "Reduces damage taken from all sources");
			list.add(ChatColor.GOLD + " Damage Reduction: " + ChatColor.GREEN + (20 * effect.getAmplifier())
					+ "%" + ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.FAST_DIGGING)) {
			list.add(ChatColor.WHITE + "Improves attack speed and digging speed");
			list.add(ChatColor.GOLD + " Swing Speed: " + ChatColor.GREEN + "+"
					+ (10 * effect.getAmplifier()) + "%");
			list.add(ChatColor.GOLD + " Mining Speed: " + ChatColor.GREEN + "+"
					+ (20 * effect.getAmplifier()) + "%" + ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.HEALTH_BOOST)) {
			list.add(ChatColor.WHITE + "Grants a boost to maximum health for");
			list.add(ChatColor.WHITE + "the duration of the effect");
			list.add(ChatColor.GOLD + " Bonus Health: " + ChatColor.GREEN + (4 * effect.getAmplifier())
					 + ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
			list.add(ChatColor.WHITE + "Increases melee damage dealt");
			list.add(ChatColor.GOLD + " Damage Increase: " + ChatColor.GREEN + (3 * effect.getAmplifier())
					+ ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.LUCK)) {
			list.add(ChatColor.WHITE + "Increases the chance of getting better");
			list.add(ChatColor.WHITE + "loot from monsters and chests");
			list.add(ChatColor.GOLD + " Luck Bonus: " + ChatColor.GREEN + effect.getAmplifier()
				+ ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.POISON)) {
			list.add(ChatColor.WHITE + "Deals damage over time until cured");
			list.add(ChatColor.WHITE + "or the effect runs out");
			
			int rate = 25, c = effect.getAmplifier();
			while (c > 0 && rate > 1) {
				c --;
				rate /= 2;
			}
			
			
			list.add(String.format(ChatColor.GOLD + " Damage: " + ChatColor.RED	+ (effect.getDuration() / rate) 
					+ ChatColor.GOLD + " over " + ChatColor.RED + "%.2f seconds" + ChatColor.RESET, 
					(float) effect.getDuration() / 20));
		} else if (effect.getType().equals(PotionEffectType.REGENERATION)) {
			list.add(ChatColor.WHITE + "Restores health gradually");
			
			int rate = 50, c = effect.getAmplifier();
			while (c > 0 && rate > 1) {
				c --;
				rate /= 2;
			}
			
			
			list.add(String.format(ChatColor.GOLD + " Health: " + ChatColor.GREEN +(effect.getDuration() / rate) 
					+ ChatColor.GOLD + " over " + ChatColor.GREEN + "%.2f seconds" + ChatColor.RESET, 
					(float) effect.getDuration() / 20));
		} else if (effect.getType().equals(PotionEffectType.SLOW)) {
			list.add(ChatColor.WHITE + "Reduces all movement speed");
			list.add(ChatColor.GOLD + " Reduction: " + ChatColor.RED + (15 * effect.getAmplifier())
					+ "%" + ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.SLOW_DIGGING)) {
			list.add(ChatColor.WHITE + "Reduces swing and mining speed");
			list.add(ChatColor.GOLD + " Swing Speed: " + ChatColor.RED + "-"
					+ (10 * effect.getAmplifier()) + "%");
		} else if (effect.getType().equals(PotionEffectType.SPEED)) {
			list.add(ChatColor.WHITE + "Increases movement speed");
			list.add(ChatColor.GOLD + " Reduction: " + ChatColor.GREEN + (20 * effect.getAmplifier())
					+ "%" + ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.WEAKNESS)) {
			list.add(ChatColor.WHITE + "Decreases melee damage dealt");
			list.add(ChatColor.GOLD + " Damage Penalty: " + ChatColor.RED + (4 * effect.getAmplifier())
					+ ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.WITHER)) {
			list.add(ChatColor.WHITE + "Deals damage over time until cured");
			list.add(ChatColor.WHITE + ", the effect runs out, or death");
			
			int rate = 40, c = effect.getAmplifier();
			while (c > 0 && rate > 1) {
				c --;
				rate /= 2;
			}
			
			
			list.add(String.format(ChatColor.GOLD + " Damage: " + ChatColor.RED	+ (effect.getDuration() / rate) 
					+ ChatColor.GOLD + " over " + ChatColor.RED + "%.2f seconds" + ChatColor.RESET, 
					(float) effect.getDuration() / 20));
		} else if (effect.getType().equals(PotionEffectType.BLINDNESS)) {
			list.add(ChatColor.WHITE + "Decreases vision" + ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.CONFUSION)) {
			list.add(ChatColor.WHITE + "Disorients the target" + ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.FIRE_RESISTANCE)) {
			list.add(ChatColor.WHITE + "Negates all fire and lava damage" + ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.GLOWING)) {
			list.add(ChatColor.WHITE + "Allows target to be seen through");
			list.add(ChatColor.WHITE + "blocks and air alike" + ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.HUNGER)) {
			list.add(ChatColor.WHITE + "Decreases food level over time" + ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.INVISIBILITY)) {
			list.add(ChatColor.WHITE + "Renders the target invisible, making");
			list.add(ChatColor.WHITE + "it harder for entities to detect them");
		} else if (effect.getType().equals(PotionEffectType.JUMP)) {
			list.add(ChatColor.WHITE + "Increases maximum jump height");
		} else if (effect.getType().equals(PotionEffectType.LEVITATION)) {
			list.add(ChatColor.WHITE + "Causes the target to float up");
			list.add(ChatColor.WHITE + "into the air uncontrollably");
		} else if (effect.getType().equals(PotionEffectType.NIGHT_VISION)) {
			list.add(ChatColor.WHITE + "Negates the effects of darkness");
		} else if (effect.getType().equals(PotionEffectType.SATURATION)) {
			list.add(ChatColor.WHITE + "Replenishes hunger levels");
		} else if (effect.getType().equals(PotionEffectType.UNLUCK)) {
			list.add(ChatColor.WHITE + "Decreases the chance of getting better");
			list.add(ChatColor.WHITE + "loot from monsters and chests");
			list.add(ChatColor.GOLD + " Luck Penalty: " + ChatColor.RED + effect.getAmplifier()
				+ ChatColor.RESET);
		} else if (effect.getType().equals(PotionEffectType.WATER_BREATHING)) {
			list.add(ChatColor.WHITE + "Grants unlimited breath underwater");			
		} else {
			;
		}
		
		
		return list;
	}
	
	public boolean isEffectHarmful(PotionEffectType type) {
		if (type.equals(PotionEffectType.BLINDNESS) 
			|| type.equals(PotionEffectType.CONFUSION) 
			|| type.equals(PotionEffectType.HARM) 
			|| type.equals(PotionEffectType.HUNGER) 
			|| type.equals(PotionEffectType.LEVITATION) 
			|| type.equals(PotionEffectType.POISON) 
			|| type.equals(PotionEffectType.SLOW) 
			|| type.equals(PotionEffectType.SLOW_DIGGING) 
			|| type.equals(PotionEffectType.UNLUCK) 
			|| type.equals(PotionEffectType.WEAKNESS) 
			|| type.equals(PotionEffectType.WITHER)) {
			return true;
		}
				
		return false;
	}

	@Override
	public ImbuementEffect getCopyAtPotency(double potency) {
		PotionEffect ef = this.effect;
		ef = new PotionEffect(ef.getType(), ef.getAmplifier(), (int) (ef.getDuration() * potency));
		StatusEffect effect = new StatusEffect(ef);
		effect.setDisplayName(getDisplayName());
		return effect;		
	}
	
	
}
