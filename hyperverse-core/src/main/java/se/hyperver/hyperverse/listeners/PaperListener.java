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

package se.hyperver.hyperverse.listeners;

import com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent;
import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.flags.implementation.AdvancementFlag;
import se.hyperver.hyperverse.flags.implementation.CreatureSpawnFlag;
import se.hyperver.hyperverse.flags.implementation.MobSpawnFlag;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.WorldManager;

public final class PaperListener implements Listener {

    private final WorldManager worldManager;

    PaperListener(final @NonNull WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @EventHandler
    public void onEntityPreSpawn(final @NonNull PreCreatureSpawnEvent event) {
        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getSpawnLocation().getWorld());
        if (hyperWorld == null) {
            return;
        }
        if (hyperWorld.getFlag(CreatureSpawnFlag.class)) {
            return;
        }
        if (event.getReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }
        event.setCancelled(true);
        event.setShouldAbortSpawn(true);
    }

    @EventHandler
    public void onMobPreSpawn(final @NonNull PlayerNaturallySpawnCreaturesEvent event) {
        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getPlayer().getWorld());
        if (hyperWorld == null) {
            return;
        }
        if (hyperWorld.getFlag(MobSpawnFlag.class)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onAdvancementGrant(final @NonNull PlayerAdvancementCriterionGrantEvent event) {
        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getPlayer().getWorld());
        if (hyperWorld == null) {
            return;
        }
        if (hyperWorld.getFlag(AdvancementFlag.class)) {
            return;
        }
        event.setCancelled(true);
    }

}
