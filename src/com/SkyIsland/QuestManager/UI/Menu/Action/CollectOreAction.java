package com.SkyIsland.QuestManager.UI.Menu.Action;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.SkyIsland.QuestManager.Effects.ChargeEffect;
import com.SkyIsland.QuestManager.Player.QuestPlayer;

/**
 * Sees if the player wasn't successfull in their fishing attempts, adn then rubs it in their face!
 * @author Skyler
 *
 */
public class CollectOreAction implements MenuAction, FillableInventoryAction {

	private QuestPlayer player;
	
	private ItemStack ret;
	
	private static final ChargeEffect successEffect = new ChargeEffect(Effect.HAPPY_VILLAGER);
	
	private static final ChargeEffect failEffect = new ChargeEffect(Effect.SMALL_SMOKE);
	
	public CollectOreAction(QuestPlayer player) {
		this.player = player;
		ret = null;
	}
	
	@Override
	public void onAction() {
				
		Player p = player.getPlayer().getPlayer();
		
		if (ret == null || ret.getAmount() <= 0) {
			//p.sendMessage(FishingGui.loseMessage);
			failEffect.play(p, null);
		} else {
			successEffect.play(p, null);
			if (!(p.getInventory().addItem(ret)).isEmpty()) {
				p.sendMessage(ChatColor.RED + "There is no space left in your inventory");
				p.getWorld().dropItem(p.getEyeLocation(), ret);
			}
		}
		
		
	}

	@Override
	public void provideItems(ItemStack[] objects) {
		if (objects == null || objects.length < 1) {
			ret = null;
		} else
			ret = objects[0];
	}

}
