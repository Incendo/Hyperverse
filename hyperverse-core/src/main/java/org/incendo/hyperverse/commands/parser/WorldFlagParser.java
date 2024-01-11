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

package org.incendo.hyperverse.commands.parser;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.flags.GlobalWorldFlagContainer;
import org.incendo.hyperverse.flags.WorldFlag;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

public final class WorldFlagParser<C> implements ArgumentParser<C, WorldFlag<?, ?>> {

    private final GlobalWorldFlagContainer flagContainer;

    public WorldFlagParser(@NonNull final GlobalWorldFlagContainer flagContainer) {
        this.flagContainer = flagContainer;
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull WorldFlag<?, ?>> parse(
            @NonNull final CommandContext<@NonNull C> commandContext,
            @NonNull final Queue<@NonNull String> inputQueue
    ) {
        if (inputQueue.isEmpty()) {
            return ArgumentParseResult.failure(new NoInputProvidedException(getClass(), commandContext));
        }
        final WorldFlag<?, ?> flag = this.flagContainer.getFlagFromString(inputQueue.poll().toLowerCase(Locale.ENGLISH));
        if (flag == null) {
            return ArgumentParseResult.failure(new IllegalArgumentException(Messages.messageFlagUnknown.withoutColorCodes()));
        }
        return ArgumentParseResult.success(flag);
    }

    @Override
    public @NonNull List<String> suggestions(
            @NonNull CommandContext<C> context,
            @NonNull String input
    ) {
        return this.flagContainer.getFlagMap().values().stream()
                .map(WorldFlag::getName)
                .filter(name -> name.startsWith(input))
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    @Override
    public boolean isContextFree() {
        return true;
    }

}
