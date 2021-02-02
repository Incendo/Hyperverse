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

package se.hyperver.hyperverse.modules;

import cloud.commandframework.bukkit.BukkitSynchronizer;
import cloud.commandframework.tasks.TaskFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

public final class TaskFactoryModule extends AbstractModule {

    private final JavaPlugin javaPlugin;

    public TaskFactoryModule(final @NonNull JavaPlugin javaPlugin) {
        this.javaPlugin = Objects.requireNonNull(javaPlugin);
    }

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public TaskFactory provideTaskFactory() {
        return new TaskFactory(new BukkitSynchronizer(this.javaPlugin));
    }

}
