package se.hyperver.hyperverse.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import se.hyperver.hyperverse.world.HyperWorld;

import java.util.Objects;

/**
 * Called when a {@link org.bukkit.entity.Player} is supposed to re-spawn in a
 * {@link se.hyperver.hyperverse.world.HyperWorld}, this is used to determine
 * their re-spawn location.
 *
 * Cancelling this event means that Hyperverse ignores the event entirely,
 * and lets vanilla/other Bukkit plugins handle it
 * {@inheritDoc}
 */
public class PlayerSeekSpawnEvent extends HyperPlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private Location respawnLocation;

    private PlayerSeekSpawnEvent(@NotNull final Player player, @NotNull final HyperWorld world,
        @NotNull final Location respawnLocation) {
        super(player, world);
        this.cancelled = false;
        this.respawnLocation = Objects.requireNonNull(respawnLocation, "respawn location");
    }

    @SuppressWarnings("unused") public static HandlerList getHandlerList() {
        return handlers;
    }

    public static PlayerSeekSpawnEvent callFor(@NotNull final Player player, @NotNull final HyperWorld world,
        @NotNull final Location respawnLocation) {
        final PlayerSeekSpawnEvent playerSetSpawnEvent = new PlayerSeekSpawnEvent(player, world, respawnLocation);
        Bukkit.getServer().getPluginManager().callEvent(playerSetSpawnEvent);
        return playerSetSpawnEvent;
    }

    @Override @NotNull public HandlerList getHandlers() {
        return handlers;
    }

    @Override public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Get the location where the player is supposed to re-spawn
     *
     * @return Re-spawn location
     */
    @NotNull public Location getRespawnLocation() {
        return this.respawnLocation;
    }

    /**
     * Set the location where the player is supposed to respawn
     *
     * @param respawnLocation Re-spawn location
     */
    public void setRespawnLocation(@NotNull final Location respawnLocation) {
        this.respawnLocation = Objects.requireNonNull(respawnLocation);
    }

}
