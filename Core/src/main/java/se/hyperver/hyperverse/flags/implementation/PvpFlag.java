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

public class PvpFlag extends BooleanFlag<PvpFlag> {

    public static final PvpFlag PVP_FLAG_TRUE = new PvpFlag(true);
    public static final PvpFlag PVP_FLAG_FALSE = new PvpFlag(false);

    private PvpFlag(final boolean value) {
        super(value, Messages.flagDescriptionPvp);
    }

    @Override protected PvpFlag flagOf(@NotNull Boolean value) {
        return value ? PVP_FLAG_TRUE : PVP_FLAG_FALSE;
    }

}
