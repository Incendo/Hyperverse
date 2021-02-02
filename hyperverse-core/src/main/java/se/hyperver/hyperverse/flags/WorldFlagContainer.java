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

import com.google.common.collect.ImmutableMap;
import com.google.inject.assistedinject.Assisted;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("TypeParameterUnusedInFormals")
public class WorldFlagContainer implements FlagContainer {

    private final Map<String, String> unknownFlags = new HashMap<>();
    private final Map<Class<?>, WorldFlag<?, ?>> flagMap = new HashMap<>();
    private final WorldFlagUpdateHandler worldFlagUpdateHandler;
    private final Collection<WorldFlagUpdateHandler> updateSubscribers = new ArrayList<>();
    private FlagContainer parentContainer;

    /**
     * Construct a new flag container with an optional parent container and update handler.
     * Default values are inherited from the parent container. At the top
     * of the parent-child hierarchy must be the {@link GlobalWorldFlagContainer}
     * (or an equivalent top level flag container).
     *
     * @param parentContainer        Parent container. The top level flag container should not have a parent,
     *                               and can set this parameter to null. If this is not a top level
     *                               flag container, the parent should not be null.
     * @param worldFlagUpdateHandler Event handler that will be called whenever a world flag is
     *                               added, removed or updated in this flag container.
     */
    @Inject
    public WorldFlagContainer(
            final GlobalWorldFlagContainer parentContainer,
            @Assisted final @NonNull WorldFlagUpdateHandler worldFlagUpdateHandler
    ) {
        this.parentContainer = parentContainer;
        this.worldFlagUpdateHandler = worldFlagUpdateHandler;
        if (parentContainer != null) {
            parentContainer.subscribe(this::handleUnknowns);
        }
    }

    /**
     * Cast a world flag with wildcard parameters into a parametrisized
     * PlotFlag. This is an unsafe operation, and should only be performed
     * if the generic parameters are known beforehand.
     *
     * @param flag Flag instance
     * @param <V>  Flag value type
     * @param <T>  Flag type
     * @return Casted flag
     */
    @SuppressWarnings("unchecked")
    public static <V, T extends WorldFlag<V, ?>> T castUnsafe(
            final @NonNull WorldFlag<?, ?> flag
    ) {
        return (T) flag;
    }

    @Override
    public final @Nullable FlagContainer getParentContainer() {
        return this.parentContainer;
    }

    @Override
    public final void setParentContainer(final @NonNull FlagContainer parentContainer) {
        this.parentContainer = parentContainer;
    }

    @Override
    @SuppressWarnings("unused")
    public final @NonNull Map<Class<?>, WorldFlag<?, ?>> getInternalWorldFlagMap() {
        return this.flagMap;
    }

    @Override
    public final @NonNull Map<Class<?>, WorldFlag<?, ?>> getFlagMap() {
        return ImmutableMap.<Class<?>, WorldFlag<?, ?>>builder().putAll(this.flagMap).build();
    }

    @Override
    public final <V, T extends WorldFlag<V, ?>> void addFlag(final @NonNull T flag) {
        final WorldFlag<?, ?> oldInstance = this.flagMap.put(flag.getClass(), flag);
        final WorldFlagUpdateType worldFlagUpdateType;
        if (oldInstance != null) {
            worldFlagUpdateType = WorldFlagUpdateType.FLAG_UPDATED;
        } else {
            worldFlagUpdateType = WorldFlagUpdateType.FLAG_ADDED;
        }
        if (this.worldFlagUpdateHandler != null) {
            this.worldFlagUpdateHandler.handle(flag, worldFlagUpdateType);
        }
        this.updateSubscribers.forEach(subscriber -> subscriber.handle(flag, worldFlagUpdateType));
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <V, T extends WorldFlag<V, ?>> @Nullable V removeFlag(final @NonNull T flag) {
        final Object value = this.flagMap.remove(flag.getClass());
        if (this.worldFlagUpdateHandler != null) {
            this.worldFlagUpdateHandler.handle(flag, WorldFlagUpdateType.FLAG_REMOVED);
        }
        this.updateSubscribers
                .forEach(subscriber -> subscriber.handle(flag, WorldFlagUpdateType.FLAG_REMOVED));
        if (value == null) {
            return null;
        } else {
            return (V) value;
        }
    }

    @Override
    public final void addAll(final @NonNull Collection<WorldFlag<?, ?>> flags) {
        for (final WorldFlag<?, ?> flag : flags) {
            this.addFlag(flag);
        }
    }

    @Override
    public final void addAll(final @NonNull FlagContainer container) {
        this.addAll(container.getFlagMap().values());
    }

    @Override
    public final void clearLocal() {
        this.flagMap.clear();
    }

    @Override
    public final @NonNull Collection<WorldFlag<?, ?>> getRecognizedWorldFlags() {
        return this.getHighestClassContainer().getFlagMap().values();
    }

    @Override
    public final @NonNull FlagContainer getHighestClassContainer() {
        if (this.getParentContainer() != null) {
            return this.getParentContainer();
        }
        return this;
    }

    /**
     * Has the same functionality as {@link #getFlag(Class)}, but
     * with wildcard generic types.
     *
     * @param flagClass The {@link WorldFlag} class.
     */
    @Override
    public @Nullable WorldFlag<?, ?> getFlagErased(final @NonNull Class<?> flagClass) {
        final WorldFlag<?, ?> flag = this.flagMap.get(flagClass);
        if (flag != null) {
            return flag;
        } else {
            if (this.getParentContainer() != null) {
                return this.getParentContainer().getFlagErased(flagClass);
            }
        }
        return null;
    }

    /**
     * Query all levels of flag containers for a flag. This guarantees that a flag
     * instance is returned, as long as it is registered in the
     * {@link GlobalWorldFlagContainer global flag container}.
     *
     * @param flagClass Flag class to query for
     * @param <V>       Flag value type
     * @param <T>       Flag type
     * @return Flag instance
     */
    @Override
    public @Nullable <V, T extends WorldFlag<V, ?>> T getFlag(final @NonNull Class<? extends T> flagClass) {
        final WorldFlag<?, ?> flag = this.flagMap.get(flagClass);
        if (flag != null) {
            return castUnsafe(flag);
        } else {
            if (this.getParentContainer() != null) {
                return this.getParentContainer().getFlag(flagClass);
            }
        }
        return null;
    }

    @Override
    public final @Nullable <V, T extends WorldFlag<V, ?>> T queryLocal(final @NonNull Class<?> flagClass) {
        final WorldFlag<?, ?> localFlag = this.flagMap.get(flagClass);
        if (localFlag == null) {
            return null;
        } else {
            return castUnsafe(localFlag);
        }
    }

    @Override
    public final void subscribe(
            final FlagContainer.@NonNull WorldFlagUpdateHandler worldFlagUpdateHandler
    ) {
        this.updateSubscribers.add(worldFlagUpdateHandler);
    }

    @Override
    public final void handleUnknowns(
            final @NonNull WorldFlag<?, ?> flag,
            final @NonNull WorldFlagUpdateType worldFlagUpdateType
    ) {
        if (worldFlagUpdateType != WorldFlagUpdateType.FLAG_REMOVED && this.unknownFlags
                .containsKey(flag.getName())) {
            final String value = this.unknownFlags.remove(flag.getName());
            if (value != null) {
                try {
                    this.addFlag(flag.parse(value));
                } catch (final Exception ignored) {
                }
            }
        }
    }

    @Override
    public final void addUnknownFlag(
            final @NonNull String flagName,
            final @NonNull String value
    ) {
        this.unknownFlags.put(flagName.toLowerCase(Locale.ENGLISH), value);
    }

}
