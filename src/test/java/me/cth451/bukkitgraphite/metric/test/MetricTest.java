package me.cth451.bukkitgraphite.metric.test;
import me.cth451.bukkitgraphite.metric.model.MetricPath;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MetricTest {
    @Test
    public void testNoTagMetricPath() {
        MetricPath path = new MetricPath("minecraft.server", null);
        assertEquals("minecraft.server", path.toGraphite());
    }

    @Test
    public void testTagMetricPath() {
        Map<String, String> tags = Map.ofEntries(
          Map.entry("world", "minecraft:overworld"),
          Map.entry("gamemode", "creative")
        );
        MetricPath path = new MetricPath("minecraft.player.active", tags);
        String possible1 = "minecraft.player.active;world=minecraft:overworld;gamemode=creative";
        String possible2 = "minecraft.player.active;gamemode=creative;world=minecraft:overworld";
        assertTrue(possible1.equals(path.toGraphite()) || possible2.equals(path.toGraphite()));
    }
}
