package com.SkyIsland.QuestManager.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.Utils.LocationState;
import com.SkyIsland.QuestManager.Effects.ChargeEffect;
import com.SkyIsland.QuestManager.Fanciful.FancyMessage;
import com.SkyIsland.QuestManager.Magic.Imbuement;
import com.SkyIsland.QuestManager.Magic.ImbuementSet;
import com.SkyIsland.QuestManager.Magic.MagicRegenEvent;
import com.SkyIsland.QuestManager.Magic.MagicUser;
import com.SkyIsland.QuestManager.Magic.SpellPylon;
import com.SkyIsland.QuestManager.Magic.Spell.Spell;
import com.SkyIsland.QuestManager.Magic.Spell.SpellWeavingManager;
import com.SkyIsland.QuestManager.Magic.Spell.SpellWeavingSpell;
import com.SkyIsland.QuestManager.Magic.Spell.Effect.DamageEffect;
import com.SkyIsland.QuestManager.Player.Skill.Skill;
import com.SkyIsland.QuestManager.Player.Skill.Default.ImbuementSkill;
import com.SkyIsland.QuestManager.Player.Skill.Event.CombatEvent;
import com.SkyIsland.QuestManager.Player.Utils.Compass;
import com.SkyIsland.QuestManager.Player.Utils.CompassTrackable;
import com.SkyIsland.QuestManager.Player.Utils.ImbuementHolder;
import com.SkyIsland.QuestManager.Player.Utils.QuestJournal;
import com.SkyIsland.QuestManager.Player.Utils.QuestLog;
import com.SkyIsland.QuestManager.Player.Utils.SpellHolder;
import com.SkyIsland.QuestManager.Quest.Goal;
import com.SkyIsland.QuestManager.Quest.Quest;
import com.SkyIsland.QuestManager.Quest.History.History;
import com.SkyIsland.QuestManager.Quest.History.HistoryEvent;
import com.SkyIsland.QuestManager.Quest.Requirements.Requirement;
import com.SkyIsland.QuestManager.UI.ChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.ActiveInventoryMenu;
import com.SkyIsland.QuestManager.UI.Menu.ChatMenuOption;
import com.SkyIsland.QuestManager.UI.Menu.InventoryMenu;
import com.SkyIsland.QuestManager.UI.Menu.MultioptionChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.SimpleChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.Action.BootFromPartyAction;
import com.SkyIsland.QuestManager.UI.Menu.Action.ChangeSpellHolderAction;
import com.SkyIsland.QuestManager.UI.Menu.Action.ChangeTitleAction;
import com.SkyIsland.QuestManager.UI.Menu.Action.ChargeAction;
import com.SkyIsland.QuestManager.UI.Menu.Action.CreateImbuementAction;
import com.SkyIsland.QuestManager.UI.Menu.Action.ForgeAction;
import com.SkyIsland.QuestManager.UI.Menu.Action.ImbueAction;
import com.SkyIsland.QuestManager.UI.Menu.Action.PartyInviteAction;
import com.SkyIsland.QuestManager.UI.Menu.Action.ShowChatMenuAction;
import com.SkyIsland.QuestManager.UI.Menu.Action.TogglePlayerOptionAction;
import com.SkyIsland.QuestManager.UI.Menu.Inventory.BasicInventory;
import com.SkyIsland.QuestManager.UI.Menu.Inventory.BasicInventoryItem;
import com.SkyIsland.QuestManager.UI.Menu.Inventory.ContributionInventory;
import com.SkyIsland.QuestManager.UI.Menu.Message.PlainMessage;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.MultiversePortals.event.MVPortalEvent;

import io.puharesource.mc.titlemanager.api.TitleObject;

/**
 * Player wrapper to store questing information and make saving player quest status
 * easier
 * @author Skyler
 *
 */
public class QuestPlayer implements Participant, Listener, MagicUser, Comparable<QuestPlayer> {
	
	public static final String damageMessage = ChatColor.GRAY + "%s "
			+ ChatColor.DARK_GRAY + "did " + ChatColor.DARK_RED + "%.2f damage"
			+ ChatColor.DARK_GRAY + " to you" + ChatColor.RESET;
	
	public static final String damageBlockMessage = ChatColor.DARK_GRAY + "You received " 
			+ ChatColor.RED + "%.2f damage" + ChatColor.RESET;
	
	public static final String pylonsResetMessage = ChatColor.DARK_GRAY + "Your spell pylons have been cleared"
			+ ChatColor.RESET;
	
	public static final String spellWeavingManaMesage = ChatColor.DARK_GRAY + "Your energies were properly"
			+ " attuned, but you " + ChatColor.GRAY + "lacked the mana" + ChatColor.DARK_GRAY 
			+ " to properly invoke your spell" + ChatColor.RESET;
	
	public static boolean meetsRequirement(QuestPlayer player, String requirement) {
		if (requirement.contains("|")) {
			String[] reqs = requirement.split("\\|");
			for (String req : reqs) {
				if (meetsRequirement(player, req)) {
					return true;
				}
			}
			
			return false;
		}
		
		if (requirement.startsWith("*")) {
			String req = requirement.substring(1);
			for (Quest q : player.getCurrentQuests()) {
				if (q.getName().equals(req)) {
					return true;
				}
			}
			
			return false;
		}
		
		//not |'s or *----, so regular
		return player.getCompletedQuests().contains(requirement);
	}
	
	private UUID playerID;
	
	private PlayerOptions options;
	
	private History history;
	
	private List<Quest> currentQuests;
	
	private List<String> completedQuests;
	
	private String focusQuest;
	
	private List<String> journalNotes;
	
	private int fame;
	
	/**
	 * Tracks how much fame this player has ever had, not how much is fluid and spendable
	 */
	private int alphaFame;
	
	private int money;
	
	private int level;
	
	private int maxHp;
	
	private double mp;
	
	private double maxMp;
	
	private Map<Material, String> storedSpells;
	
	private Map<Integer, ImbuementSet> storedImbuements;
	
	private List<SpellPylon> pylons;
	
	private List<String> spells;
	
	private String title;
	
	private List<String> unlockedTitles;
	
	private Location questPortal;
	
	private Party party;
	
	private CompassTrackable compassTarget;
	
	private Imbuement currentImbuement;
	
	private Map<Skill, Integer> skillLevels;
	
	private Map<Skill, Float> skillXP;
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(QuestPlayer.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(QuestPlayer.class);
	}
	

	private enum aliases {
		FULL("com.SkyIsland.QuestManager.Player.QuestPlayer"),
		DEFAULT(QuestPlayer.class.getName()),
		SHORT("QuestPlayer"),
		INFORMAL("QP"),
		QUALIFIED_INFORMAL("QMQP");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	private QuestPlayer() {
		System.out.println("creation");
		this.fame = 0;
		this.alphaFame = 0;
		this.money = 0;
		this.maxMp = QuestManagerPlugin.questManagerPlugin.getPluginConfiguration()
				.getStartingMana();
		this.mp = maxMp;
		this.maxHp = 20;
		this.level = 1;
		this.title = "The Unknown";
		this.unlockedTitles = new LinkedList<String>();
		this.journalNotes = new LinkedList<String>();
		this.spells = new LinkedList<>();
		this.storedSpells = new HashMap<>();
		this.storedImbuements = new HashMap<>();
		this.pylons = new LinkedList<SpellPylon>();
		this.skillLevels = new HashMap<>();
		this.skillXP = new HashMap<>();
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	/**
	 * Creates a new QuestPlayer wrapper for the given player.<br />
	 * This wrapper holds no information, and is best used when the player has never been
	 * wrapped before
	 * @param player
	 */
	public QuestPlayer(OfflinePlayer player) {
		this();
		this.playerID = player.getUniqueId();
		this.currentQuests = new LinkedList<Quest>();
		this.completedQuests = new LinkedList<String>();
		this.history = new History();
		
		if (player.isOnline()) {
			Player p = player.getPlayer();
			if (QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getWorlds()
					.contains(p.getWorld().getName())) {
				questPortal = p.getWorld().getSpawnLocation();
			}
		}
	}
	
	
	public History getHistory() {
		return history;
	}
	
	/**
	 * Adds a quest book to the players inventory, if there is space.<br />
	 * This method will produce a fully updated quest book in the players inventory.
	 */
	public void addQuestBook() {
		QuestLog.addQuestlog(this);
	}
	
	/**
	 * Adds a journal to the player's inventory if there's space. Also updates immediately.
	 */
	public void addJournal() {
		QuestJournal.addQuestJournal(this);
	}
	
	/**
	 * Updates the players quest book, if they have it in their inventory.<br />
	 * If the user does not have abook already or has discarded it, this method will do nothing.
	 */
	public void updateQuestBook(boolean silent) {
		QuestLog.updateQuestlog(this, silent);
		updateCompass(true);
	}
	
	public void updateQuestLog(boolean silent) {
		QuestJournal.updateQuestJournal(this, silent);
	}
	
	public void updateCompass(boolean silent) {
		this.getNextTarget();
		Compass.updateCompass(this, silent);
	}
	
	public void setCompassTarget(CompassTrackable target, boolean silent) {
		this.compassTarget = target;
		updateCompass(silent);
	}
	
	public Location getCompassTarget() {
		if (compassTarget == null) {
			return null;
		}
		
		return compassTarget.getLocation();
	}
	
	public List<Quest> getCurrentQuests() {
		return currentQuests;
	}
	
	public List<String> getCompletedQuests() {
		return completedQuests;
	}
	
	public boolean hasCompleted(Quest quest) {
		return this.hasCompleted(quest.getName());
	}
	
	public boolean hasCompleted(String name) {
		return completedQuests.contains(name);
	}
	
	/**
	 * Checks and returns whether or not the player is in this TYPE of quest.<br />
	 * To see whether this player is in this particular instance of the quest, use
	 * the quest's {@link com.SkyIsland.QuestManager.Quest.Quest#getPlayers() getPlayers()}
	 * method and traditional lookup techniques instead.
	 * @param quest
	 * @return
	 */
	public boolean isInQuest(Quest quest) {
		return isInQuest(quest.getName());
	}
	
	public boolean isInQuest(String questName) {
		for (Quest quest : currentQuests) {
			if (quest.getName().equals(questName)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void addQuest(Quest quest) {
		currentQuests.add(quest);
		history.addHistoryEvent(new HistoryEvent("Accepted the quest " + ChatColor.DARK_PURPLE + quest.getName()));
		if (focusQuest == null) {
			setFocusQuest(quest.getName());
		}
		//addQuestBook();
		//updateQuestBook();
	}
	
	public boolean removeQuest(Quest quest) {
		
		if (currentQuests.isEmpty()) {
			return false;
		}
		
		Iterator<Quest> it = currentQuests.iterator();
		
		while (it.hasNext()) {
			Quest q = it.next();
			if (q.equals(quest)) {
				it.remove();
				if (focusQuest.equals(quest.getName())) {
					if (currentQuests.isEmpty()) {
						focusQuest = null;
						QuestJournal.addQuestJournal(this);
					} else {
						setFocusQuest(currentQuests.get(0).getName());
					}
				}
				return true;
			}
		}
		
		return false;
	}
	
	public void completeQuest(Quest quest) {
		if (!completedQuests.contains(quest.getName())) {
			completedQuests.add(quest.getName());			
		}
		removeQuest(quest);
		
		history.addHistoryEvent(
				new HistoryEvent("Completed the quest " + ChatColor.DARK_PURPLE + quest.getName()));
		
		if (getPlayer().isOnline()) {
			Player p = getPlayer().getPlayer();
			p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.2f);
		}
	}
	
	public int getFame() {
		return fame;
	}
	
	/**
	 * Returns how much fame this player has received over their game life.<Br />
	 * Alpha fame is not spent when leveling up or doing similar fame-spending activities.
	 * @return
	 */
	public int getAlphaFame() {
		return alphaFame;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void addFame(int fame) {
		this.fame += fame;
		if (fame > 0) {
			this.alphaFame += fame;
		}
	}
	
	public void setFame(int fame) {
		this.fame = fame;
	}
	
	public Party getParty() {
		return party;
	}
	
	public Party createParty() {
		this.party = new Party(this);
		return party;
	}
	
	public void joinParty(Party party) {
		if (party.addMember(this))	{
			this.party = party;
		}
	}
	
	public void leaveParty(String message) {
		if (getPlayer().isOnline()) {
			getPlayer().getPlayer().sendMessage(message);
			getPlayer().getPlayer().setScoreboard(
					Bukkit.getScoreboardManager().getNewScoreboard());
		}
		
		if (!currentQuests.isEmpty()) {
			for (Quest q : currentQuests) {
				if (q.getRequireParty()) {
					removeQuest(q);
					if (getPlayer().isOnline()) {
						getPlayer().getPlayer().sendMessage(ChatColor.YELLOW + "The quest " 
								+ ChatColor.DARK_PURPLE + q.getName() + ChatColor.YELLOW
								+ " has been failed because you left the party!");
					}
				}
			}
		}
		
		this.party = null;
	}
	
	/**
	 * @return the money
	 */
	public int getMoney() {
		return money;
	}

	/**
	 * @param money the money to set
	 */
	public void setMoney(int money) {
		this.money = money;
		if (getPlayer().isOnline())
		if (QuestManagerPlugin.questManagerPlugin.getPluginConfiguration()
					.getWorlds().contains(getPlayer().getPlayer().getWorld().getName())) {
			getPlayer().getPlayer().setLevel(this.money);
		}
	}
	
	/**
	 * Add some money to the player's wallet
	 * @param money
	 */
	public void addMoney(int money) {
		this.money += money;
		if (getPlayer().isOnline())
			if (QuestManagerPlugin.questManagerPlugin.getPluginConfiguration()
						.getWorlds().contains(getPlayer().getPlayer().getWorld().getName())) {
				getPlayer().getPlayer().setLevel(this.money);
			}
	}
	
	public void levelUp(int hpIncrease, int mpIncrease) {
		level++;
		maxHp += hpIncrease;
		maxMp += mpIncrease;
		mp = maxMp;
		if (getPlayer().isOnline()) {
			Player p = getPlayer().getPlayer();
			p.setMaxHealth(maxHp);
			p.setHealth(maxHp);
			
			refreshPlayer();
		}
	}

	public int getLevel() {
		return level;
	}

	public int getMaxHp() {
		return maxHp;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void addTitle(String title) {
		if (this.unlockedTitles.contains(title)) {
			return;
		}
		this.unlockedTitles.add(title);
		
		if (!getPlayer().isOnline()) {
			return;
		}
		
		ChatMenu menu = new SimpleChatMenu(
				new FancyMessage("You've unlocked the ")
					.color(ChatColor.DARK_GRAY)
				.then(title)
					.color(ChatColor.GOLD)
					.style(ChatColor.BOLD)
				.then(" title!"));
		
		menu.show(getPlayer().getPlayer());
		
		(new TitleObject(ChatColor.GREEN + "Title Unlocked",
				""))
		.setFadeIn(30).setFadeOut(30).setStay(80).send(getPlayer().getPlayer());
        
        getPlayer().getPlayer().playSound(getPlayer().getPlayer().getLocation(), Sound.ENTITY_FIREWORK_TWINKLE, 10, 1);
	}

	public void addSpell(String spellName) {
		if (this.spells.contains(spellName)) {
			return;
		}
		this.spells.add(spellName);
		
		if (!getPlayer().isOnline()) {
			return;
		}
		
		ChatMenu menu = new SimpleChatMenu(
				new FancyMessage("You've learned the ")
					.color(ChatColor.DARK_PURPLE)
				.then(spellName)
					.color(ChatColor.GOLD)
					.style(ChatColor.BOLD)
				.then(" spell!"));
		
		menu.show(getPlayer().getPlayer());
		
		(new TitleObject(ChatColor.GREEN + "Spell Learned!",
				""))
		.setFadeIn(30).setFadeOut(30).setStay(80).send(getPlayer().getPlayer());
        
        getPlayer().getPlayer().playSound(getPlayer().getPlayer().getLocation(), Sound.ENTITY_FIREWORK_TWINKLE, 10, 1);
	}
	
//	/**
//	 * Returns the currently-stored information in a YamlConfiguration. <br />
//	 * Output from this method should be expected to be used with {@link QuestPlayer#fromConfig(YamlConfiguration)}
//	 * to produce an exact duplicate.
//	 * @return
//	 */
//	public YamlConfiguration toConfig() {
//		
//		if (player == null) {
//			return null;
//		}
//		
//		YamlConfiguration config = new YamlConfiguration();
//		
//		config.set("Player", player.getUniqueId().toString());
//		config.set("History", history.toConfig());
//		config.set("Quests", currentQuests);
//		
//		return config;
//	}

	@Override
	public Collection<QuestPlayer> getParticipants() {
		Collection<QuestPlayer> col = new ArrayList<QuestPlayer>();
		col.add(this);
		return col;
	}

	/**
	 * Converts the quest player to serialized configuration output
	 */
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>(3);
		map.put("title", title);
		map.put("unlockedtitles", unlockedTitles);
		map.put("fame", fame);
		map.put("alphaFame", alphaFame);
		map.put("money", money);
		map.put("level", level);
		map.put("maxhp", maxHp);
		map.put("mp", mp);
		map.put("maxmp", maxMp);
		map.put("id", getPlayer().getUniqueId().toString());
		map.put("portalloc", this.questPortal);
		map.put("completedquests", completedQuests);
		map.put("focusquest", focusQuest);
		map.put("notes", journalNotes);
		map.put("spells", spells);
		
		Map<String, String> stored = new TreeMap<>();
		for (Material m : storedSpells.keySet()) {
			stored.put(m.name(), storedSpells.get(m));
		}
		map.put("storedspells", stored);
		
		Map<Integer, ImbuementSet> imbs = new TreeMap<>();
		for (Integer data : storedImbuements.keySet()) {
			imbs.put(data, storedImbuements.get(data));
		}
		map.put("storedimbuements", imbs);
		
		Map<String, Map<String, Object>> skillMap = new TreeMap<>();
		if (!skillLevels.isEmpty()) {
			for (Skill skill : skillLevels.keySet()) {
				Map<String, Object> detailMap = new TreeMap<>();
				detailMap.put("level", skillLevels.get(skill));
				detailMap.put("xp", skillXP.get(skill));
				
				skillMap.put(skill.getConfigKey(), detailMap);
			}
			
			map.put("skills", skillMap);
		}
		
		map.put("options", this.getOptions());

		return map;
	}
	
	/**
	 * Constucts and returns a QuestPlayer to match the data given in the passed map.
	 * @param map The configuration map to initialize the player on.
	 * @return A new quest player or null on error
	 */
	@SuppressWarnings("unchecked")
	public static QuestPlayer valueOf(Map<String, Object> map) {
		if (map == null || !map.containsKey("id") || !map.containsKey("fame") 
				 || !map.containsKey("title") || !map.containsKey("completedquests")
				 || !map.containsKey("portalloc") || !map.containsKey("money")
				 || !map.containsKey("unlockedtitles")) {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Invalid Quest Player! "
					+ (map.containsKey("id") ? ": " + map.get("id") : ""));
			return null;
		}
		
		OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(
				(String) map.get("id")));
		QuestPlayer qp = new QuestPlayer(player);
		
		if (map.get("portalloc") == null) {
			qp.questPortal = null;
		} else {
			qp.questPortal = ((LocationState) map.get("portalloc")).getLocation();
		}
		
		qp.fame = (int) map.get("fame");
		qp.money = (int) map.get("money");
		qp.title = (String) map.get("title");
		qp.unlockedTitles = (List<String>) map.get("unlockedtitles");
		qp.completedQuests = (List<String>) map.get("completedquests");
		qp.focusQuest = (String) map.get("focusquest");
		qp.journalNotes = (List<String>) map.get("notes");
		
		////////Update code 1///////////
		if (map.containsKey("alphaFame")) {
			qp.alphaFame = (int) map.get("alphaFame");
		} else {
			qp.alphaFame = qp.fame;
		}
		if (map.containsKey("mp")) {
			qp.mp = (double) map.get("mp");
		} //else handled by default constructor
		
		if (map.containsKey("maxmp")) {
			qp.maxMp = (double) map.get("maxmp");
		} //else again handled by default constructor
		
		if (map.containsKey("maxhp")) {
			qp.maxHp = (int) map.get("maxhp");
		} //""
		
		if (map.containsKey("level")) {
			qp.level = (int) map.get("level");
		} //""
		
		if (map.containsKey("spells")) {
			qp.spells = (List<String>) map.get("spells");
		} // ""
		
		if (map.containsKey("storedspells")) {
			Map<Material, String> stored = new HashMap<>();
			Map<String, String> act = (Map<String, String>) map.get("storedspells");
			
			for (String name : act.keySet()) {
				try {
					stored.put(Material.valueOf(name), act.get(name));
				} catch (Exception e) {
					QuestManagerPlugin.questManagerPlugin.getLogger().warning(
						"Failed to find material [" + name + "] when restoring player ["
						+ qp.getIDString() + "]'s spells!");
					continue;
				}
			}
			
			qp.storedSpells = stored;
		}
		
		////////Update code 2///////////
		if (map.containsKey("skills")) {
			Map<String, Object> skillMap = (Map<String, Object>) map.get("skills");
			for (String skillName : skillMap.keySet()) {
				for (Skill skill : QuestManagerPlugin.questManagerPlugin.getSkillManager().getAllSkills()) {
					if (skill.getConfigKey().equals(skillName)) {
						try {
							Map<String, Object> detailMap = (Map<String, Object>) skillMap.get(skillName);
							qp.setSkillLevel(skill, (int) detailMap.get("level"));
							if (detailMap.get("xp") == null) {
								qp.setSkillExperience(skill, 0f);
							} else {
								qp.setSkillExperience(skill,(float) ((double) detailMap.get("xp")));
							}
						} catch (Exception e) {
							e.printStackTrace();
							QuestManagerPlugin.questManagerPlugin.getLogger().warning("Failed to load skill configuration for skill " + skillName);
						}
						
						break;
						
					}
				}
			}
		}
		
		if (map.containsKey("options")) {
			qp.options = (PlayerOptions) map.get("options");
		} else {
			if (player != null && player.isOnline()) {
				FancyMessage msg = new FancyMessage("Welcome! Please take a moment to review your ")
						.color(ChatColor.GOLD)
						.then("Player Options")
						.color(ChatColor.GREEN)
						.command("/player options")
						.tooltip(ChatColor.AQUA + "Click here to open player options");
				msg.send(player.getPlayer());
			}
		}
		

		
		if (map.containsKey("storedimbuements")) {
			qp.storedImbuements = (Map<Integer, ImbuementSet>) map.get("storedimbuements");
		}
		
		////////////////////////////////
				
		if (qp.completedQuests == null) {
			qp.completedQuests = new LinkedList<String>();
		}
		
		if (qp.unlockedTitles == null) {
			qp.unlockedTitles = new LinkedList<String>();
		}
		
		if (qp.journalNotes == null) {
			qp.journalNotes = new LinkedList<String>();
		}
		
		return qp;
	}

	@Override
	public String getIDString() {
		return getPlayer().getUniqueId().toString();
	}

	/**
	 * @return the questPortal
	 */
	public Location getQuestPortal() {
		return questPortal;
	}

	/**
	 * @param questPortal the questPortal to set
	 */
	public void setQuestPortal(Location questPortal) {
		this.questPortal = questPortal;
	}
	
	@EventHandler
	public void onPortal(MVPortalEvent e) {
		
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getUsePortals()) {
			return;
		}
		
		if (!getPlayer().isOnline() || e.isCancelled()) {
			return;
		}
		
		if (!e.getFrom().getWorld().equals(e.getDestination().getLocation(getPlayer().getPlayer()).getWorld())) {
			this.clearSpellPylons();
			getPlayer().getPlayer().sendMessage(pylonsResetMessage);
		}
			
		if (e.getTeleportee().equals(getPlayer())) {
			List<String> qworlds = QuestManagerPlugin.questManagerPlugin.getPluginConfiguration()
					.getWorlds();
			if (qworlds.contains(e.getFrom().getWorld().getName())) {
				
				//check that we aren't going TO antoher quest world
				if (qworlds.contains(e.getDestination().getLocation(getPlayer().getPlayer()).getWorld().getName())) {
					//we are! Don't interfere here
					return;
				}
				
				//we're leaving a quest world, so save the portal!
				this.questPortal = e.getFrom();
				
				//player quit
				onPlayerQuit();
				return;
			}
			if (qworlds.contains(e.getDestination().getLocation(getPlayer().getPlayer()).getWorld().getName())) {
				//Before we warp to our old location, we need to make sure we HAVE one
				if (this.questPortal == null) {
					//this is our first time coming in, so just let the portal take us
					//and save where it plops us out at
					this.questPortal = e.getDestination().getLocation(getPlayer().getPlayer());
					return;
				}
				
				//we're moving TO a quest world, so actually go to our saved location
				e.setCancelled(true);
				getPlayer().getPlayer().teleport(questPortal);
			}
		}
	}

	@EventHandler
	public void onExp(PlayerExpChangeEvent e) {
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getXPMoney()) {
			return;
		}
		if (!getPlayer().isOnline()) {
			return;
		}
		
		Player p = getPlayer().getPlayer();
		
		if (!p.getUniqueId().equals(e.getPlayer().getUniqueId())) {
			return;
		}
		
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration()
				.getWorlds().contains(p.getWorld().getName())) {
			return;
		}

		money += e.getAmount();
		p.setLevel(money);
		
		if (QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getMagicEnabled()
				 && QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getMagicRegenXP() != 0) {
			double amt = QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getMagicRegenXP();
			regenMP(amt);
		}
		
		e.setAmount(0);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		
		if (!getPlayer().isOnline()) {
			return;
		}
		
		Player p = getPlayer().getPlayer();
		
		if (!p.getUniqueId().equals(e.getPlayer().getUniqueId())) {
			return;
		}
		
		if (e.getItem() == null) {
			return;
		}
		
		if (e.getItem() != null && e.getItem().getType().equals(Material.WRITTEN_BOOK)) {
			BookMeta meta = (BookMeta) e.getItem().getItemMeta();
			
			if (meta.getTitle().equals("Quest Log") && 
					e.getItem().getEnchantmentLevel(Enchantment.LUCK) == 5) {
				//it's a quest log. Update it
				
				updateQuestBook(true);
			}
			
			return;
		}
		
		if (e.getItem().hasItemMeta() && e.getItem().getType() == Material.BOOK_AND_QUILL) {
			BookMeta meta = (BookMeta) e.getItem().getItemMeta();
			if (meta.hasTitle() && meta.getTitle().equals("Journal")
					&& meta.hasAuthor() && meta.getAuthor().equals(p.getName())
					&& e.getItem().getEnchantmentLevel(Enchantment.LUCK) == 5) {
				updateQuestLog(true);
			}
			
			return;
		}
		
		if (Compass.CompassDefinition.isCompass(e.getItem())) {
			updateCompass(false);
			return;
		}
		
		if (SpellHolder.SpellHolderDefinition.isHolder(e.getItem())) {
			//check for alter first
			if (e.getClickedBlock() != null)
			if (SpellHolder.SpellAlterTableDefinition.isTable(e.getClickedBlock())) {
				if (QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getMagicEnabled())
				showSpellAlterMenu(e.getItem());
				e.setCancelled(true);
				return;
			}
			
			castSpell(SpellHolder.getSpell(this, e.getItem()));
			e.setCancelled(true);
			return;
		}
		
		if (ImbuementHolder.ImbuementHolderDefinition.isHolder(e.getItem())) {
			if (e.getClickedBlock() != null)
			if (ImbuementHolder.ImbuementAlterTableDefinition.isTable(e.getClickedBlock())) {
				if (QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getMagicEnabled())
				showImbuementAlterMenu(e.getItem());
				e.setCancelled(true);
				return;
			}
			
			//final check; do they have a weapon in offhand?
			ItemStack otherItem;
			if (e.getHand().equals(EquipmentSlot.HAND)) {
				otherItem = e.getPlayer().getInventory().getItemInOffHand();
			} else {
				otherItem = e.getPlayer().getInventory().getItemInMainHand();
			}
			
			if (otherItem == null || !ForgeAction.Repairable.isRepairable(otherItem.getType())) {
				return; //can only imbue equipment
			}
			
			/*
			 * Calculate apply cost and time.
			 * Calculate slash cost
			 * Make imbuement
			 * start sharging
			 */
			ImbuementSkill skill = QuestManagerPlugin.questManagerPlugin.getImbuementHandler().getImbuementSkill();
			ImbuementSet set = ImbuementHolder.getImbuement(this, e.getItem());
			
			if (set == null) {
				return;
			}
			
			double applyCost = (skill == null ? 0.0 : skill.getApplyCost(this, set));
			
			if (!getPlayer().getPlayer().getGameMode().equals(GameMode.CREATIVE) && 
					mp < applyCost) {
				getPlayer().getPlayer().playSound(getPlayer().getPlayer().getLocation(), Sound.BLOCK_WATERLILY_PLACE, 1.0f, 0.5f);
				return;
			}
			
			double applyTime = (skill == null ? 1.0 : skill.getApplyTime(this));
			double slashCost = (skill == null ? 0.0 : skill.getSlashCost(this, set));

			Imbuement imb = new Imbuement(this, set.getEffects(), slashCost);
			
			ImbueAction action = new ImbueAction(this, imb);
			new ChargeAction(action, this, false, false, false, applyTime);
			
			this.addMP(-applyCost);
			
			e.setCancelled(true);
			return;
		}
		
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (!getPlayer().isOnline()) {
			return;
		}
		
		Player p = getPlayer().getPlayer();
		
		if (!p.getUniqueId().equals(e.getEntity().getUniqueId())) {
			return;
		}
		
		System.out.println("ID's matched for " + p.getName() + " and " + e.getEntity().getName());
		System.out.println(this.hashCode());
		
		
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration()
				.getWorlds().contains(p.getWorld().getName())) {
			return;
		}
		
		e.setDroppedExp(0);
		e.setNewLevel(money);
		e.setKeepInventory(true);
		
		boolean trip = false;
		
		//step through inventory, reduce durability of equipment
		for (ItemStack item : p.getInventory()) {
			if (item == null || item.getType() == Material.AIR) {
				continue;
			}
			
			if (ForgeAction.Repairable.isRepairable(item.getType()))
			if (!item.getItemMeta().spigot().isUnbreakable()) {
				trip = true;
				item.setDurability((short) Math.min(item.getType().getMaxDurability() - 1, 
						item.getDurability() + item.getType().getMaxDurability() / 2));
			}
		}
		
		for (ItemStack item : p.getEquipment().getArmorContents()) {
			if (item == null || item.getType() == Material.AIR) {
				continue;
			}
			
			if (ForgeAction.Repairable.isRepairable(item.getType())) {
				trip = true;
				item.setDurability((short) Math.min(item.getType().getMaxDurability() - 1, 
						item.getDurability() + item.getType().getMaxDurability() / 2));
			}
		}
		
		if (trip) {
			p.sendMessage(ChatColor.DARK_RED + "Your equipment has been damaged!" + ChatColor.RESET);
		}
		
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {

		if (!getPlayer().getUniqueId().equals(e.getPlayer().getUniqueId())) {
			return;
		}

		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration()
				.getWorlds().contains(e.getRespawnLocation().getWorld().getName())) {
			return;
		}
		
		refreshPlayer();
		if (this.party != null) {
			this.party.updateScoreboard(this, this.maxHp);
		}
		
		//in a quest world, so put them back to their last checkpoint
		e.setRespawnLocation(
				this.questPortal);
		MultiversePortals mvp = (MultiversePortals) Bukkit.getPluginManager().getPlugin("Multiverse-Portals");
		
		if (mvp == null) {
			System.out.println("null");
			return;
		}
		
		PortalPlayerSession ps = mvp.getPortalSession(e.getPlayer());
		ps.playerDidTeleport(questPortal);
		ps.setTeleportTime(new Date());
		
		
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if (e.getPlayer().getUniqueId().equals(getPlayer().getUniqueId())) {
			onPlayerQuit();
		}
	}
	
	/**
	 * Internal helper method to house what happens when a player quits (by leaving, logging out, etc)
	 */
	private void onPlayerQuit() {
		if (party != null) {
			party.removePlayer(this, "You've been disconnected!");
			party = null;
		}
		
		for (Quest q : currentQuests) {
			if (q.getTemplate().isSession()) {
				q.fail();
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractWithPlayer(PlayerInteractEntityEvent e) {
		if (!getPlayer().isOnline()) {
			return;
		}
		
		Player p = getPlayer().getPlayer();
		
		if (!p.getUniqueId().equals(e.getPlayer().getUniqueId())) {
			return;
		}
		
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getWorlds()
				.contains(p.getWorld().getName())) {
			return;
		}
		
		//did interact with another player?
		if (e.getRightClicked() instanceof Player) {
			QuestPlayer qp = QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer(
					(Player) e.getRightClicked());
			
			showPlayerMenu(qp);
			return;
		}
	
	}
	
	@EventHandler
	public void onPlayerRuinJournal(PlayerEditBookEvent e) {
		if (!getPlayer().isOnline()) {
			return;
		}
		
		if (!e.getPlayer().equals(getPlayer().getPlayer())) {
			return;
		}
		
		BookMeta oldMeta = e.getPreviousBookMeta(),
				newMeta = e.getNewBookMeta();
		
		if (oldMeta.hasTitle() && oldMeta.getTitle().equals("Journal")
			&& oldMeta.hasAuthor() && oldMeta.getAuthor().equals(e.getPlayer().getName())
			&& oldMeta.getEnchantLevel(Enchantment.LUCK) == 5) {
			//grab the player notes
			int pageNum;
			String page;
			for (pageNum = 1; pageNum <= newMeta.getPageCount(); pageNum++) {
				page = newMeta.getPage(pageNum);
				if (page.contains("  Player Notes")) {
					break;
				}
			}
			pageNum++;
			this.journalNotes.clear();
			if (pageNum > newMeta.getPageCount()) {
				//we went beyond what we have
			} else {
				//save their notes
				for (; pageNum <= newMeta.getPageCount(); pageNum++) {
					journalNotes.add(newMeta.getPage(pageNum));
				}
			}
			
			e.setCancelled(true);
			QuestJournal.updateQuestJournal(this, true);
			
		}
	}
	
	/**
	 * Displays for this quest player a player menu for the given player.
	 * @param player
	 */
	private void showPlayerMenu(QuestPlayer player) {
		/*
		 * ++++++++++++++++++++++++++++++
		 *     Name - Title
		 *     
		 *  Send Message    View Info      Trade
		 *  Invite To Party
		 * ++++++++++++++++++++++++++++++
		 */
		FancyMessage msg = new FancyMessage(player.getPlayer().getName() + "  -  " + player.getTitle());
		
		ChatMenuOption opt1;
		
		if (party != null && player.party != null && player.getParty().getIDString().equals(party.getIDString())) {
			//already in party, so give option to kick
			if (party.getLeader().getIDString().equals(getIDString())) {
				opt1 = new ChatMenuOption(new PlainMessage("Kick from Party"),
						new BootFromPartyAction(party, player));
			} else {
				opt1 = new ChatMenuOption(new PlainMessage(new FancyMessage("Kick from Party").color(ChatColor.DARK_GRAY)),
						new ShowChatMenuAction(
								new SimpleChatMenu(new FancyMessage("Only the party leader can kick players!").color(ChatColor.DARK_RED))
								, getPlayer().getPlayer()));
			}
		} else {
			opt1 = new ChatMenuOption(new PlainMessage("Invite to Party"), 
					new PartyInviteAction(this, player));
		}
		
		
		ChatMenuOption opt2 = new ChatMenuOption(new PlainMessage("View Info"), 
				new ShowChatMenuAction(new SimpleChatMenu(
						new FancyMessage(player.getPlayer().getName())
							.color(ChatColor.DARK_PURPLE)
						.then(" - ")
							.color(ChatColor.WHITE)
						.then(player.getTitle())
						.then("\n\n")
						.then("Level " + player.level + "\n")
						.then("This player has ")
						.then(player.money + "")
							.color(ChatColor.GOLD)
						.then("gold and ")
						    .color(ChatColor.WHITE)
						.then(player.alphaFame + "")
							.color(ChatColor.GOLD)
					    .then(" fame.\nThis player has completed ")
							.color(ChatColor.WHITE)
						.then("" + player.completedQuests.size())
							.color(ChatColor.GREEN)
							.tooltip(player.completedQuests)
						.then(" quests.")
							.color(ChatColor.WHITE)
					), 
				this.getPlayer().getPlayer()));
		
		ChatMenu menu = new MultioptionChatMenu(new PlainMessage(msg), opt1, opt2);
		
		menu.show(this.getPlayer().getPlayer().getPlayer());
		
	}
	
	/**
	 * Shows to this player their personal title menu, used to switch titles
	 */
	public void showTitleMenu() {		
		if (!getPlayer().isOnline()) {
			return;
		}
		
		if (this.unlockedTitles.isEmpty()) {
			ChatMenu menu = new SimpleChatMenu(new FancyMessage("You have not unlocked any titles!").color(ChatColor.DARK_RED));
			menu.show(getPlayer().getPlayer());
			return;
		}
		
		LinkedList<ChatMenuOption> opts = new LinkedList<ChatMenuOption>();
		
		for (String t : unlockedTitles) {
			opts.add(new ChatMenuOption(
					new PlainMessage(t),
					new ChangeTitleAction(this, t)));
		}
		

		MultioptionChatMenu menu = new MultioptionChatMenu(new PlainMessage("Choose your title:"), opts);
		
		menu.show(getPlayer().getPlayer());
		
	}
	
	public void showPlayerOptionMenu() {
		if (!getPlayer().isOnline()) {
			return;
		}
		
		
		BasicInventory inv = new BasicInventory();
		
		for (PlayerOptions.Key key : PlayerOptions.Key.values()) {
			inv.addInventoryItem(new BasicInventoryItem(key.getIcon(), key.getHint(), 
					new TogglePlayerOptionAction(this, key)
					));
			
		}
		
		InventoryMenu menu = new InventoryMenu(this, inv);
		QuestManagerPlugin.questManagerPlugin.getInventoryGuiHandler().showMenu(
				getPlayer().getPlayer(), menu);
	}
	
	public void showSpellAlterMenu(ItemStack holder) {
		if (!getPlayer().isOnline()) {
			return;
		}
		
		if (this.spells.isEmpty()) {
			ChatMenu menu = new SimpleChatMenu(new FancyMessage("You have not unlocked any spells!").color(ChatColor.DARK_RED));
			menu.show(getPlayer().getPlayer());
			return;
		}
		
		LinkedList<ChatMenuOption> opts = new LinkedList<ChatMenuOption>();
		
		//spells.sort(null); Not working. Dunno why :S
		
		for (String t : spells) {
			List<String> descList = new LinkedList<>();
			Spell sp = QuestManagerPlugin.questManagerPlugin.getSpellManager().getSpell(t);
			if (sp != null) {
				
				descList.add(ChatColor.RED + sp.getName());
				descList.add(ChatColor.GOLD + "Difficulty: " + sp.getDifficulty());
				descList.add(ChatColor.BLUE + "Mana Cost: " + sp.getCost());
				
				String desc;
				desc = sp.getDescription();
				
				String mid;
				int pos;
				while (desc.length() > 30) {
					
					desc = desc.trim();
					
					//find first space before 30
					mid = desc.substring(0, 30);
					pos = mid.lastIndexOf(" ");
					if (pos == -1) {
						descList.add(mid);
						desc = desc.substring(30);
						continue;
					}
					//else we found a space
					descList.add(mid.substring(0, pos));
					desc = desc.substring(pos);
				}
				
				descList.add(desc.trim());
			} else {
				descList.add("No Description");
			}
			
			String desc = "";
			for (int i = 0; i < descList.size() - 1; i++) {
				desc += descList.get(i) + "\n";
			}
			desc += descList.get(descList.size() - 1);
			opts.add(new ChatMenuOption(
					new PlainMessage(t),
					new ChangeSpellHolderAction(this, holder, t),
					new FancyMessage("").then(desc)));
		}
		

		MultioptionChatMenu menu = new MultioptionChatMenu(new PlainMessage("Assign one of the following:"), opts);
		
		menu.show(getPlayer().getPlayer());
				
	}
	
	public void showImbuementAlterMenu(ItemStack holder) {
		if (!getPlayer().isOnline()) {
			return;
		}
		
		InventoryMenu menu = new ActiveInventoryMenu(
				this, new ContributionInventory(getPlayer().getPlayer(), 
						QuestManagerPlugin.questManagerPlugin.getImbuementHandler().getImbuementSlots(this),
						null, "Imbuement Table"),
				new CreateImbuementAction(this, holder));
		QuestManagerPlugin.questManagerPlugin.getInventoryGuiHandler().showMenu(getPlayer().getPlayer(), menu);
	}
	
	/**
	 * Finalizes a calculated imbuement creation. If imbuement is null, considered a failure and does not
	 * replace currently held imbuement
	 * @param holder
	 * @param imbuement
	 */
	public void performImbuement(ItemStack holder, ImbuementSet imbuement) {
		ImbuementSkill skill = QuestManagerPlugin.questManagerPlugin.getImbuementHandler().getImbuementSkill();
		
		if (skill != null) {
			skill.perform(this, imbuement == null);
		}
		this.storedImbuements.put(Short.toUnsignedInt(holder.getDurability()), imbuement);
	}
	
	public OfflinePlayer getPlayer() {
		return Bukkit.getOfflinePlayer(playerID);
	}
	
	public Quest getFocusQuest() {
		if (focusQuest == null) {
			return null;
		}
		
		for (Quest q : currentQuests) {
			if (q.getName().equals(focusQuest)) {
				return q;
			}
		}
		
		return null;
	}
	
	public List<String> getPlayerNotes() {
		return this.journalNotes;
	}
	
	public void setFocusQuest(String questName) {
		for (Quest q : currentQuests) {
			if (q.getName().equals(questName)) {
				focusQuest = questName;
				break;
			}
		}
		QuestJournal.updateQuestJournal(this, false);
		if (getPlayer().isOnline()) {
			getPlayer().getPlayer().sendMessage("Your now focusing on the quest " + ChatColor.DARK_PURPLE + questName);
		}
		
		updateCompass(true);
	}
	
	/**
	 * Helper method to select the next compass target from the current focus quest's goal
	 */
	private void getNextTarget() {
		Quest quest = this.getFocusQuest();
		
		if (quest == null) {
			this.compassTarget = null;
			return;
		}
		
		Goal goal = quest.getCurrentGoal();
		if (goal.getRequirements().isEmpty()) {
			this.compassTarget = null;
			return;
		}
		
		for (Requirement req : goal.getRequirements()) {
			if (req instanceof CompassTrackable && !req.isCompleted()) {
				compassTarget = (CompassTrackable) req;
				return;
			}
		}
		
		//got all the way through. Are all requirements complete? Then either the quest is done or there are
		//reqs left we can't track, so we point to null.
		
		//if (goal.isComplete()) {
			//HOPE that the quest is actually complete.
		this.compassTarget = null;
		return;
		//}
	}
	
	/**
	 * Checks whether this player has enough of the provided item.<br />
	 * This method checks the name of the item when calculating how much they have
	 * @param searchItem
	 * @return
	 */
	public boolean hasItem(ItemStack searchItem) {
		if (!getPlayer().isOnline()) {
			return false;
		}
		
		Inventory inv = getPlayer().getPlayer().getInventory();
		int count = 0;
		String itemName = null;
		
		if (searchItem.hasItemMeta() && searchItem.getItemMeta().hasDisplayName()) {
			itemName = searchItem.getItemMeta().getDisplayName();
		}
		
		for (ItemStack item : inv.all(searchItem.getType()).values()) {
			if ((itemName == null && (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName())) || 
					(item.hasItemMeta() && item.getItemMeta().getDisplayName() != null 
					  && item.getItemMeta().getDisplayName().equals(itemName))) {
				count += item.getAmount();
			}
		}
		
		if (count >= searchItem.getAmount()) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Removes the passed item from the player's inventory.<br />
	 * This method also uses item lore to make sure the correct items are removed
	 * @param inv
	 * @param item
	 */
	public void removeItem(ItemStack searchItem) {
		
		if (!getPlayer().isOnline()) {
			return;
		}
		
		Inventory inv = getPlayer().getPlayer().getInventory();
		//gotta go through and find ones that match the name
		int left = searchItem.getAmount();
		String itemName = null;
		ItemStack item;
		
		if (searchItem.hasItemMeta() && searchItem.getItemMeta().hasDisplayName()) {
			itemName = searchItem.getItemMeta().getDisplayName();
		}
		
		for (int i = 0; i <= 35; i++) {
			item = inv.getItem(i);
			if (item != null && item.getType() == searchItem.getType())
			if (  (itemName == null && (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()))
				|| (item.hasItemMeta() && item.getItemMeta().getDisplayName() != null && item.getItemMeta().getDisplayName().equals(itemName))	
					) {
				//deduct from this item stack as much as we can, up to 'left'
				//but if there's more than 'left' left, just remove it
				int amt = item.getAmount();
				if (amt <= left) {
					//gonna remove entire stack
					item.setType(Material.AIR);
					item.setAmount(0);
					item.setItemMeta(null);
				} else {
					item.setAmount(amt - left);
				}
				
				inv.setItem(i, item);
				left-=amt;
				
				if (left <= 0) {
					break;
				}
			}
		}
	}

	@Override
	public Entity getEntity() {
		OfflinePlayer p = getPlayer();
		if (p.isOnline()) {
			return (Player) p;
		}

		return null;
	}

	@Override
	public double getMP() {
		return mp;
	}
	
	public double getMaxMp() {
		return maxMp;
	}

	@Override
	public void addMP(double amount) {
		mp = Math.max(Math.min(maxMp, mp + amount), 0);
		
		if (getPlayer().isOnline()) {
			Player p = (Player) getPlayer();
			p.setExp( Math.min(.99f, (float) (mp / maxMp)));
			//p.setExp(mp / maxMp);
		}
	}
	
	/**
	 * Regens the player's MP by the amount specified.<br />
	 * If the amount is negative, the player regens a fraction of their max mp (-amount%)
	 * If the player is at or past full mana, this function will return out immediately
	 * @param amt
	 */
	public void regenMP(double amt) {
		if (amt == 0) {
			return;
		}
		
		if (mp >= maxMp) {
			return;
		}
		
		MagicRegenEvent e = new MagicRegenEvent(this, amt);
		
		Bukkit.getPluginManager().callEvent(e);
		
		if (e.isCancelled()) {
			return;
		}
		
		amt = e.getFinalAmount();
		
		if (amt < 0) {
			//it's a rate
			addMP((this.maxMp * -amt) / 100);
		} else {
			addMP(amt);
		}
	}
	
	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent e) {
		
		if (QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getMagicEnabled()
		 && QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getMagicRegenKill() != 0)
		if (e.getEntity().getKiller() != null && 
				e.getEntity().getKiller().equals(getPlayer())) {
			//we killed it; regen mana
			double amt = QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getMagicRegenKill();
			regenMP(amt);
			return;
		}
	}
	
	@EventHandler
	public void onFoodEat(PlayerItemConsumeEvent e) {
		if (e.getPlayer().getUniqueId().equals(getPlayer().getUniqueId())) {
			//do mana regen, if it counts as food
			if (QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getMagicEnabled()
					 && QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getMagicRegenFood() != 0)
			switch (e.getItem().getType()) {
			case RAW_BEEF:
			case COOKED_BEEF:
			case RAW_CHICKEN:
			case COOKED_CHICKEN:
			case APPLE:
			case CARROT_ITEM:
			case BAKED_POTATO:
			case POTATO_ITEM:
			case BREAD:
			case COOKED_FISH:
			case COOKED_MUTTON:
			case COOKED_RABBIT:
			case COOKIE:
			case GRILLED_PORK:
			case MELON:
			case MUSHROOM_SOUP:
			case MUTTON:
			case PORK:
			case PUMPKIN_PIE:
			case RABBIT:
			case RABBIT_STEW:
			case RAW_FISH:	
				double amt = QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getMagicRegenFood();
				regenMP(amt);	
				break;
			default:
				break;
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (e.getPlayer().getUniqueId().equals(this.playerID)) {
			refreshPlayer();
		}
	}
	
	public void refreshPlayer() {
		if (getPlayer().isOnline()) {
			Player p = getPlayer().getPlayer();
			if (QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getWorlds()
					.contains(p.getWorld().getName())) {
				
				//go through with the update
				p.setMaxHealth(maxHp);
				addMP(0);
				p.setLevel(this.money);
				this.updateQuestLog(true);
				this.updateQuestLog(true);
				this.updateCompass(true);
				
				if (this.party != null) {
					this.party.updateScoreboard(this, (int) p.getHealth());
				}
			}
		}
	}
	
	/**
	 * Returns a map of potential spells stored against their materials
	 * @see {@link com.SkyIsland.QuestManager.Player.Utils.SpellHolder SpellHolder}
	 * @return
	 */
	public Map<Material, String> getStoredSpells() {
		return storedSpells;
	}
	
	/**
	 * Returns all spells unlocked by the player
	 * @return
	 */
	public List<String> getSpells() {
		return spells;
	}
	
	private void castSpell(Spell spell) {
		if (!getPlayer().isOnline()) {
			return;
		}
		
		if (spell == null) {
			return;
		}
		
		if (!getPlayer().getPlayer().getGameMode().equals(GameMode.CREATIVE) && 
				mp < spell.getCost()) {
			getPlayer().getPlayer().playSound(getPlayer().getPlayer().getLocation(), Sound.BLOCK_WATERLILY_PLACE, 1.0f, 0.5f);
			return;
		}
		
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getMagicEnabled()) {
			return;
		}
		
		new ChargeEffect(Effect.WITCH_MAGIC)
			.play(getPlayer().getPlayer(), getPlayer().getPlayer().getLocation());
		
		addMP(-spell.getCost());
		spell.cast(this);
	}
	
	public void setSkillLevel(Skill skill, int level) {
		this.skillLevels.put(skill, level);
	}
	
	public void setSkillExperience(Skill skill, float progress) {
		if (progress >= 1.0f) {
			setSkillLevel(skill, getSkillLevel(skill) + 1);
			skillLevelup(skill);
			progress = 0f;
		}
		
		this.skillXP.put(skill, progress);
		QuestLog.updateQuestlog(this, true);
	}
	
	public int getSkillLevel(Skill skill) {
		if (!skillLevels.containsKey(skill)) {
			skillLevels.put(skill, skill.getStartingLevel());
		}
		
		return skillLevels.get(skill);
	}
	
	public float getSkillExperience(Skill skill) {
		if (!skillXP.containsKey(skill)) {
			skillXP.put(skill, 0.0f);
		}
		
		return skillXP.get(skill);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDamage(EntityDamageByEntityEvent e) {
		if (e.isCancelled()) {
			return;
		}
		
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getWorlds()
				.contains(e.getEntity().getWorld().getName())) {
			return;
		}
		
		if (e.getEntity() instanceof Player) {
			if (((Player) e.getEntity()).getUniqueId().equals(this.playerID)) {
				//we just got hurt
				onPlayerHurt(e);
				return;
			}
		}
		
		Player player = null;
		ItemStack weapon = null, other = null;;
		if (!(e.getDamager() instanceof Player)) {
			if (e.getDamager() instanceof Projectile) {
				ProjectileSource source = ((Projectile) e.getDamager()).getShooter();
				if (source instanceof Player) {
					player = (Player) source;
					if (player.getInventory().getItemInMainHand().getType() == Material.BOW) {
						weapon = player.getInventory().getItemInMainHand(); 
						other = player.getInventory().getItemInOffHand();
					} else {
						weapon = player.getInventory().getItemInOffHand();
						other = player.getInventory().getItemInMainHand();
					}
				}
			} else
				return;
		}
		
		
		
		if (player == null) {
			player = (Player) e.getDamager();
			weapon = player.getInventory().getItemInMainHand();
			other = player.getInventory().getItemInOffHand();
		}
		
		if (!this.playerID.equals(player.getUniqueId())) {
			return;
		}
		
		if (!(e.getEntity() instanceof LivingEntity)) {
			return;
		}
		
		if (!e.getCause().equals(DamageCause.ENTITY_ATTACK) && !e.getCause().equals(DamageCause.PROJECTILE)) {
			return;
		}
		
		List<MetadataValue> meta = e.getEntity().getMetadata(DamageEffect.damageMetaKey);
		
		if (meta != null && !meta.isEmpty() && meta.get(0).asBoolean()) {
			return;
		}
		
		LivingEntity target = (LivingEntity) e.getEntity();
		
		//our player just damaged something. who knows what. Don't matter
		CombatEvent event = new CombatEvent(this, target, weapon, other, e.getFinalDamage());
		Bukkit.getPluginManager().callEvent(event);
		
		if (event.isMiss()) {
			e.setCancelled(true);
			CombatEvent.doMiss(this, target.getEyeLocation());
			return;
		}
		
		if (event.getFinalDamage() <= 0.0) {
			e.setCancelled(true);
			CombatEvent.doNoDamage(this, target.getEyeLocation());
			return;
		}
		
		e.setDamage(event.getFinalDamage());
		String name;
		if (target.getCustomName() != null) {
			name = target.getCustomName();
		} else {
			name = target.getType().toString();
		}
		CombatEvent.doHit(this, target.getEyeLocation(), event.getFinalDamage(), name);
		
	}
	
	private void onPlayerHurt(EntityDamageByEntityEvent e) {
		if (getOptions().getOption(PlayerOptions.Key.CHAT_COMBAT_DAMAGE)) {
			String name;
			Entity damager = e.getDamager();
			
			if (damager instanceof Projectile) {
				Projectile p = (Projectile) damager;
				if (p.getShooter() instanceof Entity) {
					damager = (Entity) p.getShooter();
				} else {
					//custom message for arrows and crap from non-entities
					getPlayer().getPlayer().sendMessage(String.format(damageBlockMessage, e.getFinalDamage()));
					return;
				}
			}
			
			if (damager.getCustomName() != null) {
				name = damager.getCustomName();
			} else {
				name = damager.getType().toString();
			}
			getPlayer().getPlayer().sendMessage(String.format(damageMessage, name, e.getFinalDamage()));
		}
	}
	
	private void skillLevelup(Skill skill) {
		//just leveled up a skill
		if (!getPlayer().isOnline()) {
			return;
		}
		
		int skillLevel = getSkillLevel(skill);
		Player p = getPlayer().getPlayer();
		p.sendMessage(ChatColor.GREEN + "You've obtained level " + ChatColor.GOLD 
				+ skillLevel + ChatColor.GREEN + " in " + skill.getName() + ChatColor.RESET);
		
		if (this.party != null) {
			for (QuestPlayer qp : party.getParticipants()) {
				if (qp.playerID.equals(playerID)) {
					continue;
				}
				
				if (!qp.getPlayer().isOnline()) {
					continue;
				}
				
				qp.getPlayer().getPlayer().sendMessage(ChatColor.GOLD + this.getPlayer().getName()
						+ " just attained level " + skillLevel + " in " + skill.getName() + "!" + ChatColor.RESET);
			}
		}
		
		(new ChargeEffect(Effect.HAPPY_VILLAGER)).play(p, p.getEyeLocation());
		p.getWorld().playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 0.75f);
		p.getWorld().playSound(p.getLocation(), Sound.BLOCK_NOTE_HARP, 1, 0.5f);
	}
	
	public PlayerOptions getOptions() {
		if (this.options == null) {
			this.options = new PlayerOptions();
		}
		
		return this.options;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof QuestPlayer)) {
			return false;
		}
		
		return ((QuestPlayer) o).getIDString().equals(this.getIDString());
	}

	@Override
	public int compareTo(QuestPlayer o) {
		return this.getIDString().compareTo(o.getIDString());
	}
	
	@Override
	public List<SpellPylon> getSpellPylons() {
		return this.pylons;
	}
	
	@Override
	public void addSpellPylon(SpellPylon pylon) {
		this.pylons.add(pylon);
	}
	
	@Override
	public void clearSpellPylons() {
		this.pylons.clear();
	}

	@Override
	public void castSpellWeavingSpell() {
		
		SpellWeavingSpell spell;
		List<String> typeList = new ArrayList<String>(pylons.size());
		List<Location> points = new ArrayList<Location>(pylons.size());
		for (SpellPylon pylon : pylons) {
			typeList.add(pylon.getType());
			points.add(pylon.getLocation());
			pylon.remove();
		}
		
		pylons.clear();
		
		if (!getPlayer().isOnline()) {
			return;
		}
		
		spell = QuestManagerPlugin.questManagerPlugin.getSpellWeavingManager()
				.getSpell(typeList);
				
		if (spell == null) {
			getPlayer().getPlayer().sendMessage(SpellWeavingManager.badRecipeMessage);
			return;
		}
		
		if (!getPlayer().getPlayer().getGameMode().equals(GameMode.CREATIVE) && 
				mp < spell.getCost()) {
			getPlayer().getPlayer().sendMessage(spellWeavingManaMesage);
			getPlayer().getPlayer().playSound(getPlayer().getPlayer().getLocation(), Sound.BLOCK_WATERLILY_PLACE, 1.0f, 0.5f);
			return;
		}
		
		if (!QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getMagicEnabled()) {
			return;
		}
		
		addMP(-spell.getCost());
		
		switch (spell.getTargetType()) {
		case ENTITY:
			spell.castOnEntities(this, getEntitiesInBound(points));
			break;
		case BLOCK:
			spell.castOnLocations(this, getBlocksInBound(points));
			break;
		case BOTH:
			spell.castOnAll(this, getEntitiesInBound(points), getBlocksInBound(points));
			break;
		}
		
	}
	
	/**
	 * Returns a list of all entities in the custom multi-point bounding poly provided.<br />
	 * Behavior is undefined when all listed points are not in the same world (as we use
	 * one of the points' world to get a list of entities to check!)
	 * @param points
	 * @return
	 */
	private List<Entity> getEntitiesInBound(List<Location> points) {
		List<Entity> list = new LinkedList<>();
		
		if (points == null || points.isEmpty()) {
			return list;
		}
		
		//optimizations: get absolute min, max for initial discard of entities
		//               get center
		double minx = 0, minz = 0, maxx = 0, maxz = 0;
		double centerx = 0, centerz = 0;
		boolean flip = false;
		for (Location loc : points) {
			centerx += loc.getX();
			centerz += loc.getY();
			
			if (!flip) {
				flip = true;
				minx = maxx = loc.getX();
				minz = maxz = loc.getZ();
				continue;
			}
			
			
			if (loc.getX() < minx)
				minx = loc.getX();
			if (loc.getX() > maxx)
				maxx = loc.getX();
			if (loc.getZ() < minz)
				minz = loc.getZ();
			if (loc.getZ() > maxz)
				maxz = loc.getZ();
		}
		
		//finish center calculation
		centerx = centerx / points.size();
		centerz = centerz / points.size();
		
		final double centx = centerx, centz = centerz;
		
		//sort points for convext-ivity
		Collections.sort(points, new Comparator<Location>(){

			@Override
			public int compare(Location o1, Location o2) {
				//Code taken from
				//http://stackoverflow.com/questions/6989100/sort-points-in-clockwise-order
				if (o1.getX() - centx >= 0 && o2.getX() - centx < 0)
			        return -1;
			    if (o1.getX() - centx < 0 && o2.getX() - centx >= 0)
			        return 1;
			    if (o1.getX() - centx == 0 && o2.getX() - centx == 0) {
			        if (o1.getY() - centz >= 0 || o2.getY() - centz >= 0)
			            return Double.compare(o2.getY(), o1.getY());
			        return Double.compare(o1.getY(), o2.getY());
			    }

			    // compute the cross product of vectors (center -> a) x (center -> b)
			    double det = (o1.getX() - centx) * (o2.getY() - centz) - (o2.getX() - centx) * (o1.getY() - centz);
			    if (det < 0)
			        return -1;
			    if (det > 0)
			        return 1;

			    // points a and b are on the same line from the center
			    // check which point is closer to the center
			    double d1 = (o1.getX() - centx) * (o1.getX() - centx) + (o1.getY() - centz) * (o1.getY() - centz);
			    double d2 = (o2.getX() - centx) * (o2.getX() - centx) + (o2.getY() - centz) * (o2.getY() - centz);
			    return Double.compare(d2,d1);
			}
			
		});
		
		for (Entity entity : points.get(0).getWorld().getEntities()) {
			if (   entity.getLocation().getX() < minx
				|| entity.getLocation().getZ() < minz
				|| entity.getLocation().getX() > maxx
				|| entity.getLocation().getZ() > maxz) {
				continue; //early elimination based on abs max, min
			}
			
			
			//get line closest
			double minDist = 0, dist;
			double x0, x1, x2, z0, z1, z2;
			x0 = entity.getLocation().getX();
			z0 = entity.getLocation().getZ();
			flip = true;
			Location l1, l2, minl1 = null, minl2 = null;
			l1 = null;
			Iterator<Location> it = points.iterator();
			l2 = points.get(points.size() - 1);
			while (it.hasNext()) {
				l1 = l2;
				l2 = it.next();
				
				x1 = l1.getX(); z1 = l1.getZ();
				x2 = l2.getX(); z2 = l2.getZ();
				
				dist = (
						Math.abs(
								  ((x1 - x0) * (z2 - z1)) - ((x1 - x2) * (z0 - z1))
								)
						/
						Math.sqrt(
						  Math.pow(z2 - z1, 2)
						  +
						  Math.pow(x2 - x1, 2)
						)
						);
				
				if (flip || dist < minDist) {
					flip = false;
					minDist = dist;
					minl1 = l1; minl2 = l2;
				}
			}
			
			//minl1, minl2 are points with minimum distance. 
			//all we need to do now is return if we're on the same side as the center (the inside)
			if(((z0 - centerz)*(minl1.getX() - x0) + (centerx - x0)*(minl1.getZ() - z0))
					* ((z0 - centerz)*(minl2.getX() - x0) + (centerx - x0)*(minl2.getZ() - z0)) < 0) {
				list.add(entity);
			}
		}
				
		return list;
	}
	
	private List<Location> getBlocksInBound(List<Location> points) {
		//TODO
		return null;
	}

	public Imbuement getCurrentImbuement() {
		return currentImbuement;
	}

	public void setCurrentImbuement(Imbuement currentImbuement) {
		ImbuementSkill skill = QuestManagerPlugin.questManagerPlugin.getImbuementHandler().getImbuementSkill();
		
		if (skill != null) {
			skill.perform(this, true);
		}
		
		if (this.currentImbuement != null) {
			currentImbuement.cancel();
		}
		this.currentImbuement = currentImbuement;
		currentImbuement.start();
	}
	
	public ImbuementSet getStoredImbuement(short data) {
		return storedImbuements.get(Short.toUnsignedInt(data));
	}
	
}
