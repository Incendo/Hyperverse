//
// Hyperverse - A minecraft world management plugin
// Copyright © 2020 Alexander Söderberg (sauilitired@gmail.com)
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package com.intellectualsites.hyperverse.listeners;

import com.google.inject.Inject;
import com.intellectualsites.hyperverse.configuration.HyperConfiguration;
import com.intellectualsites.hyperverse.configuration.Messages;
import com.intellectualsites.hyperverse.util.MessageUtil;
import com.intellectualsites.hyperverse.world.HyperWorld;
import com.intellectualsites.hyperverse.world.SimpleWorldManager;
import com.intellectualsites.hyperverse.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

public class WorldListener implements Listener {

    private final SimpleWorldManager worldManager;
    private final HyperConfiguration hyperConfiguration;

    @Inject public WorldListener(final SimpleWorldManager worldManager,
        final HyperConfiguration hyperConfiguration) {
        this.worldManager = worldManager;
        this.hyperConfiguration = hyperConfiguration;
    }

    @EventHandler
    public void onWorldInit(final WorldInitEvent event) {
        final World world = event.getWorld();
        MessageUtil.sendMessage(Bukkit.getConsoleSender(), Messages.messageWorldLoadDetected,
            "%world%", world.getName());
        HyperWorld hyperWorld = this.worldManager.getWorld(world.getUID());
        if (hyperWorld != null ||
            (hyperWorld = this.worldManager.getWorld(world.getName())) != null) {
            if (hyperWorld.getBukkitWorld() != null) {
                hyperWorld.setBukkitWorld(world);
            }
        } else if (hyperConfiguration.shouldImportAutomatically()) {
            // Assume it's a non-vanilla world, but don't guess the generator
            // This can be done because if it's the default level, we don't have
            // any control over the generator, and otherwise we just take on
            // responsibility over the world
            final WorldManager.WorldImportResult result = this.worldManager.importWorld(world, false, null);
            if (result == WorldManager.WorldImportResult.SUCCESS) {
                MessageUtil.sendMessage(Bukkit.getConsoleSender(),
                    Messages.messageWorldImportedOnLoad, "%world%", world.getName(), "%generator%",
                    this.worldManager.getWorld(world.getUID()).getConfiguration().getGenerator());
            } else {
                MessageUtil.sendMessage(Bukkit.getConsoleSender(), Messages.messageWorldImportFailure,
                    "%world%", world.getName(), "%result%", result.getDescription());
            }
        }
    }

}
