package com.SkyIsland.QuestManager.UI.ActionSequence;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.SkyIsland.QuestManager.QuestManagerPlugin;
import com.SkyIsland.QuestManager.Configuration.Utils.YamlWriter;
import com.SkyIsland.QuestManager.Effects.ChargeEffect;
import com.SkyIsland.QuestManager.Fanciful.FancyMessage;
import com.SkyIsland.QuestManager.Player.QuestPlayer;
import com.SkyIsland.QuestManager.Player.Skill.QualityItem;
import com.SkyIsland.QuestManager.Player.Skill.Default.LumberjackSkill;
import com.SkyIsland.QuestManager.Scheduling.Alarm;
import com.SkyIsland.QuestManager.Scheduling.Alarmable;

public class LumberjackSequence implements Listener, Alarmable<Integer> {
	
	private enum State {
		STOPPED,
		STARTING,
		RUNNING,
		FINISHED
	}

	private static final Random random = new Random();
	
	public static final String missMessage = ChatColor.RED + "You miss the swing, resulting in no good wood";
	
	public static final String earlyMessage = ChatColor.RED + "You swung too eagerly, ruining the wood";
	
	public static final String winMessage = ChatColor.GREEN + "You successfully chopped ";
	
	public static final String perfectMessage = ChatColor.GREEN + "You landed a perfect chop!";
	
	public static final Sound countdownSound = Sound.BLOCK_NOTE_PLING;
	
	public static final Sound startSound = Sound.BLOCK_GRAVEL_BREAK;
	
	public static final Sound loseSound = Sound.ENTITY_CAT_DEATH;
	
	public static final Sound winSound = Sound.ENTITY_PLAYER_LEVELUP;
	
	public static final Sound clickSound = Sound.BLOCK_WOOD_BREAK;
	
	public static final Sound perfectSound = Sound.BLOCK_NOTE_PLING;
	
	private static final ChargeEffect successEffect = new ChargeEffect(Effect.CRIT);
	
	private static final ChargeEffect failEffect = new ChargeEffect(Effect.SMALL_SMOKE);
	
	private static final double perfectHitThreshhold = 0.025;
	
	private static final double hitOffbyInterval = 0.1;
	
	private static final int countdownTimer = 3;
	
	private static final double timeStep = 0.05;
	
	private static final double perfectBonus = .50;
	
	private static LumberjackSkill skillLink;
	
	private State state;
	
	private QuestPlayer player;
	
	private QualityItem input;
	
	private Vector treeLocation;
	
	private double averageSwingTime;
	
	private double swingTimeDeviation;
	
	private double reactionTime;
	
	private int hits;
	
	private int offByIndex;
	
	private BossBar displayBar;
	
	private String displayName;
	
	private double secondsRemaining;
	
	private double swingTime;
	
	private int skillLevel;

	public LumberjackSequence(QuestPlayer player, Vector treeLocation, QualityItem input, double averageSwingTime,
			double swingTimeDeviation, double reactionTime, int hits, String displayName, int skillLevel) {
		this.player = player;
		this.input = input;
		this.averageSwingTime = averageSwingTime;
		this.swingTimeDeviation = swingTimeDeviation;
		this.reactionTime = reactionTime;
		this.hits = hits;
		this.treeLocation = treeLocation;
		this.displayName = displayName;
		this.skillLevel = skillLevel;
		
		this.state = State.STOPPED;
		this.offByIndex = 0;
	}
	
	public void start() {
		if (!player.getPlayer().isOnline())
			return;
		
		state = State.STARTING;
		Alarm.getScheduler().schedule(this, 0, 1);
		player.getPlayer().getPlayer().sendMessage(ChatColor.GREEN + "Get Ready...");
		
		this.displayBar = Bukkit.createBossBar(displayName, BarColor.BLUE, BarStyle.SEGMENTED_20, new BarFlag[0]);
		displayBar.setProgress(1f);
		displayBar.addPlayer(player.getPlayer().getPlayer());
		
		Bukkit.getPluginManager().registerEvents(this, QuestManagerPlugin.questManagerPlugin);
	}
	
	private void setTimer() {
		Alarm.getScheduler().unregister(this);
			
		this.secondsRemaining = averageSwingTime + (random.nextGaussian() * swingTimeDeviation);
		this.swingTime = secondsRemaining;
		
		displayBar.setProgress(1);
		displayBar.setColor(BarColor.YELLOW);
		
		Alarm.getScheduler().schedule(this, 0, timeStep);
		
	}
	
	@Override
	public void alarm(Integer key) {
		
		if (!player.getPlayer().isOnline()) {
			return;
		}

		Player p = player.getPlayer().getPlayer();
		if (state == State.STARTING) {
			//starting countdown
			if (key >= countdownTimer) {
				player.getPlayer().getPlayer().sendMessage(ChatColor.RED + "Go!");
				state = State.RUNNING;
				p.playSound(p.getLocation(), startSound, 1, 1);
				setTimer();
				return;
			}
			
			p.sendMessage(ChatColor.YELLOW + "" + (countdownTimer - key));
			p.playSound(p.getLocation(), countdownSound, 1, 1);
			Alarm.getScheduler().schedule(this, key + 1, 1);
			return;
		}
		
		if (state == State.RUNNING) {
			
			this.secondsRemaining -= timeStep;
			
			displayBar.setProgress(Math.max(0, secondsRemaining / swingTime));
			if (this.secondsRemaining < reactionTime)
				displayBar.setColor(BarColor.GREEN);
			
			if (this.secondsRemaining <= perfectHitThreshhold)
				displayBar.setColor(BarColor.BLUE);
			
			if (this.secondsRemaining < -reactionTime) {
				loseGame();
				return;
			}
			
			Alarm.getScheduler().schedule(this, 0, timeStep);
		}
	}
	
	private void loseGame() {
		this.state = State.FINISHED;
		
		Alarm.getScheduler().unregister(this);
		displayBar.removeAll();
		
		if (skillLink != null) {
			int range = QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillCutoff();
			skillLink.playerFinish(player);
			skillLink.perform(player, Math.max(player.getSkillLevel(skillLink) - range, Math.min(player.getSkillLevel(skillLink) + range, skillLevel)), true);
		}
		
		if (!player.getPlayer().isOnline()) {
			return;
		}
		Player p = player.getPlayer().getPlayer();
		
		p.sendMessage(missMessage);
		p.getWorld().playSound(p.getLocation(), loseSound, 1, 1);
		failEffect.play(p, p.getLocation());
		
	}
	
	private void winGame() {
		this.state = State.FINISHED;
		Alarm.getScheduler().unregister(this);
		
		displayBar.removeAll();
		
		if (skillLink != null) {
			int range = QuestManagerPlugin.questManagerPlugin.getPluginConfiguration().getSkillCutoff();
			skillLink.playerFinish(player);
			skillLink.performMajor(player, Math.max(player.getSkillLevel(skillLink) - range, Math.min(player.getSkillLevel(skillLink) + range, skillLevel)), false);
		}
		
		if (!player.getPlayer().isOnline()) {
			return;
		}
		Player p = player.getPlayer().getPlayer();
		
		p.sendMessage(winMessage);
		p.getWorld().playSound(p.getLocation(), winSound, 1, 1);
		successEffect.play(p, p.getLocation());
		
		double qratio;
		if (offByIndex == 0)
			qratio = 1 + perfectBonus;
		else {
			qratio = 1.0 / (1.0 + (offByIndex / 5.0));
		}
		input.setQuality(input.getQuality() * qratio);
		
		String name;
		if (input.getItem().getItemMeta() == null || input.getItem().getItemMeta().getDisplayName() == null) {
			name = YamlWriter.toStandardFormat(input.getItem().getType().toString());
		} else {
			name = input.getItem().getItemMeta().getDisplayName();
		}
		
		FancyMessage msg = new FancyMessage(winMessage)
				.color(ChatColor.GREEN)
			.then(input.getItem().getAmount() > 1 ? input.getItem().getAmount() + "x " : "a ")
			.then("[" + name + "]")
				.color(ChatColor.DARK_PURPLE)
				.itemTooltip(input.getItem());
		
		
		msg.send(p);
		if (!(p.getInventory().addItem(input.getItem())).isEmpty()) {
			p.sendMessage(ChatColor.RED + "There is no space left in your inventory");
			p.getWorld().dropItem(p.getEyeLocation(), input.getItem());
		}
	}
	
	private static int getOffbyIndex(double secondOffset) {
		secondOffset = Math.abs(secondOffset);
		
		if (secondOffset <= perfectHitThreshhold)
			return 0;
		
		return (int) Math.round(Math.max(1, secondOffset / hitOffbyInterval));
	}
	
	@EventHandler
	public void onTreeHit(PlayerInteractEvent e) {
		if (state != State.RUNNING)
			return;
		
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if (!e.getPlayer().getUniqueId().equals(player.getPlayer().getUniqueId()))
			return;
		
		if (e.getItem() == null || !e.getItem().getType().name().contains("AXE"))
			return;
		
		Vector clicked = e.getClickedBlock().getLocation().toVector();
		if (clicked.getBlockX() != treeLocation.getBlockX()
				|| clicked.getBlockZ() != treeLocation.getBlockZ()) 
			return;
		
		//wood chop. 
		doChop();
		
	}
	
	private void doChop() {
		//check time. Add offby points or fail game
		if (!player.getPlayer().isOnline())
			return;
		
		Player p = player.getPlayer().getPlayer();
		p.getWorld().playSound(p.getLocation(), clickSound, 1.1f, 1);
		
		double timeDifference;
		timeDifference = Math.abs(secondsRemaining);
		
		if (timeDifference > reactionTime) {
			loseGame();
			return;
		}
		
		int add = getOffbyIndex(timeDifference);
		this.offByIndex += add;
		if (add == 0) {
			p.sendMessage(perfectMessage);
			p.playSound(p.getLocation(), perfectSound, 1, 1);
		}
		
		this.hits--;
		
		if (hits <= 0) {
			winGame();
			return;
		}
		
		setTimer();
		
	}
	
	public static void setSkillLink(LumberjackSkill skill) {
		skillLink = skill;
	}
}
