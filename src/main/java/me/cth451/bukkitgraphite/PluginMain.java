package me.cth451.bukkitgraphite;

import me.cth451.bukkitgraphite.updater.UpdaterManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginMain extends JavaPlugin {
	private UpdaterManager manager = null;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		this.reloadConfig();
		this.saveConfig();
		manager = new UpdaterManager(this);
		manager.reloadComponentsFromConfig();
		manager.start();
	}

	@Override
	public void onDisable() {
		manager.stop();
		Bukkit.getScheduler().cancelTasks(this);
	}
}
