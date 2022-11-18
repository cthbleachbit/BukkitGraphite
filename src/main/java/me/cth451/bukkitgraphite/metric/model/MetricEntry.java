package me.cth451.bukkitgraphite.metric.model;

import java.time.Instant;
import java.util.Map;

/**
 * One metric data point that can be submitted to graphite server
 */
public record MetricEntry(String key, Map<String, String> tags, Double value, Instant timestamp) {
	public MetricEntry(MetricPath path, Double value) {
		this(path.key(), path.tags(), value, Instant.now());
	}

	public MetricEntry(String key, Map<String, String> tags, Double value) {
		this(key, tags, value, Instant.now());
	}

	/**
	 * Adapting metric to graphite payload
	 *
	 * @return plain text metric in graphite plain text format
	 */
	public String toGraphite() {
		return new MetricPath(key(),
		                      tags()).toGraphite() + " " + value().toString() + " " + timestamp().getEpochSecond();
	}
}
