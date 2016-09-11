package me.endureblackout.duelme;

import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ArenaChecks implements Listener {
	
	DuelMeMain plugin;
	
	public ArenaChecks(DuelMeMain instance) {
		this.plugin = instance;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
		final Player p = e.getPlayer();
		
		for(String cmd : plugin.y.getStringList("blocked cmds")) {
		if(e.getMessage().toLowerCase().startsWith("/" + cmd)) {
			for(Entry<UUID, UUID> kv : CommandListener.inProg.entrySet()) {
				if(kv.getKey().equals(p.getUniqueId()) || kv.getValue().equals(p.getUniqueId())) {
					e.setCancelled(true);
					p.sendMessage(ChatColor.RED + "You cannot use that command here!");
				}
			}
		}
		}
		
		if(e.getMessage().toLowerCase().startsWith("/duel accept")) {
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(this.plugin, new Runnable() {
				@Override
				public void run() {
					p.sendMessage(ChatColor.RED + "Duel ended after 15 mins!");
					
					for(Entry<UUID, UUID> k : CommandListener.inProg.entrySet()) {
						for(Player p1 : Bukkit.getServer().getOnlinePlayers()) {
							if(k.getKey().equals(p1.getUniqueId())) {
								World world = Bukkit.getServer().getWorld(plugin.y.getString("lobby.world"));
								Location p1Loc = new Location(world, plugin.y.getInt("lobby.x"), plugin.y.getInt("lobby.y"), plugin.y.getInt("lobby.z"));
								Location pLoc = new Location(world, plugin.y.getInt("lobby.x"), plugin.y.getInt("lobby.y"), plugin.y.getInt("lobby.z"));
								
								CommandListener.inProg.remove(p1.getUniqueId());
								p1.sendMessage(ChatColor.RED + "Duel ended after 15 mins!");
								p1.setHealth(p1.getMaxHealth());
								p.setHealth(p.getMaxHealth());
								p1.teleport(p1Loc);
								p.teleport(pLoc);
							}
						}
					}
				}
			}, 900 * 20);
				
		}
	}
}
