package nmt.minecraft.QuestManager.Quest;

import java.util.Collection;

import org.bukkit.entity.Player;

/**
 * Quest Interface!<br />
 * 
 * 
 * 
 * 
 * @breakdown
 * quests run and stop. They save their state and load their state. They 
 * subscribe to events and have objectives. They are completed or failed.
 * They have rewards, disperse rewards, and collect tolls. They do whatever
 * the heck they want.
 * 
 * Specifically the quest interface specifies that quests can be started,
 * stopped, and halted. Quests must also keep track of involved players and
 * any parts of the quest involved (future work?). 
 * @author Skyler
 *
 */
public interface Quest {
	
	/**
	 * Stops the quest softly, optionally performing state-saving procedures
	 * and displaying messages to the involved players. Quests should also 
	 * deliver players back to an area where they are free to roam and return
	 * to homeworld portals (or the equivalent) when they stop.
	 */
	public void stop();
	
	/**
	 * <i>Immediately</i> stops the quest, returning players to a free-roaming
	 * state. Quests are not expected to perform save-state procedures when
	 * halted, but may. <br/>
	 * <b>Quests must immediately stop execution when asked to halt.<b>
	 */
	public void halt();
	
	/**
	 * Return all players involved in this quests.<br />
	 * Involved players are those participating in any way in the quest. For
	 * example, if a player is marked as a pvp target in a quest then they are
	 * involved in the quest.
	 * @return
	 */
	public Collection<Player> getPlayers();
	
	/**
	 * Returns the name of the quest, including text formatters and colors.
	 * @return The name of the quest
	 * @see {@link org.bukkit.ChatColor ChatColor}
	 */
	public String getName();
	
	/**
	 * Returns a (possibly multilined) description of the quest that will be made
	 * visible to players to aid in the quest selection process.<br />
	 * Descriptions should have a list of stakes and rewards, as well as either
	 * a hint or outline of/to objectives.
	 * @return
	 */
	public String getDescription();
}
