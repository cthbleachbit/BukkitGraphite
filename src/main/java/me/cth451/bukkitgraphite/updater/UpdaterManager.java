package me.cth451.bukkitgraphite.updater;

import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import me.cth451.bukkitgraphite.metric.model.MetricGroup;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UpdaterManager implements Runnable {

    private final Plugin plugin;
    private final List<Updater> updaters;
    private final List<MetricGroup> metricList;

    public UpdaterManager(Plugin plugin) {
        this.updaters = new LinkedList<>();
        this.metricList = new LinkedList<>();
        this.plugin = plugin;
    }

    public void registerUpdater(Updater updater) {
        Bukkit.getLogger().info("Registered updater: " + updater.name());
        this.updaters.add(updater);
    }

    public void registerMetric(MetricGroup metric) {
        Bukkit.getLogger().info("Registered metric: " + metric.name());
        this.metricList.add(metric);
    }

    @Override
    public void run() {
        List<MetricEntry> entries = metricList
                .stream()
                .map(MetricGroup::scrape) /* Collect stats for all registered metric */
                .flatMap(List::stream) /* Merge the list together */
                .toList();
        updaters.stream()
                .parallel()
                .map(e -> Map.entry(e, e.sendUpdates(entries))) /* Execute stat sender */
                .filter(e -> !e.getValue()) /* Find ones that failed to update */
                .forEach(e -> Bukkit.getLogger().warning(e.getKey().name() + " has failed!"));
    }
}
