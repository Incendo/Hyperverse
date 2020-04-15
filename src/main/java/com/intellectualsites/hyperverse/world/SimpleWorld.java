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

import co.aikar.taskchain.TaskChainFactory;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.intellectualsites.hyperverse.configuration.Messages;
import com.intellectualsites.hyperverse.exception.HyperWorldValidationException;
import com.intellectualsites.hyperverse.modules.HyperWorldCreatorFactory;
import com.intellectualsites.hyperverse.util.MessageUtil;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class SimpleWorld implements HyperWorld {

    private final UUID worldUUID;
    private final WorldConfiguration configuration;
    private final HyperWorldCreatorFactory hyperWorldCreatorFactory;
    private final WorldManager worldManager;
    private final TaskChainFactory taskChainFactory;
    private World bukkitWorld;

    @Inject public SimpleWorld(@Assisted final UUID worldUUID,
        @Assisted final WorldConfiguration configuration,
        final HyperWorldCreatorFactory hyperWorldCreatorFactory, final WorldManager worldManager,
        final TaskChainFactory taskChainFactory) {
        this.worldUUID = Objects.requireNonNull(worldUUID);
        this.configuration = Objects.requireNonNull(configuration);
        this.hyperWorldCreatorFactory = Objects.requireNonNull(hyperWorldCreatorFactory);
        this.worldManager = Objects.requireNonNull(worldManager);
        this.taskChainFactory = Objects.requireNonNull(taskChainFactory);
    }

    @Override public void setBukkitWorld(@NotNull final World world) {
        if (this.bukkitWorld != null) {
            throw new IllegalStateException("Cannot replace bukkit world");
        }
        this.bukkitWorld = Objects.requireNonNull(world);
    }

    @Override public void saveConfiguration() {
        this.taskChainFactory.newChain().async(() ->
            getConfiguration().writeToFile(this.worldManager.getWorldDirectory().
            resolve(String.format("%s.json", this.getConfiguration().getName()))))
            .execute();
    }

    @Override public boolean isLoaded() {
        return this.bukkitWorld != null;
    }

    @Override @NotNull public WorldUnloadResult unloadWorld() {
        if (!this.isLoaded()) {
            return WorldUnloadResult.SUCCESS;
        }
        if (Bukkit.getWorlds().get(0).equals(this.bukkitWorld)) {
            return WorldUnloadResult.FAILURE_ONLY_WORLD;
        }
        if (!this.bukkitWorld.getPlayers().isEmpty()) {
            return WorldUnloadResult.FAILURE_HAS_PLAYERS;
        }
        if (!Bukkit.unloadWorld(this.bukkitWorld, true)) {
            return WorldUnloadResult.FAILURE_OTHER;
        }

        // Update the load status in the configuration file
        this.configuration.setLoaded(false);
        this.saveConfiguration();

        this.bukkitWorld = null;
        return WorldUnloadResult.SUCCESS;
    }

    @Override public void sendWorldInfo(@NotNull CommandSender sender) {
        MessageUtil.sendMessage(sender, Messages.messageWorldProperty,
            "%property%", "name", "%value%", configuration.getName());
        MessageUtil.sendMessage(sender, Messages.messageWorldProperty,
            "%property%", "type", "%value%", configuration.getType().name());
        MessageUtil.sendMessage(sender, Messages.messageWorldProperty,
            "%property%", "seed", "%value%", Long.toString(configuration.getSeed()));
        MessageUtil.sendMessage(sender, Messages.messageWorldProperty,
            "%property%", "structures", "%value%", Boolean.toString(configuration.isGenerateStructures()));
        MessageUtil.sendMessage(sender, Messages.messageWorldProperty,
            "%property%", "settings", "%value%", configuration.getSettings());
        MessageUtil.sendMessage(sender, Messages.messageWorldProperty,
            "%property%", "generator", "%value%", configuration.getGenerator());
        MessageUtil.sendMessage(sender, Messages.messageWorldProperty,
            "%property%", "generator arg", "%value%", configuration.getGeneratorArg());
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
