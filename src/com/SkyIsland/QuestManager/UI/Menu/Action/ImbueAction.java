package com.SkyIsland.QuestManager.UI.Menu.Action;

import com.SkyIsland.QuestManager.Magic.Imbuement;
import com.SkyIsland.QuestManager.Player.QuestPlayer;

public class ImbueAction implements MenuAction {

	private QuestPlayer player;
	
	private Imbuement imbuement;
	
	public ImbueAction(QuestPlayer player, Imbuement imbuement) {
		this.player = player;
		this.imbuement = imbuement;
	}
	
	@Override
	public void onAction() {
		/*
		 * Apply has just happened. Just need to put it on, actually
		 */
		player.setCurrentImbuement(imbuement);
		
	}

}
