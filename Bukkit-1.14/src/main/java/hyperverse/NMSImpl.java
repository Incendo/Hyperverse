//
// Hyperverse Bukkit 1.15 - A minecraft world management plugin
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

package hyperverse;

import com.intellectualsites.hyperverse.util.NMS;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EnumDirection;
import net.minecraft.server.v1_14_R1.PortalTravelAgent;
import net.minecraft.server.v1_14_R1.ShapeDetector;
import net.minecraft.server.v1_14_R1.Vec3D;
import net.minecraft.server.v1_14_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("unused")
public class NMSImpl implements NMS {

    @Override public @Nullable Location getOrCreateNetherPortal(@NotNull final Entity entity,
        @NotNull final Location origin) {
        final WorldServer worldServer = Objects.requireNonNull(((CraftWorld) origin.getWorld()).getHandle());
        final PortalTravelAgent portalTravelAgent = Objects.requireNonNull(worldServer.getTravelAgent());
        final net.minecraft.server.v1_14_R1.Entity nmsEntity = Objects.requireNonNull(((CraftEntity) entity).getHandle());
        final BlockPosition
            blockPosition = new BlockPosition(origin.getBlockX(), origin.getBlockY(), origin.getBlockZ());
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
            .a(Objects.requireNonNull(blockPosition, "position"),
                Objects.requireNonNull(mot, "mot"), Objects.requireNonNull(enumDirection, "direction"), portalOffset.x, portalOffset.y,
                Objects.requireNonNull(nmsEntity, "entity") instanceof EntityHuman);
        if (portalShape == null && portalTravelAgent.a(nmsEntity)) {
            portalShape = portalTravelAgent
                .a(blockPosition,
                    nmsEntity.getMot(), nmsEntity.getPortalDirection(), portalOffset.x, portalOffset.y,
                    nmsEntity instanceof EntityHuman);
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

}
