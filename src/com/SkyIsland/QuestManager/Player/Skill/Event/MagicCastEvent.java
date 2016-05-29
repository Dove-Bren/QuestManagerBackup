package com.SkyIsland.QuestManager.Player.Skill.Event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.SkyIsland.QuestManager.Magic.Spell.Spell;
import com.SkyIsland.QuestManager.Player.QuestPlayer;

/**
 * Thrown when a {@link com.SkyIsland.QuestManager.Player.QuestPlayer QuestPlayer}
 * is casting a spell.
 * @author Skyler
 *
 */
public class MagicCastEvent extends Event {
	
	public enum MagicType {
		MAGERY,
		SPELLWEAVING,
		UTILITY;
	}

	private static final HandlerList handlers = new HandlerList();
		
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	private QuestPlayer player;
	
	private Spell castSpell;
	
	private MagicType type;
	
	private boolean isFail;
	
	public MagicCastEvent(QuestPlayer player, MagicType type, Spell spell) {
		this.player = player;
		this.type = type;
		this.castSpell = spell;
		isFail = false;
	}

	public MagicType getType() {
		return type;
	}
	
	public QuestPlayer getPlayer() {
		return player;
	}

	public void setPlayer(QuestPlayer player) {
		this.player = player;
	}

	public Spell getCastSpell() {
		return castSpell;
	}

	public void setCastSpell(Spell castSpell) {
		this.castSpell = castSpell;
	}

	public boolean isFail() {
		return isFail;
	}

	public void setFail(boolean isFail) {
		this.isFail = isFail;
	}
	
}
