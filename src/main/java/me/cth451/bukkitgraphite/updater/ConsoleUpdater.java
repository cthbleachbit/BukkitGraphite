package me.cth451.bukkitgraphite.updater;

import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Scrape and dump serialized stats to console
 */
public class ConsoleUpdater implements Updater {

    @Override
    public boolean sendUpdates(@NotNull List<MetricEntry> entries) {
        entries.forEach(e -> Bukkit.getLogger().info(e.toString()));
        return true;
    }

    @Override
    public String name() {
        return "Server Console";
    }

    @Override
    public String id() {
        return "console";
    }

    /**
     * This updater doesn't have configurable options.
     * @param section   metric group specific configuration section
     * @return true
     */
    @Override
    public boolean configure(ConfigurationSection section) {
        return true;
    }
}
