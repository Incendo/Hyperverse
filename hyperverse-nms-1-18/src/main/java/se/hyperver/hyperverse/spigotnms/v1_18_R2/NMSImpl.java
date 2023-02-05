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

package se.hyperver.hyperverse.spigotnms.v1_18_R2;

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
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.hyperver.hyperverse.util.HyperConfigShouldGroupProfiles;
import se.hyperver.hyperverse.util.NMS;

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
        Optional<BlockUtil.FoundRectangle> portalShape = Objects.requireNonNull(portalTravelAgent, "travel agent")
                .findPortalAround(Objects.requireNonNull(blockPosition, "position"), worldBorder,128);
        if (!portalShape.isPresent()) {
            portalShape = portalTravelAgent.createPortal(blockPosition, nmsEntity.getDirection().getAxis(), nmsEntity,  16);
        }
        if (!portalShape.isPresent()) {
            return null;
        }
        final BlockUtil.FoundRectangle rectangle = portalShape.get();
        return new Location(origin.getWorld(), rectangle.minCorner.getX() + 1, rectangle.minCorner.getY() - 1,
                rectangle.minCorner.getZ() + 1);
    }

    @Override @Nullable public Location getDimensionSpawn(@NotNull final Location origin) {
        if (Objects.requireNonNull(origin.getWorld()).getEnvironment()
                == World.Environment.THE_END) {
            return new Location(origin.getWorld(), 100, 50, 0);
        }
        return origin.getWorld().getSpawnLocation();
    }

    @Override public void writePlayerData(@NotNull final Player player, @NotNull final Path file) {
        final CompoundTag playerTag = new CompoundTag();
        final net.minecraft.world.entity.player.Player entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.save(playerTag);

        if (!playerTag.contains("hyperverse")) {
            playerTag.put("hyperverse", new CompoundTag());
        }
        final CompoundTag hyperverse = playerTag.getCompound("hyperverse");
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

    @Override public void readPlayerData(@NotNull final Player player, @NotNull final Path file, @NotNull final Runnable whenDone) {
        final Location originLocation = player.getLocation().clone();
        taskFactory.newChain().asyncFirst(() -> {
            try (final InputStream inputStream = Files.newInputStream(file)) {
                return Optional.of(NbtIo.readCompressed(inputStream));
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
                // Health and hunger don't update properly, so we
                // give them a little help
                final float health = compound.getFloat("Health");
                final int foodLevel = compound.getInt("foodLevel");

                // Restore bed spawn
                final String spawnWorld = compound.getString("SpawnWorld");
                final int spawnX = compound.getInt("SpawnX");
                final int spawnY = compound.getInt("SpawnY");
                final int spawnZ = compound.getInt("SpawnZ");
                final Location spawnLocation = new Location(Bukkit.getWorld(spawnWorld), spawnX,
                        spawnY, spawnZ);

                final ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();

                // We re-write the extra Bukkit data as to not
                // mess up the profile
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

                // entityPlayer.updateEffects = true;
                // entityPlayer.updateAbilities();
                player.teleport(originLocation);

                final ServerLevel worldServer = ((CraftWorld) originLocation.getWorld()).getHandle();
                final DimensionType dimensionManager = worldServer.dimensionType();

                // Prevent annoying message
                // Spigot-obf = decouple()
                entityPlayer.unRide();
                worldServer.removePlayerImmediately(entityPlayer, Entity.RemovalReason.CHANGED_DIMENSION);
                // worldServer.removePlayer above should remove the player from the
                // map, but that doesn't always happen. This is a last effort
                // attempt to prevent the annoying "Force re-added" message
                // from appearing
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
                worldServer.getServer().getPlayerList().respawn(entityPlayer, worldServer, true, originLocation, true);

                // Apply health and foodLevel
                player.setHealth(health);
                player.setFoodLevel(foodLevel);
                player.setPortalCooldown(40);
                player.setBedSpawnLocation(spawnLocation, true);
            });
        }).execute(whenDone);
    }

    @Override @Nullable public Location findBedRespawn(@NotNull final Location spawnLocation) {
        final CraftWorld craftWorld = (CraftWorld) spawnLocation.getWorld();
        if (craftWorld == null) {
            return null;
        }
        return net.minecraft.world.entity.player.Player.findRespawnPositionAndUseSpawnBlock(craftWorld.getHandle(), new BlockPos(spawnLocation.getBlockX(),
                        spawnLocation.getBlockY(), spawnLocation.getBlockZ()), 0, true, false)
                .map(vec3D -> new Location(spawnLocation.getWorld(), vec3D.x(), vec3D.y(), vec3D.z()))
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
