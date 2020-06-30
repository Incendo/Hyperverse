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

package se.hyperver.hyperverse.flags.implementation;

import se.hyperver.hyperverse.configuration.Message;
import se.hyperver.hyperverse.flags.FlagParseException;
import se.hyperver.hyperverse.flags.WorldFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public abstract class BooleanFlag<F extends WorldFlag<Boolean, F>> extends WorldFlag<Boolean, F> {

    private static final Collection<String> positiveValues =
        Arrays.asList("1", "yes", "allow", "true");
    private static final Collection<String> negativeValues =
        Arrays.asList("0", "no", "deny", "disallow", "false");

    /**
     * Construct a new flag instance.
     *
     * @param value       Flag value
     * @param description Flag description
     */
    protected BooleanFlag(final boolean value, final Message description) {
        super(value,  description);
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

    @Override public F parse(@NotNull String input) throws FlagParseException {
        if (positiveValues.contains(input.toLowerCase(Locale.ENGLISH))) {
            return this.flagOf(true);
        } else if (negativeValues.contains(input.toLowerCase(Locale.ENGLISH))) {
            return this.flagOf(false);
        } else {
            throw new FlagParseException(this, input, "The value must be a boolean value (true/false)");
        }
    }

    @Override public F merge(@NotNull Boolean newValue) {
        return this.flagOf(getValue() || newValue);
    }

    @Override public String getExample() {
        return "true";
    }

    @Override public String toString() {
        return this.getValue().toString();
    }

    @Override public Collection<String> getTabCompletions() {
        return Arrays.asList("true", "false");
    }

    @Override public @NotNull String getValueAsString() {
        return getValue().toString();
    }
}
