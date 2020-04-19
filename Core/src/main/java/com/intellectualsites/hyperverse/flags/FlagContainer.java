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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public interface FlagContainer {

    /**
     * Return the parent container (if the container has a parent)
     *
     * @return Parent container, if it exists
     */
    @Nullable FlagContainer getParentContainer();

    void setParentContainer(FlagContainer parentContainer);

    @SuppressWarnings("unused") Map<Class<?>, WorldFlag<?, ?>> getInternalPlotFlagMap();

    /**
     * Get an immutable view of the underlying flag map
     *
     * @return Immutable flag map
     */
    Map<Class<?>, WorldFlag<?, ?>> getFlagMap();

    /**
     * Add a flag to the container
     *
     * @param flag Flag to add
     * @see #addAll(Collection) to add multiple flags
     */
    <V, T extends WorldFlag<V, ?>> void addFlag(T flag);

    /**
     * Remove a flag from the container
     *
     * @param flag Flag to remove
     */
    <V, T extends WorldFlag<V, ?>> V removeFlag(T flag);

    /**
     * Add all flags to the container
     *
     * @param flags Flags to add
     * @see #addFlag(WorldFlag) to add a single flagg
     */
    void addAll(Collection<WorldFlag<?, ?>> flags);

    void addAll(FlagContainer container);

    /**
     * Clears the local flag map
     */
    void clearLocal();

    /**
     * Get a collection of all recognized world flags. Will by
     * default use the values contained in {@link GlobalWorldFlagContainer}.
     *
     * @return All recognized flag types
     */
    Collection<WorldFlag<?, ?>> getRecognizedPlotFlags();

    /**
     * Recursively seek for the highest order flag container.
     * This will by default return {@link GlobalWorldFlagContainer}.
     *
     * @return Highest order class container.
     */
    FlagContainer getHighestClassContainer();

    /**
     * Has the same functionality as {@link #getFlag(Class)}, but
     * with wildcard generic types.
     *
     * @param flagClass The {@link WorldFlag} class.
     */
    WorldFlag<?, ?> getFlagErased(Class<?> flagClass);

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
    <V, T extends WorldFlag<V, ?>> T getFlag(Class<? extends T> flagClass);

    /**
     * Check for flag existence in this flag container instance.
     *
     * @param flagClass Flag class to query for
     * @param <V>       Flag value type
     * @param <T>       Flag type
     * @return The flag instance, if it exists in this container, else null.
     */
    @Nullable <V, T extends WorldFlag<V, ?>> T queryLocal(Class<?> flagClass);

    /**
     * Subscribe to flag updates in this particular flag container instance.
     * Updates are: a flag being removed, a flag being added or a flag
     * being updated.
     *
     * @param worldFlagUpdateHandler The update handler which will react to changes.
     * @see WorldFlagUpdateType World flag update types
     */
    void subscribe(@NotNull FlagContainer.WorldFlagUpdateHandler worldFlagUpdateHandler);

    void handleUnknowns(WorldFlag<?, ?> flag, WorldFlagUpdateType worldFlagUpdateType);

    /**
     * Register a flag key-value pair which cannot yet be associated with
     * an existing flag instance (such as when third party flag values are
     * loaded before the flag type has been registered).
     * <p>
     * These values will be registered in the flag container if the associated
     * flag type is registered in the top level flag container.
     *
     * @param flagName Flag name
     * @param value    Flag value
     */
    void addUnknownFlag(String flagName, String value);

    /**
     * Handler for update events in {@link WorldFlagContainer flag containers}.
     */
    @FunctionalInterface interface WorldFlagUpdateHandler {

        /**
         * Act on the flag update event
         *
         * @param worldFlag World flag
         * @param type      Update type
         */
        void handle(WorldFlag<?, ?> worldFlag, WorldFlagUpdateType type);

    }

    /**
     * Update event types used in {@link WorldFlagUpdateHandler}.
     */
    enum WorldFlagUpdateType {
        /**
         * A flag was added to a world container
         */
        FLAG_ADDED,
        /**
         * A flag was removed from a world container
         */
        FLAG_REMOVED,
        /**
         * A flag was already stored in this container,
         * but a new instance has bow replaced it
         */
        FLAG_UPDATED
    }

}
