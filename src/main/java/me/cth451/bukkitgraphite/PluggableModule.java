package me.cth451.bukkitgraphite;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public abstract class PluggableModule {
	protected final PluginMain plugin;

	protected PluggableModule(PluginMain plugin) {
		this.plugin = plugin;
	}

	/**
	 * @return human-readable name for this module service
	 */
	public abstract @NotNull String name();

	/**
	 * A unique identifier for this module service. Server admins use this field to specify whether the updater should
	 * be enabled and configure its behavior in plugin configuration file.
	 *
	 * @return A unique identifier (can use alphanumeric, underscore and hyphen)
	 */
	public abstract @NotNull String id();

	/**
	 * Configure local parameters. Metric group configurations should reside in options.metric-group.[id] and is
	 * presented via section
	 *
	 * @param section metric group specific configuration section
	 * @return whether configuration was successful.
	 */
	public abstract boolean configure(ConfigurationSection section);

	/**
	 * Initialize internal states and start any necessary background tasks.
	 */
	public abstract void start();

	/**
	 * Stop activity and release states (if any)
	 */
	public abstract void halt();

	/**
	 * @return component name, either "updater" or "metric-group"
	 */
	public abstract String component();
}
