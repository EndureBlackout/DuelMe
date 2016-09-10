package me.endureblackout.duelme;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DuelMeMain extends JavaPlugin {
	
	private File f = new File(getDataFolder(), "arenas.yml");
	public YamlConfiguration y;
	
	private ConfigurationSection arenaList;
	List<String> arenas = new ArrayList<String>();
	
	public void onEnable() {

		getCommand("duel").setExecutor(new CommandListener(this));
		getCommand("arena").setExecutor(new CommandListener(this));
		
		Bukkit.getPluginManager().registerEvents(new ArenaChecks(this), this);
		
		File dataFolder = getDataFolder();
		if(!dataFolder.exists()) {
			dataFolder.mkdir();
		}
		if(!new File(dataFolder, "arenas.yml").exists()) {
			saveResource("arenas.yml", false);
		}
		
		y = YamlConfiguration.loadConfiguration(f);
	}
	
	public void saveArenas() {
		try{
			y.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getArenas() {
		arenaList = y.getConfigurationSection("arenas");
		arenas.addAll(arenaList.getKeys(false));
	}

}
