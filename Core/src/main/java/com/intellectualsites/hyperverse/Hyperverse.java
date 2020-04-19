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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.intellectualsites.hyperverse.commands.HyperCommandManager;
import com.intellectualsites.hyperverse.configuration.HyperConfiguration;
import com.intellectualsites.hyperverse.configuration.Messages;
import com.intellectualsites.hyperverse.database.HyperDatabase;
import com.intellectualsites.hyperverse.listeners.InventoryListener;
import com.intellectualsites.hyperverse.listeners.PlayerListener;
import com.intellectualsites.hyperverse.listeners.WorldListener;
import com.intellectualsites.hyperverse.modules.HyperverseModule;
import com.intellectualsites.hyperverse.modules.TaskChainModule;
import com.intellectualsites.hyperverse.world.WorldManager;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.AbstractConfigurationLoader;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Plugin main class
 */
@Singleton public final class Hyperverse extends JavaPlugin {

    public static final int BSTATS_ID = 7177;

    private WorldManager worldManager;
    private Injector injector;
    private HyperDatabase hyperDatabase;

    @Override public void onEnable() {
        this.injector = Guice.createInjector(Stage.PRODUCTION, new HyperverseModule(), new TaskChainModule(this));

        final HyperConfiguration hyperConfiguration = this.injector.getInstance(HyperConfiguration.class);
        this.getLogger().info("§6Hyperverse Options");
        this.getLogger().info("§8- §7use persistent locations? " + hyperConfiguration.shouldPersistLocations());
        this.getLogger().info("§8- §7keep spawns loaded? " + hyperConfiguration.shouldKeepSpawnLoaded());
        this.getLogger().info("§8- §7should detect worlds?  " + hyperConfiguration.shouldImportAutomatically());
        this.getLogger().info("§8- §7per-world inventories?  " + hyperConfiguration.shouldEnablePerWorldInventories());

        // Message configuration
        final Path messagePath = this.getDataFolder().toPath().resolve("messages.conf");
        final AbstractConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader
            .builder()
            .setParseOptions(ConfigParseOptions.defaults().setClassLoader(this.getClass().getClassLoader()))
            .setRenderOptions(ConfigRenderOptions.defaults()
                .setComments(true)
                .setFormatted(true)
                .setOriginComments(false)
                .setJson(false))
            .setDefaultOptions(ConfigurationOptions.defaults()).setPath(messagePath).build();
        ConfigurationNode translationNode;
        try {
            translationNode = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            translationNode = loader.createEmptyNode();
        }
        if (!Files.exists(messagePath)) {
            try {
                Files.createFile(messagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final Map<String, String> messages = Messages.getConfiguredMessages();
        final Collection<String> messageKeys = new ArrayList<>(messages.keySet());
        for (final String key : messageKeys) {
            final ConfigurationNode messageNode = translationNode.getNode(key);
            if (messageNode.isVirtual()) {
                messageNode.setValue(messages.get(key));
            } else {
                messages.put(key, messageNode.getString());
            }
        }
        try {
            loader.save(translationNode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load the database
        this.hyperDatabase = injector.getInstance(HyperDatabase.class);
        if (!this.hyperDatabase.attemptConnect()) {
            getLogger().severe("Failed to connect to database...");
        }

        // Load the world manager
        this.worldManager = injector.getInstance(WorldManager.class);
        this.worldManager.loadWorlds();

        // Register events
        this.getServer().getPluginManager()
            .registerEvents(injector.getInstance(WorldListener.class), this);
        this.getServer().getPluginManager()
            .registerEvents(injector.getInstance(PlayerListener.class), this);

        if (hyperConfiguration.shouldEnablePerWorldInventories()) {
            System.out.println("Enabling listener");
            this.getServer().getPluginManager()
                    .registerEvents(injector.getInstance(InventoryListener.class), this);
        }

        // Create the command manager instance
        injector.getInstance(HyperCommandManager.class);

        // Initialize bStats metrics tracking
        new Metrics(this, BSTATS_ID);
    }

    @Override public void onDisable() {
        this.hyperDatabase.attemptClose();
    }

}
