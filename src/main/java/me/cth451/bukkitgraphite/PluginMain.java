package me.cth451.bukkitgraphite;

import me.cth451.bukkitgraphite.metric.ServerLoadedMetric;
import me.cth451.bukkitgraphite.updater.ConsoleUpdater;
import me.cth451.bukkitgraphite.updater.UpdaterManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginMain extends JavaPlugin {
	@Override
	public void onEnable() {
		UpdaterManager manager = new UpdaterManager(this);
		manager.registerUpdater(new ConsoleUpdater());
		// manager.registerUpdater(new GraphiteUpdater("127.0.0.1", 2003));
		manager.registerMetric(new ServerLoadedMetric());
		// manager.registerMetric(new ServerTpsMetric());
		// manager.registerMetric(new PlayersActiveMetric());
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, manager, 0, 40);
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
	}
}
