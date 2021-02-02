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
import se.hyperver.hyperverse.Hyperverse;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.flags.FlagParseException;
import se.hyperver.hyperverse.flags.WorldFlag;
import se.hyperver.hyperverse.util.WorldUtil;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.WorldConfiguration;

import java.util.Collection;
import java.util.stream.Collectors;

public final class NetherFlag extends WorldFlag<String, NetherFlag> {

    public static final NetherFlag NETHER_FLAG_DEFAULT = new NetherFlag("");

    private NetherFlag(final @NonNull String value) {
        super(value, Messages.flagDescriptionNether);
    }

    @Override
    public NetherFlag parse(final @NonNull String input) throws FlagParseException {
        if (WorldUtil.validateName(input)) {
            return this.flagOf(input);
        }
        throw new FlagParseException(this, input, "A world name may only contain (up to) 16 alphanumerical characters, - and _");
    }

    @Override
    public NetherFlag merge(final @NonNull String newValue) {
        return this.flagOf(newValue);
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    @Override
    public String getExample() {
        return "nether_world";
    }

    @Override
    protected NetherFlag flagOf(final @NonNull String value) {
        return new NetherFlag(value);
    }

    @Override
    public Collection<String> getTabCompletions() {
        return Hyperverse.getApi().getWorldManager().getWorlds().stream()
                .map(HyperWorld::getConfiguration).map(WorldConfiguration::getName).collect(Collectors.toList());
    }

}
