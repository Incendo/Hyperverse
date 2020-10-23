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

package se.hyperver.hyperverse.modules;

import cloud.commandframework.tasks.TaskFactory;
import cloud.commandframework.tasks.TaskSynchronizer;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Objects;

public class TaskFactoryModule extends AbstractModule {

    private final JavaPlugin javaPlugin;

    public TaskFactoryModule(@NotNull final JavaPlugin javaPlugin) {
        this.javaPlugin = Objects.requireNonNull(javaPlugin);
    }

    @Override protected void configure() { }

    @Provides
    @Singleton
    public TaskFactory provideTaskFactory() {
        final TaskSynchronizer synchronizer;
        try {
            final Constructor<?> constructor = Class.forName("cloud.commandframework.bukkit.BukkitSynchronizer").getDeclaredConstructor(Plugin.class);
            constructor.setAccessible(true);
            synchronizer = (TaskSynchronizer) constructor.newInstance(javaPlugin);
        } catch(final ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
        return new TaskFactory(synchronizer);
    }

}
