package nmt.minecraft.QuestManager.Quest;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import nmt.minecraft.QuestManager.QuestManagerPlugin;
import nmt.minecraft.QuestManager.Configuration.GoalState;
import nmt.minecraft.QuestManager.Configuration.RequirementState;
import nmt.minecraft.QuestManager.Configuration.StatekeepingRequirement;
import nmt.minecraft.QuestManager.Quest.Requirements.Requirement;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Tracks objectives in a quest.<br />
 * Goals have specific requirements that must be met before they are considered clear.
 * @author Skyler
 *
 */
public class Goal {
	
	private List<Requirement> requirements;
	
	private String name;
	
	private String description;
	
	private Quest quest;
	

//	public static Goal fromConfig(Quest quest, YamlConfiguration config) throws InvalidConfigurationException {
//		return fromMap(quest, config.)
//	}
	
	/**
	 * Creates a goal from the provided goal configuration
	 * @param config
	 * @return
	 * @throws InvalidConfigurationException 
	 */
	public static Goal fromMap(Quest quest, Map<String, Object> map) throws InvalidConfigurationException {
		/* goal construction configuration involves:
		 * Goal name, description
		 * The requirements that are in it
		 * 
		 * The req's are in a list, with each element being a con section with the
		 * key being the type of req and the value being the config section for setting
		 * up the req
		 */
		
		if (!map.containsKey("type") || !map.get("type").equals("goalcnf")) {
			throw new InvalidConfigurationException();
		}
		
		String name, description;
		
		name = (String) map.get("name");
		description = (String) map.get("description");
		

		Goal goal = new Goal(quest, name, description);
		
//		@SuppressWarnings("unchecked")
//		List<YamlConfiguration> reqs = (List<YamlConfiguration>) map.get("requirements");
//		if (reqs == null || reqs.isEmpty()) {
//			return goal;
//		}
//		
//		for (YamlConfiguration req : reqs) {
//			String type = req.getKeys(false).iterator().next();
//			YamlConfiguration conf = (YamlConfiguration) req.getConfigurationSection(type);
//			
//			Requirement r = RequirementType.valueOf(type).instance();
//			
//			r.fromConfig(conf);
//			
//			goal.addRequirement(r);
//		}
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> reqs = (List<Map<String, Object>>) map.get("requirements");
		
		for (Map<String, Object> req : reqs) {
			String type = req.keySet().iterator().next();
			@SuppressWarnings("unchecked")
			Map<String, Object> cmap = (Map<String, Object>) req.get(type);
			YamlConfiguration conf = new YamlConfiguration();
			conf.createSection(type, cmap);
			
			Requirement r = QuestManagerPlugin.questManagerPlugin.getRequirementManager()
					.instanceRequirement(type, goal, conf);
			
			if (r == null) {
				QuestManagerPlugin.questManagerPlugin.getLogger()
					.warning("    Invalid requirement type for goal: " + goal.name);
			}
			
			goal.addRequirement(r);
		}
		
		return goal;
		
	}
	
	public Goal(Quest quest, String name, String description) {
		this.quest = quest;
		this.name = name;
		this.description = description;
		
		this.requirements = new LinkedList<Requirement>();
	}
	
	public Goal(Quest quest, String name) {
		this(quest, name, "");
	}
	
	public void loadState(GoalState state) throws InvalidConfigurationException {
		
		if (!state.getName().equals(name)) {
			QuestManagerPlugin.questManagerPlugin.getLogger().warning("Loading state information"
					+ "from a file that has a mismatched goal name!");
		}
		
		//WARNING:
		//this is assuming that the lists are maintianed in the right order.
		//it should work this way, but this is a point of error!
		ListIterator<RequirementState> states = state.getRequirementStates().listIterator();
		for (Requirement req : requirements) {
			if (req instanceof StatekeepingRequirement) {
				((StatekeepingRequirement) req).loadState(states.next());
			}
		}
	}
	
	public GoalState getState() {
		
		GoalState state = new GoalState();
		state.setName(name);
		
		for (Requirement req : requirements) {
			if (req instanceof StatekeepingRequirement) {
				state.addRequirementState(((StatekeepingRequirement) req).getState());
			}
		}
		
		return state;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the quest
	 */
	public Quest getQuest() {
		return quest;
	}
	
	/**
	 * Adds a new requirement to this goal
	 * @param requirement
	 */
	public void addRequirement(Requirement requirement) {
		requirements.add(requirement);
	}
	
	
	/**
	 * Assesses and reports whether the goal has been completed.<br />
	 * Please note that goals that have no requirements defaultly return true.
	 * @return
	 */
	public boolean isComplete() {
		if (requirements.isEmpty()) {
			return true;
		}
		
		for (Requirement req : requirements) {
			if (req.isCompleted() == false) {
				return false;
			}
		}
		
		return true;
	}
		
	
}
