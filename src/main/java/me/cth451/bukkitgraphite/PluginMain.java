package me.cth451.bukkitgraphite;

import me.cth451.bukkitgraphite.updater.UpdaterManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginMain extends JavaPlugin {
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		UpdaterManager manager = new UpdaterManager(this);
		manager.reloadFromConfig();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, manager, 0, 40);
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
	}
}
