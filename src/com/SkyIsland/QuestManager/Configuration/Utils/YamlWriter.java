package com.SkyIsland.QuestManager.Configuration.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Utility class used to make an actual good looking YAML file with comments and crap
 * @author Skyler
 *
 */
public class YamlWriter {
	
	public static final String commentPrefix = "#";
	
	public static final int prefixRepeat = 10;
	
	/**
	 * Takes a string and changes the first letter to be capital, and the rest lowercase
	 * @param string
	 * @return
	 */
	public static String toStandardCase(String string) {
		return (string.substring(0, 1).toUpperCase()) + (string.substring(1).toLowerCase());
	}
	
	/**
	 * Takes a string (word) and changes the first letter to capital. Then, for every
	 * Underscore ("_") in the word, it inserts a space and capitalizes the letter right after
	 * @param string
	 * @return
	 */
	public static String toStandardFormat(String string) {
		string = YamlWriter.toStandardCase(string);
		
		while (string.indexOf("_") != -1) {
			string = string.substring(0, string.indexOf("_")) + " "
				+ YamlWriter.toStandardCase(string.substring(string.indexOf("_") + 1));
		}
		
		return string;
	}
	
	private class Entry {
		private String key;
		
		private Object obj;
		
		private List<String> comments;
		
		public Entry(String key, Object obj, List<String> comments) {
			this.key = key;
			this.obj = obj;
			this.comments = comments;
		}

		public String getKey() {
			return key;
		}

		public Object getObj() {
			return obj;
		}

		public List<String> getComments() {
			return comments;
		}
	}
	
	private List<Entry> entries;
	
	public YamlWriter() {
		this.entries = new LinkedList<Entry>();
	}
	
	public YamlWriter addLine(String key, Object value) {
		return addLine(key, value, null);
	}
	
	public YamlWriter addLine(String key, Object value, List<String> comments) {
		entries.add(new Entry(key, value, comments));
		
		return this;
	}
	
	/**
	 * Constructs a YAML from the given entries, discarding comments.<br />
	 * This is nice for a working YAML from what was created, but not for saving manually.<br />
	 * If you wish to save the yaml with the defined comments, use the {@link #save(File)} method.
	 * @return
	 */
	public YamlConfiguration buildYaml() {
		YamlConfiguration config = new YamlConfiguration();
		
		for (Entry entry : entries) {
			config.set(entry.getKey(), entry.getObj());
		}
		
		return config;
	}
	
	/**
	 * Saves the contained configuration with comments out to the provide file.<br />
	 * <b>IMPORTANT:</b> this method only works with simple object outputs. It serialized with a simple
	 * <i>toString()</i> method call.
	 * @param outFile
	 * @throws FileNotFoundException
	 */
	public void save(File outFile) throws FileNotFoundException {
		PrintWriter writer = new PrintWriter(outFile);
		
		for (Entry entry : entries) {
			
			if (entry.getComments() != null && !entry.getComments().isEmpty()) {
				for (int i = 0; i < prefixRepeat; i++)
					writer.print(commentPrefix);
				
				writer.println();
				
				for (String comment : entry.getComments()) 
					writer.println(commentPrefix + comment); 
				
				for (int i = 0; i < prefixRepeat; i++)
					writer.print(commentPrefix);
				
				writer.println();
			}
			
			//write actual entry
			if (entry.getObj() instanceof Map<?,?>) {
				YamlConfiguration y = new YamlConfiguration();;
				y.createSection(entry.getKey(), (Map<?, ?>) entry.getObj());
				writer.println(y.saveToString());
			} else {
				writer.println(entry.getKey() + ": " + entry.getObj().toString());
			}
			writer.println();
			
		}
		
		writer.close();
	}
}
