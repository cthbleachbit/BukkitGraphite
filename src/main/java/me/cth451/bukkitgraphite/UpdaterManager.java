package me.cth451.bukkitgraphite;

import me.cth451.bukkitgraphite.metric.JavaRuntimeMetric;
import me.cth451.bukkitgraphite.metric.PlayersActiveMetric;
import me.cth451.bukkitgraphite.metric.ServerLoadedMetric;
import me.cth451.bukkitgraphite.metric.ServerTpsMetric;
import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import me.cth451.bukkitgraphite.metric.model.MetricGroup;
import me.cth451.bukkitgraphite.updater.ConsoleUpdater;
import me.cth451.bukkitgraphite.updater.GraphiteUpdater;
import me.cth451.bukkitgraphite.updater.Updater;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.stream.Stream;

public class UpdaterManager {

	private final PluginMain plugin;
	private final ReentrantLock configurationLock = new ReentrantLock();
	private final HashMap<String, PluggableModule> modules = new HashMap<>();
	private final ConcurrentLinkedQueue<Collection<MetricEntry>> updateQueue = new ConcurrentLinkedQueue<>();
	private int updateIntervalTicks = 20;
	private BukkitTask scrapeTask = null;
	private BukkitTask updateTask = null;

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

	public UpdaterManager(PluginMain plugin) {
		this.plugin = plugin;
	}

	private void registerModuleWithLock(@NotNull PluggableModule module) {
		plugin.getLogger().info("Registered " + module.component() + ": " + module.name());
		this.modules.put(module.id(), module);
	}

	/**
	 * Remove all currently registered updaters and metric groups. This method is intended to be thread safe.
	 */
	public void unregisterAll() {
		configurationLock.lock();
		this.modules.values().forEach(PluggableModule::halt);
		this.modules.clear();
		configurationLock.unlock();
	}

	/**
	 * Retrieve configuration section for an updater
	 * <p>
	 * This method should only be called when configurationLock is held.
	 *
	 * @param m module to fetch config for
	 * @return configuration section as a map, or null if the node doesn't exist.
	 */
	private @Nullable ConfigurationSection retrieveConfigSection(@NotNull PluggableModule m) {
		ConfigurationSection root = this.plugin.getConfig();
		ConfigurationSection options = root.getConfigurationSection("options." + m.component() + "s");
		return options == null ? null : options.getConfigurationSection(m.id());
	}

	private @NotNull List<String> retrieveEnabledModules() {
		ConfigurationSection updaterConf = this.plugin.getConfig().getConfigurationSection("updaters");
		ConfigurationSection metricGroupConf = this.plugin.getConfig().getConfigurationSection("metric-groups");
		Stream<String> updaterIds =
				updaterConf == null ? Stream.empty() :
						knownUpdaters.keySet().stream()
						             .filter(k -> updaterConf.getBoolean(k, false));
		Stream<String> metricIds =
				metricGroupConf == null ? Stream.empty() :
						knownMetricGroups.keySet().stream()
						                 .filter(k -> metricGroupConf.getBoolean(k, false));
		return Stream.concat(updaterIds, metricIds).toList();
	}

	/**
	 * Register updaters and metrics enabled in the server config and apply options.
	 * <p>
	 * This function assumes configuration has been reloaded from disk before here.
	 *
	 * @param p player who initiated reloading - null if invoked from program or command or entity
	 */
	public void reloadComponentsFromConfig(@Nullable Player p) {
		List<String> modulesToEnable = retrieveEnabledModules();

		unregisterAll();

		if (modulesToEnable.isEmpty()) {
			String message = "No module enabled?";
			if (p == null) {
				plugin.getLogger().warning(message);
			} else {
				p.sendMessage(message);
			}
		}

		configurationLock.lock();

		stopWithLock();

		long mInitFail = modulesToEnable
				.parallelStream()
				.filter(id -> { /* keep ones that failed to initialize */
					Class<? extends PluggableModule> cm = null;
					if (knownUpdaters.containsKey(id)) {
						cm = knownUpdaters.get(id);
					} else if (knownMetricGroups.containsKey(id)) {
						cm = knownMetricGroups.get(id);
					} else {
						plugin.getLogger().warning("Unknown module id: " + id);
						/* Failed to initialize, returning true */
						return true;
					}

					try {
						registerModuleWithLock(
								cm.getConstructor(PluginMain.class).newInstance(this.plugin));
					} catch (InstantiationException | IllegalAccessException |
					         InvocationTargetException |
					         NoSuchMethodException e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						String message = "Cannot instantiate updater id " + id + "\n" + sw;
						plugin.getLogger().severe(message);
						/* Cannot call constructor, returning true */
						return true;
					}

					/* Initialization successful */
					return false;
				})
				.count();

		if (mInitFail > 0) {
			this.plugin.complainToChatOrConsole(Level.WARNING, p, mInitFail + "modules failed to initialize.");
		}

		/* For enabled components call configure() */
		List<PluggableModule> failed =
				modules.values().stream()
				       .filter(m -> {
					       boolean ret = m.configure(retrieveConfigSection(m));
					       if (!ret) {
						       this.plugin.getLogger().severe("Cannot configure module " + m.id());
					       }
					       return !ret;
				       })
				       .toList(); /* Warn on stuff that failed configure() */
		failed.forEach(m -> {
			modules.remove(m.id());
		});
		if (!failed.isEmpty()) {
			this.plugin.complainToChatOrConsole(Level.WARNING, p, failed.size() + "modules failed to configure.");
		}
		modules.values().forEach(PluggableModule::start);

		/* Load global updater preference in `options.global` */
		{
			if (!plugin.getConfig().isInt("options.global.scrape-interval-ticks")) {
				plugin.getConfig().set("options.global.scrape-interval-ticks", 20);
			}
			this.updateIntervalTicks = plugin.getConfig().getInt("options.global.scrape-interval-ticks");
			String message = "Update Interval: every " + this.updateIntervalTicks + " ticks";
			plugin.getLogger().info(message);
		}

		startWithLock();

		configurationLock.unlock();
	}

	/**
	 * To be called synchronously - scape one round of update and append to sending queue.
	 */
	public void scrape() {
		configurationLock.lock();
		List<MetricEntry> entries = modules.values().stream()
		                                   .filter(MetricGroup.class::isInstance)
		                                   .map(MetricGroup.class::cast)
		                                   .map(MetricGroup::scrape) /* Collect stats for all registered metric */
		                                   .flatMap(List::stream) /* Merge the list together */
		                                   .toList();
		configurationLock.unlock();
		this.updateQueue.add(entries);
	}

	/**
	 * Called async or sync. Send updates to the server
	 */
	public void update() {
		Collection<MetricEntry> shard;
		while ((shard = this.updateQueue.poll()) != null) {
			Collection<MetricEntry> finalShard = shard;
			modules.values().stream()
			       .parallel()
			       .filter(Updater.class::isInstance)
			       .map(Updater.class::cast)
			       .map(e -> Map.entry(e, e.sendUpdates(finalShard))) /* Execute stat sender */
			       .filter(e -> !e.getValue()) /* Find ones that failed to update */
			       .forEach(e -> plugin.getLogger().warning(e.getKey().name() + " has failed!"));
		}
	}

	/**
	 * Repeating routine that generate one round of update.
	 */
	public Runnable getSyncScrapeTask() {
		class SyncScrapeTask implements Runnable {
			private final UpdaterManager manager;

			SyncScrapeTask(UpdaterManager manager) {
				this.manager = manager;
			}

			@Override
			public void run() {
				this.manager.scrape();
			}
		}
		return new SyncScrapeTask(this);
	}

	public Runnable getAsyncUpdateTask() {
		class AsyncUpdateTask implements Runnable {
			private final UpdaterManager manager;

			AsyncUpdateTask(UpdaterManager manager) {
				this.manager = manager;
			}

			@Override
			public void run() {
				this.manager.update();
			}
		}
		return new AsyncUpdateTask(this);
	}

	private void startWithLock() {
		if (scrapeTask == null) {
			scrapeTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this.getSyncScrapeTask(), 0, updateIntervalTicks);
		}
		if (updateTask == null) {
			updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, this.getAsyncUpdateTask(), 0 ,updateIntervalTicks);
		}
	}

	private void stopWithLock() {
		if (scrapeTask != null) {
			Bukkit.getScheduler().cancelTask(scrapeTask.getTaskId());
			scrapeTask = null;
		}
		if (updateTask != null) {
			Bukkit.getScheduler().cancelTask(updateTask.getTaskId());
			updateTask = null;
		}
	}

	public void start() {
		configurationLock.lock();
		startWithLock();
		configurationLock.unlock();
	}

	public void stop() {
		configurationLock.lock();
		stopWithLock();
		configurationLock.unlock();
	}
}
