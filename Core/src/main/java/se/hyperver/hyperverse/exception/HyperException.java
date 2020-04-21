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

package se.hyperver.hyperverse.exception;

import se.hyperver.hyperverse.world.HyperWorld;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Exception related to a {@link HyperWorld}
 */
public class HyperException extends Exception {

    private final HyperWorld world;

    public HyperException(@NotNull final HyperWorld world, @NotNull final String message) {
        super(Objects.requireNonNull(message));
        this.world = Objects.requireNonNull(world);
    }

    @NotNull public HyperWorld getWorld() {
        return this.world;
    }

}
