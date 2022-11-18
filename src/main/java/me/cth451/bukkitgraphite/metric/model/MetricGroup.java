package me.cth451.bukkitgraphite.metric.model;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Base class describing a bunch of metric keys logically collected and grouped together
 */
public abstract class MetricGroup {
	/**
	 * Reap current sensors value into a list of entries.
	 *
	 * @return sensors reaped
	 */
	public abstract @NotNull List<MetricEntry> scrape();

	/**
	 * Polling interval in ticks. This should be a constant value
	 *
	 * @return polling interval
	 */
	public abstract double interval();

	/**
	 * @return Human-readable name for this metric
	 */
	public abstract @NotNull String name();

	/**
	 * A unique identifier for this metric group. Server admins use this field to specify whether the group should be
	 * enabled.
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
}