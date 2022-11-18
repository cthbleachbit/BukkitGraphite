# BukkitGraphite: Graphite plaintext metrics agent for Bukkit

This plugin allows you to track server metrics with a monitoring service that supports Graphite plaintext data ingestion protocol. More protocols might be added later.

### Configuration guide

All the knobs and levers for this plugin can be controlled via `config.yml`. In-game op-only command `/graphite-reload` can be used to reload in-memory settings from file.
The configuration file will be created from the following default template if it doesn't exist.

```yaml
# Which updaters to enable
updaters:
  graphite: false
  console: false

# Which metric groups to enable
metric-groups:
  runtime: true
  player-active: true
  server-chunk-entity: true
  server-tps: true

# Options section
options:
  # Global updater options
  global:
    scrape-interval-ticks: 20
  # Knobs specific to updater backend
  updaters:
    graphite:
      # Graphite metric hierarchy. Keys will be derived by appending to this key
      root-namespace: ""
      # Graphite protocol server host domain name / IP address
      host: ""
      # Graphite protocol server host port
      port: 0
    # Console doesn't have configurable elements (for now)  
    console:
  # Metric groups don't have configurable elements (for now)
  metric-groups:
    runtime:
    player-active:
    server-chunk-entity:
    server-tps:
```