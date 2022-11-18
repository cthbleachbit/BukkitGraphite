package me.cth451.bukkitgraphite.metric.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Map;

/**
 * One data point that can be adapted to different metric service protocol.
 */
public record MetricEntry(String key, Map<String, String> tags, double value, Instant timestamp) {
	/**
	 * Create a metric data point from path, value and construction time
	 *
	 * @param path  full path containing a key (or metric name) and optional tags (or labels)
	 * @param value numeric value for this metric
	 */
	public MetricEntry(@NotNull MetricPath path, double value) {
		this(path.key(), path.tags(), value, Instant.now());
	}

	/**
	 * Create a metric data point from key, tags and construction time
	 *
	 * @param key   metric key (aka. prometheus metric name)
	 * @param tags  optional tags (aka. prometheus labels). Passing null is equivalent to an empty map.
	 * @param value numeric value for this metric
	 */
	public MetricEntry(@NotNull String key, @Nullable Map<String, String> tags, double value) {
		this(key, tags, value, Instant.now());
	}

	/**
	 * Adapting metric to graphite payload
	 *
	 * @return plain text metric in graphite plain text format
	 */
	public String toGraphite(String namespace) {
		String namespacedKey = (namespace == null | namespace.isEmpty()) ? key() : namespace + "." + key();
		String pathString = new MetricPath(namespacedKey, tags()).toGraphite();
		return pathString + " " + value() + " " + timestamp().getEpochSecond();
	}
}
