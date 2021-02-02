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

package se.hyperver.hyperverse.flags.implementation;

import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.configuration.Messages;

public final class UnloadSpawnFlag extends BooleanFlag<UnloadSpawnFlag> {

    public static final UnloadSpawnFlag UNLOAD_SPAWN_FALSE = new UnloadSpawnFlag(false);
    public static final UnloadSpawnFlag UNLOAD_SPAWN_TRUE = new UnloadSpawnFlag(true);

    public UnloadSpawnFlag(final boolean value) {
        super(value, Messages.flagDescriptionUnloadSpawn);
    }

    @Override
    protected UnloadSpawnFlag flagOf(final @NonNull Boolean value) {
        return value ? UNLOAD_SPAWN_TRUE : UNLOAD_SPAWN_FALSE;
    }

}
