package me.cth451.bukkitgraphite.metric;

import me.cth451.bukkitgraphite.PluginMain;
import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import me.cth451.bukkitgraphite.metric.model.MetricGroup;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Metrics reporting java runtime information. Memory usage is measured in bytes.
 */
public class JavaRuntimeMetric extends MetricGroup {
	public static String ID = "runtime";
	public static String JVM_TOTAL_MEM = "runtime.mem.total";
	public static String JVM_FREE_MEM = "runtime.mem.free";

	public JavaRuntimeMetric(PluginMain plugin) {
		super(plugin);
	}

	@Override
	public @NotNull List<MetricEntry> scrape() {
		List<MetricEntry> results = new LinkedList<>();
		results.add(new MetricEntry(JVM_TOTAL_MEM, null, (double) Runtime.getRuntime().totalMemory()));
		results.add(new MetricEntry(JVM_FREE_MEM, null, (double) Runtime.getRuntime().freeMemory()));
		return results;
	}

	@Override
	public double interval() {
		return 20;
	}

	@Override
	public @NotNull String name() {
		return "Java Runtime Information";
	}

	@Override
	public @NotNull String id() {
		return ID;
	}

	/**
	 * This metric group does not have configurable elements.
	 *
	 * @param section metric group specific configuration section
	 * @return true
	 */
	@Override
	public boolean configure(ConfigurationSection section) {
		return true;
	}
}
