package me.endureblackout.duelme;

import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ArenaChecks implements Listener {
	
	DuelMeMain plugin;
	
	public ArenaChecks(DuelMeMain instance) {
		this.plugin = instance;
	}

	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
			Player p = e.getPlayer();
			
			if(e.getMessage().toLowerCase().startsWith("/teleport") || e.getMessage().toLowerCase().startsWith("/tp")) {
				for(Entry<UUID, UUID> kv : CommandListener.inProg.entrySet()) {
					if(kv.getKey().equals(p.getUniqueId()) || kv.getValue().equals(p.getUniqueId())) {
						e.setCancelled(true);
						p.sendMessage(ChatColor.RED + "You cannot use that command here!");
					}
				}
			}
	}
}
