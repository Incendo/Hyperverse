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
import se.hyperver.hyperverse.flags.FlagParseException;
import se.hyperver.hyperverse.flags.WorldFlag;

import java.util.regex.Pattern;

public final class WorldPermissionFlag extends WorldFlag<String, WorldPermissionFlag> {

    public static final WorldPermissionFlag WORLD_PERMISSION_FLAG_DEFAULT = new WorldPermissionFlag("");
    private static final Pattern PERMISSION_PATTERN = Pattern.compile("[A-Za-z0-9\\-_.]+");

    public WorldPermissionFlag(final @NonNull String value) {
        super(value, Messages.flagDescriptionWorldPermission);
    }

    @Override
    public WorldPermissionFlag parse(final @NonNull String input) throws
            FlagParseException {
        if (input.isEmpty()) {
            return WORLD_PERMISSION_FLAG_DEFAULT;
        }
        if (PERMISSION_PATTERN.matcher(input).matches()) {
            return this.flagOf(input);
        }
        throw new FlagParseException(this, input, "A permission node may only contain alphanumerical characters,"
                + " -, . and _");
    }

    @Override
    public WorldPermissionFlag merge(final @NonNull String newValue) {
        return this.flagOf(newValue);
    }

    @Override
    public String toString() {
        return getValue();
    }

    @Override
    public String getExample() {
        return "your.permission.node";
    }

    @Override
    protected WorldPermissionFlag flagOf(final @NonNull String value) {
        return new WorldPermissionFlag(value);
    }

}
