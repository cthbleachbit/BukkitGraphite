package me.cth451.bukkitgraphite.metric;

import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import me.cth451.bukkitgraphite.metric.model.MetricGroup;
import org.bukkit.Bukkit;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Load entity / chunks per world
 */
public class ServerLoadedMetric implements MetricGroup {

	public final static String ENTITY_LOADED = "server.entity";
	public final static String CHUNK_LOADED = "server.chunk.loaded";
	public final static String CHUNK_PINNED = "server.chunk.pinned";

	private static MetricEntry assemble(String key, String worldName, int value) {
		return new MetricEntry(key, Map.of("world", worldName), (double) value);
	}

	@Override
	public List<MetricEntry> scrape() {
		List<MetricEntry> results = new LinkedList<>();
		Stream<MetricEntry> entityCount =
				Bukkit.getWorlds()
				      .stream()
				      .map(w -> assemble(ENTITY_LOADED, w.getName(), w.getEntities().size()));
		Stream<MetricEntry> chunkCount =
				Bukkit.getWorlds()
				      .stream()
				      .map(w -> assemble(CHUNK_LOADED, w.getName(), w.getLoadedChunks().length));
		Stream<MetricEntry> pinnedChunkCount =
				Bukkit.getWorlds()
				      .stream()
				      .map(w -> assemble(CHUNK_PINNED, w.getName(), w.getForceLoadedChunks().size()));
		results.addAll(entityCount.toList());
		results.addAll(chunkCount.toList());
		results.addAll(pinnedChunkCount.toList());
		return results;
	}

	@Override
	public double interval() {
		return 20;
	}

	@Override
	public String name() {
		return "Loaded entity / chunks";
	}

	@Override
	public String id() {
		return "server_chunk_entity";
	}
}
