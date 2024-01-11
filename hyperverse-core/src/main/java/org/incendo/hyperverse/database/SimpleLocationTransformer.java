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

package org.incendo.hyperverse.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Location;
import org.bukkit.Server;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.modules.PersistentLocationTransformer;

@Singleton
public final class SimpleLocationTransformer implements PersistentLocationTransformer {

    private final Server server;

    @Inject
    SimpleLocationTransformer(final @NonNull Server server) {
        this.server = server;
    }

    @Override
    public @NonNull Location transform(final @NonNull PersistentLocation persistent) {
        return new Location(this.server.getWorld(persistent.getWorld()), persistent.getX(), persistent.getY(), persistent.getZ());
    }

}
