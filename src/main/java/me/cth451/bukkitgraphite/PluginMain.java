package me.cth451.bukkitgraphite;

import me.cth451.bukkitgraphite.command.Reload;
import me.cth451.bukkitgraphite.updater.UpdaterManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class PluginMain extends JavaPlugin {
	private UpdaterManager manager = null;

	public UpdaterManager getManager() {
		return manager;
	}

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		this.reloadConfig();
		manager = new UpdaterManager(this);
		manager.reloadComponentsFromConfig(null);
		manager.start();
		Objects.requireNonNull(this.getCommand("graphite-reload")).setExecutor(new Reload(this));
	}

	@Override
	public void onDisable() {
		manager.stop();
		Bukkit.getScheduler().cancelTasks(this);
	}
}
