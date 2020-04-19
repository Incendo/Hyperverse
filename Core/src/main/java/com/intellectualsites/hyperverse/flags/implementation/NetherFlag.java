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

package com.intellectualsites.hyperverse.flags.implementation;

import com.intellectualsites.hyperverse.configuration.Messages;
import com.intellectualsites.hyperverse.flags.FlagParseException;
import com.intellectualsites.hyperverse.flags.WorldFlag;
import com.intellectualsites.hyperverse.util.WorldUtil;
import org.jetbrains.annotations.NotNull;

public class NetherFlag extends WorldFlag<String, NetherFlag> {

    public static final NetherFlag NETHER_FLAG_DEFAULT = new NetherFlag("");

    private NetherFlag(@NotNull final String value) {
        super(value, Messages.flagDescriptionNether);
    }

    @Override public NetherFlag parse(@NotNull final String input) throws FlagParseException {
        if (WorldUtil.validateName(input)) {
            return flagOf(input);
        }
        throw new FlagParseException(this, input, "A world name may only contain (up to) 16 alphanumerical characters, - and _");
    }

    @Override public NetherFlag merge(@NotNull String newValue) {
        return flagOf(newValue);
    }

    @Override public String toString() {
        return this.getValue();
    }

    @Override public String getExample() {
        return "nether_world";
    }

    @Override protected NetherFlag flagOf(@NotNull final String value) {
        return new NetherFlag(value);
    }

}
