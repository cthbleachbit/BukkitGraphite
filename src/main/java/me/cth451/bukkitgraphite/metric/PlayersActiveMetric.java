package me.cth451.bukkitgraphite.metric;

import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import me.cth451.bukkitgraphite.metric.model.MetricGroup;
import me.cth451.bukkitgraphite.metric.model.MetricPath;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlayersActiveMetric implements MetricGroup {

	static MetricPath pathFromWorldAndGameMode(World w, GameMode gm) {
		return new MetricPath("player.active", Map.ofEntries(
				Map.entry("world", w.getName()),
				Map.entry("gamemode", gm.name())
		));
	}

	static MetricPath pathFromActivePlayer(Player p) {
		return pathFromWorldAndGameMode(p.getWorld(), p.getGameMode());
	}

	@Override
	public List<MetricEntry> scrape() {
		HashMap<MetricPath, List<Player>> stats = new HashMap<>();

		/* Account for online players */
		Map<MetricPath, Double> playerCount =
				Bukkit.getOnlinePlayers()
				      .stream()
				      .collect(
						      Collectors.groupingBy(
								      PlayersActiveMetric::pathFromActivePlayer,
								      Collectors.reducing(0.0d, p -> 1.0d, Double::sum)
						      )
				      );

		/* Account for empty entries */
		for (World w : Bukkit.getWorlds()) {
			for (GameMode gm : GameMode.values()) {
				MetricPath key = pathFromWorldAndGameMode(w, gm);
				if (!playerCount.containsKey(key)) {
					playerCount.put(key, 0d);
				}
			}
		}

		return playerCount.entrySet().stream().map(e -> new MetricEntry(e.getKey(), e.getValue())).toList();
	}

	@Override
	public double interval() {
		return 20;
	}

	@Override
	public String name() {
		return "Number of players active";
	}

	@Override
	public String id() {
		return "player_active";
	}

	/**
	 * This metric group does not have configurable elements.
	 * @param section   metric group specific configuration section
	 * @return true
	 */
	@Override
	public boolean configure(ConfigurationSection section) {
		return true;
	}
}
