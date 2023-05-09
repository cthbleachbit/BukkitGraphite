package me.cth451.bukkitgraphite.metric;

import com.google.common.collect.ImmutableMap;
import me.cth451.bukkitgraphite.PluginMain;
import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import me.cth451.bukkitgraphite.metric.model.MetricGroup;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Metric group tracking server login statistics: login counter, failed logins etc.
 * <p>
 * Output metric is parametrized by login results.
 *
 * @see PlayerLoginEvent.Result#values()
 */
public class ServerLoginMetric extends MetricGroup implements Listener {
	public static final String ID = "server-login";
	public static final String Name = "Server Login Attempt Counters";
	public final static String LOGIN = "server.login";
	public final static String PARAMETRIZE = "result";
	public final HashMap<PlayerLoginEvent.Result, Long> loginCounters = new HashMap<>();

	/**
	 * Constructor
	 *
	 * @param plugin constructing plugin
	 */
	public ServerLoginMetric(PluginMain plugin) {
		super(plugin);
		synchronized (this.loginCounters) {
			for (PlayerLoginEvent.Result r : PlayerLoginEvent.Result.values()) {
				this.loginCounters.put(r, 0L);
			}
		}
	}

	/**
	 * Increment counters for login attempts.
	 *
	 * @param event player login attempt outcome
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	private void loginEventHandler(PlayerLoginEvent event) {
		synchronized (this.loginCounters) {
			PlayerLoginEvent.Result r = event.getResult();
			this.loginCounters.put(r, (this.loginCounters.get(r) % Long.MAX_VALUE) + 1L);
		}
	}

	/**
	 * Initialize and register event handler
	 */
	@Override
	public void start() {
		plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
	}

	/**
	 * Reset counters and remove event handler
	 */
	@Override
	public void halt() {
		synchronized (this.loginCounters) {
			for (PlayerLoginEvent.Result r : PlayerLoginEvent.Result.values()) {
				this.loginCounters.put(r, 0L);
			}
		}
		PlayerLoginEvent.getHandlerList().unregister(this);
	}

	@Override
	public @NotNull List<MetricEntry> scrape() {
		List<MetricEntry> updates = new ArrayList<>(PlayerLoginEvent.Result.values().length);
		ImmutableMap<PlayerLoginEvent.Result, Long> copy;
		synchronized (this.loginCounters) {
			copy = ImmutableMap.copyOf(this.loginCounters);
		}
		for (PlayerLoginEvent.Result r : PlayerLoginEvent.Result.values()) {
			updates.add(new MetricEntry(LOGIN,
			                            Map.of(PARAMETRIZE, r.name().toLowerCase()),
			                            this.loginCounters.get(r)
			));
		}

		return updates;
	}

	@Override
	public double interval() {
		return 20;
	}

	@Override
	public @NotNull String name() {
		return Name;
	}

	@Override
	public @NotNull String id() {
		return ID;
	}
}
