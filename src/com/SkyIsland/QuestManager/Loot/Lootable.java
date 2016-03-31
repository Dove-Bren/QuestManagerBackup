package com.SkyIsland.QuestManager.Loot;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Specifies a given object keeps a list of loot you can get from it.<br />
 * Example application are enemies (which have loot on drop) or random chests.
 * @author Skyler
 *
 */
public interface Lootable {
	
	public List<Loot> getLoot();
	
	/**
	 * Standard method of selecting a piece of loot from a list of loot items.<br />
	 * This method will return null if the argument list is either null or empty.
	 * <p>
	 * The algorithm this method follows the definition of {@link Loot}'s weight member; the chance a piece
	 * of loot is selected is equal to<br />
	 * &nbsp;&nbsp;&nbsp;&nbsp;(<i>weight</i>) / (<i>Pool weight total</i>)
	 * </p>
	 * @param loot
	 * @return
	 */
	public static Loot pickLoot(List<Loot> loot) {
		if (loot == null || loot.isEmpty()) {
			return null;
		}
		
		Random rand = new Random();
		Collections.shuffle(loot, rand);
		
		double max = 0;
		for (Loot l : loot) {
			max += l.getWeight();
		}
		
		double index = rand.nextDouble() * max;
		for (Loot l : loot) {
			index -= l.getWeight();
			if (index <= 0) {
				//reached piece of loot to select
				return l;
			}
		}
		
		//we reached end with a positive index. Something went wrong (probably double precious error)
		//return end of list.
		return loot.get(loot.size() - 1);
		
	}
	
}
