package me.cth451.bukkitgraphite.updater;

import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import org.bukkit.Bukkit;

import java.util.List;

/**
 * Scrape and dump serialized stats to console
 */
public class ConsoleUpdater implements Updater {

    @Override
    public boolean sendUpdates(List<MetricEntry> entries) {
        entries.forEach(e -> Bukkit.getLogger().info(e.toString()));
        return true;
    }

    @Override
    public String name() {
        return "Server Console";
    }
}
