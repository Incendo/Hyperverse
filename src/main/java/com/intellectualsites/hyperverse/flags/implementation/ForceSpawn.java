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

package com.intellectualsites.hyperverse.flags.implementation;

import com.intellectualsites.hyperverse.configuration.Messages;
import org.jetbrains.annotations.NotNull;

public class ForceSpawn extends BooleanFlag<ForceSpawn> {

    public static final ForceSpawn FORCE_SPAWN_TRUE  = new ForceSpawn(true);
    public static final ForceSpawn FORCE_SPAWN_FALSE = new ForceSpawn(false);

    private ForceSpawn(final boolean value) {
        super(value, Messages.flagDescriptionForceSpawn);
    }

    @Override protected ForceSpawn flagOf(@NotNull final Boolean value) {
        return value ? FORCE_SPAWN_TRUE : FORCE_SPAWN_FALSE;
    }

}
