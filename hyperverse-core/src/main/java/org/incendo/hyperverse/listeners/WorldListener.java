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

package org.incendo.hyperverse.listeners;

import com.google.inject.Inject;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.configuration.HyperConfiguration;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.flags.implementation.SaveWorldFlag;
import org.incendo.hyperverse.util.MessageUtil;
import org.incendo.hyperverse.world.HyperWorld;
import org.incendo.hyperverse.world.SimpleWorldManager;
import org.incendo.hyperverse.world.WorldManager;

import java.util.Objects;

public final class WorldListener implements Listener {

    private final SimpleWorldManager worldManager;
    private final HyperConfiguration hyperConfiguration;
    private final Server server;

    @Inject
    public WorldListener(
            final @NonNull SimpleWorldManager worldManager,
            final @NonNull HyperConfiguration hyperConfiguration,
            final @NonNull Server server
    ) {
        this.server = server;
        this.worldManager = worldManager;
        this.hyperConfiguration = hyperConfiguration;
    }

    @EventHandler
    public void onWorldInit(final @NonNull WorldInitEvent event) {
        final World world = event.getWorld();
        // We ignore our own world loads
        if (this.worldManager.shouldIgnore(world.getName())) {
            return;
        }
        MessageUtil.sendMessage(this.server.getConsoleSender(),
                Messages.messageWorldLoadDetected, "%world%", world.getName()
        );
        HyperWorld hyperWorld = this.worldManager.getWorld(world);
        if (hyperWorld != null
                || (hyperWorld = this.worldManager.getWorld(world.getName())) != null) {
            if (hyperWorld.getBukkitWorld() != null) {
                hyperWorld.setBukkitWorld(world);
            }
            world.setKeepSpawnInMemory(hyperWorld.shouldKeepSpawnLoaded());
        } else if (this.hyperConfiguration.shouldImportAutomatically()) {
            // Assume it's a non-vanilla world, but don't guess the generator
            // This can be done because if it's the default level, we don't have
            // any control over the generator, and otherwise we just take on
            // responsibility over the world
            final WorldManager.WorldImportResult result =
                    this.worldManager.importWorld(world, false, null);
            if (result == WorldManager.WorldImportResult.SUCCESS) {
                MessageUtil
                        .sendMessage(this.server.getConsoleSender(), Messages.messageWorldImportedOnLoad,
                                "%world%", world.getName(), "%generator%",
                                Objects.requireNonNull(this.worldManager.getWorld(world)).getConfiguration()
                                        .getGenerator()
                        );
                if ((hyperWorld = this.worldManager.getWorld(world)) != null) {
                    world.setKeepSpawnInMemory(hyperWorld.shouldKeepSpawnLoaded());
                } else if (this.hyperConfiguration.shouldKeepSpawnLoaded()) {
                    world.setKeepSpawnInMemory(true);
                }
            } else {
                MessageUtil
                        .sendMessage(this.server.getConsoleSender(), Messages.messageWorldImportFailure,
                                "%world%", world.getName(), "%result%", result.getDescription()
                        );
            }
        }
    }

    @EventHandler
    public void onChunkUnload(final @NonNull ChunkUnloadEvent event) {
        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getWorld());

        if (hyperWorld != null) {
            event.setSaveChunk(hyperWorld.getFlag(SaveWorldFlag.class));
        }
    }

}
