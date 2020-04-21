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

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import se.hyperver.hyperverse.Hyperverse;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigRenderOptions;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.AbstractConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * {@inheritDoc}
 */
@Singleton
public class FileHyperConfiguration implements HyperConfiguration {

    private final boolean importAutomatically;
    private final boolean persistLocations;
    private final boolean keepSpawnLoaded;
    private final boolean groupProfiles;

    @Inject public FileHyperConfiguration(@NotNull final Hyperverse hyperverse) {
        final File configFile = new File(hyperverse.getDataFolder(), "hyperverse.conf");
        final AbstractConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
            .setParseOptions(ConfigParseOptions.defaults().setClassLoader(hyperverse.getClass().getClassLoader()))
            .setRenderOptions(ConfigRenderOptions.defaults().setComments(true).setFormatted(true).setOriginComments(false).setJson(false))
            .setDefaultOptions(ConfigurationOptions.defaults()).setFile(configFile).build();
        FileConfigurationObject configObject = null;
        ConfigurationNode configurationNode;
        try {
            configurationNode = loader.load();
        } catch (final IOException e) {
            e.printStackTrace();
            configurationNode = loader.createEmptyNode();
        }
        if (!configFile.exists()) {
            configObject = new FileConfigurationObject();
            try {
                final CommentedConfigurationNode defaultNode = loader.createEmptyNode();
                defaultNode.setComment("");
                loader.save(defaultNode.setValue(TypeToken.of(FileConfigurationObject.class),
                    configObject));
            } catch (final IOException | ObjectMappingException e) {
                e.printStackTrace();
            }
        } else {
            try {
                configObject = configurationNode
                    .getValue(TypeToken.of(FileConfigurationObject.class), new FileConfigurationObject());
            } catch (final ObjectMappingException e) {
                e.printStackTrace();
            }
        }
        this.importAutomatically = configObject.isImportAutomatically();
        this.keepSpawnLoaded = configObject.isKeepSpawnLoaded();
        this.persistLocations = configObject.isPersistLocations();
        this.groupProfiles = configObject.useGroupedProfiles();
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

    @Override public boolean shouldGroupProfiles() {
        return this.groupProfiles;
    }

}
