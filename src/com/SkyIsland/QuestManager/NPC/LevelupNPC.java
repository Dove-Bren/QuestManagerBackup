package com.SkyIsland.QuestManager.NPC;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.EquipmentConfiguration;
import com.SkyIsland.QuestManager.Configuration.Utils.LocationState;
import com.SkyIsland.QuestManager.Fanciful.FancyMessage;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.UI.ChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.BioptionChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.ChatMenuOption;
import com.SkyIsland.QuestManager.UI.Menu.MultioptionChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.SimpleChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.Action.LevelupHealthAction;
import com.SkyIsland.QuestManager.UI.Menu.Action.LevelupManaAction;
import com.SkyIsland.QuestManager.UI.Menu.Action.ShowChatMenuAction;
import com.SkyIsland.QuestManager.UI.Menu.Message.BioptionMessage;
import com.SkyIsland.QuestManager.UI.Menu.Message.Message;
import com.SkyIsland.QuestManager.UI.Menu.Message.PlainMessage;

/**
 * Prompts the player with some amount of text and gives them the option to level up.<br />
 * The amount given in the level up's is also determined in the configuration, including options
 * for rate and base amounts.
 * If both the base and rate amounts for any attribute, the option to increase it will not be included.
 * @author Skyler
 *
 */
public class LevelupNPC extends SimpleNPC {

	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(LevelupNPC.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(LevelupNPC.class);
	}
	

	private enum aliases {
		FULL("com.SkyIsland.QuestManager.NPC.SimpleBioptionNPCC"),
		DEFAULT(LevelupNPC.class.getName()),
		SHORT("LevelupNPC"),
		INFORMAL("LUNPC");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}
	
	protected BioptionMessage chat;
	
	protected double hpRate;
	
	protected int hpBase;
	
	protected double mpRate;
	
	protected int mpBase;
	
	protected double fameRate;
	
	protected int fameBase;
	
	protected LevelupNPC(Location startingLoc) {
		super(startingLoc);
	}
		
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>(4);
		
		map.put("name", name);
		map.put("type", getEntity().getType());
		map.put("location", new LocationState(getEntity().getLocation()));
		
		EquipmentConfiguration econ;
		
		if (getEntity() instanceof LivingEntity) {
			econ = new EquipmentConfiguration(
					((LivingEntity) getEntity()).getEquipment()
					);
		} else {
			econ = new EquipmentConfiguration();
		}
		
		map.put("equipment", econ);
		
		map.put("message", chat);
		
		map.put("hpbase", hpBase);
		map.put("hprate", hpRate);
		map.put("mpbase", mpBase);
		map.put("mprate", mpRate);	
		map.put("famebase", fameBase);
		map.put("famerate", fameRate);
		
		return map;
	}
	
	public static LevelupNPC valueOf(Map<String, Object> map) {
		if (map == null || !map.containsKey("name") || !map.containsKey("type") 
				 || !map.containsKey("location") || !map.containsKey("equipment")
				  || !map.containsKey("message") || !map.containsKey("famerate") 
				  || !map.containsKey("famebase")) {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Invalid NPC info! "
					+ (map.containsKey("name") ? ": " + map.get("name") : ""));
			return null;
		}
		
		
		EquipmentConfiguration econ = new EquipmentConfiguration();
		try {
			YamlConfiguration tmp = new YamlConfiguration();
			tmp.createSection("key",  (Map<?, ?>) map.get("equipment"));
			econ.load(tmp.getConfigurationSection("key"));
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LocationState ls = (LocationState) map.get("location");
		Location loc = ls.getLocation();
		

		LevelupNPC npc = new LevelupNPC(loc);
		EntityType type = EntityType.valueOf((String) map.get("type"));
		
		npc.name = (String) map.get("name");
		

		loc.getChunk();
		npc.setEntity(loc.getWorld().spawnEntity(loc, type));
		npc.getEntity().setCustomName((String) map.get("name"));

		if (npc.getEntity() instanceof LivingEntity) {
			EntityEquipment equipment = ((LivingEntity) npc.getEntity()).getEquipment();
			equipment.setHelmet(econ.getHead());
			equipment.setChestplate(econ.getChest());
			equipment.setLeggings(econ.getLegs());
			equipment.setBoots(econ.getBoots());
			equipment.setItemInMainHand(econ.getHeldMain());
			equipment.setItemInOffHand(econ.getHeldOff());
			
		}
		
		npc.chat = (BioptionMessage) map.get("message");
		
		//provide our npc's name, unless we don't have one!
		if (npc.name != null && !npc.name.equals("")) {
			npc.chat.setSourceLabel(
					new FancyMessage(npc.name));
			
		}
		
		npc.fameBase = (int) map.get("famebase");
		npc.fameRate = (double) map.get("famerate");
		
		npc.hpBase = npc.mpBase = 0;
		npc.hpRate = npc.mpRate = 0.0;
		
		if (map.containsKey("hpbase")) {
			npc.hpBase = (int) map.get("hpbase");
		}
		if (map.containsKey("hprate")) {
			npc.hpRate = (double) map.get("hprate");
		}
		if (map.containsKey("mpbase")) {
			npc.mpBase = (int) map.get("mpbase");
		}
		if (map.containsKey("mprate")) {
			npc.mpRate = (double) map.get("mprate");
		}
		
		return npc;
	}

	@Override
	protected void interact(Player player) {
		QuestPlayer p = QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer(player);
		int fame;
		
		fame = ((int) ((p.getLevel() - 1) * fameRate)) + fameBase;
		
		//amt = (player.stat * (Rate)) + Base;
		
		Collection<ChatMenuOption> opts = new LinkedList<>();
		if (hpRate != 0 || hpBase != 0) {
			int hp = ((int) (p.getMaxHp() * hpRate)) + hpBase;
			opts.add(new ChatMenuOption(new PlainMessage("Health (" + hp + ")"), 
					new LevelupHealthAction(p, fame, hp),
					new FancyMessage("").then("This will permanently increase your maximum health by " + hp)));
		}
		if (mpRate != 0 || mpBase != 0) { 
			int mp = ((int) (p.getMaxMp() * mpRate)) + mpBase;
			opts.add(new ChatMenuOption(new PlainMessage("Mana (" + mp + ")"), 
				new LevelupManaAction(p, fame, mp),
				new FancyMessage("").then("This will permanently increase your maximum mana by " + mp)));
		}
		
		ChatMenu cMenu = new SimpleChatMenu(new FancyMessage("Very well."));
		opts.add(
				new ChatMenuOption(new PlainMessage("Cancel"),
					new ShowChatMenuAction(cMenu, player))
				);
		FancyMessage fmsg = new FancyMessage("") .then("This will cost you")
				.color(ChatColor.RED)
			.then(" " + fame + " ")
				.color(ChatColor.GOLD)
			.then("fame!")
				.color(ChatColor.RED);
		Message msg = new PlainMessage(fmsg);

		
		ChatMenu levelChat = new MultioptionChatMenu(msg, opts);
		//ChatMenu levelChat = new SimpleChatMenu(new FancyMessage("inplace"));

		ChatMenu messageChat = new BioptionChatMenu(chat, new ShowChatMenuAction(levelChat, player), null);
		messageChat.show(player);
	}
	
	@Override
	/**
	 * Render this NPC imobile using slowness instead of teleporting them
	 */
	public void tick() {
		Entity e = getEntity();
		
		if (e == null) {
			return;
		}
		

		if (!e.getLocation().getChunk().isLoaded()) {
			return;
		}
		
		if (e instanceof LivingEntity) {
			((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 99999999, 10, false, false), true);
		}
	}
	
//	@Override
//	protected void interact(Player player) {
//		QuestPlayer p = QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer(player);
//		int hp, mp, fame;
//		
//		fame = ((int) ((p.getLevel() - 1) * fameRate)) + fameBase;
//		
//		//amt = (player.stat * (Rate)) + Base;
//		hp = ((int) (p.getMaxHp() * hpRate)) + hpBase;
//		mp = ((int) (p.getMaxMp() * mpRate)) + mpBase;
//		
//		FancyMessage fmsg = new FancyMessage("This will cost you")
//				.color(ChatColor.RED)
//			.then(" " + fame + " ")
//				.color(ChatColor.GOLD)
//			.then("fame!");
//		BioptionMessage msg = new BioptionMessage(fmsg, 
//				new FancyMessage("Health (2)").color(ChatColor.DARK_GREEN).tooltip("Increase your maximum health by 2"), 
//				new FancyMessage("Mana (5)").color(ChatColor.DARK_PURPLE).tooltip("Increase your maximum mana by 5"),
//				new FancyMessage("Very Well"), 
//				new FancyMessage("Very Well"));
//
//		
//		ChatMenu levelChat = new BioptionChatMenu(msg, new LevelupHealthAction(p, fame, hp), new LevelupManaAction(p, fame, mp));
//		//ChatMenu levelChat = new SimpleChatMenu(new FancyMessage("inplace"));
//
//		ChatMenu messageChat = new BioptionChatMenu(chat, new ShowChatMenuAction(levelChat, player), null);
//		messageChat.show(player);
//	}

}