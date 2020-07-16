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

package se.hyperver.hyperverse.features;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.util.MessageUtil;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * Manager for {@link PluginFeature plugin features}
 */
public class PluginFeatureManager {

    private final Map<String, Class<? extends PluginFeature>> registeredFeatures;
    private final Collection<String> loadedFeatures;

    private boolean loaded;

    public PluginFeatureManager() {
        this.registeredFeatures = Maps.newHashMap();
        this.loadedFeatures = Sets.newHashSet();
        this.loaded = false;
    }

    public void registerFeature(@NotNull final String pluginName, @NotNull final Class<? extends PluginFeature> featureClass) {
        Constructor<?> constructor = null;
        try {
            constructor = featureClass.getConstructor();
        } catch (final NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (constructor == null) {
            throw new IllegalArgumentException("The class needs a public no-args constructor");
        }
        this.registeredFeatures.put(pluginName, featureClass);
        if (this.loaded && isPluginPresent(pluginName)) {
            this.loadFeature(pluginName);
        }
    }

    /**
     * Load all features
     */
    public void loadFeatures() {
        MessageUtil.sendMessage(Bukkit.getConsoleSender(), Messages.messageFeaturesLoading, "%num%",
            Integer.toString(this.registeredFeatures.size()));
        for (final String feature : this.registeredFeatures.keySet()) {
            if (isPluginPresent(feature)) {
                this.loadFeature(feature);
            }
        }
        this.loaded = true;
    }

    /**
     * Check if a plugin is enabled on the server
     *
     * @param name Plugin name
     * @return True if the plugin is present
     */
    public boolean isPluginPresent(@NotNull final String name) {
        return Bukkit.getPluginManager().isPluginEnabled(name);
    }

    /**
     * Load a specific feature.
     *
     * @param name Name of the plugin. It has to be enabled on the
     *             server
     */
    public void loadFeature(@NotNull final String name) {
        if (this.loadedFeatures.contains(name)) {
            return;
        }
        try {
            final Constructor<? extends PluginFeature> featureConstructor = this.registeredFeatures.get(name)
                .getConstructor();
            final PluginFeature feature = featureConstructor.newInstance();
            feature.initializeFeature();

            MessageUtil.sendMessage(Bukkit.getConsoleSender(), Messages.messageFeatureLoaded, "%plugin%", name,
                "%feature%", featureConstructor.getDeclaringClass().getSimpleName());
        } catch (final Exception e) {
            throw new IllegalStateException("Cannot initialize plugin feature", e);
        }
        this.loadedFeatures.add(name);
    }

    @NotNull public Collection<String> getRegisteredFeatures() {
        return new HashSet<>(registeredFeatures.keySet());
    }

}
