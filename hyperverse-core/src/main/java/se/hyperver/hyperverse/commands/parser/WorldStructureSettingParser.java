package se.hyperver.hyperverse.commands.parser;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.world.WorldStructureSetting;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

public class WorldStructureSettingParser<C> implements ArgumentParser<C, WorldStructureSetting> {


    @Override
    public @NonNull ArgumentParseResult<@NonNull WorldStructureSetting> parse(
            @NonNull final CommandContext<@NonNull C> commandContext,
            @NonNull final Queue<@NonNull String> inputQueue
    ) {
        if (inputQueue.isEmpty()) {
            return ArgumentParseResult.failure(new NoInputProvidedException(getClass(), commandContext));
        }
        return switch (inputQueue.poll().toLowerCase(Locale.ENGLISH)) {
            case "yes", "true", "generate_structures", "structures" ->
                    ArgumentParseResult.success(WorldStructureSetting.GENERATE_STRUCTURES);
            case "no", "false", "no_structures" -> ArgumentParseResult.success(WorldStructureSetting.NO_STRUCTURES);
            default ->
                    ArgumentParseResult.failure(new IllegalArgumentException(Messages.messageInvalidStructureSetting.withoutColorCodes()));
        };
    }

    @Override
    public @NonNull List<@NonNull String> suggestions(
            @NonNull final CommandContext<C> commandContext,
            @NonNull final String input
    ) {
        List<String> defaults = Arrays.asList(
                "yes", "true", "generate_structures", "Structures",
                "no", "false", "no_structures"
        );
        if (input.isBlank()) {
            return defaults;
        }
        return defaults.stream()
                .filter(x -> x.startsWith(input))
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    @Override
    public boolean isContextFree() {
        return true;
    }

}
