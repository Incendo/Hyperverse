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

package se.hyperver.hyperverse.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import se.hyperver.hyperverse.Hyperverse;

import java.io.File;
import java.io.IOException;

/**
 * {@inheritDoc}
 */
@Singleton
public class FileHyperConfiguration implements HyperConfiguration {

    private FileConfigurationObject fileConfigurationObject;
    private final Hyperverse hyperverse;

    @Inject public FileHyperConfiguration(@NotNull final Hyperverse hyperverse) {
        this.hyperverse = hyperverse;
        this.loadConfiguration();
    }

    public void loadConfiguration() {
        final File configFile = new File(hyperverse.getDataFolder(), "hyperverse.conf");
        final AbstractConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader
            .builder()
                .emitComments(true)
                .prettyPrinting(true)
                .emitJsonCompatible(false)
                .file(configFile)
                .build();
        FileConfigurationObject configObject = null;
        ConfigurationNode configurationNode;
        try {
            configurationNode = loader.load();
        } catch (final IOException e) {
            e.printStackTrace();
            configurationNode = loader.createNode();
        }
        if (!configFile.exists()) {
            configObject = new FileConfigurationObject();
            try {
                final CommentedConfigurationNode defaultNode = loader.createNode();
                defaultNode.comment("");
                loader.save(defaultNode.set(FileConfigurationObject.class, configObject));
            } catch (final IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                configObject = configurationNode.get(FileConfigurationObject.class, new FileConfigurationObject());
            } catch (final SerializationException e) {
                e.printStackTrace();
            }
        }
        this.fileConfigurationObject = configObject;
    }

    @Override public boolean shouldImportAutomatically() {
        return this.fileConfigurationObject.isImportAutomatically();
    }

    @Override public boolean shouldPersistLocations() {
        return this.fileConfigurationObject.isPersistLocations();
    }

    @Override public boolean shouldKeepSpawnLoaded() {
        return this.fileConfigurationObject.isKeepSpawnLoaded();
    }

    @Override public boolean shouldGroupProfiles() {
        return this.fileConfigurationObject.useGroupedProfiles();
    }

    @Override @NotNull public String getLanguageCode() {
        return this.fileConfigurationObject.getLanguageCode();
    }

    @Override public boolean shouldSafeTeleport() {
        return this.fileConfigurationObject.shouldSafeTeleport();
    }

    @Override public boolean shouldHookEssentials() {
        return this.fileConfigurationObject.shouldHookEssentials();
    }

    @Override
    public boolean shouldPrintDebug() {
        return this.fileConfigurationObject.shouldPrintDebug();
    }

}
