package com.SkyIsland.QuestManager.UI.Menu.Action;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Effects.ChargeEffect;
import com.SkyIsland.QuestManager.Magic.ImbuementSet;
import com.SkyIsland.QuestManager.Magic.Spell.Effect.ImbuementEffect;
import com.SkyIsland.QuestManager.Player.QuestPlayer;

/**
 * Takes items (presumably from the imbuement table menu) and makes them into an imbuement, and registers
 * such to the player
 * @author Skyler
 *
 */
public class CreateImbuementAction implements MenuAction, FillableInventoryAction {

	private QuestPlayer player;
	
	private ItemStack holder;
	
	private Material[] componentTypes;
	
	private static final ChargeEffect successEffect = new ChargeEffect(Effect.FLYING_GLYPH);
	
	private static final Sound successSound = Sound.ENTITY_PLAYER_LEVELUP;
	
	private static final ChargeEffect failEffect = new ChargeEffect(Effect.CRIT);
	
	private static final Sound failSound = Sound.BLOCK_ANVIL_PLACE;
	
	private static final String failMessage = ChatColor.RED + "Your imbuement failed to show any signs of magic";
	
	private static final String successMessage = ChatColor.GREEN + "Your imbuement succeeded with the following effects:";
	
	public CreateImbuementAction(QuestPlayer player, ItemStack holder) {
		this.player = player;
		this.holder = holder;
		this.componentTypes = null;
	}
	
	public void setComponentTypes(Material[] types) {
		componentTypes = types;
	}
	
	@Override
	public void onAction() {
		/*
		 * Create imbuement based on rules and stuff
		 */
		ImbuementSet set = QuestManagerPlugin.questManagerPlugin.getImbuementHandler()
				.getCombinedEffects(player, componentTypes);
		
		if (set == null || set.getEffectMap().isEmpty()) {
			//failure
			player.performImbuement(holder, null);
			
			if (!player.getPlayer().isOnline()) {
				return;
			}
			
			failEffect.play(player.getPlayer().getPlayer(), null);
			player.getPlayer().getPlayer().getWorld().playSound(player.getPlayer().getPlayer().getLocation(),
					failSound, 1, 1);
			player.getPlayer().getPlayer().sendMessage(failMessage);
			return;
		}
		
		player.performImbuement(holder, set);

		if (!player.getPlayer().isOnline()) {
			return;
		}
		
		Player p = player.getPlayer().getPlayer();
		
		
		successEffect.play(p, null);
		p.getWorld().playSound(p.getLocation(),	successSound, 1, 1);
		p.sendMessage(successMessage);
		for (Entry<ImbuementEffect, Double> effect : set.getEffectMap().entrySet()) {
			p.sendMessage(ChatColor.AQUA + "" + ((int) (effect.getValue() * 100)) + "%"
					+ ChatColor.BLACK + " - " + ChatColor.GOLD
					+ effect.getKey().getDisplayName()); 
		}
		
		
		
	}

	@Override
	public void provideItems(ItemStack[] objects) {
		//throw into set, eliminate dups
		Set<Material> types = new HashSet<>();
		for (ItemStack item : objects) {
			if (item == null) {
				continue;
			}
			types.add(item.getType());
		}
		
		Material[] ret = new Material[types.size()];
		setComponentTypes(types.toArray(ret));
	}

}
