package me.cth451.bukkitgraphite.updater;

import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Prometheus node exporter
 */
public class PrometheusNodeExporter extends Updater {

	public static String ID = "prometheus-node-exporter";

	/**
	 * Prometheus exporter address
	 */
	private InetAddress listenAddr = null;

	/**
	 * Prometheus exporter port
	 */
	private int listenPort = 0;

	/**
	 * A namespace key to prepend onto all keys submitted
	 */
	private String rootNamespace = null;

	/**
	 * Internal state - send update will send stats here, and collected in one shot during remote pull
	 */
	private ConcurrentLinkedQueue<MetricEntry> stagingMetric;

	protected PrometheusNodeExporter(Plugin plugin) {
		super(plugin);
		this.stagingMetric = new ConcurrentLinkedQueue<>();
	}

	/**
	 * Set root namespace that metric data points should be placed under
	 *
	 * @param namespace namespace - set null or empty to not use any enclosing namespace
	 */
	public void setRootNamespace(@Nullable String namespace) {
		this.rootNamespace = namespace;
	}

	/**
	 * Set endpoint this updater should listen on
	 *
	 * @param address endpoint listen address
	 * @param port    endpoint port
	 */
	public void setEndpoint(InetAddress address, int port) {
		this.listenAddr = address;
		this.listenPort = port;

		if (address == null) {
			this.listenPort = 0;
		} else if (port == 0) {
			this.listenAddr = null;
		}
	}

	/**
	 * Deposit stats into queue
	 * @param entryList Collected metrics to send
	 * @return whether enqueue is successful.
	 */
	@Override
	boolean sendUpdates(@NotNull List<MetricEntry> entryList) {
		return this.stagingMetric.addAll(entryList);
	}

	@Override
	@NotNull String name() {
		if (listenAddr == null || listenPort == 0) {
			return "Prometheus exporter (inactive - no backend)";
		}
		return "Prometheus exporter listening at " + listenAddr + ":" + listenPort;
	}

	@Override
	@NotNull String id() {
		return ID;
	}

	/**
	 * Reconfigure internal options
	 * @param section   metric group specific configuration section
	 * @return true iff all required keys are found and applied
	 */
	@Override
	boolean configure(ConfigurationSection section) {
		if (section == null) {
			this.listenAddr = null;
			this.listenPort = 0;
			plugin.getLogger().warning(this.name() + ": No configuration found - check your config!");
			return false;
		}
		this.setRootNamespace(section.getString("root-namespace"));
		if (section.isString("listen-addr") && section.isInt("port")) {
			/* TODO: validate endpoint settings */
			/* TODO: call setEndpoint()*/
		}
		return true;
	}

	/**
	 * Start background listening socket and event handler
	 * TODO
	 */
	@Override
	void start() {

	}

	/**
	 * Close listening socket and Wait for event handlers to complete
	 * TODO
	 */
	@Override
	void halt() {

	}
}
