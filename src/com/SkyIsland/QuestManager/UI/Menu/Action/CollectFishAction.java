package com.SkyIsland.QuestManager.UI.Menu.Action;

import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.SkyIsland.QuestManager.Effects.ChargeEffect;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.UI.Menu.Inventory.Minigames.FishingGui;

/**
 * Sees if the player wasn't successfull in their fishing attempts, adn then rubs it in their face!
 * @author Skyler
 *
 */
public class CollectFishAction implements MenuAction, FillableInventoryAction {

	private QuestPlayer player;
	
	private ItemStack ret;
	
	private static final ChargeEffect successEffect = new ChargeEffect(Effect.SPLASH);
	
	private static final ChargeEffect failEffect = new ChargeEffect(Effect.SMALL_SMOKE);
	
	public CollectFishAction(QuestPlayer player) {
		this.player = player;
		ret = null;
	}
	
	@Override
	public void onAction() {
				
		Player p = player.getPlayer().getPlayer();
		
		if (ret == null) {
			successEffect.play(p, null);
		} else {
			p.sendMessage(FishingGui.loseMessage);
			failEffect.play(p, null);
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
