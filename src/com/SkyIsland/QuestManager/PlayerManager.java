package com.SkyIsland.QuestManager;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.SkyIsland.QuestManager.Configuration.PluginConfiguration;
import com.SkyIsland.QuestManager.Configuration.Utils.GUID;
import com.SkyIsland.QuestManager.Fanciful.FancyMessage;
import com.SkyIsland.QuestManager.Player.Participant;
import com.SkyIsland.QuestManager.Player.Party;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Special.TitleEffect;
import com.SkyIsland.QuestManager.Scheduling.IntervalScheduler;
import com.SkyIsland.QuestManager.Scheduling.Tickable;

/**
 * Stores a database of QuestPlayers for lookup and loading
 * @author Skyler
 *
 */
public class PlayerManager implements Tickable {
	
	private Map<UUID, QuestPlayer> players;
	
	private Map<GUID, Party> parties;
	
	private TitleEffect titleEffect;
	
	/**
	 * Creates and loads player manager information from the provided configuration file.
	 * @param config
	 */
	public PlayerManager(YamlConfiguration config) {
		
		players = new HashMap<UUID, QuestPlayer>();
		parties = new HashMap<GUID, Party>();
		
		QuestManagerPlugin.questManagerPlugin.getLogger().info("Loading player database...");
		
		ConfigurationSection pSex = config.getConfigurationSection("players");
		
		if (!pSex.getKeys(false).isEmpty()) {
		QuestPlayer player;
			for (String key : pSex.getKeys(false)) {
				player = (QuestPlayer) pSex.get(key);
				players.put(UUID.fromString(player.getIDString()), player);
			}
		}
			
		ConfigurationSection gSex = config.getConfigurationSection("parties");
		
		if (!gSex.getKeys(false).isEmpty())
		for (String key : gSex.getKeys(false)) {
			parties.put(
					GUID.valueOf(key), (Party) gSex.get(key));
		}
		
		//check if we need to do day/night regen
		PluginConfiguration pc = QuestManagerPlugin.questManagerPlugin.getPluginConfiguration();
		if (pc.getMagicEnabled())
		if (pc.getMagicRegenDay() != 0 || pc.getMagicRegenNight() != 0) {
			IntervalScheduler.getScheduler().register(this);
		}
		
		this.titleEffect = new TitleEffect();
		
	}
	
	/**
	 * Returns the QuestPlayer corresponding the the passed OfflinePlayer.<br />
	 * This method creates a new QuestPlayer wrapper for the provided UUID if there does not
	 * already exist a record for it.
	 * @param id
	 * @return
	 */	
	public QuestPlayer getPlayer(OfflinePlayer player) {
		return (player == null ? null : getPlayer(player.getUniqueId()));
	}
	
	/**
	 * Returns the QuestPlayer corresponding the the passed UUID.<br />
	 * This method creates a new QuestPlayer wrapper for the provided UUID if there does not
	 * already exist a record for it.
	 * @param id
	 * @return
	 */
	public QuestPlayer getPlayer(UUID id) {
		if (players.containsKey(id)) {
			return players.get(id);
		}
		
		//initialize a player!
		QuestPlayer player = new QuestPlayer(Bukkit.getOfflinePlayer(id));
		players.put(id, player);
			
		if (player.getPlayer() != null && player.getPlayer().isOnline()) {
			FancyMessage msg = new FancyMessage("Welcome! Please take a moment to review your ")
					.color(ChatColor.GOLD)
					.then("Player Options")
					.color(ChatColor.GREEN)
					.command("/player options")
					.tooltip(ChatColor.AQUA + "Click here to open player options");
			msg.send(player.getPlayer().getPlayer());
		}
		
		return player;
	}
	
	/**
	 * Returns the party paired with the given ID.
	 * <br />if the party doesn't exist, null is returned instead
	 * @param id
	 * @return
	 */
	public Party getParty(GUID id) {
		if (!parties.containsKey(id)) {
			return null;
		}
		
		return parties.get(id);
	}
	
	public void addParty(Party party) {
		parties.put(party.getID(), party);
	}
	
	public void removeParty(Party party) {
		parties.remove(party.getID());
	}
	
	public Participant getParticipant(String idString) {
		
		if (GUID.valueOf(idString) != null) {
			return parties.get(GUID.valueOf(idString));
		}
		
		//assume it's a player string
		return getPlayer(UUID.fromString(idString));
	}
	
	public Collection<Party> getParties() {
		return parties.values();
	}
	
	public Collection<QuestPlayer> getPlayers() {
		return players.values();
	}
	
	public void save(File saveFile) {
		
		QuestManagerPlugin.questManagerPlugin.getLogger().info(
				"Saving player database...");
		
		YamlConfiguration config = new YamlConfiguration();
		ConfigurationSection playSex = config.createSection("players");
		
		QuestPlayer qp;
		if (!players.isEmpty()) {
			for (UUID key : players.keySet()) {
				qp = getPlayer(key);
				String name = qp.getPlayer().getName();
				playSex.set(name == null ? key.toString() : name + key.toString().substring(0, 5), getPlayer(key));
			}
		}
		
		ConfigurationSection gSex = config.createSection("parties");
		if (!parties.isEmpty()) {
			for (GUID key : parties.keySet()) {
				if (key == null) {
					continue;
				}
				gSex.set(key.toString(), getParty(key));
			}
		}
		
		try {
			config.save(saveFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void tick() {
		PluginConfiguration pc = QuestManagerPlugin.questManagerPlugin.getPluginConfiguration();
		double day = pc.getMagicRegenDay();
		double night = pc.getMagicRegenNight();
		for (QuestPlayer qp : players.values()) {
			OfflinePlayer p = qp.getPlayer();
			if (qp.getPlayer().isOnline() && QuestManagerPlugin.questManagerPlugin.getPluginConfiguration()
					.getWorlds().contains(p.getPlayer().getWorld().getName())) {
				//potential for regen
				long time = p.getPlayer().getWorld().getTime();
				Location ploc = p.getPlayer().getLocation();
				if (day != 0 && (time < 13000 || time >= 23000))
				if (!pc.getMagicRegenOutside() || (ploc.getBlockY() < ploc.getWorld().getMaxHeight()
						&& ploc.getBlock().getLightFromSky() > 13)) {
										
					qp.regenMP(day);
				}
				if (night != 0 && (time >= 13000 && time < 23000)) 
				if (!pc.getMagicRegenOutside() || (ploc.getBlockY() < ploc.getWorld().getMaxHeight()
						&& p.getPlayer().getLocation().getBlock().getLightFromSky() > 13)) {
										
					qp.regenMP(night);
				}
			}
		}
	}
	
	public TitleEffect getTitleEffect() {
		return titleEffect;
	}
	
}
