package me.cth451.bukkitgraphite.updater;

import me.cth451.bukkitgraphite.metric.model.MetricEntry;
import me.cth451.bukkitgraphite.metric.model.MetricPath;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GraphiteUpdater implements Updater {
    /**
     * Prepare a list of entries for upload
     * @param entryList metric entries to upload
     * @return a string with multiple lines that can be submitted to a Graphite backend
     */
    private static String prepareEntriesForUpload(List<MetricEntry> entryList) {
        return String.join("\n", entryList.stream().parallel().map(MetricEntry::toGraphite).toList());
    }

    public final String host;
    public final int port;

    public GraphiteUpdater(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean sendUpdates(List<MetricEntry> entryList) {
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
}
