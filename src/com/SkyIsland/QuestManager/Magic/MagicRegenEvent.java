package com.SkyIsland.QuestManager.Magic;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MagicRegenEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	
	private MagicUser entity;
	
	private double amount;
	
	private boolean cancelled;
	
	private double modifier;
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	/**
	 * 
	 * @param entity
	 * @param amount
	 */
	public MagicRegenEvent(MagicUser entity, double amount) {
		this.entity = entity;
		this.amount = amount;
		this.modifier = 0;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public MagicUser getEntity() {
		return entity;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean arg0) {
		this.cancelled = arg0;
	}
	
	public double getModifier() {
		return modifier;
	}
	
	/**
	 * Set the modifier. This amount (+ 1) is multiplied by the amount to get the final amount
	 * @param mod
	 */
	public void setModifier(double mod) {
		this.modifier = mod;
	}
	
	public double getFinalAmount() {
		return amount * (1 + modifier);
	}

}
