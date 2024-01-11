package se.hyperver.hyperverse.commands.parser;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;

public final class EnumParser<E extends Enum<E>, C> implements ArgumentParser<C, E> {

    private final Class<E> enumClass;
    private final ArgumentParseResult<E> errorResult;
    private final Function<String, Optional<E>> fromStringMapper;
    private final Function<E, String> toStringMapper;

    private static <E extends Enum<E>> Optional<E> parseConstant(@NonNull final Class<E> enumClass, @NonNull final String input) {
        try {
            return Optional.of(Enum.valueOf(enumClass, input));
        } catch (IllegalArgumentException ignored) {
            // Invalid constant name so we return an empty optional
            return Optional.empty();
        }
    }
    public static <E extends Enum<E>, C> @NonNull EnumParser<E, C> createParer(
            @NonNull final Class<E> enumClass,
            @NonNull final String errorMessage
    ) {
        return new EnumParser<>(
                enumClass,
                s -> parseConstant(enumClass, s),
                Enum::name,
                errorMessage
        );
    }

    public EnumParser(
            @NonNull final Class<E> enumClass,
            @NonNull final Function<@NonNull String, @NonNull Optional<E>> fromStringMapper,
            @NonNull final Function<@NonNull E, @NonNull String> toStringMapper,
            @NonNull final String errorMessage
    ) {
        this.enumClass = enumClass;
        this.fromStringMapper = fromStringMapper;
        this.toStringMapper = toStringMapper;
        this.errorResult = ArgumentParseResult.failure(new IllegalStateException(errorMessage));
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull E> parse(
            @NonNull final CommandContext<@NonNull C> commandContext,
            @NonNull final Queue<@NonNull String> inputQueue
    ) {
        if (inputQueue.isEmpty()) {
            return ArgumentParseResult.failure(new NoInputProvidedException(getClass(), commandContext));
        }
        return this.fromStringMapper.apply(inputQueue.poll())
                .map(ArgumentParseResult::success)
                .orElse(this.errorResult);
    }

    @Override
    public @NonNull List<@NonNull String> suggestions(
            @NonNull final CommandContext<C> commandContext,
            @NonNull final String input
    ) {
        return Arrays.stream(this.enumClass.getEnumConstants())
                .map(this.toStringMapper)
                .filter(name -> name.startsWith(input))
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    @Override
    public boolean isContextFree() {
        return true;
    }

}
