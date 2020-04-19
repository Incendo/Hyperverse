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

package com.intellectualsites.hyperverse.database;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.UUID;

@DatabaseTable(tableName = "inventory")
public final class PersistentInventory {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField(uniqueCombo = true)
    private String worldName;
    @DatabaseField(uniqueCombo = true)
    private String ownerUUID;
    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    private byte[] content;

    public PersistentInventory() {
    }

    public PersistentInventory(@NotNull final String worldUID, @NotNull final PlayerInventory playerInventory) {
        this.worldName = worldUID;
        this.content = serialize(playerInventory);
        this.ownerUUID = Objects.requireNonNull(playerInventory.getHolder()).getUniqueId().toString();
    }

    @NotNull public static PersistentInventory fromPlayer(Player player) {
        return new PersistentInventory(player.getWorld().getName(), player.getInventory());
    }

    /**
     * Serialize a {@link PlayerInventory} contents into a base64 encoded byte array
     *
     * @param inventory The Inventory object to serialize.
     * @return Returns encoded serialized inventory contents
     */
    @NotNull private static byte[] serialize(@NotNull final PlayerInventory inventory) {
        try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            try (final BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream)) {
                bukkitObjectOutputStream.writeInt(inventory.getSize());
                for (final ItemStack stack : inventory) {
                    bukkitObjectOutputStream.writeObject(stack);
                }
            }
            return byteArrayOutputStream.toByteArray();
        } catch (final Exception e) {
             throw new RuntimeException(e);
        }
    }

    @NotNull public String getOwnerUUID() {
        return ownerUUID;
    }

    @NotNull public String getWorldName() {
        return worldName;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Deserialize this object back into an {@link PlayerInventory} and applies it
     * to the player
     */
    public void toInventory() {
        if (this.content.length == 0) {
            return;
        }
        final Player player = Bukkit.getPlayer(UUID.fromString(ownerUUID));
        if (player == null) {
            return;
        }
        final PlayerInventory inventory = player.getInventory();
        try (final ByteArrayInputStream byteArrayInputStream =
            new ByteArrayInputStream(this.content)) {
            try (final BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream)) {
                final int size = bukkitObjectInputStream.readInt();
                for (int i = 0; i < size; i++) {
                    inventory.setItem(i, (ItemStack) bukkitObjectInputStream.readObject());
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }
}
