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

package se.hyperver.hyperverse.util;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Generator utility methods
 */
public final class GeneratorUtil {

    private static Method generatorGetter;
    private static Class<?> pluginClassLoaderClass;
    private static Field pluginGetter;

    private GeneratorUtil() {
    }

    /**
     * Attempt to find the generator for a given world name
     *
     * @param world world name
     * @return Generator, if found
     */
    public static @Nullable ChunkGenerator getGenerator(final @NonNull String world)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (generatorGetter == null) {
            final Class<? extends Server> serverClass = Bukkit.getServer().getClass();
            generatorGetter = serverClass.getDeclaredMethod("getGenerator", String.class);
            generatorGetter.setAccessible(true);
        }
        return (ChunkGenerator) generatorGetter
                .invoke(Bukkit.getServer(), Objects.requireNonNull(world));
    }

    /**
     * Attempt to find the generator plugin from a chunk generator instance
     *
     * @param generator Generator instance
     * @return Plugin, if found
     */
    public static @Nullable JavaPlugin matchGenerator(final @NonNull ChunkGenerator generator)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Objects.requireNonNull(generator);
        final ClassLoader classLoader = generator.getClass().getClassLoader();
        if (pluginClassLoaderClass == null) {
            pluginClassLoaderClass = Class.forName("org.bukkit.plugin.java.PluginClassLoader");
            pluginGetter = pluginClassLoaderClass.getDeclaredField("plugin");
            pluginGetter.setAccessible(true);
        }
        if (pluginClassLoaderClass.isInstance(classLoader)) {
            return (JavaPlugin) pluginGetter.get(classLoader);
        }
        return null;
    }

    /**
     * Check if there is an available generator with the given name
     *
     * @param generatorName Generator name
     * @return True if the generator is available, false if not
     */
    public static boolean isGeneratorAvailable(final @Nullable String generatorName) {
        if (generatorName == null
                || generatorName.isEmpty()
                || generatorName.equalsIgnoreCase("vanilla")) {
            return true;
        }
        final String pluginName;
        if (generatorName.contains(":")) {
            pluginName = generatorName.split(Pattern.quote(":"))[0];
        } else {
            pluginName = generatorName;
        }
        return Bukkit.getPluginManager().isPluginEnabled(pluginName);
    }

}
