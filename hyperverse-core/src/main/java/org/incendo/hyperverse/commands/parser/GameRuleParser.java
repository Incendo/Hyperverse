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
import org.bukkit.GameRule;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.configuration.Messages;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public final class GameRuleParser<C> implements ArgumentParser<C, GameRule<?>> {

    @Override
    public @NonNull ArgumentParseResult<@NonNull GameRule<?>> parse(
            @NonNull final CommandContext<@NonNull C> commandContext,
            @NonNull final Queue<@NonNull String> inputQueue
    ) {
        if (inputQueue.isEmpty()) {
            return ArgumentParseResult.failure(new NoInputProvidedException(getClass(), commandContext));
        }
        return java.util.Optional.ofNullable(GameRule.getByName(inputQueue.poll()))
                .<ArgumentParseResult<GameRule<?>>>map(ArgumentParseResult::success)
                .orElseGet(() -> ArgumentParseResult.failure(new IllegalArgumentException(Messages.messageInvalidGameRule.withoutColorCodes())));
    }

    @Override
    public @NonNull List<@NonNull String> suggestions(
            @NonNull final CommandContext<C> commandContext,
            @NonNull final String input
    ) {
        return Arrays.stream(GameRule.values())
                .map(GameRule::getName)
                .filter(name -> name.startsWith(input))
                .toList();
    }

    @Override
    public boolean isContextFree() {
        return true;
    }

}
