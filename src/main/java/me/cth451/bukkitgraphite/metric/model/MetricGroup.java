package me.cth451.bukkitgraphite.metric.model;

import java.util.List;

public interface MetricGroup {
	/**
	 * Reap current sensors value into a list of entries.
	 *
	 * @return sensors reaped
	 */
	public List<MetricEntry> scrape();

	/**
	 * Polling interval in ticks. This should be a constant value
	 *
	 * @return polling interval
	 */
	public double interval();

	/**
	 * @return Human-readable name for this metric
	 */
	public String name();
}