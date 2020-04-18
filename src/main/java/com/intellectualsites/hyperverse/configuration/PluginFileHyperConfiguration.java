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

    @Inject public PluginFileHyperConfiguration(@NotNull final Hyperverse hyperverse) {
        hyperverse.saveDefaultConfig();
        final FileConfiguration config = hyperverse.getConfig();
        this.importAutomatically = config.getBoolean("worlds.import-automatically", true);
        this.persistLocations = config.getBoolean("worlds.persist-locations", true);
    }

    @Override public boolean shouldImportAutomatically() {
        return this.importAutomatically;
    }

    @Override public boolean shouldPersistLocations() {
        return this.persistLocations;
    }
}
