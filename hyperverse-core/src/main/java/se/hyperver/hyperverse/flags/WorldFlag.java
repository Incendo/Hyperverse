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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.configuration.Message;

import java.util.Collection;
import java.util.Collections;

public abstract class WorldFlag<T, F extends WorldFlag<T, F>> {

    private final T value;
    private final Message flagDescription;
    private final String flagName;

    protected WorldFlag(final @NonNull T value, final @NonNull Message flagDescription) {
        this.value = value;
        this.flagDescription = flagDescription;
        final StringBuilder flagName = new StringBuilder();
        final char[] chars = this.getClass().getSimpleName().replace("Flag", "").toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                flagName.append(Character.toLowerCase(chars[i]));
            } else if (Character.isUpperCase(chars[i])) {
                flagName.append('-').append(Character.toLowerCase(chars[i]));
            } else {
                flagName.append(chars[i]);
            }
        }
        this.flagName = flagName.toString();
    }

    /**
     * Get the flag value
     *
     * @return Non-nullable flag value
     */
    @NonNull
    public final T getValue() {
        return this.value;
    }

    /**
     * Parse a string into a flag, and throw an exception in the case that the
     * string does not represent a valid flag value. This instance won't change its
     * state, but instead an instance holding the parsed flag value will be returned.
     *
     * @param input String to parse.
     * @return Parsed value, if valid.
     * @throws FlagParseException If the value could not be parsed.
     */
    public abstract F parse(final @NonNull String input) throws FlagParseException;

    /**
     * Merge this flag's value with another value and return an instance
     * holding the merged value.
     *
     * @param newValue New flag value.
     * @return Flag containing parsed flag value.
     */
    public abstract F merge(final @NonNull T newValue);

    /**
     * Returns a string representation of the flag instance, that when
     * passed through {@link #parse(String)} will result in an equivalent
     * instance of the flag.
     *
     * @return String representation of the flag
     */
    @Override
    public abstract String toString();

    /**
     * Get the flag name.
     *
     * @return Flag name
     */
    public final String getName() {
        return this.flagName;
    }

    /**
     * Get a simple caption that describes the flag usage.
     *
     * @return Flag description.
     */
    public Message getFlagDescription() {
        return this.flagDescription;
    }

    /**
     * An example of a string that would parse into a valid
     * flag value.
     *
     * @return An example flag value.
     */
    public abstract String getExample();

    protected abstract F flagOf(@NonNull T value);

    /**
     * Create a new instance of the flag using a provided
     * (non-null) value.
     *
     * @param value The flag value
     * @return The created flag instance
     */
    public final F createFlagInstance(final @NonNull T value) {
        return this.flagOf(Preconditions.checkNotNull(value));
    }

    /**
     * Get the tab completable values associated with the flag type, or
     * an empty collection if tab completion isn't supported.
     *
     * @return Collection containing tab completable flag values
     */
    public Collection<String> getTabCompletions() {
        return Collections.emptyList();
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WorldFlag<?, ?> worldFlag = (WorldFlag<?, ?>) o;
        return Objects.equal(this.value, worldFlag.value);
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(this.value);
    }

}
