package me.cth451.bukkitgraphite.updater;

import me.cth451.bukkitgraphite.metric.JavaRuntimeMetric;
import me.cth451.bukkitgraphite.metric.PlayersActiveMetric;
import me.cth451.bukkitgraphite.metric.ServerLoadedMetric;
import me.cth451.bukkitgraphite.metric.ServerTpsMetric;
import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import me.cth451.bukkitgraphite.metric.model.MetricGroup;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class UpdaterManager implements Runnable {

	private final Plugin plugin;
	private final ReentrantLock configurationLock;
	private final HashMap<String, Updater> updaters;
	private final HashMap<String, MetricGroup> metricGroups;
	private int updateIntervalTicks = 20;
	private Integer updaterTaskId = null;

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

	private void registerUpdaterWithLock(@NotNull Updater updater) {
		plugin.getLogger().info("Registered updater: " + updater.name());
		this.updaters.put(updater.id(), updater);
	}

	/**
	 * Register an updater that would report local stats to somewhere else. This method is intended to be thread safe.
	 *
	 * @param updater updater instance to register
	 */
	public void registerUpdater(@NotNull Updater updater) {
		configurationLock.lock();
		registerUpdaterWithLock(updater);
		configurationLock.unlock();
	}

	private void registerMetricWithLock(@NotNull MetricGroup metric) {
		plugin.getLogger().info("Registered metric: " + metric.name());
		this.metricGroups.put(metric.id(), metric);
	}

	/**
	 * Register a metric group that specifies metrics to harvest. This method is intended to be thread safe.
	 *
	 * @param metric metric group instance to register
	 */
	public void registerMetric(@NotNull MetricGroup metric) {
		configurationLock.lock();
		registerMetricWithLock(metric);
		configurationLock.unlock();
	}

	/**
	 * Remove all currently registered updaters and metric groups. This method is intended to be thread safe.
	 */
	public void unregisterAll() {
		configurationLock.lock();
		this.updaters.values().forEach(Updater::halt);
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
		ConfigurationSection conf = this.plugin.getConfig().getConfigurationSection("updaters");
		if (conf == null) {
			return new LinkedList<>();
		}
		return knownUpdaters.keySet().stream()
		                    .filter(k -> conf.getBoolean(k, false))
		                    .toList();
	}

	private @NotNull List<String> retrieveEnabledMetricGroups() {
		ConfigurationSection conf = this.plugin.getConfig().getConfigurationSection("metric-groups");
		if (conf == null) {
			return new LinkedList<>();
		}
		return knownMetricGroups.keySet().stream()
		                        .filter(k -> conf.getBoolean(k, false))
		                        .toList();
	}

	/**
	 * Register updaters and metrics enabled in the server config and apply options.
	 * <p>
	 * This function assumes configuration has been reloaded from disk before here.
	 *
	 * @param p player who initiated reloading - null if invoked from program or command or entity
	 */
	public void reloadComponentsFromConfig(@Nullable Player p) {
		List<String> updatersToEnable = retrieveEnabledUpdaters();
		List<String> metricGroupsToEnable = retrieveEnabledMetricGroups();

		unregisterAll();

		if (updatersToEnable.isEmpty()) {
			String message = "No updater enabled?";
			if (p == null) {
				plugin.getLogger().warning(message);
			} else {
				p.sendMessage(message);
			}
		}

		if (metricGroupsToEnable.isEmpty()) {
			String message = "No metric group enabled?";
			if (p == null) {
				plugin.getLogger().warning(message);
			} else {
				p.sendMessage(message);
			}
		}

		configurationLock.lock();
		updatersToEnable.forEach(
				id -> {
					if (knownUpdaters.containsKey(id)) {
						Class<? extends Updater> u = knownUpdaters.get(id);
						try {
							registerUpdaterWithLock(u.getConstructor(Plugin.class).newInstance(this.plugin));
						} catch (InstantiationException | IllegalAccessException | InvocationTargetException |
						         NoSuchMethodException e) {
							String message = "Cannot initialize updater id " + id;
							if (p == null) {
								plugin.getLogger().severe(message);
							} else {
								p.sendMessage(message);
							}
						}
					} else {
						String message = "Unknown updater: " + id;
						if (p == null) {
							plugin.getLogger().severe(message);
						} else {
							p.sendMessage(message);
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
							String message = "Cannot initialize metric group id " + id;
							if (p == null) {
								plugin.getLogger().severe(message);
							} else {
								p.sendMessage(message);
							}
						}
					} else {
						String message = "Unknown metric group: " + id;
						if (p == null) {
							plugin.getLogger().severe(message);
						} else {
							p.sendMessage(message);
						}
					}
				}
		);

		/* For enabled components call configure() */
		metricGroups.values().forEach(m -> m.configure(retrieveConfigSection(m)));
		updaters.values().forEach(u -> u.configure(retrieveConfigSection(u)));
		updaters.values().forEach(Updater::start);
		configurationLock.unlock();

		/* Load global updater preference in `options.global` */
		{
			if (!plugin.getConfig().isInt("options.global.scrape-interval-ticks")) {
				plugin.getConfig().set("options.global.scrape-interval-ticks", 20);
			}
			this.updateIntervalTicks = plugin.getConfig().getInt("options.global.scrape-interval-ticks");
			String message = "Update Interval: every " + this.updateIntervalTicks + " ticks";
			if (p == null) {
				plugin.getLogger().info(message);
			} else {
				p.sendMessage(message);
			}
		}
	}

	/**
	 * Repeating routine that generate one round of update.
	 */
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

	public void start() {
		configurationLock.lock();
		if (updaterTaskId == null) {
			updaterTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this, 0, updateIntervalTicks);
			if (updaterTaskId == -1) {
				plugin.getLogger().severe("Cannot start metric update task!");
				updaterTaskId = null;
			}
		}
		configurationLock.unlock();
	}

	public void stop() {
		configurationLock.lock();
		if (updaterTaskId != null) {
			Bukkit.getScheduler().cancelTask(updaterTaskId);
			updaterTaskId = null;
		}
		configurationLock.unlock();
	}
}
