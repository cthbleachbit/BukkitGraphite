package me.cth451.bukkitgraphite.metric;

import java.util.Map;
import java.util.List;
import java.util.Objects;

/**
 * Interface for a metric hierarchy entry available and can be collected on the server. A metric with path
 * `server.entity` and path `{"world":"minecraft:overworld"}' becomes the following when submitting to graphite server.
 * <pre>
 *     minecraft.server.entity;world=minecraft:overworld
 * </pre>
 *
 * @param path path for the metric without root namespace and leading dot separator
 * @param tags tags for this path
 */
public record MetricPath(String path, Map<String, String> tags) {
	public MetricPath {
		Objects.requireNonNull(path);
	}

	public String toString() {
		if (tags == null || tags.isEmpty()) {
			return path;
		}
		List<String> tagString = tags.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).toList();
		return path + ";" + String.join(";", tagString);
	}
}