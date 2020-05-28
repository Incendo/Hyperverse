<p align="center">
    <img height="256px" width="246px" src="https://plotsquared.com/assets/img/LogoMoonv2.png">
</p>

**Hyperverse is W.I.P!**

[![TeamCity](https://ci.intellectualsites.com/app/rest/builds/aggregated/strob:(buildType:(project:(id:Hyperverse_2)))/statusIcon.svg)]
[![CodeFactor](https://www.codefactor.io/repository/github/sauilitired/hyperverse/badge)](https://www.codefactor.io/repository/github/sauilitired/hyperverse) ![Java CI with Maven](https://github.com/Sauilitired/Hyperverse/workflows/Java%20CI%20with%20Maven/badge.svg)

A Minecraft world management plugin for Bukkit 1.15 (backported to 1.14).
More versions may come to be supported in the future.

## Features

Current Features:
- World creation 
- World importing (will automatically import worlds)
- World loading/unloading
- World teleportation
- World flags (gamemode, local-spawn, force-spawn, 
pve, pvp, world-permission, nether, end, profile-group, difficulty, creature-spawn, mob-spawn, respawn-world, ignore-beds, alias)
- World game rules
- Tab completed commands
- Persistent world locations
- Nether & end portal linking
- Per world player data
- Per world beds
- World regeneration

## Links


### Downloads
- [Spigot](https://www.spigotmc.org/resources/hyperverse-w-i-p.77550/)
- [Development Builds](https://ci.athion.net/job/Hyperverse/)
- [Issue Tracker](https://issues.intellectualsites.com/projects/58b45c18-71d9-4e6a-9095-68761926c007)

### Documentation
- [Wiki](https://wiki.intellectualsites.com/hyperverse/home)
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
      <version>0.6.0-SNAPSHOT</version>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>se.hyperver.hyperverse</groupId>
      <artifactId>Core</artifactId>
      <version>0.6.0-SNAPSHOT</version>
      <classifier>javadoc</classifier>
      <scope>provided</scope>
    </dependency>
    <dependency>
      <groupId>se.hyperver.hyperverse</groupId>
      <artifactId>Core</artifactId>
      <version>0.6.0-SNAPSHOT</version>
      <classifier>sources</classifier>
      <scope>provided</scope>
    </dependency>
</dependencies>
```

## API

A majority of the API is accessible using `Hyperverse.getApi()`. For example, creating
a world is easy as:

```java
WorldConfiguration worldConfiguration = WorldConfiguration.builder()
    .setName("your world").setType(WorldType.NETHER)
    .setWorldFeatures(WorldFeatures.FLATLAND).createWorldConfiguration();
try {
    HyperWorld world = Hyperverse.getApi().createWorld(worldConfiguration);
} catch (HyperWorldCreationException e) {
    e.printStackTrace();
}
```
