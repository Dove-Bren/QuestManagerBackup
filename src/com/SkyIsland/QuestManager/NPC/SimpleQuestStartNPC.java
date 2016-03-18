package com.SkyIsland.QuestManager.NPC;

import java.util.HashMap;
import java.util.List;
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

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.EquipmentConfiguration;
import com.SkyIsland.QuestManager.Configuration.QuestConfiguration;
import com.SkyIsland.QuestManager.Configuration.Utils.LocationState;
import com.SkyIsland.QuestManager.Fanciful.FancyMessage;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Utils.CompassTrackable;
import com.SkyIsland.QuestManager.Quest.Quest;
import com.SkyIsland.QuestManager.UI.ChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.BioptionChatMenu;
import com.SkyIsland.QuestManager.UI.Menu.Action.QuestStartAction;
import com.SkyIsland.QuestManager.UI.Menu.Message.BioptionMessage;
import com.SkyIsland.QuestManager.UI.Menu.Message.Message;

/**
 * NPC that starts a quest :D<br />
 * This simple starting version mounts atop a {@link SimpleBioptionNPC}, and has all the capability
 * and limits defined therein.
 * @author Skyler
 *
 */
public class SimpleQuestStartNPC extends SimpleStaticBioptionNPC implements CompassTrackable {
	
	/**
	 * Registers this class as configuration serializable with all defined 
	 * {@link aliases aliases}
	 */
	public static void registerWithAliases() {
		for (aliases alias : aliases.values()) {
			ConfigurationSerialization.registerClass(SimpleQuestStartNPC.class, alias.getAlias());
		}
	}
	
	/**
	 * Registers this class as configuration serializable with only the default alias
	 */
	public static void registerWithoutAliases() {
		ConfigurationSerialization.registerClass(SimpleQuestStartNPC.class);
	}
	

	private enum aliases {
		FULL("com.SkyIsland.QuestManager.NPC.SimpleQuestStartNPC"),
		DEFAULT(SimpleQuestStartNPC.class.getName()),
		SHORT("SimpleQuestStartNPC"),
		INFORMAL("QSNPC");
		
		private String alias;
		
		private aliases(String alias) {
			this.alias = alias;
		}
		
		public String getAlias() {
			return alias;
		}
	}

	
	private SimpleQuestStartNPC(Location startingLoc) {
		super(startingLoc);
	}
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<String, Object>(4);
		System.out.println("point 1");
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
		
		map.put("firstmessage", chat);
		map.put("duringmessage", duringMessage);
		map.put("postmessage", afterMessage);
		map.put("badrequirementmessage", altMessage);
				
		return map;
	}
	
	public static SimpleQuestStartNPC valueOf(Map<String, Object> map) {
		if (map == null || !map.containsKey("name") || !map.containsKey("type") 
				 || !map.containsKey("location") || !map.containsKey("equipment")
				  || !map.containsKey("firstmessage") || !map.containsKey("duringmessage")
				  || !map.containsKey("postmessage") || !map.containsKey("badrequirementmessage")) {
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
		
		EntityType type = EntityType.valueOf((String) map.get("type"));
		
		
		SimpleQuestStartNPC npc = new SimpleQuestStartNPC(loc);
		npc.isEnd = false;
		
		npc.name = (String) map.get("name");
		
		//load the chunk
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
		
		npc.chat = (BioptionMessage) map.get("firstmessage");
		npc.duringMessage = (Message) map.get("duringmessage");
		npc.afterMessage = (Message) map.get("postmessage");
		npc.altMessage = (Message) map.get("badrequirementmessage");
		
		
		//provide our npc's name, unless we don't have one!
		if (npc.name != null && !npc.name.equals("")) {
			FancyMessage label = new FancyMessage(npc.name);
			npc.chat.setSourceLabel(label);
			npc.duringMessage.setSourceLabel(label);
			npc.afterMessage.setSourceLabel(label);
			npc.altMessage.setSourceLabel(label);
		}
		
		return npc;
	}
	
	private QuestConfiguration quest;
	
	private boolean isEnd;
	
	private Message duringMessage;
	
	private Message afterMessage;
	
	private Message finishMessage;
	
	private Message altMessage;
	
	public void setQuestTemplate(QuestConfiguration questTemplate) {
		this.quest = questTemplate;
	}
	
	public void markAsEnd(Message finishMessage) {
		this.finishMessage = finishMessage;
		
		if (name != null && !name.equals("")) {
			this.finishMessage.setSourceLabel(
					new FancyMessage(name));
		}
		
		isEnd = true;
	}
	
	@Override
	protected void interact(Player player) {
		
		//do different things depending on if the player has or is doing the quest
		QuestPlayer qp = QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer(
				player.getUniqueId());
		
		ChatMenu messageChat = null;
		boolean meetreqs = true;
		
		
		List<String> reqs = quest.getRequiredQuests();
		
		if (reqs != null && !reqs.isEmpty()) {
			//go through reqs, see if the player has those quests completed
			for (String req : reqs) {
				if (!QuestPlayer.meetsRequirement(qp, req)) {
					meetreqs = false;
					break;
				}
			}
		}
		
		if (!meetreqs) {
			//doesn't have all the required quests done yet!
			messageChat = ChatMenu.getDefaultMenu(altMessage);
		} else if (!quest.isRepeatable() && qp.hasCompleted(quest.getName())) {
			//already completed it
			messageChat = ChatMenu.getDefaultMenu(afterMessage);
		} else if (qp.isInQuest(quest.getName())) {
			//is currently in it
			
			//Is this the possible end?
			
			if (isEnd) {
				//fetch instance of quest
				Quest qInst = null;
				for (Quest q : qp.getCurrentQuests()) {
					if (q.getName().equals(quest.getName())) {
						qInst = q;
						break;
					}
				}
				
				if (qInst == null) {
					//something went wrong!
					QuestManagerPlugin.questManagerPlugin.getLogger().warning(
							"Unable to find matching quest in SimpleQuestStartNPC!!!!!!!");
					return;
				}
				
				//perform check against completion!!!!
				if (qInst.isReady()) {	
					messageChat = ChatMenu.getDefaultMenu(finishMessage);
					qInst.completeQuest(false);
				} else {
					QuestManagerPlugin.questManagerPlugin.getPlayerManager().getPlayer(player).updateQuestBook(true);
				}
			}
			if (messageChat == null) {
				Quest qInst = null;
				for (Quest q : qp.getCurrentQuests()) {
					if (q.getName().equals(quest.getName())) {
						qInst = q;
						break;
					}
				}
				
				messageChat = ChatMenu.getDefaultMenu(duringMessage);
				messageChat.setQuestBacker(qInst);
			}
		} else {
			messageChat = new BioptionChatMenu(chat, 
					new QuestStartAction(quest, new FancyMessage(this.name).color(ChatColor.DARK_GRAY).style(ChatColor.BOLD)
							.then("\n").then(chat.getBody()), chat.getResponse1(), player),  null);			
		}

		messageChat.show(player);
		//this.updateQuestHistory(qp, messageChat.getMessage().toOldMessageFormat()
		//		.replaceAll(ChatColor.WHITE + "", ChatColor.BLACK + ""));
	}
	
	@Override
	public Location getLocation() {
		Entity e = getEntity();
		
		if (e != null) {
			return e.getLocation();
		} else {
			return null;
		}
	}
	
}
