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

package com.intellectualsites.hyperverse.exception;

import com.intellectualsites.hyperverse.world.HyperWorld;
import com.intellectualsites.hyperverse.world.HyperWorldCreator;
import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when the validation of a {@link HyperWorld} fails
 */
public class HyperWorldValidationException extends HyperException {

    private final HyperWorldCreator.ValidationResult validationResult;

    public HyperWorldValidationException(
        @NotNull final HyperWorldCreator.ValidationResult validationResult,
        @NotNull final HyperWorld world) {
        super(world, String
            .format("Failed to validate world configuration for world %s. Result: %s",
                world.getConfiguration().getName(), validationResult.name()));
        this.validationResult = validationResult;
    }

    @NotNull public HyperWorldCreator.ValidationResult getValidationResult() {
        return this.validationResult;
    }

}
