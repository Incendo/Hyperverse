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

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import org.bukkit.Server;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Utility module binding commonly used bukkit interfaces to bukkit
 */
public final class BukkitModule extends AbstractModule {

    private final Server server;

    public BukkitModule(final @NonNull Server server) {
        this.server = server;
    }

    @Override
    protected void configure() {
        bind(Server.class).toInstance(this.server);
        bind(PluginManager.class).toProvider(this.server::getPluginManager).in(Singleton.class);
        bind(BukkitScheduler.class).toProvider(this.server::getScheduler).in(Singleton.class);
        bind(ItemFactory.class).toProvider(this.server::getItemFactory).in(Singleton.class);
        bind(ServicesManager.class).toProvider(this.server::getServicesManager).in(Singleton.class);
        bind(ScoreboardManager.class).toProvider(this.server::getScoreboardManager).in(Singleton.class);
    }

}
