package se.hyperver.hyperverse.commands.parser;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import org.bukkit.GameRule;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.configuration.Messages;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;

public class GameRuleParser<C> implements ArgumentParser<C, GameRule<?>> {

    @Override
    public @NonNull ArgumentParseResult<@NonNull GameRule<?>> parse(
            @NonNull final CommandContext<@NonNull C> commandContext,
            @NonNull final Queue<@NonNull String> inputQueue
    ) {
        if (inputQueue.isEmpty()) {
            return ArgumentParseResult.failure(new IllegalStateException("Input queue is empty"));
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
