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
import com.intellectualsites.hyperverse.configuration.Messages;
import com.intellectualsites.hyperverse.database.HyperDatabase;
import com.intellectualsites.hyperverse.listeners.PlayerListener;
import com.intellectualsites.hyperverse.listeners.WorldListener;
import com.intellectualsites.hyperverse.modules.HyperverseModule;
import com.intellectualsites.hyperverse.modules.TaskChainModule;
import com.intellectualsites.hyperverse.world.WorldManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

        // Message configuration
        final Path messagePath = this.getDataFolder().toPath().resolve("messages.yml");
        if (!Files.exists(messagePath)) {
            try {
                Files.createFile(messagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        final FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(messagePath.toFile());
        final Map<String, String> messages = Messages.getConfiguredMessages();
        final Collection<String> messageKeys = new ArrayList<>(messages.keySet());
        for (final String key : messageKeys) {
            if (fileConfiguration.contains(key)) {
                messages.put(key, fileConfiguration.getString(key));
            } else {
                fileConfiguration.set(key, messages.get(key));
            }
        }
        try {
            fileConfiguration.save(messagePath.toFile());
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

        // Create the command manager instance
        injector.getInstance(HyperCommandManager.class);

        // Initialize bStats metrics tracking
        new Metrics(this, BSTATS_ID);
    }

    @Override public void onDisable() {
        this.hyperDatabase.attemptClose();
    }

}
