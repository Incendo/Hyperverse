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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.Ambient;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Boss;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Monster;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Slime;
import org.bukkit.entity.WaterMob;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.Hyperverse;
import se.hyperver.hyperverse.configuration.HyperConfiguration;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.database.HyperDatabase;
import se.hyperver.hyperverse.database.LocationType;
import se.hyperver.hyperverse.database.PersistentLocation;
import se.hyperver.hyperverse.events.PlayerSeekSpawnEvent;
import se.hyperver.hyperverse.events.PlayerSetSpawnEvent;
import se.hyperver.hyperverse.flags.implementation.CreatureSpawnFlag;
import se.hyperver.hyperverse.flags.implementation.EndFlag;
import se.hyperver.hyperverse.flags.implementation.GamemodeFlag;
import se.hyperver.hyperverse.flags.implementation.LocalRespawnFlag;
import se.hyperver.hyperverse.flags.implementation.MobSpawnFlag;
import se.hyperver.hyperverse.flags.implementation.NetherFlag;
import se.hyperver.hyperverse.flags.implementation.ProfileGroupFlag;
import se.hyperver.hyperverse.flags.implementation.PveFlag;
import se.hyperver.hyperverse.flags.implementation.PvpFlag;
import se.hyperver.hyperverse.flags.implementation.RespawnWorldFlag;
import se.hyperver.hyperverse.util.MessageUtil;
import se.hyperver.hyperverse.util.NMS;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.WorldManager;
import se.hyperver.hyperverse.world.WorldType;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public final class EventListener implements Listener {

    private final Cache<UUID, Long> teleportationTimeout =
            CacheBuilder.newBuilder().expireAfterWrite(5L, TimeUnit.SECONDS).build();
    private final WorldManager worldManager;
    private final HyperDatabase hyperDatabase;
    private final HyperConfiguration hyperConfiguration;
    private final Hyperverse hyperverse;
    private final NMS nms;

    @Inject
    public EventListener(
            final @NonNull WorldManager worldManager,
            final @NonNull HyperDatabase hyperDatabase,
            final @NonNull HyperConfiguration hyperConfiguration,
            final @NonNull Hyperverse hyperverse,
            final @NonNull NMS nms
    ) {
        this.worldManager = worldManager;
        this.hyperDatabase = hyperDatabase;
        this.hyperConfiguration = hyperConfiguration;
        this.hyperverse = hyperverse;
        this.nms = nms;
        // Register pre-spawn listeners
        if (PaperLib.isPaper()) {
            Bukkit.getPluginManager()
                    .registerEvents(new PaperListener(this.worldManager), hyperverse);
        }
    }

    /**
     * Whether or not mob spawn should be cancelled
     *
     * @param world  World
     * @param entity Entity
     * @return {@code false} if the entity should be allowed to spawn, else {@code false}
     */
    public static boolean shouldCancelSpawn(
            final @NonNull HyperWorld world,
            final @NonNull Entity entity
    ) {
        if (!world.getFlag(CreatureSpawnFlag.class)) {
            return entity instanceof IronGolem || entity instanceof Animals
                    || entity instanceof WaterMob || entity instanceof Ambient || entity instanceof NPC;
        }
        if (!world.getFlag(MobSpawnFlag.class)) {
            return entity instanceof Shulker || entity instanceof Monster || entity instanceof Boss
                    || entity instanceof Slime || entity instanceof Ghast || entity instanceof Phantom
                    || entity instanceof EnderCrystal;
        }
        return false;
    }

    @EventHandler
    public void onPlayerLogin(final @NonNull AsyncPlayerPreLoginEvent event) {
        if (this.hyperConfiguration.shouldPersistLocations()) {
            try {
                this.hyperDatabase.getLocations(event.getUniqueId()).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onTeleport(final @NonNull PlayerTeleportEvent event) {
        if (this.hyperConfiguration.shouldPersistLocations()) {
            final Location from = event.getFrom();
            final Location to = event.getTo();
            if (Objects.equals(from.getWorld(), Objects.requireNonNull(to).getWorld())) {
                // The player was moving inside of the world, so we don't
                // need to update the location
                return;
            }
            // The player moved between two different worlds, so we
            // need to update
            final UUID uuid = event.getPlayer().getUniqueId();
            this.hyperDatabase.storeLocation(PersistentLocation.fromLocation(uuid, from,
                    LocationType.PLAYER_LOCATION
            ), true, false);
            this.hyperDatabase.storeLocation(PersistentLocation.fromLocation(uuid, to,
                    LocationType.PLAYER_LOCATION
            ), true, false);

            if (this.hyperConfiguration.shouldGroupProfiles()) {
                final HyperWorld hyperWorld = this.worldManager.getWorld(from.getWorld());
                if (hyperWorld != null) {
                    final Path oldWorldDirectory =
                            this.hyperverse.getDataFolder().toPath().resolve("profiles")
                                    .resolve(hyperWorld.getFlag(ProfileGroupFlag.class));
                    if (!Files.exists(oldWorldDirectory)) {
                        try {
                            Files.createDirectories(oldWorldDirectory);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    this.nms.writePlayerData(event.getPlayer(), oldWorldDirectory.resolve(
                            String.format("%s.nbt", event.getPlayer().getUniqueId().toString())));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(final @NonNull PlayerQuitEvent event) {
        if (this.hyperConfiguration.shouldPersistLocations()) {
            // Persist the locations when the player quits
            final UUID uuid = event.getPlayer().getUniqueId();
            this.hyperDatabase.storeLocation(
                    PersistentLocation.fromLocation(uuid, event.getPlayer().getLocation(),
                            LocationType.PLAYER_LOCATION
                    ), false,
                    true
            );
            this.hyperDatabase.clearLocations(uuid);
        }
    }

    @EventHandler
    public void onWorldChange(final @NonNull PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        final HyperWorld hyperWorld = this.worldManager.getWorld(player.getWorld());
        if (hyperWorld == null) {
            return;
        }
        if (this.hyperConfiguration.shouldGroupProfiles()) {
            final HyperWorld from = this.worldManager.getWorld(event.getFrom());
            // Only load player data if the worlds belong to different groups
            if (from == null || !from.getFlag(ProfileGroupFlag.class)
                    .equals(hyperWorld.getFlag(ProfileGroupFlag.class))) {
                final Path newWorldDirectory =
                        this.hyperverse.getDataFolder().toPath().resolve("profiles")
                                .resolve(hyperWorld.getFlag(ProfileGroupFlag.class));
                if (!Files.exists(newWorldDirectory)) {
                    try {
                        Files.createDirectories(newWorldDirectory);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                final Path playerData = newWorldDirectory
                        .resolve(String.format("%s.nbt", player.getUniqueId().toString()));
                if (Files.exists(playerData)) {
                    final GameMode originalGameMode = player.getGameMode();
                    this.nms.readPlayerData(event.getPlayer(), playerData,
                            () -> Bukkit.getScheduler().runTaskLater(this.hyperverse, () -> {
                                // We need to trick Bukkit into updating the gamemode
                                final GameMode worldGameMode = hyperWorld.getFlag(GamemodeFlag.class);
                                if (worldGameMode != GameMode.ADVENTURE) {
                                    player.setGameMode(GameMode.ADVENTURE);
                                } else {
                                    player.setGameMode(GameMode.SURVIVAL);
                                }
                                player.setGameMode(GameMode.SPECTATOR);
                                if (!this.setDefaultGameMode(player, hyperWorld)) {
                                    player.setGameMode(originalGameMode);
                                }
                                // Apply any other flags here
                            }, 1L)
                    );
                } else {
                    // The player has no stored data. Reset everything
                    player.setBedSpawnLocation(player.getWorld().getSpawnLocation(), true);
                    player.getInventory().clear();
                    player.getEnderChest().clear();
                    player.setTotalExperience(0);
                    for (final PotionEffectType potionEffectType : PotionEffectType.values()) {
                        player.removePotionEffect(potionEffectType);
                    }
                    player.setVelocity(new Vector(0, 0, 0));
                    player.setTicksLived(1);
                    player.setFireTicks(1);
                    player.getInventory().setHeldItemSlot(0);
                    for (final Attribute attribute : Attribute.values()) {
                        final AttributeInstance attributeInstance = player.getAttribute(attribute);
                        if (attributeInstance != null) {
                            attributeInstance.setBaseValue(attributeInstance.getDefaultValue());
                            for (final AttributeModifier attributeModifier : attributeInstance
                                    .getModifiers()) {
                                attributeInstance.removeModifier(attributeModifier);
                            }
                        }
                    }
                    this.setDefaultGameMode(player, hyperWorld);
                }
            }
        } else {
            this.setDefaultGameMode(player, hyperWorld);
        }
    }

    private boolean setDefaultGameMode(final @NonNull Player player, final @NonNull HyperWorld world) {
        if (player.hasPermission("hyperverse.override.gamemode")) {
            if (world.getFlag(GamemodeFlag.class) != player.getGameMode()) {
                MessageUtil.sendMessage(player, Messages.messageGameModeOverride, "%mode%",
                        world.getFlag(GamemodeFlag.class).name().toLowerCase()
                );
            }
            return false;
        }
        player.setGameMode(world.getFlag(GamemodeFlag.class));
        return true;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRespawn(final @NonNull PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final HyperWorld hyperWorld = this.worldManager.getWorld(player.getWorld());
        if (hyperWorld == null) {
            return;
        }

        if (hyperWorld.getConfiguration().getType() == WorldType.END
                && event.getPlayer().getLocation().getBlock().getType() == Material.END_PORTAL) {
            final Location destination =
                    hyperWorld.getTeleportationManager().endDestination(event.getPlayer());
            if (destination != null) {
                final boolean allowedEntry = hyperWorld.getTeleportationManager()
                        .allowedTeleport(event.getPlayer(), destination).getNow(false);
                if (!allowedEntry) {
                    MessageUtil.sendMessage(event.getPlayer(), Messages.messageNotPermittedEntry);
                } else {
                    event.setRespawnLocation(destination);
                }
            }
            return;
        }

        Location spawnLocation = event.getRespawnLocation();

        if (hyperWorld.getFlag(LocalRespawnFlag.class)) {
            spawnLocation = hyperWorld.getTeleportationManager().getSpawnLocation(player, hyperWorld);
        } else if (!hyperWorld.getFlag(RespawnWorldFlag.class).isEmpty()) {
            final HyperWorld respawnWorld = this.worldManager.getWorld(hyperWorld.getFlag(RespawnWorldFlag.class));
            if (respawnWorld != null) {
                spawnLocation = respawnWorld.getTeleportationManager().getSpawnLocation(player, respawnWorld);
            } else {
                MessageUtil.sendMessage(player, Messages.messageRespawnWorldNonExistent);
            }
        }

        final PlayerSeekSpawnEvent seekSpawnEvent = PlayerSeekSpawnEvent.callFor(player, hyperWorld, spawnLocation);
        if (seekSpawnEvent.isCancelled()) {
            return;
        }

        event.setRespawnLocation(seekSpawnEvent.getRespawnLocation());
    }

    @EventHandler
    public void onEntityDamageEvent(final @NonNull EntityDamageByEntityEvent event) {
        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getEntity().getWorld());
        if (hyperWorld == null) {
            return;
        }
        final Entity first = event.getEntity();
        final Entity second = event.getDamager();
        if (first.getType() == EntityType.PLAYER || second.getType() == EntityType.PLAYER) {
            if (first.getType() == second.getType()) {
                if (!hyperWorld.getFlag(PvpFlag.class)) {
                    event.setCancelled(true);
                }
            } else {
                if (!hyperWorld.getFlag(PveFlag.class)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerPortalEvent(final @NonNull PlayerPortalEvent event) {
        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getPlayer().getWorld());
        if (hyperWorld == null) {
            this.hyperverse.getLogger().warning(String.format(
                    "(PlayerPortalEvent) Player %s entered world '%s' but no world could be found",
                    event.getPlayer().getName(), event.getPlayer().getWorld().getName()
            ));
            return;
        }

        final Long lastTeleportion =
                this.teleportationTimeout.getIfPresent(event.getPlayer().getUniqueId());
        if (lastTeleportion != null && (System.currentTimeMillis() - lastTeleportion) < 5000L) {
            event.setCancelled(true);
            return;
        }

        final Location destination;
        final boolean isNether;

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            destination = hyperWorld.getTeleportationManager()
                    .netherDestination(event.getPlayer(), event.getFrom());
            isNether = true;
        } else if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            Location portalLocation;
            final Location current = event.getFrom();
            final DragonBattle battle = current.getWorld().getEnderDragonBattle();
            if (battle != null && (portalLocation = battle.getEndPortalLocation()) != null) {
                current.clone().setY(portalLocation.getY());
                if (portalLocation.distanceSquared(current) > 9) {
                    return;
                }
            }
            destination = hyperWorld.getTeleportationManager().endDestination(event.getPlayer());
            isNether = false;
        } else {
            return;
        }

        if (destination != null) {
            final boolean allowedEntry =
                    hyperWorld.getTeleportationManager().allowedTeleport(event.getPlayer(), destination)
                            .getNow(false);
            if (!allowedEntry) {
                MessageUtil.sendMessage(event.getPlayer(), Messages.messageNotPermittedEntry);
            } else {
                event.setTo(destination);
                this.teleportationTimeout
                        .put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
            }
        } else {
            final String flag =
                    isNether ? hyperWorld.getFlag(NetherFlag.class) : hyperWorld.getFlag(EndFlag.class);
            if (!flag.isEmpty()) {
                event.setCancelled(
                        true); // We do not want to allow default teleportation unless it has
                // been configured
                MessageUtil.sendMessage(event.getPlayer(), Messages.messagePortalNotLinked);
            }
        }
    }

    @EventHandler
    public void onEntityPortalEnter(final @NonNull EntityPortalEnterEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            return;
        }

        final HyperWorld hyperWorld =
                this.worldManager.getWorld(Objects.requireNonNull(event.getLocation().getWorld()));
        if (hyperWorld == null) {
            return;
        }

        final Long lastTeleportion =
                this.teleportationTimeout.getIfPresent(event.getEntity().getUniqueId());
        if (lastTeleportion != null && (System.currentTimeMillis() - lastTeleportion) < 5000L) {
            return;
        }

        if (event.getLocation().getBlock().getType() == Material.NETHER_PORTAL && !hyperWorld
                .getFlag(NetherFlag.class).isEmpty()) {
            final Location destination = hyperWorld.getTeleportationManager()
                    .netherDestination(event.getEntity(), event.getLocation());
            if (destination != null) {
                // Destination is the location from which we want to search, now we need to find the
                // actual portal destination
                final Location location =
                        this.nms.getOrCreateNetherPortal(event.getEntity(), destination);
                if (location != null) {
                    this.teleportationTimeout
                            .put(event.getEntity().getUniqueId(), System.currentTimeMillis());
                    PaperLib.teleportAsync(event.getEntity(), location,
                            PlayerTeleportEvent.TeleportCause.COMMAND
                    );
                } else {
                    this.hyperverse.getLogger().warning(String
                            .format(
                                    "Failed to find/create a portal surrounding %s",
                                    destination.toString()
                            ));
                }
            }
        } else if (event.getLocation().getBlock().getType() == Material.END_PORTAL && !hyperWorld
                .getFlag(EndFlag.class).isEmpty()) {
            Location portalLocation;
            final Location current = event.getLocation();
            final DragonBattle battle = current.getWorld().getEnderDragonBattle();
            if (battle != null && (portalLocation = battle.getEndPortalLocation()) != null) {
                current.clone().setY(portalLocation.getY());
                if (portalLocation.distanceSquared(current) > 9) {
                    return;
                }
            }
            final Location destination =
                    hyperWorld.getTeleportationManager().endDestination(event.getEntity());
            if (destination != null) {
                PaperLib.teleportAsync(event.getEntity(), destination,
                        PlayerTeleportEvent.TeleportCause.COMMAND
                );
            }
        }
    }

    @EventHandler
    public void onEntityPortalEvent(final @NonNull EntityPortalEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            return;
        }

        final HyperWorld hyperWorld =
                this.worldManager.getWorld(Objects.requireNonNull(event.getFrom().getWorld()));
        if (hyperWorld == null) {
            return;
        }

        if (event.getFrom().getBlock().getType() == Material.NETHER_PORTAL && !hyperWorld
                .getFlag(NetherFlag.class).isEmpty()) {
            event.setCancelled(true);
        } else if (event.getFrom().getBlock().getType() == Material.END_PORTAL && !hyperWorld
                .getFlag(EndFlag.class).isEmpty()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPreSpawn(final @NonNull CreatureSpawnEvent event) {
        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getLocation().getWorld());
        if (hyperWorld == null) {
            return;
        }
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) {
            return;
        }
        final Entity entity = event.getEntity();
        if (shouldCancelSpawn(hyperWorld, entity)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChunkGeneration(final @NonNull ChunkLoadEvent event) {
        if (!event.isNewChunk()) {
            return;
        }
        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getWorld());
        if (hyperWorld == null) {
            return;
        }
        for (final Entity entity : event.getChunk().getEntities()) {
            if (shouldCancelSpawn(hyperWorld, entity)) {
                entity.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSleep(final @NonNull PlayerBedEnterEvent event) {
        if (!this.hyperConfiguration.shouldPersistLocations()) {
            return;
        }

        final PlayerBedEnterEvent.BedEnterResult bedEnterResult = event.getBedEnterResult();
        if ((event.useBed() == Event.Result.DEFAULT && (bedEnterResult == PlayerBedEnterEvent.BedEnterResult.NOT_POSSIBLE_HERE
                || bedEnterResult == PlayerBedEnterEvent.BedEnterResult.TOO_FAR_AWAY
                || bedEnterResult == PlayerBedEnterEvent.BedEnterResult.OTHER_PROBLEM))
                || event.useBed() == Event.Result.DENY) {
            return;
        }

        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getBed().getWorld());
        if (hyperWorld == null) {
            return;
        }

        final PlayerSetSpawnEvent playerSetSpawnEvent = PlayerSetSpawnEvent.callFor(event.getPlayer(), hyperWorld);
        if (playerSetSpawnEvent.isCancelled()) {
            return;
        }

        this.hyperDatabase.storeLocation(PersistentLocation.fromLocation(event.getPlayer().getUniqueId(),
                event.getBed().getLocation(), LocationType.BED_SPAWN
        ), true, false);
    }

}
