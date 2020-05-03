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

import org.jetbrains.annotations.NotNull;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.HyperWorldCreator;
import se.hyperver.hyperverse.world.WorldConfiguration;

/**
 * Exception thrown during the creation of a {@link HyperWorld},
 * if it for some reason cannot be created
 * {@inheritDoc}
 */
public class HyperWorldCreationException extends Exception {

    private final HyperWorldCreator.ValidationResult validationResult;
    private final WorldConfiguration configuration;

    public HyperWorldCreationException(@NotNull final HyperWorldCreator.ValidationResult validationResult,
        @NotNull final WorldConfiguration configuration) {
        super(String.format("Failed to create world '%s' from configuration. Result: %s",
                configuration.getName(), validationResult.name()));
        this.validationResult = validationResult;
        this.configuration = configuration;
    }

    public HyperWorldCreationException(@NotNull final Throwable cause,
        @NotNull final WorldConfiguration configuration) {
        super(String.format("Failed to create world '%s' from configuration for an unknown reason.",
            configuration.getName()), cause);
        this.validationResult = HyperWorldCreator.ValidationResult.UNKNOWN_ERROR;
        this.configuration = configuration;
    }

    @NotNull public HyperWorldCreator.ValidationResult getValidationResult() {
        return this.validationResult;
    }

    @NotNull public WorldConfiguration getConfiguration() {
        return this.configuration;
    }

}
