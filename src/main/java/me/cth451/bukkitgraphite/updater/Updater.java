package me.cth451.bukkitgraphite.updater;

import me.cth451.bukkitgraphite.metric.model.MetricEntry;

import java.util.List;

public interface Updater {
    public boolean sendUpdates(List<MetricEntry> entryList);
    public String name();
}
