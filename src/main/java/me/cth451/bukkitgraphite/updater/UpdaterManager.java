package me.cth451.bukkitgraphite.updater;

import me.cth451.bukkitgraphite.metric.JavaRuntimeMetric;
import me.cth451.bukkitgraphite.metric.PlayersActiveMetric;
import me.cth451.bukkitgraphite.metric.ServerLoadedMetric;
import me.cth451.bukkitgraphite.metric.ServerTpsMetric;
import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import me.cth451.bukkitgraphite.metric.model.MetricGroup;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class UpdaterManager implements Runnable {
	public static String UPDATER_PACKAGE = "me.cth451.bukkitgraphite.updater";
	public static String METRIC_PACKAGE = "me.cth451.bukkitgraphite.metric";

	private final Plugin plugin;
	private final ReentrantLock configurationLock;
	private final HashMap<String, Updater> updaters;
	private final HashMap<String, MetricGroup> metricGroups;

	private static final Map<String, Class<? extends Updater>> knownUpdaters =
			Map.ofEntries(
					Map.entry(ConsoleUpdater.ID, ConsoleUpdater.class),
					Map.entry(GraphiteUpdater.ID, GraphiteUpdater.class)
			);

	private static final Map<String, Class<? extends MetricGroup>> knownMetricGroups =
			Map.ofEntries(
					Map.entry(JavaRuntimeMetric.ID, JavaRuntimeMetric.class),
					Map.entry(PlayersActiveMetric.ID, PlayersActiveMetric.class),
					Map.entry(ServerLoadedMetric.ID, ServerLoadedMetric.class),
					Map.entry(ServerTpsMetric.ID, ServerTpsMetric.class)
			);

	public UpdaterManager(Plugin plugin) {
		this.configurationLock = new ReentrantLock();
		this.updaters = new HashMap<>();
		this.metricGroups = new HashMap<>();
		this.plugin = plugin;
	}

	private void registerUpdaterWithLock(Updater updater) {
		plugin.getLogger().info("Registered updater: " + updater.name());
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
		plugin.getLogger().info("Registered metric: " + metric.name());
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
	 * Retrieve configuration section for an updater
	 * <p>
	 * This method should only be called when configurationLock is held.
	 *
	 * @param u updater to fetch config for
	 * @return configuration section as a map, or null if the node doesn't exist.
	 */
	private @Nullable ConfigurationSection retrieveConfigSection(@NotNull Updater u) {
		ConfigurationSection root = this.plugin.getConfig();
		ConfigurationSection options = root.getConfigurationSection("options.updaters");
		return options == null ? null : options.getConfigurationSection(u.id());
	}

	/**
	 * Retrieve configuration section for a metric group.
	 * <p>
	 * This method should only be called when configurationLock is held.
	 *
	 * @param m metric group to fetch config for
	 * @return configuration section as a map, or null if the node doesn't exist.
	 */
	private @Nullable ConfigurationSection retrieveConfigSection(@NotNull MetricGroup m) {
		ConfigurationSection root = this.plugin.getConfig();
		ConfigurationSection options = root.getConfigurationSection("options.metric-groups");
		return options == null ? null : options.getConfigurationSection(m.id());
	}

	private @NotNull List<String> retrieveEnabledUpdaters() {
		return this.plugin.getConfig().getStringList("updaters");
	}

	private @NotNull List<String> retrieveEnabledMetricGroups() {
		return this.plugin.getConfig().getStringList("metric-groups");
	}

	/**
	 * Register updaters and metrics enabled in the server config and apply options.
	 * <p>
	 * This function assumes configuration has been reloaded from disk before here.
	 */
	public void reloadFromConfig() {
		List<String> updatersToEnable = retrieveEnabledUpdaters();
		List<String> metricGroupsToEnable = retrieveEnabledMetricGroups();

		unregisterAll();

		configurationLock.lock();
		updatersToEnable.forEach(
				id -> {
					if (knownUpdaters.containsKey(id)) {
						Class<? extends Updater> u = knownUpdaters.get(id);
						try {
							registerUpdaterWithLock(u.getConstructor(Plugin.class).newInstance(this.plugin));
						} catch (InstantiationException | IllegalAccessException | InvocationTargetException |
						         NoSuchMethodException e) {
							plugin.getLogger().severe("Cannot initialize updater id " + id);
						}
					}
				}
		);
		metricGroupsToEnable.forEach(
				id -> {
					if (knownMetricGroups.containsKey(id)) {
						Class<? extends MetricGroup> m = knownMetricGroups.get(id);
						try {
							registerMetricWithLock(m.getConstructor().newInstance());
						} catch (InstantiationException | IllegalAccessException | InvocationTargetException |
						         NoSuchMethodException e) {
							plugin.getLogger().severe("Cannot initialize metric group id " + id);
						}
					}
				}
		);
		/* For enabled components call configure() */
		metricGroups.values().forEach(m -> m.configure(retrieveConfigSection(m)));
		updaters.values().forEach(u -> u.configure(retrieveConfigSection(u)));
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
		        .forEach(e -> plugin.getLogger().warning(e.getKey().name() + " has failed!"));
		configurationLock.unlock();
	}
}
