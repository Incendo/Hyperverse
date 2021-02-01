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

package se.hyperver.hyperverse.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.world.HyperWorld;

/**
 * Called when a {@link org.bukkit.entity.Player} attempts to set their
 * spawn point using a bed in a {@link se.hyperver.hyperverse.world.HyperWorld}.
 * <p>
 * Cancelling this event will prevent Hyperverse from updating the spawn point.
 * This will not affect the vanilla spawn point, however. To do that, one would
 * need to interact with {@link org.bukkit.event.player.PlayerBedEnterEvent}
 */
public final class PlayerSetSpawnEvent extends HyperPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;

    private PlayerSetSpawnEvent(
            final @NonNull Player player,
            final @NonNull HyperWorld world
    ) {
        super(player, world);
        this.cancelled = false;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static @NonNull PlayerSetSpawnEvent callFor(
            final @NonNull Player player,
            final @NonNull HyperWorld world
    ) {
        final PlayerSetSpawnEvent playerSetSpawnEvent = new PlayerSetSpawnEvent(player, world);
        Bukkit.getServer().getPluginManager().callEvent(playerSetSpawnEvent);
        return playerSetSpawnEvent;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

}
