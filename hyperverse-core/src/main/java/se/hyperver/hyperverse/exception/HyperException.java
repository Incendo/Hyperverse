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

package se.hyperver.hyperverse.exception;

import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.world.HyperWorld;

import java.util.Objects;

/**
 * Exception related to a {@link HyperWorld}
 */
public class HyperException extends Exception {

    private static final long serialVersionUID = 4059156250285134733L;

    private final HyperWorld world;

    public HyperException(
            final @NonNull HyperWorld world,
            final @NonNull String message
    ) {
        super(Objects.requireNonNull(message));
        this.world = Objects.requireNonNull(world);
    }

    /**
     * Get the world involved in the exception
     *
     * @return World
     */
    public final @NonNull HyperWorld getWorld() {
        return this.world;
    }

}
