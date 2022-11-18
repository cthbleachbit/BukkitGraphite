package me.cth451.bukkitgraphite.metric.model;

import java.util.Map;
import java.util.List;
import java.util.Objects;

/**
 * Interface for a metric hierarchy entry available and can be collected on the server. A metric with key
 * `server.entity` and key `{"world":"world_nether"}' becomes the following when submitting to graphite server.
 * <pre>
 *     minecraft.server.entity;world=world_nether
 * </pre>
 *
 * @param key  key for the metric without root namespace and leading dot separator
 * @param tags tags for this key
 */
public record MetricPath(String key, Map<String, String> tags) {
	public MetricPath {
		Objects.requireNonNull(key);
	}

	public String toGraphite() {
		if (tags == null || tags.isEmpty()) {
			return key;
		}
		List<String> tagString = tags.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).toList();
		return key + ";" + String.join(";", tagString);
	}
}