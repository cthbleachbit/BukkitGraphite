package me.cth451.bukkitgraphite.metric;

import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import me.cth451.bukkitgraphite.metric.model.MetricGroup;
import me.cth451.bukkitgraphite.metric.model.MetricPath;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedList;
import java.util.List;

/**
 * Ticks-per-second and milliseconds-per-tick perf counters
 */
public class ServerTpsMetric implements MetricGroup {

	@Override
	public List<MetricEntry> scrape() {
		List<MetricEntry> results = new LinkedList<>();
		/* tick per second */
		results.add(new MetricEntry(new MetricPath("server.tps", null), Bukkit.getTPS()[0]));
		/* ms per tick*/
		results.add(new MetricEntry(new MetricPath("server.mspt", null), Bukkit.getAverageTickTime()));
		return results;
	}

	@Override
	public double interval() {
		return 20;
	}

	@Override
	public String name() {
		return "Server ticks-per-second";
	}

	@Override
	public String id() {
		return "server_tps";
	}

	/**
	 * This metric group does not have configurable elements.
	 * @param section   metric group specific configuration section
	 * @return true
	 */
	@Override
	public boolean configure(ConfigurationSection section) {
		return true;
	}
}
