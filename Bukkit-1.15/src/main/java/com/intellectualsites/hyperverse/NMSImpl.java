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

package com.intellectualsites.hyperverse;

import co.aikar.taskchain.TaskChainFactory;
import com.google.inject.Inject;
import com.intellectualsites.hyperverse.util.NMS;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.DimensionManager;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EnumDirection;
import net.minecraft.server.v1_15_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.PortalTravelAgent;
import net.minecraft.server.v1_15_R1.ShapeDetector;
import net.minecraft.server.v1_15_R1.Vec3D;
import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@SuppressWarnings("unused")
public class NMSImpl implements NMS {

    private final TaskChainFactory taskChainFactory;

    @Inject public NMSImpl(final TaskChainFactory taskChainFactory) {
        this.taskChainFactory = taskChainFactory;
    }

    @Override public @Nullable Location getOrCreateNetherPortal(@NotNull final Entity entity,
        @NotNull final Location origin) {
        final WorldServer worldServer = Objects.requireNonNull(((CraftWorld) origin.getWorld()).getHandle());
        final PortalTravelAgent portalTravelAgent = Objects.requireNonNull(worldServer.getTravelAgent());
        final net.minecraft.server.v1_15_R1.Entity nmsEntity = Objects.requireNonNull(((CraftEntity) entity).getHandle());
        final BlockPosition blockPosition = new BlockPosition(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
        EnumDirection enumDirection = nmsEntity.getPortalDirection();
        if (enumDirection == null) {
            enumDirection = Objects.requireNonNull(nmsEntity.getDirection());
        }
        Vec3D mot = nmsEntity.getMot();
        if (mot == null) {
            mot = new Vec3D(1, 1, 1);
        }
        Vec3D portalOffset = nmsEntity.getPortalOffset();
        if (portalOffset == null) {
            portalOffset = new Vec3D(0, 0, 0);
        }
        ShapeDetector.Shape portalShape = Objects.requireNonNull(portalTravelAgent, "travel agent")
            .findPortal(Objects.requireNonNull(blockPosition, "position"),
                Objects.requireNonNull(mot, "mot"), Objects.requireNonNull(enumDirection, "direction"), portalOffset.x, portalOffset.y,
                Objects.requireNonNull(nmsEntity, "entity") instanceof EntityHuman, 128);
        if (portalShape == null && portalTravelAgent.createPortal(nmsEntity, blockPosition, 16)) {
            portalShape = portalTravelAgent
                .findPortal(blockPosition,
                    nmsEntity.getMot(), nmsEntity.getPortalDirection(), portalOffset.x, portalOffset.y,
                    nmsEntity instanceof EntityHuman, 128);
        }
        if (portalShape == null) {
            return null;
        }
        return new Location(origin.getWorld(), portalShape.position.getX() + 1, portalShape.position.getY() - 1,
            portalShape.position.getZ() + 1);
    }

    @Override @Nullable public Location getDimensionSpawn(@NotNull final Location origin) {
        final WorldServer worldServer = ((CraftWorld) origin.getWorld()).getHandle();
        final BlockPosition dimensionSpawn = worldServer.getDimensionSpawn();
        if (dimensionSpawn != null) {
            return new Location(origin.getWorld(), dimensionSpawn.getX(), dimensionSpawn.getY(), dimensionSpawn.getZ());
        }
        return origin.getWorld().getSpawnLocation();
    }

    @Override public void writePlayerData(@NotNull final Player player, @NotNull final Path file) {
        final NBTTagCompound playerTag = new NBTTagCompound();
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        entityPlayer.save(playerTag);
        taskChainFactory.newChain().async(() -> {
            try (final OutputStream outputStream = Files.newOutputStream(file)) {
                NBTCompressedStreamTools.a(playerTag, outputStream);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }).execute();
    }

    @Override public void readPlayerData(@NotNull final Player player, @NotNull final Path file, @NotNull final Runnable whenDone) {
        final Location originLocation = player.getLocation().clone();
        taskChainFactory.newChain().asyncFirst(() -> {
            try (final InputStream inputStream = Files.newInputStream(file)) {
                return NBTCompressedStreamTools.a(inputStream);
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return null;
        }).syncLast(compound -> {
            if (compound == null) {
                return;
            }
            final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            // We re-write the extra Bukkit data as to not
            // mess up the profile
            ((CraftPlayer) player).setExtraData(compound);
            entityPlayer.effects.clear();
            entityPlayer.f(compound);
            // entityPlayer.updateEffects = true;
            // entityPlayer.updateAbilities();
            player.teleport(originLocation);
            final WorldServer worldServer = ((CraftWorld) originLocation.getWorld()).getHandle();
            final DimensionManager dimensionManager = worldServer.worldProvider.getDimensionManager();
            entityPlayer.server.getPlayerList().moveToWorld(entityPlayer, dimensionManager,
                true, originLocation, true);
        }).execute(whenDone);
    }

}
