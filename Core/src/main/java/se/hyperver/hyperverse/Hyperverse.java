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

package se.hyperver.hyperverse;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;
import io.papermc.lib.PaperLib;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.AbstractConfigurationLoader;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import se.hyperver.hyperverse.commands.HyperCommandManager;
import se.hyperver.hyperverse.configuration.FileHyperConfiguration;
import se.hyperver.hyperverse.configuration.HyperConfiguration;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.database.HyperDatabase;
import se.hyperver.hyperverse.listeners.EventListener;
import se.hyperver.hyperverse.listeners.WorldListener;
import se.hyperver.hyperverse.modules.HyperverseModule;
import se.hyperver.hyperverse.modules.TaskChainModule;
import se.hyperver.hyperverse.util.MessageUtil;
import se.hyperver.hyperverse.world.WorldManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Plugin main class
 * {@inheritDoc}
 */
@Singleton public final class Hyperverse extends JavaPlugin implements HyperverseAPI {

    public static final int BSTATS_ID = 7177;

    private static HyperverseAPI instance;
    private WorldManager worldManager;
    private Injector injector;
    private HyperDatabase hyperDatabase;
    private HyperConfiguration hyperConfiguration;

    /**
     * Get the (singleton) implementation of
     * {@link HyperverseAPI}
     *
     * @return API implementation
     */
    public static HyperverseAPI getApi() {
        return instance;
    }

    @Override public void onLoad() {
        instance = this;
    }

    @Override public void onEnable() {
        // Disgusting try-catch mess below, but Guice freaks out completely if it encounters
        // any errors, and is unable to report them because of the plugin class loader
        if (!this.getDataFolder().exists()) {
            if (!this.getDataFolder().mkdir()) {
                throw new RuntimeException("Could not create Hyperverse main directory");
            }
        }

        try {
            this.injector = Guice.createInjector(Stage.PRODUCTION, new HyperverseModule(), new TaskChainModule(this));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        if (!this.loadConfiguration()) {
            getLogger().severe("Failed to load configuration file. Disabling!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Try to copy stored captions files
        if (!this.hyperConfiguration.getLanguageCode().equalsIgnoreCase("en")) {
            try {
                this.attemptCopyCaptions(this.hyperConfiguration.getLanguageCode());
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        if (!this.loadMessages(this.hyperConfiguration.getLanguageCode())) {
            getLogger().severe("Failed to load messages");
        }

        if (this.hyperConfiguration.shouldGroupProfiles()) {
            getLogger().warning("------------------ WARNING ------------------");
            getLogger().warning("Per-world player data is still very experimental.");
            getLogger().warning("This may cause your server to freeze, crash, etc.");
            getLogger().warning("Use at your own risk!");
            getLogger().warning("------------------ WARNING ------------------");
        }

        if (!this.loadDatabase()) {
            getLogger().severe("Failed to connect to the database. Disabling!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!this.loadWorldManager()) {
            getLogger().severe("Failed to load world manager. Disabling!");
            try {
                this.worldManager = injector.getInstance(WorldManager.class);
                this.worldManager.loadWorlds();
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        // Register events
        try {
            this.getServer().getPluginManager().registerEvents(injector.getInstance(WorldListener.class), this);
            this.getServer().getPluginManager().registerEvents(injector.getInstance(EventListener.class), this);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // Create the command manager instance
        try {
            injector.getInstance(HyperCommandManager.class);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // Initialize bStats metrics tracking
        new Metrics(this, BSTATS_ID);

        // Add paper suggestion
        PaperLib.suggestPaper(this);
    }

    @Override public void onDisable() {
        this.hyperDatabase.attemptClose();
    }

    private boolean loadConfiguration() {
        try {
            this.hyperConfiguration = this.injector.getInstance(HyperConfiguration.class);
            this.getLogger().info("§6Hyperverse Options");
            this.getLogger().info("§8- §7use persistent locations? " + this.hyperConfiguration.shouldPersistLocations());
            this.getLogger().info("§8- §7keep spawns loaded? " + this.hyperConfiguration.shouldKeepSpawnLoaded());
            this.getLogger().info("§8- §7should detect worlds? " + this.hyperConfiguration.shouldImportAutomatically());
            this.getLogger().info("§8- §7should separate player profiles? " + this.hyperConfiguration.shouldGroupProfiles());
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean loadWorldManager() {
        try {
            this.worldManager = injector.getInstance(WorldManager.class);
            this.worldManager.loadWorlds();
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean loadDatabase() {
        try {
            this.hyperDatabase = injector.getInstance(HyperDatabase.class);
            if (!this.hyperDatabase.attemptConnect()) {
                getLogger().severe("Failed to connect to database...");
                return false;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Reload the configuration and messages for hyperverse. If an invoker is
     * supplied, this method will also send a user-friendly message.
     *
     * @param invoker An optional CommandSender who invoked the reload.
     */
    public boolean reloadConfiguration(@Nullable final CommandSender invoker) {
        ((FileHyperConfiguration) this.hyperConfiguration).loadConfiguration();
        final boolean reloadMessages = loadMessages(this.hyperConfiguration.getLanguageCode());
        if (invoker != null) {
            MessageUtil.sendMessage(invoker, reloadMessages ?
                Messages.messageMessagesReloaded :
                Messages.messageMessageReloadFailed, "%reason%", ""); //Force a reload.
            MessageUtil.sendMessage(invoker, Messages.messageConfigReloaded);
        }
        return reloadMessages;
    }

    private boolean loadMessages(final String language) {
        // Message configuration
        final Path messagePath = this.getDataFolder().toPath().resolve(String.format("messages_%s.conf", language));
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
                return false;
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
            return false;
        }
        return true;
    }

    private void attemptCopyCaptions(final String code) {
        final String fileName = String.format("messages_%s.conf", code.toLowerCase());
        final Path path = this.getDataFolder().toPath().resolve(fileName);
        if (!Files.exists(path)) {
            this.saveResource(fileName, false);
        }
    }

    @Override @NotNull public WorldManager getWorldManager() {
        return this.worldManager;
    }

    @Override @NotNull public Injector getInjector() {
        return this.injector;
    }

    @Override @NotNull public HyperDatabase getDatabase() {
        return this.hyperDatabase;
    }

    @Override @NotNull public HyperConfiguration getConfiguration() {
        return this.hyperConfiguration;
    }

}
