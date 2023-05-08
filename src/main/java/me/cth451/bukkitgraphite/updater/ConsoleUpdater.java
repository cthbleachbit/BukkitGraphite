package me.cth451.bukkitgraphite.updater;

import me.cth451.bukkitgraphite.PluginMain;
import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Scrape and dump serialized stats to console
 */
public class ConsoleUpdater extends Updater {
	public static String ID = "console";

	public ConsoleUpdater(PluginMain plugin) {
		super(plugin);
	}

	@Override
	public boolean sendUpdates(@NotNull Collection<MetricEntry> entries) {
		entries.forEach(e -> plugin.getLogger().info(e.toString()));
		return true;
	}

	@Override
	public @NotNull String name() {
		return "Server Console";
	}

	@Override
	public @NotNull String id() {
		return ID;
	}

	/**
	 * This updater doesn't have configurable options.
	 *
	 * @param section metric group specific configuration section
	 * @return true
	 */
	@Override
	public boolean configure(ConfigurationSection section) {
		return true;
	}
}
