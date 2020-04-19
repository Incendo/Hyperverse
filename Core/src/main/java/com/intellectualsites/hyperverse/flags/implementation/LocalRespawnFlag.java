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

package com.intellectualsites.hyperverse.flags.implementation;

import com.intellectualsites.hyperverse.configuration.Messages;
import org.jetbrains.annotations.NotNull;

public class LocalRespawnFlag extends BooleanFlag<LocalRespawnFlag> {

    public static LocalRespawnFlag RESPAWN_TRUE = new LocalRespawnFlag(true);
    public static LocalRespawnFlag RESPAWN_FALSE = new LocalRespawnFlag(false);

    private LocalRespawnFlag(final boolean value) {
        super(value, Messages.flagDescriptionLocalRespawn);
    }

    @Override protected LocalRespawnFlag flagOf(@NotNull final Boolean value) {
        return value ? RESPAWN_TRUE : RESPAWN_FALSE;
    }

}
