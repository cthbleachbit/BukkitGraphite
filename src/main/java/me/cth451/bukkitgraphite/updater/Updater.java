package me.cth451.bukkitgraphite.updater;

import me.cth451.bukkitgraphite.PluggableModule;
import me.cth451.bukkitgraphite.PluginMain;
import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Bare minimum APIs defining interface to a logging protocol for use with some remote metric service
 */
public abstract class Updater extends PluggableModule {
	protected Updater(PluginMain plugin) {
		super(plugin);
	}

	/**
	 * Transmit a list of metrics to a remote logging service.
	 *
	 * @param entryList Collected metrics to send
	 * @return whether update to remote logging service
	 */
	public abstract boolean sendUpdates(@NotNull List<MetricEntry> entryList);

	@Override
	public abstract @NotNull String name();

	@Override
	public abstract @NotNull String id();

	@Override
	public String component() {
		return "updater";
	}

	/**
	 * Configure local parameters. Updater configurations should reside in options.updater.[id] and is presented via
	 * section. Updaters that require configuration should override this method.
	 *
	 * @param section updater specific configuration section
	 * @return whether configuration was successful.
	 */
	@Override
	public boolean configure(ConfigurationSection section) {
		return true;
	}

	@Override
	public void start() {}

	@Override
	public void halt() {}
}
