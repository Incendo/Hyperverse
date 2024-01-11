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

package org.incendo.hyperverse.events;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.modules.HyperEventFactory;
import org.incendo.hyperverse.world.HyperWorld;

@Singleton
public final class SimpleHyperEventFactory implements HyperEventFactory {

    private final PluginManager pluginManager;

    @Inject
    SimpleHyperEventFactory(final @NonNull PluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public void callWorldCreation(final @NonNull HyperWorld hyperWorld) {
        final Event event = new HyperWorldCreateEvent(hyperWorld);
        this.pluginManager.callEvent(event);
    }

    @Override
    public void callWorldDeletion(final @NonNull HyperWorld hyperWorld) {
        final Event event = new HyperWorldDeleteEvent(hyperWorld);
        this.pluginManager.callEvent(event);
    }

    @Override
    public @NonNull PlayerSeekSpawnEvent callPlayerSeekSpawn(
            final @NonNull Player player,
            final @NonNull HyperWorld world,
            final @NonNull Location respawnLocation
    ) {
        final PlayerSeekSpawnEvent playerSetSpawnEvent = new PlayerSeekSpawnEvent(player, world, respawnLocation);
        this.pluginManager.callEvent(playerSetSpawnEvent);
        return playerSetSpawnEvent;
    }

    @Override
    public @NonNull PlayerSetSpawnEvent callPlayerSetSpawn(final @NonNull Player player, final @NonNull HyperWorld world) {
        final PlayerSetSpawnEvent playerSetSpawnEvent = new PlayerSetSpawnEvent(player, world);
        this.pluginManager.callEvent(playerSetSpawnEvent);
        return playerSetSpawnEvent;
    }

}
