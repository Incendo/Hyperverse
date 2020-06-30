package se.hyperver.hyperverse.util;

import com.bergerkiller.bukkit.mw.WorldConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.flags.implementation.*;
import se.hyperver.hyperverse.modules.HyperWorldFactory;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.WorldConfiguration;
import se.hyperver.hyperverse.world.WorldManager;

import java.util.Collection;
import java.util.UUID;

@Singleton public class MyWorldsImporter {

    private final WorldManager worldManager;
    private final HyperWorldFactory hyperWorldFactory;

    @Inject public MyWorldsImporter(final WorldManager worldManager,
        @Assisted final HyperWorldFactory hyperWorldFactory) {
        this.worldManager = worldManager;
        this.hyperWorldFactory = hyperWorldFactory;
    }

    public void performImport(@NotNull final CommandSender sender) {
        Collection<WorldConfig> worlds = WorldConfig.all();
        MessageUtil
            .sendMessage(sender, Messages.messageImportPluginInitializing, "%plugin%", "MyWorlds", "%worlds%", String.valueOf(worlds.size()));
        for (WorldConfig config : worlds) {
            HyperWorld hyperWorld = this.worldManager.getWorld(config.worldname);
            final World bukkitWorld = config.getWorld();
            if (hyperWorld == null) {
                final WorldConfiguration configuration = WorldConfiguration.fromWorld(bukkitWorld);
                hyperWorld = this.hyperWorldFactory
                    .create(bukkitWorld == null ? UUID.randomUUID() : bukkitWorld.getUID(),
                        configuration);
                this.worldManager.addWorld(hyperWorld);
            }
            hyperWorld.setFlagInstance(
                config.forcedRespawn ? ForceSpawn.FORCE_SPAWN_TRUE : ForceSpawn.FORCE_SPAWN_FALSE);
            if (config.gameMode != null) {
                hyperWorld.setFlagInstance(GamemodeFlag.GAMEMODE_CREATIVE.createFlagInstance(config.gameMode));
            }
            hyperWorld.setFlagInstance(config.bedRespawnEnabled ?
                LocalRespawnFlag.RESPAWN_TRUE :
                LocalRespawnFlag.RESPAWN_FALSE);
            hyperWorld.setFlagInstance(config.pvp ? PvpFlag.PVP_FLAG_TRUE : PvpFlag.PVP_FLAG_FALSE);
            if (config.difficulty != null) {
                hyperWorld.setFlagInstance(DifficultyFlag.DIFFICULTY_FLAG_NORMAL.createFlagInstance(config.difficulty));
            }
            if (config.spawnControl != null) {
                hyperWorld.setFlagInstance(config.spawnControl.getAnimals() ?
                    CreatureSpawnFlag.CREATURE_SPAWN_ALLOWED :
                    CreatureSpawnFlag.CREATURE_SPAWN_FORBIDDEN);
                hyperWorld.setFlagInstance(config.spawnControl.getMonsters() ?
                    MobSpawnFlag.MOB_SPAWN_ALLOWED :
                    MobSpawnFlag.MOB_SPAWN_FORBIDDEN);
            }
            MessageUtil.sendMessage(sender, Messages.messageMultiverseImported, "%world%",
                config.worldname, "%plugin%", "MyWorlds");
        }
        MessageUtil.sendMessage(sender, Messages.messageImportDone, "%plugin%", "MyWorlds");
    }

}
