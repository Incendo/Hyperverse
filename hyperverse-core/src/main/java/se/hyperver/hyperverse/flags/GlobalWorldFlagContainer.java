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

package se.hyperver.hyperverse.flags;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import se.hyperver.hyperverse.flags.implementation.AdvancementFlag;
import se.hyperver.hyperverse.flags.implementation.AliasFlag;
import se.hyperver.hyperverse.flags.implementation.CreatureSpawnFlag;
import se.hyperver.hyperverse.flags.implementation.DifficultyFlag;
import se.hyperver.hyperverse.flags.implementation.EndFlag;
import se.hyperver.hyperverse.flags.implementation.ForceSpawn;
import se.hyperver.hyperverse.flags.implementation.GamemodeFlag;
import se.hyperver.hyperverse.flags.implementation.IgnoreBedsFlag;
import se.hyperver.hyperverse.flags.implementation.LocalRespawnFlag;
import se.hyperver.hyperverse.flags.implementation.MobSpawnFlag;
import se.hyperver.hyperverse.flags.implementation.NetherFlag;
import se.hyperver.hyperverse.flags.implementation.ProfileGroupFlag;
import se.hyperver.hyperverse.flags.implementation.PveFlag;
import se.hyperver.hyperverse.flags.implementation.PvpFlag;
import se.hyperver.hyperverse.flags.implementation.RespawnWorldFlag;
import se.hyperver.hyperverse.flags.implementation.SaveWorldFlag;
import se.hyperver.hyperverse.flags.implementation.UnloadSpawnFlag;
import se.hyperver.hyperverse.flags.implementation.WorldPermissionFlag;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class GlobalWorldFlagContainer extends WorldFlagContainer {

    private static Map<String, Class<?>> stringClassMap = new HashMap<>();

    public GlobalWorldFlagContainer() {
        super(null, (flag, type) -> {
            if (type == WorldFlagUpdateType.FLAG_ADDED) {
                stringClassMap.put(flag.getName().toLowerCase(Locale.ENGLISH), flag.getClass());
            }
        });
        // Register all default flags here
        this.addFlag(GamemodeFlag.GAMEMODE_SURVIVAL);
        this.addFlag(LocalRespawnFlag.RESPAWN_FALSE);
        this.addFlag(ForceSpawn.FORCE_SPAWN_FALSE);
        this.addFlag(PvpFlag.PVP_FLAG_TRUE);
        this.addFlag(PveFlag.PVE_FLAG_TRUE);
        this.addFlag(WorldPermissionFlag.WORLD_PERMISSION_FLAG_DEFAULT);
        this.addFlag(NetherFlag.NETHER_FLAG_DEFAULT);
        this.addFlag(EndFlag.END_FLAG_DEFAULT);
        this.addFlag(ProfileGroupFlag.PROFILE_GROUP_FLAG_EMPTY);
        this.addFlag(DifficultyFlag.DIFFICULTY_FLAG_NORMAL);
        this.addFlag(MobSpawnFlag.MOB_SPAWN_ALLOWED);
        this.addFlag(CreatureSpawnFlag.CREATURE_SPAWN_ALLOWED);
        this.addFlag(AdvancementFlag.ADVANCEMENTS_ALLOWED);
        this.addFlag(RespawnWorldFlag.RESPAWN_WORLD_FLAG_EMPTY);
        this.addFlag(IgnoreBedsFlag.IGNORE_BEDS_FALSE);
        this.addFlag(AliasFlag.ALIAS_NONE);
        this.addFlag(UnloadSpawnFlag.UNLOAD_SPAWN_FALSE);
        this.addFlag(SaveWorldFlag.SAVE_WORLD_TRUE);
    }

    @Override
    public @NonNull WorldFlag<?, ?> getFlagErased(final @NonNull Class<?> flagClass) {
        final WorldFlag<?, ?> flag = super.getFlagErased(flagClass);
        if (flag != null) {
            return flag;
        } else {
            throw new IllegalStateException(String.format("Unrecognized flag '%s'. All flag types"
                    + " must be present in the global flag container.", flagClass.getSimpleName()));
        }
    }

    @Override
    public <V, T extends WorldFlag<V, ?>> @NonNull T getFlag(final @NonNull Class<? extends T> flagClass) {
        final WorldFlag<?, ?> flag = super.getFlag(flagClass);
        if (flag != null) {
            return castUnsafe(flag);
        } else {
            throw new IllegalStateException(String.format("Unrecognized flag '%s'. All flag types"
                    + " must be present in the global flag container.", flagClass.getSimpleName()));
        }
    }

    public @Nullable Class<?> getFlagClassFromString(final @NonNull String name) {
        return stringClassMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    public @Nullable WorldFlag<?, ?> getFlagFromString(final @NonNull String name) {
        final Class<?> flagClass = this.getFlagClassFromString(name);
        if (flagClass == null) {
            return null;
        }
        return this.getFlagErased(flagClass);
    }

}
