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

package org.incendo.hyperverse.platform.v1_21_5;

import co.aikar.taskchain.TaskChainFactory;
import com.google.inject.Inject;
import io.papermc.lib.PaperLib;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.RegexFilter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.incendo.hyperverse.util.HyperConfigShouldGroupProfiles;
import org.incendo.hyperverse.util.NMS;

@SuppressWarnings("unused")
public class NMSImpl implements NMS {

    private final TaskChainFactory taskFactory;
    private Field entitySectionManager;
    private Field entityLookup;
    private org.apache.logging.log4j.core.Logger worldServerLogger;

    @Inject public NMSImpl(final TaskChainFactory taskFactory, final @HyperConfigShouldGroupProfiles boolean hyperConfiguration) {
        this.taskFactory = taskFactory;
        if (hyperConfiguration) {
            try {
                final Field field = ServerLevel.class.getDeclaredField("LOGGER");
                field.setAccessible(true);
                this.worldServerLogger = (Logger) field.get(null);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            try {
                final RegexFilter regexFilter = RegexFilter
                        .createFilter("[\\S\\s]*Force-added player with duplicate UUID[\\S\\s]*", null, false,
                                Filter.Result.DENY, Filter.Result.ACCEPT);
                this.worldServerLogger.addFilter(regexFilter);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override @Nullable public Location getOrCreateNetherPortal(@NotNull final org.bukkit.entity.Entity entity,
                                                                @NotNull final Location origin) {
        final ServerLevel worldServer = Objects.requireNonNull(((CraftWorld) origin.getWorld()).getHandle());
        final PortalForcer portalTravelAgent = Objects.requireNonNull(worldServer.getPortalForcer());
        final Entity nmsEntity = Objects.requireNonNull(((CraftEntity) entity).getHandle());
        final BlockPos blockPosition = new BlockPos(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
        final WorldBorder worldBorder = worldServer.getWorldBorder();
        Optional<BlockPos> existingPortalPosition = Objects.requireNonNull(portalTravelAgent, "travel agent")
                .findClosestPortalPosition(Objects.requireNonNull(blockPosition, "position"), worldBorder,128);
        if (existingPortalPosition.isPresent()) {
            BlockPos bottomLeft = existingPortalPosition.get();
            return new Location(origin.getWorld(), bottomLeft.getX(), bottomLeft.getY(), bottomLeft.getZ());
        }
        Optional<BlockUtil.FoundRectangle> createdPortal = portalTravelAgent.createPortal(blockPosition,
                nmsEntity.getDirection().getAxis(), nmsEntity,
                16);
        if (createdPortal.isEmpty()) {
            return null;
        }
        final BlockUtil.FoundRectangle rectangle = createdPortal.get();
        return new Location(origin.getWorld(), rectangle.minCorner.getX() + 1D, rectangle.minCorner.getY() - 1D,
                rectangle.minCorner.getZ() + 1D);
    }

    @Override @Nullable public Location getDimensionSpawn(@NotNull final Location origin) {
        if (Objects.requireNonNull(origin.getWorld()).getEnvironment()
                == World.Environment.THE_END) {
            return new Location(origin.getWorld(), 100, 50, 0);
        }
        return origin.getWorld().getSpawnLocation();
    }

    @Override
    public void writePlayerData(@NotNull final Player player, @NotNull final Path file) {
        final CompoundTag playerTag = new CompoundTag();
        final net.minecraft.world.entity.player.Player entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.save(playerTag);

        // Handle the Optional return type of getCompound
        CompoundTag hyperverse = playerTag.getCompound("hyperverse").orElse(new CompoundTag());
        // If "hyperverse" didn't exist, we need to add it to playerTag
        playerTag.put("hyperverse", hyperverse);

        hyperverse.putLong("writeTime", System.currentTimeMillis());
        hyperverse.putString("version", Bukkit.getPluginManager().getPlugin("Hyperverse").getDescription().getVersion());

        taskFactory.newChain().async(() -> {
            try (final OutputStream outputStream = Files.newOutputStream(file)) {
                NbtIo.writeCompressed(playerTag, outputStream);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }).execute();
    }

    @Override
    public void readPlayerData(@NotNull final Player player, @NotNull final Path file, @NotNull final Runnable whenDone) {
        final Location originLocation = player.getLocation().clone();
        taskFactory.newChain().asyncFirst(() -> {
            try (final InputStream inputStream = Files.newInputStream(file)) {
                return Optional.of(NbtIo.readCompressed(inputStream, NbtAccounter.unlimitedHeap()));
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }).syncLast((optionalCompound) -> {
            if (!optionalCompound.isPresent()) {
                return;
            }
            final CompoundTag compound = (CompoundTag) optionalCompound.get();
            PaperLib.getChunkAtAsync(originLocation).thenAccept(chunk -> {
                // Health and hunger don't update properly, so we give them a little help
                final float health = compound.getFloat("Health").orElse(20.0f); // Default to 20 (full health)
                final int foodLevel = compound.getInt("foodLevel").orElse(20); // Default to 20 (full food)

                // Restore bed spawn
                final String spawnWorld = compound.getString("SpawnWorld").orElse(player.getWorld().getName()); // Default to current world
                final int spawnX = compound.getInt("SpawnX").orElse((int) originLocation.getX());
                final int spawnY = compound.getInt("SpawnY").orElse((int) originLocation.getY());
                final int spawnZ = compound.getInt("SpawnZ").orElse((int) originLocation.getZ());
                final Location spawnLocation = new Location(Bukkit.getWorld(spawnWorld), spawnX, spawnY, spawnZ);

                final ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();

                // We re-write the extra Bukkit data as to not mess up the profile
                ((CraftPlayer) player).setExtraData(compound);
                // Set the position to the player's current position
                Vec3 pos = entityPlayer.position();
                compound.put("Pos", doubleList(pos.x, pos.y, pos.z));
                // Set the world to the player's current world
                compound.putString("world", player.getWorld().getName());
                // Store persistent values
                ((CraftPlayer) player).storeBukkitValues(compound);

                // We start by doing a total reset
                entityPlayer.reset();
                entityPlayer.load(compound);

                entityPlayer.effectsDirty = true;
                entityPlayer.onUpdateAbilities();
                player.teleport(originLocation);

                final ServerLevel worldServer = ((CraftWorld) originLocation.getWorld()).getHandle();
                final DimensionType dimensionManager = worldServer.dimensionType();

                // Prevent annoying message
                // Spigot-obf = decouple()
                entityPlayer.unRide();
                worldServer.removePlayerImmediately(entityPlayer, Entity.RemovalReason.CHANGED_DIMENSION);
                // worldServer.removePlayer above should remove the player from the map, but that doesn't always happen
                try {
                    if (this.entitySectionManager == null) {
                        this.entitySectionManager = worldServer.getClass().getDeclaredField("entityManager");
                        this.entitySectionManager.setAccessible(true);
                    }
                    final PersistentEntitySectionManager<Entity> esm = (PersistentEntitySectionManager<Entity>) this.entitySectionManager.get(worldServer);
                    if (this.entityLookup == null) {
                        this.entityLookup = esm.getClass().getDeclaredField("visibleEntityStorage");
                    }
                    final EntityLookup<Entity> lookup = (EntityLookup<Entity>) this.entityLookup.get(esm);
                    lookup.remove(entityPlayer);
                } catch (final NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                // pre 1.18 code = PlayerList#moveToWorld
                entityPlayer.server.getPlayerList().remove(entityPlayer);
                worldServer.getServer().getPlayerList().respawn(entityPlayer, true,
                        Entity.RemovalReason.CHANGED_DIMENSION, PlayerRespawnEvent.RespawnReason.PLUGIN, originLocation);

                // Apply health and foodLevel
                player.setHealth(health);
                player.setFoodLevel(foodLevel);
                player.setPortalCooldown(40);
                player.setBedSpawnLocation(spawnLocation, true);
            });
        }).execute(whenDone);
    }

    @Override
    @Nullable
    public Location findBedRespawn(@NotNull final Location spawnLocation) {
        final CraftWorld craftWorld = (CraftWorld) spawnLocation.getWorld();
        if (craftWorld == null) {
            return null;
        }

        // Create a BlockPos for the spawn location
        BlockPos blockPos = new BlockPos(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ());

        // Get the dimension ResourceKey from the ServerLevel
        ServerLevel serverLevel = craftWorld.getHandle();
        ResourceKey<Level> dimension = serverLevel.dimension();

        // Create a RespawnConfig instance with the required arguments
        ServerPlayer.RespawnConfig respawnConfig = new ServerPlayer.RespawnConfig(
                dimension,          // The dimension of the world
                blockPos,           // The position to check for respawn
                0.0f,               // Yaw (rotation angle), default to 0
                true                // Forced flag, set to true to match previous behavior
        );

        // Call the method with the correct arguments
        return ServerPlayer.findRespawnAndUseSpawnBlock(
                        serverLevel,
                        respawnConfig,
                        false  // Assuming the boolean is respawnAfterDeath
                )
                .map(ServerPlayer.RespawnPosAngle::position)
                .map(pos -> new Location(spawnLocation.getWorld(), pos.x(), pos.y(), pos.z()))
                .orElse(null);
    }

    private static ListTag doubleList(final double... values) {
        final ListTag tagList = new ListTag();
        for (final double d : values) {
            tagList.add(DoubleTag.valueOf(d));
        }
        return tagList;
    }

}
