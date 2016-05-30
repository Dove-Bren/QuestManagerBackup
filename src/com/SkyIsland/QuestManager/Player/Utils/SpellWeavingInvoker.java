package com.SkyIsland.QuestManager.Player.Utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Player.QuestPlayer;


/**
 * Used to trigger invocation of spell weaving spells
 * @author Skyler
 *
 */
public class SpellWeavingInvoker implements Listener {
	
	public static class InvokerDefinition {
		
		private static Material invokerType = Material.CARROT_STICK;
		
		private static String displayName = "Spell Invoker";
		
		private static Enchantment enchant = Enchantment.ARROW_DAMAGE;
		
		public static boolean isInvoker(ItemStack item) {
			if (item == null || item.getType() != invokerType || !item.hasItemMeta()) {
				return false;
			}
			
			if (!displayName.equals(item.getItemMeta().getDisplayName())) {
				return false;
			}
			
			if (!item.containsEnchantment(enchant)) {
				return false;
			}
			
			return true;
		}

		public static void setInvokerType(Material invokerType) {
			InvokerDefinition.invokerType = invokerType;
		}

		public static void setDisplayName(String displayName) {
			InvokerDefinition.displayName = displayName;
		}

		public static void setEnchant(Enchantment enchant) {
			InvokerDefinition.enchant = enchant;
		}
		
	}
	
	public SpellWeavingInvoker() {
		if (QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getAllowSpellWeaving() 
				&& QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getUseWeavingInvoker()) {
			Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
		}
	}
	
	@EventHandler
	public void onPlayerUse(PlayerInteractEvent e) {
//		if (e.isCancelled()) {
//			return;
//		}
		
		if (!(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
			return; //not right click
		}
		
		if (!InvokerDefinition.isInvoker(e.getItem())) {
			return;
		}
		
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getWorlds()
				.contains(e.getPlayer().getWorld().getName())) {
			return;
		}
		
		QuestPlayer qp = QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer(e.getPlayer());
		
		qp.castSpellWeavingSpell();
	}
	
}
