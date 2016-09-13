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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;

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
								
								for(Entry<String, UUID> use : CommandListener.inUse.entrySet()) {
									if(use.getValue().equals(p.getUniqueId()) || use.getValue().equals(p1.getUniqueId())) {
										CommandListener.inUse.remove(use.getKey());
									}
								}
							}
						}
					}
				}
			}, 900 * 20);
				
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if(e.getEntity() instanceof Player) {
			EntityDamageEvent cause = e.getEntity().getLastDamageCause();
			DamageCause dCause = cause.getCause();
			
			System.out.println(dCause.toString());
			if(!(dCause == DamageCause.ENTITY_ATTACK)) {
				Player p = (Player) e.getEntity();
				
				for(Player winner : Bukkit.getOnlinePlayers()) {
					for(Entry<UUID, UUID> k : CommandListener.inProg.entrySet()) {
						if(k.getKey().equals(p.getUniqueId()) && k.getValue().equals(winner.getUniqueId()) || k.getKey().equals(winner.getUniqueId()) && k.getValue().equals(p.getUniqueId())) {
							
						}
					}
				}
			}
			
			if(dCause == DamageCause.ENTITY_ATTACK) {
				if(e.getEntity().getKiller() instanceof Player) {
					Player p = (Player) e.getEntity();
		
					for(Entry<UUID, UUID> k : CommandListener.inProg.entrySet()) {
						System.out.println(k);
						Player winner = e.getEntity().getKiller();
						
						World world = Bukkit.getServer().getWorld(plugin.y.getString("lobby.world"));
                				Location p1Loc = new Location(world, plugin.y.getInt("lobby.x"), plugin.y.getInt("lobby.y"), plugin.y.getInt("lobby.z"));
                        		Location pLoc = new Location(world, plugin.y.getInt("lobby.x"), plugin.y.getInt("lobby.y"), plugin.y.getInt("lobby.z"));

						if(k.getKey().equals(p.getUniqueId()) && k.getValue().equals(winner.getUniqueId()) || k.getKey().equals(winner.getUniqueId()) && k.getValue().equals(p.getUniqueId())) {
							System.out.println("Working here");
							
							p.sendMessage(ChatColor.GREEN + "You lost the duel and " + ChatColor.BLUE + winner.getName() + ChatColor.GREEN + " received all your drops!");
							
							p.teleport(pLoc);
							winner.teleport(p1Loc);
							winner.sendMessage(ChatColor.GREEN + "You won and received " + ChatColor.BLUE + p.getName() + "'s " + ChatColor.GREEN + "drops!");
							
							for(ItemStack drops : e.getDrops()) {
								winner.getInventory().addItem(drops);
							}
							
							for(Entry<String, UUID> use : CommandListener.inUse.entrySet()) {
								if(use.getValue().equals(winner.getUniqueId()) || use.getValue().equals(p.getUniqueId())) {
									CommandListener.inUse.remove(use.getKey());
								}
							}
							
							Bukkit.getServer().broadcastMessage(ChatColor.BLUE + winner.getName() + ChatColor.GREEN + " has won in a duel against " + ChatColor.BLUE + p.getName());
							e.getDrops().clear();
							e.setDeathMessage(null);
							winner.setHealth(winner.getMaxHealth());
						}
					}
				}
			}
		}
	}
}
