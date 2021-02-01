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
import se.hyperver.hyperverse.configuration.Message;
import se.hyperver.hyperverse.flags.FlagParseException;
import se.hyperver.hyperverse.flags.WorldFlag;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public abstract class BooleanFlag<F extends WorldFlag<Boolean, F>> extends WorldFlag<Boolean, F> {

    private static final Collection<String> POSITIVE_VALUES =
            Arrays.asList("1", "yes", "allow", "true");
    private static final Collection<String> NEGATIVE_VALUES =
            Arrays.asList("0", "no", "deny", "disallow", "false");

    /**
     * Construct a new flag instance.
     *
     * @param value       Flag value
     * @param description Flag description
     */
    protected BooleanFlag(final boolean value, final Message description) {
        super(value, description);
    }

    /**
     * Construct a new boolean flag, with
     * {@code false} as the default value.
     *
     * @param description Flag description
     */
    protected BooleanFlag(final Message description) {
        this(false, description);
    }

    @Override
    public final F parse(final @NonNull String input) throws FlagParseException {
        if (POSITIVE_VALUES.contains(input.toLowerCase(Locale.ENGLISH))) {
            return this.flagOf(true);
        } else if (NEGATIVE_VALUES.contains(input.toLowerCase(Locale.ENGLISH))) {
            return this.flagOf(false);
        } else {
            throw new FlagParseException(this, input, "The value must be a boolean value (true/false)");
        }
    }

    @Override
    public final F merge(final @NonNull Boolean newValue) {
        return this.flagOf(getValue() || newValue);
    }

    @Override
    public final String getExample() {
        return "true";
    }

    @Override
    public final String toString() {
        return this.getValue().toString();
    }

    @Override
    public final Collection<String> getTabCompletions() {
        return Arrays.asList("true", "false");
    }

}
