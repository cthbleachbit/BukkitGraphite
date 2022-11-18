package me.cth451.bukkitgraphite.updater;

import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import me.cth451.bukkitgraphite.metric.model.MetricPath;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GraphiteUpdater extends Updater {
	public static String ID = "graphite";

	/**
	 * Prepare a list of entries for upload
	 *
	 * @param entryList metric entries to upload
	 * @return a string with multiple lines that can be submitted to a Graphite backend
	 */
	private String prepareEntriesForUpload(List<MetricEntry> entryList) {
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


	public GraphiteUpdater(Plugin plugin) {
		super(plugin);
		this.host = null;
		this.port = 0;
		rootNamespace = "";
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

	public void setRootNamespace(String namespace) {
		this.rootNamespace = namespace;
	}

	@Override
	public boolean sendUpdates(@NotNull List<MetricEntry> entryList) {
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
	public String name() {
		return "Graphite Updater at " + host + ":" + port;
	}

	@Override
	public String id() {
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
			plugin.getLogger().warning(this.name() + ": No configuration specified - updater disabled");
			return false;
		}
		this.setRootNamespace(section.getString("root-namespace"));
		if (section.isString("host") && section.isInt("port")) {
			this.setEndpoint(section.getString("host"), section.getInt("port"));
			if (port <= 0 || port >= 65536) {
				port = 0;
				host = "";
			}
		}
		plugin.getLogger().info("Using graphite backend " + host + ":" + port);
		return true;
	}
}
