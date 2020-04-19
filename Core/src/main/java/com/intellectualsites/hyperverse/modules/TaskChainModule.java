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

package com.intellectualsites.hyperverse.modules;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import com.google.inject.AbstractModule;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TaskChainModule extends AbstractModule {

    private final JavaPlugin javaPlugin;

    public TaskChainModule(@NotNull final JavaPlugin javaPlugin) {
        this.javaPlugin = Objects.requireNonNull(javaPlugin);
    }

    @Override protected void configure() {
        bind(TaskChainFactory.class).toInstance(BukkitTaskChainFactory.create(javaPlugin));
    }

}
