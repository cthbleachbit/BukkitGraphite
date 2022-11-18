package me.cth451.bukkitgraphite.metric.model;

import java.util.List;

public interface MetricGroup {
	/**
	 * Reap current sensors value into a list of entries.
	 *
	 * @return sensors reaped
	 */
	List<MetricEntry> scrape();

	/**
	 * Polling interval in ticks. This should be a constant value
	 *
	 * @return polling interval
	 */
	double interval();

	/**
	 * @return Human-readable name for this metric
	 */
	String name();

	/**
	 * A unique identifier for this metric group.
	 * Server admins use this field to specify whether the group should be enabled.
	 *
	 * @return A unique identifier (can use alphanumeric and underscore)
	 */
	String id();
}