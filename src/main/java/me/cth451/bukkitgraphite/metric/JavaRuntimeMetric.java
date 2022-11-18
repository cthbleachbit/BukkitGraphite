package me.cth451.bukkitgraphite.metric;

import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import me.cth451.bukkitgraphite.metric.model.MetricGroup;

import java.util.LinkedList;
import java.util.List;

/**
 * Metrics reporting java runtime information. Memory usage is measured in bytes.
 */
public class JavaRuntimeMetric implements MetricGroup {

	public static String JVM_TOTAL_MEM = "runtime.mem.total";
	public static String JVM_FREE_MEM = "runtime.mem.free";

	@Override
	public List<MetricEntry> scrape() {
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
	public String name() {
		return "Java Runtime Information";
	}

	@Override
	public String id() {
		return "runtime";
	}
}
