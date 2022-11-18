package me.cth451.bukkitgraphite.updater;

import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import me.cth451.bukkitgraphite.metric.model.MetricGroup;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class UpdaterManager implements Runnable {

	private final Plugin plugin;

	private final ReentrantLock configurationLock;
	private final HashMap<String, Updater> updaters;
	private final HashMap<String, MetricGroup> metricGroups;

	public UpdaterManager(Plugin plugin) {
		this.configurationLock = new ReentrantLock();
		this.updaters = new HashMap<>();
		this.metricGroups = new HashMap<>();
		this.plugin = plugin;
	}

	private void registerUpdaterWithLock(Updater updater) {
		Bukkit.getLogger().info("Registered updater: " + updater.name());
		this.updaters.put(updater.id(), updater);
	}

	/**
	 * Register an updater that would report local stats to somewhere else. This method is intended to be thread safe.
	 *
	 * @param updater updater instance to register
	 */
	public void registerUpdater(Updater updater) {
		configurationLock.lock();
		registerUpdaterWithLock(updater);
		configurationLock.unlock();
	}

	private void registerMetricWithLock(MetricGroup metric) {
		Bukkit.getLogger().info("Registered metric: " + metric.name());
		this.metricGroups.put(metric.id(), metric);
	}

	/**
	 * Register a metric group that specifies metrics to harvest. This method is intended to be thread safe.
	 *
	 * @param metric metric group instance to register
	 */
	public void registerMetric(MetricGroup metric) {
		configurationLock.lock();
		registerMetricWithLock(metric);
		configurationLock.unlock();
	}

	/**
	 * Remove all currently registered updaters and metric groups. This method is intended to be thread safe.
	 */
	public void unregisterAll() {
		configurationLock.lock();
		this.updaters.clear();
		this.metricGroups.clear();
		configurationLock.unlock();
	}

	/**
	 * Register updaters and metrics enabled in the server config and apply options
	 */
	public void reloadFromConfig() {
		// TODO: grab plugin config
		unregisterAll();
		configurationLock.lock();
		// TODO: enable according to config
		/* For enabled components call configure() */
		metricGroups.values().forEach(m -> m.configure(null));
		updaters.values().forEach(u -> u.configure(null));
		configurationLock.unlock();
	}

	@Override
	public void run() {
		configurationLock.lock();
		List<MetricEntry> entries = metricGroups.values()
		                                        .stream()
		                                        .map(MetricGroup::scrape) /* Collect stats for all registered metric */
		                                        .flatMap(List::stream) /* Merge the list together */
		                                        .toList();
		updaters.values().stream()
		        .parallel()
		        .map(e -> Map.entry(e, e.sendUpdates(entries))) /* Execute stat sender */
		        .filter(e -> !e.getValue()) /* Find ones that failed to update */
		        .forEach(e -> Bukkit.getLogger().warning(e.getKey().name() + " has failed!"));
		configurationLock.unlock();
	}
}
