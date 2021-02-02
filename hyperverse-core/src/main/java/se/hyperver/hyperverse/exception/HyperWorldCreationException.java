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
import se.hyperver.hyperverse.world.HyperWorldCreator;
import se.hyperver.hyperverse.world.WorldConfiguration;

/**
 * Exception thrown during the creation of a {@link HyperWorld},
 * if it for some reason cannot be created
 * {@inheritDoc}
 */
public final class HyperWorldCreationException extends Exception {

    private static final long serialVersionUID = 7739603214208515388L;

    private final HyperWorldCreator.ValidationResult validationResult;
    private final WorldConfiguration configuration;

    public HyperWorldCreationException(
            final HyperWorldCreator.@NonNull ValidationResult validationResult,
            final @NonNull WorldConfiguration configuration
    ) {
        super(String.format("Failed to create world '%s' from configuration. Result: %s",
                configuration.getName(), validationResult.name()
        ));
        this.validationResult = validationResult;
        this.configuration = configuration;
    }

    public HyperWorldCreationException(
            final @NonNull Throwable cause,
            final @NonNull WorldConfiguration configuration
    ) {
        super(String.format(
                "Failed to create world '%s' from configuration for an unknown reason.",
                configuration.getName()
        ), cause);
        this.validationResult = HyperWorldCreator.ValidationResult.UNKNOWN_ERROR;
        this.configuration = configuration;
    }

    public HyperWorldCreator.@NonNull ValidationResult getValidationResult() {
        return this.validationResult;
    }

    public @NonNull WorldConfiguration getConfiguration() {
        return this.configuration;
    }

}
