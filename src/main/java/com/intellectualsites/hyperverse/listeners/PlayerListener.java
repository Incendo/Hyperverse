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

import com.intellectualsites.hyperverse.flags.implementation.GamemodeFlag;
import com.intellectualsites.hyperverse.flags.implementation.LocalRespawnFlag;
import com.intellectualsites.hyperverse.world.HyperWorld;
import com.intellectualsites.hyperverse.world.WorldManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import javax.inject.Inject;

public class PlayerListener implements Listener {

    private final WorldManager worldManager;

    @Inject public PlayerListener(final WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @EventHandler
    public void onWorldChange(final PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        final HyperWorld hyperWorld = this.worldManager.getWorld(player.getWorld());
        if (hyperWorld == null) {
            return;
        }
        player.setGameMode(hyperWorld.getFlag(GamemodeFlag.class));
    }

    @EventHandler
    public void onRespawn(final PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final HyperWorld hyperWorld = this.worldManager.getWorld(player.getWorld());
        if (hyperWorld == null) {
            return;
        }
        if (hyperWorld.getFlag(LocalRespawnFlag.class)) {
            event.setRespawnLocation(hyperWorld.getSpawn());
        }
    }

}
