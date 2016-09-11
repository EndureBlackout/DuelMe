package me.endureblackout.duelme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandListener implements CommandExecutor {
	
	DuelMeMain plugin;
	
	public CommandListener(DuelMeMain instance) {
		this.plugin = instance;
	}

	public static Map<UUID, UUID> duelWait = new HashMap<UUID, UUID>();
	public static Map<UUID, UUID> inProg = new HashMap<UUID, UUID>();
	public static List<String> inUse = new ArrayList<String>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only ingame players can perform commands!");
		}
		
		if(sender instanceof Player) {
			Player p = (Player) sender;
			//TODO: Cannot challenge self to duel!!!
			if(cmd.getName().equalsIgnoreCase("duel")) {
				if(args.length != 1) {
					p.sendMessage(ChatColor.RED + "Incorrect syntax!");
					p.sendMessage(ChatColor.RED + "Try /duel <Player> /duel accept or /duel decline");
					p.sendMessage(plugin.arenas.toString());
				} else if(args.length == 1) {	
					if(args[0].equalsIgnoreCase("accept")) {
						if(duelWait.values().contains(p.getUniqueId())) {
							p.sendMessage(ChatColor.GREEN + "You have accepted to duel!");
							
							plugin.getArenas();
							Random random = new Random();
							int selector = random.nextInt(plugin.arenas.size());
							String arena = plugin.arenas.get(selector);
							
							for(Entry<UUID, UUID> k : duelWait.entrySet()) {
								if(k.getValue().equals(p.getUniqueId())) {
									String choosenArena = arena;
									World world = Bukkit.getServer().getWorld(plugin.y.getString("arenas." + choosenArena + ".world"));
									UUID p1 = k.getKey();
									
									double px = plugin.y.getDouble("arenas." + choosenArena + ".spawn1.x");
									double py = plugin.y.getDouble("arenas." + choosenArena + ".spawn1.y");
									double pz = plugin.y.getDouble("arenas." + choosenArena + ".spawn1.z");
									Location pLoc = new Location(world, px, py, pz);
									
									int p1x = plugin.y.getInt("arenas." + choosenArena + ".spawn2.x");
									int p1y = plugin.y.getInt("arenas." + choosenArena + ".spawn2.y");
									int p1z = plugin.y.getInt("arenas." + choosenArena + ".spawn2.z");
									Location p1Loc = new Location(world, p1x, p1y, p1z);
									
									inProg.put(duelWait.get(k.getKey()), p.getUniqueId());
									duelWait.remove(k.getKey());
									
									for(Player pIDP : Bukkit.getServer().getOnlinePlayers()) {
										if(pIDP.getUniqueId().equals(p1)) {
											pIDP.sendMessage(ChatColor.GREEN + "Duel was accepted!");
											if(!inUse.contains(choosenArena)) {
												inUse.add(choosenArena);
												p.sendMessage(ChatColor.GREEN + "You are being teleported to the " + choosenArena + " arena!");
												pIDP.sendMessage(ChatColor.GREEN + "You are being teleported to the " + choosenArena + " arena!");
												p.teleport(pLoc);
												pIDP.teleport(p1Loc);
											}
										}
									}
								}
							}
						} else {
							p.sendMessage(ChatColor.RED + "No one has challanged you to a duel!");
						}
					}
					
					if(args[0].equalsIgnoreCase("decline")) {
						if(duelWait.values().contains(p.getUniqueId())) {
							for(Entry<UUID, UUID> k : duelWait.entrySet()) {
									if(k.getValue().equals(p.getUniqueId())) {
										p.sendMessage(ChatColor.RED + "You have declined the duel!");
										duelWait.remove(k.getKey());
										
										for(Player p1 : Bukkit.getOnlinePlayers()) {
											if(p1.getUniqueId().equals(k.getKey())) {
												p1.sendMessage(ChatColor.RED + "Your duel was declined!");
											}
										}
									}
							}
						}  else {
							p.sendMessage(ChatColor.RED + "No duels to decline!");
						}
					}
					
					if(!args[0].equalsIgnoreCase("accept") && !args[0].equalsIgnoreCase("decline")) {
						for(Player p1 : Bukkit.getOnlinePlayers()) {
							if(!p1.getName().equalsIgnoreCase(args[0])) {
								p.sendMessage(ChatColor.RED + "The player doesn't exist or isn't online!");
							} else {
								duelWait.put(p.getUniqueId(), p1.getUniqueId());
								p.sendMessage(ChatColor.GREEN + "You have challanged " + p1.getName() + " to a duel!");
								p1.sendMessage(ChatColor.GREEN + p.getName() + " has challanged you to a duel!");
							}
						}
					}
				}
				
				if(args.length == 2 && p.hasPermission("duelme.admin")) {
					if(args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("lobby")) {
						if(p.hasPermission("duelme.lobby")){
							plugin.y.set("lobby.world", p.getLocation().getWorld().getName());
							plugin.y.set("lobby.x", p.getLocation().getBlock().getX());
							plugin.y.set("lobby.y", p.getLocation().getBlock().getY());
							plugin.y.set("lobby.z", p.getLocation().getBlock().getZ());
								
							plugin.saveArenas();
								
							p.sendMessage(ChatColor.GREEN + "Duels lobby has been set!");
						}
					}
				}
			}
			
			if(cmd.getName().equalsIgnoreCase("arena") && p.hasPermission("duelme.admin")) {
				if(args.length == 2) {
					String arenaD = args[1].toLowerCase();
					
					if(args[0].equalsIgnoreCase("remove")) {
						if(plugin.y.getConfigurationSection("arenas").contains(arenaD)) {
							plugin.y.set("arenas." + arenaD, null);
							plugin.arenas.remove(arenaD);
							plugin.saveArenas();
							
							p.sendMessage(ChatColor.GREEN + "Arena deleted.");
						}
					}
				}
				
				if(args.length == 3) {
					String arenaC = args[1].toLowerCase();
					
					
					if(args[0].equalsIgnoreCase("set")) {
						if(args[2].equalsIgnoreCase("spawn1")) {
							plugin.y.set("arenas." + arenaC + ".spawn1.x", p.getLocation().getBlock().getX());
							plugin.y.set("arenas." + arenaC + ".spawn1.y", p.getLocation().getBlock().getY());
							plugin.y.set("arenas." + arenaC + ".spawn1.z", p.getLocation().getBlock().getZ());
							
							plugin.saveArenas();
							
							p.sendMessage(ChatColor.GREEN + "Spawn 1 set for arena " + ChatColor.RED + arenaC);
						}
						
						if(args[2].equalsIgnoreCase("spawn2")) {
							plugin.y.set("arenas." + arenaC + ".spawn2.x", p.getLocation().getBlock().getX());
							plugin.y.set("arenas." + arenaC + ".spawn2.y", p.getLocation().getBlock().getY());
							plugin.y.set("arenas." + arenaC + ".spawn2.z", p.getLocation().getBlock().getZ());
							
							plugin.saveArenas();
							
							p.sendMessage(ChatColor.GREEN + "Spawn 2 set for arena " + ChatColor.RED + arenaC);
						}
					}
					
					if(args[0].equalsIgnoreCase("create") && args[2].equalsIgnoreCase("pos1")) {
						if(!plugin.y.contains("arenas." + args[1])) {
							plugin.y.createSection("arenas." + arenaC);
							plugin.y.set("arenas." + arenaC + ".world", p.getWorld().getName());
							plugin.y.set("arenas." + arenaC + ".pos1.x", p.getLocation().getBlockX());
							plugin.y.set("arenas." + arenaC + ".pos1.y", p.getLocation().getBlockY());
							plugin.y.set("arenas." + arenaC + ".pos1.z", p.getLocation().getBlockZ());
							
							plugin.saveArenas();
							p.sendMessage(ChatColor.GREEN + "Arena create sucessfully with name " + ChatColor.RED + arenaC);
						}
					}
					
					if(args[0].equalsIgnoreCase("set") && args[2].equalsIgnoreCase("pos2")) {
						if(plugin.y.contains("arenas." + arenaC)) {
							plugin.y.set("arenas." + arenaC + ".pos2.x", p.getLocation().getBlockX());
							plugin.y.set("arenas." + arenaC + ".pos2.y", p.getLocation().getBlockY());
							
							plugin.y.set("arenas." + arenaC + ".pos2.z", p.getLocation().getBlockZ());
							
							plugin.saveArenas();
							p.sendMessage(ChatColor.GREEN + "You have sucessfully set point 2 for " + ChatColor.RED + arenaC);
						}
					}
				}
			}
		}
		
		return true;
	}

}
