package me.cth451.bukkitgraphite.metric.model;

import me.cth451.bukkitgraphite.PluggableModule;
import me.cth451.bukkitgraphite.PluginMain;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Base class describing a bunch of metric keys logically collected and grouped together
 */
public abstract class MetricGroup extends PluggableModule {
	protected MetricGroup(PluginMain plugin) {
		super(plugin);
	}

	/**
	 * Reap current sensors value into a list of entries.
	 *
	 * @return sensors reaped
	 */
	public abstract @NotNull List<MetricEntry> scrape();

	/**
	 * Polling interval in ticks. This should be a constant value
	 *
	 * @return polling interval
	 */
	public abstract double interval();

	@Override
	public abstract @NotNull String name();

	@Override
	public abstract @NotNull String id();

	@Override
	public String component() {
		return "metric-group";
	}

	/**
	 * Configure local parameters. Metric group configurations should reside in options.metric-group.[id] and is
	 * presented via section. Metric group that requires configuration should override this method.
	 *
	 * @param section metric group specific configuration section
	 * @return whether configuration was successful.
	 */
	@Override
	public boolean configure(ConfigurationSection section) {
		return true;
	}

	@Override
	public void start() {
	}

	@Override
	public void halt() {
	}
}