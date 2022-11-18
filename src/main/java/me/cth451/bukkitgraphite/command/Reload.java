package me.cth451.bukkitgraphite.command;

import me.cth451.bukkitgraphite.PluginMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Reload implements CommandExecutor {
	private PluginMain plugin;

	public Reload(PluginMain plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		this.plugin.reloadConfig();
		this.plugin.getManager().reloadComponentsFromConfig((sender instanceof Player player) ? player : null);
		return true;
	}
}
