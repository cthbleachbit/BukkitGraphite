package me.cth451.bukkitgraphite.metric;

import me.cth451.bukkitgraphite.PluginMain;
import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import me.cth451.bukkitgraphite.metric.model.MetricGroup;
import me.cth451.bukkitgraphite.metric.model.MetricPath;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayersActiveMetric extends MetricGroup {
	public static String ID = "player-active";

	public PlayersActiveMetric(PluginMain plugin) {
		super(plugin);
	}

	private static MetricPath pathFromWorldAndGameMode(String key, World w, GameMode gm) {
		return new MetricPath(key, Map.ofEntries(
				Map.entry("world", w.getName()),
				Map.entry("gamemode", gm.name())
		));
	}

	private static MetricPath pathFromActivePlayer(String key, Player p) {
		return pathFromWorldAndGameMode(key, p.getWorld(), p.getGameMode());
	}

	@Override
	public @NotNull List<MetricEntry> scrape() {
		/* Account for online players */
		Map<MetricPath, Double> playerCount =
				Bukkit.getOnlinePlayers()
				      .stream()
				      .collect(
						      Collectors.groupingBy(
								      p -> pathFromActivePlayer("player.active", p),
								      Collectors.reducing(0.0d, p -> 1.0d, Double::sum)
						      )
				      );

		/* Account for empty entries */
		for (World w : Bukkit.getWorlds()) {
			for (GameMode gm : GameMode.values()) {
				MetricPath key = pathFromWorldAndGameMode("player.active", w, gm);
				if (!playerCount.containsKey(key)) {
					playerCount.put(key, 0d);
				}
			}
		}

		/* Count number of server operators online */
		long ops = Bukkit.getOnlinePlayers().stream().filter(Player::isOp).count();
		playerCount.put(new MetricPath("player.op", null), (double) ops);

		return playerCount.entrySet().stream()
		                  .map(e -> new MetricEntry(e.getKey(), e.getValue()))
		                  .toList();
	}

	@Override
	public double interval() {
		return 20;
	}

	@Override
	public @NotNull String name() {
		return "Number of players active";
	}

	@Override
	public @NotNull String id() {
		return ID;
	}

	/**
	 * This metric group does not have configurable elements.
	 *
	 * @param section metric group specific configuration section
	 * @return true
	 */
	@Override
	public boolean configure(ConfigurationSection section) {
		return true;
	}
}
