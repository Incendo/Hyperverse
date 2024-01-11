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

package org.incendo.hyperverse.util;

import com.bergerkiller.bukkit.mw.WorldConfig;
import com.bergerkiller.bukkit.mw.WorldConfigStore;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.exception.HyperWorldValidationException;
import org.incendo.hyperverse.flags.implementation.CreatureSpawnFlag;
import org.incendo.hyperverse.flags.implementation.DifficultyFlag;
import org.incendo.hyperverse.flags.implementation.GamemodeFlag;
import org.incendo.hyperverse.flags.implementation.LocalRespawnFlag;
import org.incendo.hyperverse.flags.implementation.MobSpawnFlag;
import org.incendo.hyperverse.flags.implementation.PvpFlag;
import org.incendo.hyperverse.modules.HyperWorldFactory;
import org.incendo.hyperverse.modules.WorldConfigurationFactory;
import org.incendo.hyperverse.world.HyperWorld;
import org.incendo.hyperverse.world.WorldConfiguration;
import org.incendo.hyperverse.world.WorldConfigurationBuilder;
import org.incendo.hyperverse.world.WorldFeatures;
import org.incendo.hyperverse.world.WorldManager;
import org.incendo.hyperverse.world.WorldType;

import java.util.Collection;
import java.util.UUID;

@SuppressWarnings("GuiceAssistedInjectScoping")
public final class MyWorldsImporter {

    private final WorldManager worldManager;
    private final HyperWorldFactory hyperWorldFactory;
    private final WorldConfigurationFactory worldConfigurationFactory;

    @Inject
    public MyWorldsImporter(
            final WorldManager worldManager,
            final WorldConfigurationFactory worldConfigurationFactory,
            @Assisted final HyperWorldFactory hyperWorldFactory
    ) {
        this.worldManager = worldManager;
        this.worldConfigurationFactory = worldConfigurationFactory;
        this.hyperWorldFactory = hyperWorldFactory;
    }

    public void performImport(final @NonNull CommandSender sender) {
        Collection<WorldConfig> worlds = WorldConfigStore.all();
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
                    configuration = this.worldConfigurationFactory.fromWorld(bukkitWorld);
                } else {
                    WorldConfigurationBuilder builder = this.worldConfigurationFactory.builder();
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
                if (config.getSpawnLocation() != null) {
                    try {
                        hyperWorld.createBukkitWorld();
                        assert hyperWorld.getBukkitWorld() != null;
                        hyperWorld.getBukkitWorld()
                                .setSpawnLocation(config.getSpawnLocation());
                    } catch (HyperWorldValidationException ex) {
                        ex.printStackTrace();
                        MessageUtil.sendMessage(sender, Messages.messageCreationUnknownFailure);
                        return;
                    }
                }
            }
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
