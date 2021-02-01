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

package se.hyperver.hyperverse.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.world.HyperWorld;

import java.util.Objects;

/**
 * Called when a {@link org.bukkit.entity.Player} is supposed to re-spawn in a
 * {@link se.hyperver.hyperverse.world.HyperWorld}, this is used to determine
 * their re-spawn location.
 * <p>
 * Cancelling this event means that Hyperverse ignores the event entirely,
 * and lets vanilla/other Bukkit plugins handle it
 * {@inheritDoc}
 */
public final class PlayerSeekSpawnEvent extends HyperPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private Location respawnLocation;

    private PlayerSeekSpawnEvent(
            final @NonNull Player player,
            final @NonNull HyperWorld world,
            final @NonNull Location respawnLocation
    ) {
        super(player, world);
        this.cancelled = false;
        this.respawnLocation = Objects.requireNonNull(respawnLocation, "respawn location");
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static @NonNull PlayerSeekSpawnEvent callFor(
            final @NonNull Player player,
            final @NonNull HyperWorld world,
            final @NonNull Location respawnLocation
    ) {
        final PlayerSeekSpawnEvent playerSetSpawnEvent = new PlayerSeekSpawnEvent(player, world, respawnLocation);
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

    /**
     * Get the location where the player is supposed to re-spawn
     *
     * @return Re-spawn location
     */
    public @NonNull Location getRespawnLocation() {
        return this.respawnLocation;
    }

    /**
     * Set the location where the player is supposed to respawn
     *
     * @param respawnLocation Re-spawn location
     */
    public void setRespawnLocation(final @NonNull Location respawnLocation) {
        this.respawnLocation = Objects.requireNonNull(respawnLocation);
    }

}
