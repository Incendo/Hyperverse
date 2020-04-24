<p align="center">
    <img height="256px" width="246px" src="https://plotsquared.com/assets/img/LogoMoonv2.png">
</p>

**Hyperverse is W.I.P!**

[![CodeFactor](https://www.codefactor.io/repository/github/sauilitired/hyperverse/badge)](https://www.codefactor.io/repository/github/sauilitired/hyperverse)

A Minecraft world management plugin for Bukkit 1.15.
More versions may come to be supported in the future.

## Features

Current Features:
- World creation 
- World importing (will automatically import worlds)
- World loading/unloading
- World teleportation
- World flags (gamemode, local-spawn, force-spawn, 
pve, pvp, world-permission, nether, end, profile-group, difficulty)
- World game rules
- Tab completed commands
- Persistent world locations
- Nether & end portal linking
- Per world player data
- Per world beds

Planned Features:
- More flags
- Portals, sign teleportation

## Links

### Downloads
- [Spigot](https://www.spigotmc.org/resources/hyperverse-w-i-p.77550/)
- [Development Builds](https://ci.athion.net/job/Hyperverse/)

### Documentation
- [Installation](https://github.com/Sauilitired/Hyperverse/wiki/Installation)
- [Commands](https://github.com/Sauilitired/Hyperverse/wiki/Commands)
- [Configuration](https://github.com/Sauilitired/Hyperverse/wiki/Configuration)
- [Flags and Game Rules](https://github.com/Sauilitired/Hyperverse/wiki/Flags-and-Game-Rules)
- [Linking Portals](https://github.com/Sauilitired/Hyperverse/wiki/Linking-Portals)
- [JavaDoc](https://plotsquared.com/docs/hyperverse/)

## Contributing

Contributions are very welcome. Some general contribution
guidelines can be found in [CONTRIBUTING.md](https://github.com/Sauilitired/Hyperverse/blob/master/CONTRIBUTING.md)


<p align="center">
<img src="https://bstats.org/signatures/bukkit/Hyperverse.svg" />
</p>

## Maven

```xml
<repositories>
    <repository>
        <id>intellectualsites-snapshots</id>
        <url>https://mvn.intellectualsites.com/content/repositories/snapshots</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
      <groupId>se.hyperver.hyperverse</groupId>
      <artifactId>Core</artifactId>
      <version>0.2.0-SNAPSHOT</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>se.hyperver.hyperverse</groupId>
      <artifactId>Core</artifactId>
      <version>0.2.0-SNAPSHOT</version>
      <classifier>javadoc</classifier>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>se.hyperver.hyperverse</groupId>
      <artifactId>Core</artifactId>
      <version>0.2.0-SNAPSHOT</version>
      <classifier>sources</classifier>
      <scope>provided</scope>
    </dependency>
</dependencies>
```
