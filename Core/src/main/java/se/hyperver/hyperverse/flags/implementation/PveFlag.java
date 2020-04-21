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

import se.hyperver.hyperverse.configuration.Messages;
import org.jetbrains.annotations.NotNull;

public class PveFlag extends BooleanFlag<PveFlag> {

    public static final PveFlag PVE_FLAG_TRUE  = new PveFlag(true);
    public static final PveFlag PVE_FLAG_FALSE = new PveFlag(false);

    private PveFlag(final boolean value) {
        super(value, Messages.flagDescriptionPve);
    }

    @Override protected PveFlag flagOf(@NotNull final Boolean value) {
        return value ? PVE_FLAG_TRUE : PVE_FLAG_FALSE;
    }

}
