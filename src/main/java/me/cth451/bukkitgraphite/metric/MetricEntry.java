package me.cth451.bukkitgraphite.metric;

import java.time.Instant;

/**
 * One metric data point that can be submitted to graphite server
 */
public record MetricEntry<T>(MetricPath path, T value, Instant timestamp) {
	public MetricEntry(MetricPath path, T value) {
		this(path, value, Instant.now());
	}

	public String toGraphite() {
		return path + " " + value.toString() + " " + timestamp.getEpochSecond();
	}
}
