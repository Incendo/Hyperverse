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

package com.intellectualsites.hyperverse.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

@UtilityClass public class GeneratorUtil {

    private static Method generatorGetter;
    private static Class<?> pluginClassLoaderClass;
    private static Field pluginGetter;

    @Nullable public static ChunkGenerator getGenerator(@NotNull final String world)
        throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if (generatorGetter == null) {
            final Class<? extends Server> serverClass = Bukkit.getServer().getClass();
            generatorGetter = serverClass.getDeclaredMethod("getGenerator", String.class);
        }
        return (ChunkGenerator) generatorGetter.invoke(Bukkit.getServer(), Objects.requireNonNull(world));
    }

    @Nullable public static JavaPlugin matchGenerator(@NotNull final ChunkGenerator generator)
        throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Objects.requireNonNull(generator);
        final ClassLoader classLoader = generator.getClass().getClassLoader();
        if (pluginClassLoaderClass == null) {
            pluginClassLoaderClass = Class.forName("org.bukkit.plugin.java.PluginClassLoader");
            pluginGetter = pluginClassLoaderClass.getDeclaredField("plugin");
        }
        if (pluginClassLoaderClass.isInstance(classLoader)) {
            return (JavaPlugin) pluginGetter.get(classLoader);
        }
        return null;
    }

}
