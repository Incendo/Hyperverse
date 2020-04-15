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

package com.intellectualsites.hyperverse;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.Stage;
import com.intellectualsites.hyperverse.commands.HyperCommandManager;
import com.intellectualsites.hyperverse.listeners.WorldListener;
import com.intellectualsites.hyperverse.modules.HyperverseModule;
import com.intellectualsites.hyperverse.modules.TaskChainModule;
import com.intellectualsites.hyperverse.world.WorldManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

@Singleton
public final class Hyperverse extends JavaPlugin {

    public static final int BSTATS_ID = 7177;

    private WorldManager worldManager;
    private Injector injector;

    @Override public void onEnable() {
        this.injector = Guice.createInjector(Stage.PRODUCTION, new HyperverseModule(), new TaskChainModule(this));
        this.worldManager = injector.getInstance(WorldManager.class);
        this.worldManager.loadWorlds();
        this.getServer().getPluginManager()
            .registerEvents(injector.getInstance(WorldListener.class), this);
        // Create the command manager instance
        injector.getInstance(HyperCommandManager.class);
        // Initialize bStats metrics tracking
        new Metrics(this, BSTATS_ID);
    }

    @Override public void onDisable() {
    }

}
