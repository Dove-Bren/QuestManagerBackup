package com.SkyIsland.QuestManager.util;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;

/**
 * Stores a list of items associated with a weight. This list is designed to be used to pull out
 * random elements, and not for simple traversal.
 * @author Skyler
 *
 * @param <T>
 */
public class WeightedList<T>  {
	
	private static class Item<E> {
		private E o;
		
		private Double weight;
		
		public Item(E o, Double weight) {
			this.o = o;
			this.weight = weight;;
		}
		
		public E getObject() {
			return o;
		}
		
		public Double getWeight() {
			return weight;
		}
	}
	
	private List<Item<T>> list;
	
	private Random rand;
	
	public WeightedList() {
		this.list = new LinkedList<Item<T>>();
		rand = new Random();
	}
	
	/**
	 * Adds an element to the list.<br />
	 * <b>Note:</b> This method does not detect or avoid duplicates in any way.
	 * @param object
	 * @param weight
	 */
	public void add(T object, Double weight) {
		list.add(new Item<T>(object, weight));
	}
	
	/**
	 * Attempts to grab a random entry in the list (based on their weight) and reutrn it.
	 * @return <i>null</i> on error or if the list is empty, an object stored otherwise
	 */
	public T getRandom() {
		if (list.isEmpty()) {
			return null;
		}
		
		double max = 0;
		
		for (Item<T> i : list) {
			max += i.getWeight();
		}
		
		double index = rand.nextDouble() * max;
		max = 0;
		ListIterator<Item<T>> it = list.listIterator();
		
		while (it.hasNext()) {
			Item<T> i = it.next();
			max += i.getWeight();
			
			if (max >= index) {
				return i.getObject();
			}
		}
		
		//if we get here, something went wrong
		return null;
	}
	
	/**
	 * Returns all elements stored in this list, without their associated weights.
	 * @return
	 */
	public Set<T> getElements() {
		Set<T> set = new HashSet<T>();
		
		for (Item<T> i : list) {
			set.add(i.getObject());
		}
		
		return set;
	}
	
	public void clear() {
		if (list.isEmpty()) {
			return;
		}
		
		for (Item<T> item : list) {
			item.o = null;
		}
		
		list.clear();
	}
	
	public boolean isEmpty() {
		return list.isEmpty();
	}
}
