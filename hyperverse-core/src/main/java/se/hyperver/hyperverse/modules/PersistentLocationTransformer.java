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

import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.database.PersistentLocation;

/**
 * Transforms a {@link PersistentLocation} to a Bukkit {@link Location}.
 * This class exists so that the {@link org.bukkit.Bukkit} static server singleton
 * isn't used to lookup worlds.
 */
@FunctionalInterface
public interface PersistentLocationTransformer {

    @NonNull Location transform(@NonNull PersistentLocation persistentLocation);

}
