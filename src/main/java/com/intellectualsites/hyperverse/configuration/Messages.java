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

package com.intellectualsites.hyperverse.configuration;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class Messages {

    private static final Map<String, String> defaultValues = Maps.newHashMap();

    public static final Message messagePrefix = createMessage("other.prefix", "&8[&6Hyperverse&8] ");
    public static final Message messageWorldExists = createMessage("world.exists",
        "&cA world with that name already exists");
    public static final Message messageWorldNameInvalid = createMessage("world.invalid_name",
        "&cA world name may only contain (up to) 16 alphanumerical characters, - and _");
    public static final Message messageGeneratorInvalid = createMessage("world.invalid_generator",
        "&cThere is no generator with that name");
    public static final Message messageCreationUnknownFailure = createMessage("world.unknown_failure",
        "&cSomething went wrong when generating the world");
    public static final Message messageWorldCreationStarted = createMessage("world.creation_started",
        "&7Creating a world with the following properties");
    public static final Message messageWorldProperty = createMessage("world.property", "&8- &6%property%&8: &6%value%");
    public static final Message messageWorldCreationFinished = createMessage("world.creation_finished",
        "&7The world was created successfully");

    public static Message createMessage(@NotNull final String key, @NotNull final String defaultValue) {
        final Message message = new Message(Objects.requireNonNull(key), Objects.requireNonNull(defaultValue));
        defaultValues.put(key, defaultValue);
        return message;
    }

    public static String getConfigured(@NotNull final Message message) {
        // TODO: Actually load custom values
        return message.getDefaultValue();
    }

}
