package me.cth451.bukkitgraphite;

import me.cth451.bukkitgraphite.command.Reload;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Level;

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

	public void complainToChatOrConsole(@NotNull Level level, Player p, @NotNull String message) {
		if (p == null) {
			this.getLogger().log(level, message);
		} else {
			p.sendMessage(message);
		}
	}
}
