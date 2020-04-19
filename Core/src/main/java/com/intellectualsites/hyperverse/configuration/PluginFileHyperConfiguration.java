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

package com.intellectualsites.hyperverse.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.intellectualsites.hyperverse.Hyperverse;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * {@inheritDoc}
 */
@Singleton
public class PluginFileHyperConfiguration implements HyperConfiguration {

    private final boolean importAutomatically;
    private final boolean persistLocations;
    private final boolean keepSpawnLoaded;

    @Inject public PluginFileHyperConfiguration(@NotNull final Hyperverse hyperverse) {
        hyperverse.saveDefaultConfig();
        final FileConfiguration config = hyperverse.getConfig();
        if (!config.contains("worlds.import-automatically")) {
            config.set("worlds.import-automatically", true);
        }
        this.importAutomatically = config.getBoolean("worlds.import-automatically", true);
        if (!config.contains("worlds.persist-locations")) {
            config.set("worlds.persist-locations", true);
        }
        this.persistLocations = config.getBoolean("worlds.persist-locations", true);
        if (!config.contains("worlds.keep-loaded")) {
            config.set("worlds.keep-loaded", true);
        }
        this.keepSpawnLoaded = config.getBoolean("worlds.keep-loaded", true);
        hyperverse.saveConfig();
    }

    @Override public boolean shouldImportAutomatically() {
        return this.importAutomatically;
    }

    @Override public boolean shouldPersistLocations() {
        return this.persistLocations;
    }

    @Override public boolean shouldKeepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }

}
