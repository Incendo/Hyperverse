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

package com.intellectualsites.hyperverse.flags;

import com.intellectualsites.hyperverse.flags.implementation.GamemodeFlag;
import com.intellectualsites.hyperverse.flags.implementation.LocalRespawnFlag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class GlobalWorldFlagContainer extends WorldFlagContainer {

    private static Map<String, Class<?>> stringClassMap;

    public GlobalWorldFlagContainer() {
        super(null, (flag, type) -> {
            if (type == FlagContainer.WorldFlagUpdateType.FLAG_ADDED) {
                stringClassMap.put(flag.getName().toLowerCase(Locale.ENGLISH), flag.getClass());
            }
        });
        stringClassMap = new HashMap<>();
        // Register all default flags here
        this.addFlag(GamemodeFlag.GAMEMODE_SURVIVAL);
        this.addFlag(LocalRespawnFlag.RESPAWN_FALSE);
    }

    @Override public WorldFlag<?, ?> getFlagErased(Class<?> flagClass) {
        final WorldFlag<?, ?> flag = super.getFlagErased(flagClass);
        if (flag != null) {
            return flag;
        } else {
            throw new IllegalStateException(String.format("Unrecognized flag '%s'. All flag types"
                + " must be present in the global flag container.", flagClass.getSimpleName()));
        }
    }

    @NotNull @Override
    public <V, T extends WorldFlag<V, ?>> T getFlag(Class<? extends T> flagClass) {
        final WorldFlag<?, ?> flag = super.getFlag(flagClass);
        if (flag != null) {
            return castUnsafe(flag);
        } else {
            throw new IllegalStateException(String.format("Unrecognized flag '%s'. All flag types"
                + " must be present in the global flag container.", flagClass.getSimpleName()));
        }
    }

    public Class<?> getFlagClassFromString(final String name) {
        return stringClassMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    public WorldFlag<?, ?> getFlagFromString(final String name) {
        final Class<?> flagClass = this.getFlagClassFromString(name);
        if (flagClass == null) {
            return null;
        }
        return getFlagErased(flagClass);
    }

}
