//
// Hyperverse - A Minecraft world management plugin
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

package se.hyperver.hyperverse.spigotnms.v1_17_R1;

import cloud.commandframework.tasks.TaskFactory;
import com.google.inject.Inject;
import io.papermc.lib.PaperLib;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.dimension.DimensionManager;
import net.minecraft.world.level.portal.PortalTravelAgent;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.RegexFilter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.hyperver.hyperverse.util.HyperConfigShouldGroupProfiles;
import se.hyperver.hyperverse.util.NMS;

@SuppressWarnings("unused")
public class NMSImpl implements NMS {

    private final TaskFactory taskFactory;
    private Field entitiesByUUID;
    private org.apache.logging.log4j.core.Logger worldServerLogger;

    @Inject public NMSImpl(final TaskFactory taskFactory, final @HyperConfigShouldGroupProfiles boolean hyperConfiguration) {
        this.taskFactory = taskFactory;
        if (hyperConfiguration) {
            try {
                final Field field = WorldServer.class.getDeclaredField("z");
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

    @Override @Nullable public Location getOrCreateNetherPortal(@NotNull final Entity entity,
        @NotNull final Location origin) {
        final WorldServer worldServer = Objects.requireNonNull(((CraftWorld) origin.getWorld()).getHandle());
        final PortalTravelAgent portalTravelAgent = Objects.requireNonNull(worldServer.getTravelAgent());
        final net.minecraft.world.entity.Entity nmsEntity = Objects.requireNonNull(((CraftEntity) entity).getHandle());
        final BlockPosition blockPosition = new BlockPosition(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
        Optional<BlockUtil.Rectangle> portalShape = Objects.requireNonNull(portalTravelAgent, "travel agent")
                                                           .findPortal(Objects.requireNonNull(blockPosition, "position"), 128);
        if (!portalShape.isPresent()) {
            portalShape = portalTravelAgent.createPortal( blockPosition, nmsEntity.getDirection().n(),nmsEntity,  16);
        }
        if (!portalShape.isPresent()) {
            return null;
        }
        final BlockUtil.Rectangle rectangle = portalShape.get();
        return new Location(origin.getWorld(), rectangle.a.getX() + 1, rectangle.a.getY() - 1,
            rectangle.a.getZ() + 1);
    }

    @Override @Nullable public Location getDimensionSpawn(@NotNull final Location origin) {
        if (Objects.requireNonNull(origin.getWorld()).getEnvironment()
            == World.Environment.THE_END) {
            return new Location(origin.getWorld(), 100, 50, 0);
        }
        return origin.getWorld().getSpawnLocation();
    }

    @Override public void writePlayerData(@NotNull final Player player, @NotNull final Path file) {
        final NBTTagCompound playerTag = new NBTTagCompound();
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.save(playerTag);

        if (!playerTag.hasKey("hyperverse")) {
            playerTag.set("hyperverse", new NBTTagCompound());
        }
        final NBTTagCompound hyperverse = playerTag.getCompound("hyperverse");
        hyperverse.setLong("writeTime", System.currentTimeMillis());
        hyperverse.setString("version", Bukkit.getPluginManager().getPlugin("Hyperverse").getDescription().getVersion());

        taskFactory.recipe().begin(file).asynchronous(passedFile -> {
            try (final OutputStream outputStream = Files.newOutputStream(passedFile)) {
                NBTCompressedStreamTools.a(playerTag, outputStream);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }).execute();
    }

    @Override public void readPlayerData(@NotNull final Player player, @NotNull final Path file, @NotNull final Runnable whenDone) {
        final Location originLocation = player.getLocation().clone();
        taskFactory.recipe().begin(Optional.empty()).asynchronous(unused -> {
            try (final InputStream inputStream = Files.newInputStream(file)) {
                return Optional.of(NBTCompressedStreamTools.a(inputStream));
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return Optional.empty();
        }).synchronous((optionalCompound) -> {
            if (!optionalCompound.isPresent()) {
                return;
            }
            final NBTTagCompound compound = (NBTTagCompound) optionalCompound.get();
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

                final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

                // We re-write the extra Bukkit data as to not
                // mess up the profile
                ((CraftPlayer) player).setExtraData(compound);
                // Set the position to the player's current position
                compound.set("Pos", doubleList(entityPlayer.locX(), entityPlayer.locY(), entityPlayer.locZ()));
                // Set the world to the player's current world
                compound.setString("world", player.getWorld().getName());
                // Store persistent values
                ((CraftPlayer) player).storeBukkitValues(compound);

                // We start by doing a total reset
                entityPlayer.reset();
                entityPlayer.load(compound);

                // entityPlayer.updateEffects = true;
                // entityPlayer.updateAbilities();
                player.teleport(originLocation);

                final WorldServer worldServer = ((CraftWorld) originLocation.getWorld()).getHandle();
                final DimensionManager dimensionManager = worldServer.getDimensionManager();

                // Prevent annoying message
                entityPlayer.decouple();
                // Removal reason == "CHANGED_DIMENSION"
                worldServer.a(entityPlayer, net.minecraft.world.entity.Entity.RemovalReason.e);
                // worldServer.removePlayer above should remove the player from the
                // map, but that doesn't always happen. This is a last effort
                // attempt to prevent the annoying "Force re-added" message
                // from appearing
                try {
                    if (this.entitiesByUUID == null) {
                        this.entitiesByUUID = worldServer.getClass().getDeclaredField("entitiesByUUID");
                        this.entitiesByUUID.setAccessible(true);
                    }
                    final Map<UUID, Entity> map = (Map<UUID, Entity>) entitiesByUUID.get(worldServer);
                    map.remove(entityPlayer.getUniqueID());
                } catch (final NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }

                entityPlayer.c.getPlayerList().moveToWorld(entityPlayer, worldServer,
                    true, originLocation, true);

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
        return EntityHuman.getBed(craftWorld.getHandle(), new BlockPosition(spawnLocation.getBlockX(),
                                                                            spawnLocation.getBlockY(), spawnLocation.getBlockZ()), 0, true, false)
                          .map(vec3D -> new Location(spawnLocation.getWorld(), vec3D.getX(), vec3D.getY(), vec3D.getZ()))
                          .orElse(null);
    }

    private static NBTTagList doubleList(final double... values) {
        final NBTTagList nbttaglist = new NBTTagList();
        for (final double d : values) {
            nbttaglist.add(NBTTagDouble.a(d));
        }
        return nbttaglist;
    }

}
