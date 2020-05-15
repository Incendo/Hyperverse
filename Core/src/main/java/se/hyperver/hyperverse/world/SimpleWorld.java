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

package se.hyperver.hyperverse.world;

import co.aikar.taskchain.TaskChainFactory;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.hyperver.hyperverse.configuration.HyperConfiguration;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.database.HyperDatabase;
import se.hyperver.hyperverse.database.LocationType;
import se.hyperver.hyperverse.database.PersistentLocation;
import se.hyperver.hyperverse.events.HyperWorldDeleteEvent;
import se.hyperver.hyperverse.exception.HyperWorldValidationException;
import se.hyperver.hyperverse.flags.FlagContainer;
import se.hyperver.hyperverse.flags.FlagParseException;
import se.hyperver.hyperverse.flags.GlobalWorldFlagContainer;
import se.hyperver.hyperverse.flags.WorldFlag;
import se.hyperver.hyperverse.flags.implementation.DifficultyFlag;
import se.hyperver.hyperverse.modules.FlagContainerFactory;
import se.hyperver.hyperverse.modules.HyperWorldCreatorFactory;
import se.hyperver.hyperverse.modules.TeleportationManagerFactory;
import se.hyperver.hyperverse.teleportation.TeleportationManager;
import se.hyperver.hyperverse.util.MessageUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Implementation of {@link HyperWorld}
 * {@inheritDoc}
 */
public class SimpleWorld implements HyperWorld {

    private boolean flagsInitialized = false;
    private final UUID worldUUID;
    private final WorldConfiguration configuration;
    private final HyperWorldCreatorFactory hyperWorldCreatorFactory;
    private final WorldManager worldManager;
    private final TaskChainFactory taskChainFactory;
    private final FlagContainer flagContainer;
    private final HyperDatabase hyperDatabase;
    private final HyperConfiguration hyperConfiguration;
    private final TeleportationManager teleportationManager;
    private final GlobalWorldFlagContainer globalWorldFlagContainer;
    private World bukkitWorld;

    @Inject public SimpleWorld(@Assisted final UUID worldUUID,
        @Assisted final WorldConfiguration configuration,
        final HyperWorldCreatorFactory hyperWorldCreatorFactory, final WorldManager worldManager,
        final TaskChainFactory taskChainFactory, final GlobalWorldFlagContainer globalFlagContainer,
        final FlagContainerFactory flagContainerFactory, final HyperDatabase hyperDatabase,
        final HyperConfiguration hyperConfiguration, final TeleportationManagerFactory teleportationManagerFactory) {
        this.worldUUID = Objects.requireNonNull(worldUUID);
        this.configuration = Objects.requireNonNull(configuration);
        this.hyperWorldCreatorFactory = Objects.requireNonNull(hyperWorldCreatorFactory);
        this.worldManager = Objects.requireNonNull(worldManager);
        this.taskChainFactory = Objects.requireNonNull(taskChainFactory);
        this.hyperDatabase = Objects.requireNonNull(hyperDatabase);
        this.hyperConfiguration = Objects.requireNonNull(hyperConfiguration);
        this.teleportationManager = Objects.requireNonNull(teleportationManagerFactory).create(this);
        this.globalWorldFlagContainer = Objects.requireNonNull(globalFlagContainer);
        this.flagContainer = Objects.requireNonNull(flagContainerFactory).create((flag, type) -> {
            if (flagsInitialized) {
                if (type == FlagContainer.WorldFlagUpdateType.FLAG_REMOVED) {
                    this.configuration.setFlagValue(flag.getName(), null);
                } else {
                    this.configuration.setFlagValue(flag.getName(), flag.toString());
                }
                this.saveConfiguration();
            }
        });
        // Load flag values
        for (final Map.Entry<String, String> entry : this.configuration.getFlags().entrySet()) {
            final WorldFlag<?, ?> flag = globalFlagContainer.getFlagFromString(entry.getKey());
            if (flag != null) {
                try {
                    this.flagContainer.addFlag(flag.parse(entry.getValue()));
                } catch (final FlagParseException e) {
                    MessageUtil
                        .sendMessage(Bukkit.getConsoleSender(), Messages.messageFlagParseError,
                            "%flag%", e.getFlag().getName(), "%value%", e.getValue(), "%reason%",
                            e.getErrorMessage());
                }
            }
        }
        this.flagsInitialized = true;
    }

    @Override public void setBukkitWorld(@NotNull final World world) {
        if (world.equals(this.bukkitWorld)) { // implicit null check
            return;
        }
        if (this.bukkitWorld != null) {
            throw new IllegalStateException("Cannot replace bukkit world");
        }
        this.bukkitWorld = world;
    }

    @Override public void saveConfiguration() {
        this.taskChainFactory.newChain()
            .async(() -> getConfiguration().writeToFile(this.worldManager.getWorldDirectory().
                resolve(String.format("%s.json", this.getConfiguration().getName())))).execute();
    }

    @Override public boolean isLoaded() {
        return this.bukkitWorld != null;
    }

    @Override public void deleteWorld(@NotNull final Consumer<WorldUnloadResult> result) {
        if (this.bukkitWorld != null) {
            if (Bukkit.getWorlds().get(0).equals(this.bukkitWorld)) {
                result.accept(WorldUnloadResult.FAILURE_ONLY_WORLD);
                return;
            }
            if (!this.bukkitWorld.getPlayers().isEmpty()) {
                result.accept(WorldUnloadResult.FAILURE_HAS_PLAYERS);
                return;
            }
            if (!Bukkit.unloadWorld(this.bukkitWorld, true)) {
                result.accept(WorldUnloadResult.FAILURE_OTHER);
                return;
            }
            // We unload the world, then we remove the world file,
            // but we don't delete the actual world folder
            Bukkit.unloadWorld(this.bukkitWorld, true);
        }
        taskChainFactory.newChain().async(() -> {
            try {
                Files.delete(this.worldManager.getWorldDirectory().
                    resolve(String.format("%s.json", this.getConfiguration().getName())));
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }).sync(() -> {
            this.worldManager.unregisterWorld(this);
            // Delete world in the database
            this.hyperDatabase.clearWorld(this.configuration.getName());
            result.accept(WorldUnloadResult.SUCCESS);
            // Assuming everything went fine
            HyperWorldDeleteEvent.callFor(this);
        }).execute();
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
        MessageUtil
            .sendMessage(sender, Messages.messageWorldProperty, "%property%", "name", "%value%",
                configuration.getName());
        MessageUtil
            .sendMessage(sender, Messages.messageWorldProperty, "%property%", "type", "%value%",
                configuration.getType().name());
        MessageUtil
            .sendMessage(sender, Messages.messageWorldProperty, "%property%", "seed", "%value%",
                Long.toString(configuration.getSeed()));
        MessageUtil.sendMessage(sender, Messages.messageWorldProperty, "%property%", "structures",
            "%value%", Boolean.toString(configuration.isGenerateStructures()));
        MessageUtil
            .sendMessage(sender, Messages.messageWorldProperty, "%property%", "settings", "%value%",
                configuration.getSettings());
        MessageUtil.sendMessage(sender, Messages.messageWorldProperty, "%property%", "generator",
            "%value%", configuration.getGenerator());
        MessageUtil
            .sendMessage(sender, Messages.messageWorldProperty, "%property%", "generator arg",
                "%value%", configuration.getGeneratorArg());
        // Flags
        final StringBuilder flagStringBuilder = new StringBuilder();
        final Iterator<Map.Entry<String, String>> flagIterator =
            this.configuration.getFlags().entrySet().iterator();
        while (flagIterator.hasNext()) {
            final Map.Entry<String, String> entry = flagIterator.next();
            flagStringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            if (flagIterator.hasNext()) {
                flagStringBuilder.append(", ");
            }
        }
        MessageUtil
            .sendMessage(sender, Messages.messageWorldProperty, "%property%", "flags", "%value%",
                flagStringBuilder.toString());

        if (this.isLoaded()) {
            final StringBuilder gameRuleStringBuilder = new StringBuilder();

            final GameRule[] gameRules = GameRule.values();
            for (int i = 0; i < gameRules.length; i++) {
                final Object value = this.bukkitWorld.getGameRuleValue(gameRules[i]);
                if (value == this.bukkitWorld.getGameRuleDefault(gameRules[i])) {
                    continue;
                }
                gameRuleStringBuilder.append(gameRules[i].getName()).append("=")
                    .append(value.toString()).append(" ");
            }

            MessageUtil.sendMessage(sender, Messages.messageWorldProperty, "%property%", "game rules",
                "%value%", gameRuleStringBuilder.toString());
        }
    }

    @Override public void createBukkitWorld() throws HyperWorldValidationException {
        if (this.bukkitWorld != null) {
            throw new IllegalStateException("A bukkit world already exist");
        }
        // First check if the bukkit world already exists
        World world = Bukkit.getWorld(this.worldUUID);
        if (world != null) {
            this.bukkitWorld = world;
            this.refreshFlags();
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
        if (hyperConfiguration.shouldKeepSpawnLoaded()) {
            this.bukkitWorld.setKeepSpawnInMemory(true);
        }
        this.refreshFlags();
    }

    @Override public void teleportPlayer(@NotNull final Player player) {
        if (this.bukkitWorld == null) {
            throw new IllegalStateException(
                "Cannot teleport a player to a world before it has been generated");
        }

        if (player.getWorld().equals(this.bukkitWorld)) {
            return;
        }

        final Location location;
        if (this.hyperConfiguration.shouldPersistLocations()) {
            location = this.hyperDatabase.getLocation(player.getUniqueId(),
                this.getConfiguration().getName(), LocationType.PLAYER_LOCATION)
                .map(PersistentLocation::toLocation)
                .orElse(Objects.requireNonNull(this.getSpawn()));
        } else {
            location = this.getSpawn();
        }

        assert location != null;
        this.teleportationManager.allowedTeleport(player, location).thenAccept(value -> {
           if (!value) {
               MessageUtil.sendMessage(player, Messages.messageNotPermittedEntry);
           } else {
               this.teleportationManager.canTeleport(player, location).thenAccept(safe -> {
                   if (!safe) {
                       MessageUtil.sendMessage(player, Messages.messageTeleportNotSafe);
                       this.teleportationManager.findSafe(location).thenAccept(safeLocation ->
                           this.teleportationManager.teleportPlayer(player, safeLocation));
                   } else {
                       this.teleportationManager.teleportPlayer(player, location);
                   }
               });
           }
        });
    }

    @Override @Nullable public Location getSpawn() {
        if (this.bukkitWorld == null) {
            return null;
        }
        final Location location = this.bukkitWorld.getSpawnLocation().clone();
        location.add(0.5, 0, 0.5);
        return location;
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

    @Override @NotNull public UUID getWorldUUID() {
        return this.worldUUID;
    }

    @Override @Nullable public World getBukkitWorld() {
        return this.bukkitWorld;
    }

    @Override @NotNull public WorldConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public <T> void setFlag(@NotNull final WorldFlag<T, ?> flag, @NotNull final String value)
        throws FlagParseException {
        this.flagContainer.addFlag(flag.parse(value));
        this.refreshFlags();
    }

    @Override public <T> void setFlagInstance(@NotNull final WorldFlag<T, ?> flag) {
        this.flagContainer.addFlag(flag);
        this.refreshFlags();
    }

    @Override public <T> void removeFlag(@NotNull final WorldFlag<T, ?> flagInstance) {
        this.flagContainer.removeFlag(flagInstance);
    }

    @Override @NotNull
    public <T> T getFlag(@NotNull final Class<? extends WorldFlag<T, ?>> flagClass) {
        return this.flagContainer.getFlag(flagClass).getValue();
    }

    @Override @NotNull public TeleportationManager getTeleportationManager() {
        return this.teleportationManager;
    }

    @Override public void refreshFlags() {
        if (this.bukkitWorld != null) {
            this.bukkitWorld.setDifficulty(this.getFlag(DifficultyFlag.class));
        }
    }

    @Override @NotNull public Collection<WorldFlag<?, ?>> getFlags() {
        final Collection<Class<?>> recognizedFlags = this.globalWorldFlagContainer.getFlagMap().keySet();
        final Collection<WorldFlag<?, ?>> flags = new ArrayList<>();
        for (final Class<?> flagClass : recognizedFlags) {
            flags.add(this.flagContainer.getFlagErased(flagClass));
        }
        return flags;
    }

}
