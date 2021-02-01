//
//
//  Hyperverse - A minecraft world management plugin
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package se.hyperver.hyperverse.util;

import com.bergerkiller.bukkit.mw.WorldConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.exception.HyperWorldValidationException;
import se.hyperver.hyperverse.flags.implementation.CreatureSpawnFlag;
import se.hyperver.hyperverse.flags.implementation.DifficultyFlag;
import se.hyperver.hyperverse.flags.implementation.ForceSpawn;
import se.hyperver.hyperverse.flags.implementation.GamemodeFlag;
import se.hyperver.hyperverse.flags.implementation.LocalRespawnFlag;
import se.hyperver.hyperverse.flags.implementation.MobSpawnFlag;
import se.hyperver.hyperverse.flags.implementation.PvpFlag;
import se.hyperver.hyperverse.modules.HyperWorldFactory;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.WorldConfiguration;
import se.hyperver.hyperverse.world.WorldConfigurationBuilder;
import se.hyperver.hyperverse.world.WorldFeatures;
import se.hyperver.hyperverse.world.WorldManager;
import se.hyperver.hyperverse.world.WorldType;

import java.util.Collection;
import java.util.UUID;

@Singleton
@SuppressWarnings("GuiceAssistedInjectScoping")
public final class MyWorldsImporter {

    private final WorldManager worldManager;
    private final HyperWorldFactory hyperWorldFactory;

    @Inject
    public MyWorldsImporter(
            final WorldManager worldManager,
            @Assisted final HyperWorldFactory hyperWorldFactory
    ) {
        this.worldManager = worldManager;
        this.hyperWorldFactory = hyperWorldFactory;
    }

    public void performImport(final @NonNull CommandSender sender) {
        Collection<WorldConfig> worlds = WorldConfig.all();
        MessageUtil
                .sendMessage(sender, Messages.messageImportPluginInitializing, "%plugin%", "My_Worlds",
                        "%worlds%", String.valueOf(worlds.size())
                );
        for (WorldConfig config : worlds) {
            HyperWorld hyperWorld = this.worldManager.getWorld(config.worldname);
            final World bukkitWorld = config.getWorld();
            if (hyperWorld == null) {
                final WorldConfiguration configuration;
                if (bukkitWorld != null) {
                    configuration = WorldConfiguration.fromWorld(bukkitWorld);
                } else {
                    WorldConfigurationBuilder builder = WorldConfiguration.builder();
                    builder.setName(config.worldname);
                    if (config.getChunkGeneratorName() != null) {
                        builder = builder.setGenerator(config.getChunkGeneratorName());
                    }
                    if (config.worldmode.getEnvironment() != null) {
                        builder = builder
                                .setType(WorldType.fromBukkit(config.worldmode.getEnvironment()));
                        builder = builder.setWorldFeatures(
                                WorldFeatures.fromBukkitType(config.worldmode.getType()));
                    }
                    configuration = builder.createWorldConfiguration();
                }
                hyperWorld = this.hyperWorldFactory
                        .create(
                                bukkitWorld == null ? UUID.randomUUID() : bukkitWorld.getUID(),
                                configuration
                        );
                this.worldManager.addWorld(hyperWorld);
                if (config.spawnPoint != null) {
                    try {
                        hyperWorld.createBukkitWorld();
                        assert hyperWorld.getBukkitWorld() != null;
                        hyperWorld.getBukkitWorld()
                                .setSpawnLocation(config.spawnPoint.toLocation());
                    } catch (HyperWorldValidationException ex) {
                        ex.printStackTrace();
                        MessageUtil.sendMessage(sender, Messages.messageCreationUnknownFailure);
                        return;
                    }
                }
            }
            hyperWorld.setFlagInstance(
                    config.forcedRespawn ? ForceSpawn.FORCE_SPAWN_TRUE : ForceSpawn.FORCE_SPAWN_FALSE);
            if (config.gameMode != null) {
                hyperWorld.setFlagInstance(
                        GamemodeFlag.GAMEMODE_CREATIVE.createFlagInstance(config.gameMode));
            }
            hyperWorld.setFlagInstance(config.bedRespawnEnabled
                    ? LocalRespawnFlag.RESPAWN_TRUE
                    : LocalRespawnFlag.RESPAWN_FALSE);
            hyperWorld.setFlagInstance(config.pvp ? PvpFlag.PVP_FLAG_TRUE : PvpFlag.PVP_FLAG_FALSE);
            if (config.difficulty != null) {
                hyperWorld.setFlagInstance(
                        DifficultyFlag.DIFFICULTY_FLAG_NORMAL.createFlagInstance(config.difficulty));
            }
            if (config.spawnControl != null) {
                hyperWorld.setFlagInstance(config.spawnControl.getAnimals()
                        ? CreatureSpawnFlag.CREATURE_SPAWN_ALLOWED
                        : CreatureSpawnFlag.CREATURE_SPAWN_FORBIDDEN);
                hyperWorld.setFlagInstance(config.spawnControl.getMonsters()
                        ? MobSpawnFlag.MOB_SPAWN_ALLOWED
                        : MobSpawnFlag.MOB_SPAWN_FORBIDDEN);
            }
            MessageUtil.sendMessage(sender, Messages.messageExternalImportCompleted, "%world%",
                    config.worldname, "%plugin%", "My_Worlds"
            );
        }
        MessageUtil.sendMessage(sender, Messages.messageImportDone, "%plugin%", "My_Worlds");
    }

}
