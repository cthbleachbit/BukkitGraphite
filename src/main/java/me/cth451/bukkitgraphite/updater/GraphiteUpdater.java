package me.cth451.bukkitgraphite.updater;

import me.cth451.bukkitgraphite.PluginMain;
import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * Graphite plaintext TCP push protocol
 */
public class GraphiteUpdater extends Updater {
	public static String ID = "graphite";

	/**
	 * Prepare a list of entries for upload
	 *
	 * @param entryList metric entries to upload
	 * @return a string with multiple lines that can be submitted to a Graphite backend
	 */
	private String prepareEntriesForUpload(Collection<MetricEntry> entryList) {
		return String.join("\n",
		                   entryList.stream()
		                            .parallel()
		                            .map(e -> e.toGraphite(rootNamespace))
		                            .toList()
		);
	}

	/**
	 * Host / IP to find graphite service
	 */
	private String host;

	/**
	 * Port to find graphite service
	 */
	private int port;

	/**
	 * A namespace key to prepend onto all keys submitted
	 */
	private String rootNamespace;

	public GraphiteUpdater(PluginMain plugin) {
		super(plugin);
		this.host = null;
		this.port = 0;
		this.rootNamespace = "";
	}

	/**
	 * Set graphite endpoint this updater should report to
	 *
	 * @param host endpoint host
	 * @param port endpoint port
	 */
	public void setEndpoint(String host, int port) {
		this.host = host;
		this.port = port;

		if (host == null) {
			this.port = 0;
		} else if (port == 0) {
			this.host = null;
		}
	}

	/**
	 * Set root namespace that metric data points should be placed under
	 *
	 * @param namespace namespace - set null or empty to not use any enclosing namespace
	 */
	public void setRootNamespace(@Nullable String namespace) {
		this.rootNamespace = namespace;
	}

	@Override
	public boolean sendUpdates(@NotNull Collection<MetricEntry> entryList) {
		if (this.host == null || this.port == 0) {
			return true;
		}
		String payload = prepareEntriesForUpload(entryList);

		try {
			Socket sock = new Socket(host, port);
			sock.getOutputStream().write(payload.getBytes(StandardCharsets.UTF_8));
			sock.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	public @NotNull String name() {
		if (host == null || host.isEmpty() || port == 0) {
			return "Graphite Updater (no backend)";
		}
		return "Graphite Updater at " + host + ":" + port;
	}

	@Override
	public @NotNull String id() {
		return ID;
	}

	/**
	 * Load parameters for graphite logging service from configuration.
	 * <p>
	 * Here are the possible parameters:
	 * <ul>
	 *     <li>root-namespace: optional, a string for root namespace - omit this option to not use nested namespace</li>
	 *     <li>host: mandatory, graphite protocol host domain name / IP address</li>
	 *     <li>port: mandatory, graphite protocol host TCP port</li>
	 * </ul>
	 *
	 * @param section metric group specific configuration section
	 * @return true if configuration is successfully applied, false if configuration has missing mandatory keys
	 */
	@Override
	public boolean configure(ConfigurationSection section) {
		if (section == null) {
			this.host = null;
			this.port = 0;
			plugin.getLogger().warning(this.name() + ": No configuration found - check your config!");
			return false;
		}
		this.setRootNamespace(section.getString("root-namespace"));
		if (section.isString("host") && section.isInt("port")) {
			this.setEndpoint(section.getString("host"), section.getInt("port"));
			if (port <= 0 || port >= 65536) {
				plugin.getLogger().warning(this.name() + ": Invalid port specified - check your config!");
				this.setEndpoint(null, 0);
				return false;
			}
		}
		plugin.getLogger().info("Using graphite backend " + host + ":" + port + " with namespace " + rootNamespace);
		return true;
	}
}
