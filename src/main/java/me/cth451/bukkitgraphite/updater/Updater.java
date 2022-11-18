package me.cth451.bukkitgraphite.updater;

import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Bare minimum APIs defining interface to a logging protocol for use with some remote metric service
 */
public interface Updater {
	/**
	 * Transmit a list of metrics to a remote logging service.
	 *
	 * @param entryList Collected metrics to send
	 * @return whether update to remote logging service
	 */
	boolean sendUpdates(@NotNull List<MetricEntry> entryList);

	/**
	 * @return human-readable name for this updater service
	 */
	String name();

	/**
	 * A unique identifier for this updater service. Server admins use this field to specify whether the updater should
	 * be enabled and configure its behavior in plugin configuration file.
	 *
	 * @return A unique identifier (can use alphanumeric, underscore and hyphen)
	 */
	String id();

	/**
	 * Configure local parameters.
	 * Metric group configurations should reside in options.updater.[id] and is presented via section
	 * @param section   metric group specific configuration section
	 * @return whether configuration was successful.
	 */
	boolean configure(ConfigurationSection section);
}