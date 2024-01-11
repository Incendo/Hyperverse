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

package org.incendo.hyperverse.flags.implementation;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.Hyperverse;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.flags.FlagParseException;
import org.incendo.hyperverse.flags.WorldFlag;
import org.incendo.hyperverse.util.WorldUtil;
import org.incendo.hyperverse.world.HyperWorld;

import java.util.ArrayList;
import java.util.Collection;

public final class EndFlag extends WorldFlag<String, EndFlag> {

    public static final EndFlag END_FLAG_DEFAULT = new EndFlag("");

    private EndFlag(final @NonNull String value) {
        super(value, Messages.flagDescriptionEnd);
    }

    @Override
    public EndFlag parse(final @NonNull String input) throws FlagParseException {
        if (WorldUtil.validateName(input)) {
            return this.flagOf(input);
        }
        throw new FlagParseException(this, input, "A world name may only contain (up to) 16 alphanumerical characters, - and _");
    }

    @Override
    public EndFlag merge(final @NonNull String newValue) {
        return this.flagOf(newValue);
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    @Override
    public String getExample() {
        return "end_world";
    }

    @Override
    protected EndFlag flagOf(final @NonNull String value) {
        return new EndFlag(value);
    }

    @Override
    public Collection<String> getTabCompletions() {
        final Collection<HyperWorld> configurations = Hyperverse.getApi().getWorldManager().getWorlds();
        final Collection<String> worldNames = new ArrayList<>(configurations.size());
        for (final HyperWorld world : configurations) {
            worldNames.add(world.getDisplayName());
        }
        return worldNames;
    }

}
