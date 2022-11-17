# Feeding data into Graphite

Reference: https://graphite.readthedocs.io/en/latest/feeding-carbon.html

### Plain Text

```
PATH_COMPONENT := alpha+
TAG            := alpha+ '=' alphanumeric+
METRIC_PATH    := PATH_COMPONENT ('.' PATH_COMPONENT)* (';' TAG)*
STATS_LINE     := METRIC_PATH <SPACE> VALUE <SPACE> TIMESTAMP <LF> 
```

### Stats available from Minecraft server?

Assuming the following keys extend namespace `minecraft.`. This can be changed in config.

### Player activity under `player.`

| Key                                | What                                                       |
|------------------------------------|------------------------------------------------------------|
| `active;world=$world;gamemode=$gm` | Number of active players in `$world` under game mode `$gm` |
| `op`                               | Number of server operators online                          |

### Server performance statistics `server`

| Key                         | What                                                 |
|-----------------------------|------------------------------------------------------|
| `tps`                       | Ticks per second - averaged between report intervals |
| `mstp`                      | Milliseconds per tick                                |
| `entity;world=$world`       | Active number of entities loaded in `$world`         |
| `chunk.loaded;world=$world` | Active number of chunks loaded in `$world`           |
| `chunk.pinned;world=$world` | Active number of chunks pinned in `$world`           |