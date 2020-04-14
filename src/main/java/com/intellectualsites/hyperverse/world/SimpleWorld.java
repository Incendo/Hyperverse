//
// Hyperverse - A minecraft world management plugin
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

package com.intellectualsites.hyperverse.world;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intellectualsites.hyperverse.exception.HyperWorldValidationException;
import com.intellectualsites.hyperverse.modules.HyperWorldCreatorFactory;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class SimpleWorld implements HyperWorld {

    private final UUID worldUUID;
    private final WorldConfiguration configuration;
    private final HyperWorldCreatorFactory hyperWorldCreatorFactory;
    private World bukkitWorld;

    @Inject public SimpleWorld(@Assisted final UUID worldUUID,
        @Assisted final WorldConfiguration configuration,
        final HyperWorldCreatorFactory hyperWorldCreatorFactory) {
        this.worldUUID = Objects.requireNonNull(worldUUID);
        this.configuration = Objects.requireNonNull(configuration);
        this.hyperWorldCreatorFactory = Objects.requireNonNull(hyperWorldCreatorFactory);
    }

    @Override public void setBukkitWorld(@NotNull final World world) {
        if (this.bukkitWorld != null) {
            throw new IllegalStateException("Cannot replace bukkit world");
        }
        this.bukkitWorld = Objects.requireNonNull(world);
    }

    @Override public void createBukkitWorld() throws HyperWorldValidationException {
        if (this.bukkitWorld != null) {
            throw new IllegalStateException("A bukkit world already exist");
        }
        // First check if the bukkit world already exists
        World world = Bukkit.getWorld(this.worldUUID);
        if (world != null) {
            this.bukkitWorld = world;
            return;
        }
        // Otherwise we need to create the world
        final HyperWorldCreator hyperWorldCreator = this.hyperWorldCreatorFactory.create(this);
        final HyperWorldCreator.ValidationResult validationResult = hyperWorldCreator.validate();
        if (validationResult != HyperWorldCreator.ValidationResult.SUCCESS) {
            throw new HyperWorldValidationException(validationResult, this);
        }
        hyperWorldCreator.configure();
        world = Bukkit.createWorld(hyperWorldCreator);
        if (world == null) {
            throw new IllegalStateException("Failed to create the world");
        }
        this.bukkitWorld = world;
    }

    @Override public void teleportPlayer(@NotNull final Player player) {
        if (this.bukkitWorld == null) {
            throw new IllegalStateException(
                "Cannot teleport a player to a world before it has been generated");
        }
        if (player.getWorld().equals(this.bukkitWorld)) {
            return;
        }
        PaperLib.teleportAsync(player, this.bukkitWorld.getSpawnLocation());
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final HyperWorld that = (HyperWorld) o;
        return com.google.common.base.Objects.equal(getWorldUUID(), that.getWorldUUID());
    }

    @Override public int hashCode() {
        return com.google.common.base.Objects.hashCode(getWorldUUID());
    }

    @Override public String toString() {
        return "HyperWorld{" + "worldUUID=" + worldUUID + ", configuration=" + configuration + '}';
    }

    public HyperWorldCreatorFactory getHyperWorldCreatorFactory() {
        return this.hyperWorldCreatorFactory;
    }

    @Override public UUID getWorldUUID() {
        return this.worldUUID;
    }

    @Override public World getBukkitWorld() {
        return this.bukkitWorld;
    }

    @Override public WorldConfiguration getConfiguration() {
        return this.configuration;
    }
}
