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

package se.hyperver.hyperverse.world;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import se.hyperver.hyperverse.Hyperverse;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.events.HyperWorldCreateEvent;
import se.hyperver.hyperverse.exception.HyperWorldValidationException;
import se.hyperver.hyperverse.modules.HyperWorldFactory;
import se.hyperver.hyperverse.util.GeneratorUtil;
import se.hyperver.hyperverse.util.MessageUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Implementation of {@link WorldManager}
 * {@inheritDoc}
 */
@Singleton
public final class SimpleWorldManager implements WorldManager, Listener {

    private final Map<String, HyperWorld> worldMap = Maps.newHashMap();
    private final Multimap<String, HyperWorld> waitingForPlugin = HashMultimap.create();
    private final Collection<String> ignoredWorlds = Lists.newLinkedList();

    private final Hyperverse hyperverse;
    private final HyperWorldFactory hyperWorldFactory;
    private final Path worldDirectory;

    @Inject
    public SimpleWorldManager(
            final @NonNull Hyperverse hyperverse,
            final @NonNull HyperWorldFactory hyperWorldFactory
    ) {
        this.hyperverse = Objects.requireNonNull(hyperverse);
        this.hyperWorldFactory = Objects.requireNonNull(hyperWorldFactory);
        // Register the listener
        Bukkit.getPluginManager().registerEvents(this, hyperverse);
        // Create configuration file
        this.worldDirectory = this.hyperverse.getDataFolder().toPath().resolve("worlds");
    }

    @Override
    public void loadWorlds() {
        // Find all files in the worlds folder and load them
        final Path worldsPath = this.hyperverse.getDataFolder().toPath().resolve("worlds");
        if (!Files.exists(worldsPath)) {
            try {
                Files.createDirectories(worldsPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (Files.exists(worldsPath) && Files.isDirectory(worldsPath)) {
            MessageUtil
                    .sendMessage(Bukkit.getConsoleSender(), Messages.messageWorldsLoading, "%path%",
                            worldsPath.toString()
                    );
            try (final Stream<Path> stream = Files.list(worldsPath)){
                stream.forEach(path -> {
                    final WorldConfiguration worldConfiguration = WorldConfiguration.fromFile(path);
                    if (worldConfiguration == null) {
                        this.hyperverse.getLogger().warning(String
                                .format(
                                        "Failed to parse world file: %s",
                                        path.getFileName().toString()
                                ));
                    } else {
                        final HyperWorld hyperWorld =
                                this.hyperWorldFactory.create(UUID.randomUUID(), worldConfiguration);
                        this.registerWorld(hyperWorld);
                    }
                });
            } catch (IOException e) {
                this.hyperverse.getLogger().severe("Failed to load world configurations");
                e.printStackTrace();
            }
        }
        // Also loop over all other worlds to see if anything was loaded sneakily
        for (final World world : Bukkit.getWorlds()) {
            if (!this.worldMap.containsKey(world.getName())) {
                final WorldImportResult importResult = this.importWorld(world, world.equals(Bukkit.getWorlds().get(0)), "");
                if (importResult != WorldImportResult.SUCCESS) {
                    MessageUtil.sendMessage(Bukkit.getConsoleSender(), Messages.messageWorldImportFailure,
                            "%world%", world.getName(), "%result%", importResult.getDescription()
                    );
                }
            }
        }
        MessageUtil.sendMessage(Bukkit.getConsoleSender(), Messages.messageWorldLoaded, "%num%",
                Integer.toString(this.worldMap.size())
        );
        // Now create the worlds
        this.createWorlds();
    }

    @Override
    public void createWorlds() {
        // Loop over all the worlds again to see if anything has been loaded while
        // we were idle
        for (final World world : Bukkit.getWorlds()) {
            final HyperWorld hyperWorld = this.getWorld(world.getName());
            if (hyperWorld != null && hyperWorld.getBukkitWorld() == null) {
                hyperWorld.setBukkitWorld(world);
            }
        }
        // Now loop over the worlds again and create the ones that are
        // definitely missing
        for (final HyperWorld hyperWorld : this.getWorlds()) {
            if (!hyperWorld.getConfiguration().isLoaded()) {
                // These worlds are unloaded and should remain that way
                continue;
            }
            if (hyperWorld.getBukkitWorld() == null) {
                if (!GeneratorUtil.isGeneratorAvailable(hyperWorld.getConfiguration().getGenerator())) {
                    MessageUtil.sendMessage(Bukkit.getConsoleSender(), Messages.messageGeneratorNotAvailable,
                            "%world%", hyperWorld.getConfiguration().getName(),
                            "%generator%", hyperWorld.getConfiguration().getGenerator()
                    );
                    this.waitingForPlugin.put(hyperWorld.getConfiguration().getGenerator().toLowerCase(), hyperWorld);
                } else {
                    this.attemptCreate(hyperWorld);
                }
            } else {
                hyperWorld.refreshFlags();
            }
        }
    }

    @EventHandler
    public void onPluginLoad(final @NonNull PluginEnableEvent enableEvent) {
        for (final HyperWorld hyperWorld : this.waitingForPlugin.get(enableEvent.getPlugin().getName().toLowerCase())) {
            MessageUtil.sendMessage(Bukkit.getConsoleSender(), Messages.messageGeneratorAvailable,
                    "%world%", hyperWorld.getConfiguration().getName()
            );
            this.attemptCreate(hyperWorld);
        }
        this.waitingForPlugin.removeAll(enableEvent.getPlugin().getName().toLowerCase());
    }

    private void attemptCreate(final @NonNull HyperWorld hyperWorld) {
        try {
            // A last check before it's too late
            if (hyperWorld.getBukkitWorld() != null) {
                return;
            }
            this.ignoreWorld(hyperWorld.getConfiguration().getName());
            // Make sure to spam a little
            MessageUtil.sendMessage(Bukkit.getConsoleSender(), Messages.messageWorldCreationStarted);
            hyperWorld.sendWorldInfo(Bukkit.getConsoleSender());
            // Here we go...
            hyperWorld.createBukkitWorld();
        } catch (final HyperWorldValidationException validationException) {
            switch (validationException.getValidationResult()) {
                case UNKNOWN_GENERATOR:
                    MessageUtil.sendMessage(Bukkit.getConsoleSender(), Messages.messageGeneratorInvalid,
                            "%world%", hyperWorld.getConfiguration().getName(),
                            "%generator%", hyperWorld.getConfiguration().getGenerator()
                    );
                    break;
                case SUCCESS:
                    break;
                default:
                    MessageUtil.sendMessage(Bukkit.getConsoleSender(), Messages.messageCreationUnknownFailure);
                    break;
            }
        }
    }

    @Override
    public WorldImportResult importWorld(
            final @NonNull World world,
            final boolean vanilla,
            final @Nullable String generator
    ) {
        if (this.getWorld(world.getName()) != null) {
            return WorldImportResult.ALREADY_IMPORTED;
        }
        final WorldConfiguration worldConfiguration = WorldConfiguration.fromWorld(world);
        if (!vanilla) {
            final String worldGenerator = worldConfiguration.getGenerator();
            if (generator == null && worldGenerator == null) {
                return WorldImportResult.GENERATOR_NOT_FOUND;
            } else if (generator != null) {
                if (!generator.equalsIgnoreCase(worldGenerator)) {
                    return WorldImportResult.GENERATOR_NOT_FOUND;
                }
            }
        }
        final HyperWorld hyperWorld = this.hyperWorldFactory.create(world.getUID(), worldConfiguration);
        hyperWorld.setBukkitWorld(world);
        this.addWorld(hyperWorld);
        return WorldImportResult.SUCCESS;
    }

    @Override
    public void addWorld(final @NonNull HyperWorld hyperWorld) {
        this.registerWorld(hyperWorld);
        hyperWorld.saveConfiguration();
        // Assuming everything went fine
        HyperWorldCreateEvent.callFor(hyperWorld);
    }

    @Override
    public void registerWorld(final @NonNull HyperWorld hyperWorld) {
        Objects.requireNonNull(hyperWorld);
        if (this.worldMap.containsKey(hyperWorld.getConfiguration().getName())) {
            throw new IllegalArgumentException(
                    String.format("World %s already exists", hyperWorld.getConfiguration().getName()));
        }
        this.worldMap.put(hyperWorld.getConfiguration().getName(), hyperWorld);
    }

    @Override
    public @NonNull Collection<@NonNull HyperWorld> getWorlds() {
        return Collections.unmodifiableCollection(this.worldMap.values());
    }

    @Override
    public boolean shouldIgnore(final @NonNull String name) {
        return this.ignoredWorlds.contains(name.toLowerCase());
    }

    @Override
    public void ignoreWorld(final @NonNull String name) {
        this.ignoredWorlds.add(name.toLowerCase());
    }

    @Override
    public @Nullable HyperWorld getWorld(final @NonNull String name) {
        return this.worldMap.get(name);
    }

    @Override
    public @Nullable HyperWorld getWorld(final @NonNull World world) {
        return this.getWorld(world.getName());
    }

    @Override
    public @NonNull Path getWorldDirectory() {
        return this.worldDirectory;
    }

    @Override
    public void unregisterWorld(final @NonNull HyperWorld hyperWorld) {
        this.worldMap.remove(hyperWorld.getConfiguration().getName());
    }

}
