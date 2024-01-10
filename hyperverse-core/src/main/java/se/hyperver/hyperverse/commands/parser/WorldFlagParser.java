package se.hyperver.hyperverse.commands.parser;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.flags.GlobalWorldFlagContainer;
import se.hyperver.hyperverse.flags.WorldFlag;

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
            return ArgumentParseResult.failure(new IllegalStateException("Input queue is empty"));
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
