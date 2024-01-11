package se.hyperver.hyperverse.commands.parser;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import org.bukkit.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.WorldManager;

import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class HyperWorldParser<C> implements ArgumentParser<C, HyperWorld> {

    private final WorldManager worldManager;

    private final boolean filterSameWorld;
    private final WorldState allowedWorldState;

    public enum WorldState {
        ANY,
        LOADED,
        UNLOADED,
    }

    public HyperWorldParser(@NonNull final WorldManager worldManager) {
        this(worldManager, WorldState.ANY, false);
    }

    public HyperWorldParser(
            @NonNull final WorldManager worldManager,
            @NonNull final WorldState allowedWorldState,
            final boolean filterSameWorld
    ) {
        this.worldManager = worldManager;
        this.allowedWorldState = allowedWorldState;
        this.filterSameWorld = filterSameWorld;
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull HyperWorld> parse(
            @NonNull final CommandContext<@NonNull C> commandContext,
            @NonNull final Queue<@NonNull String> inputQueue
    ) {
        if (inputQueue.isEmpty()) {
            return ArgumentParseResult.failure(new NoInputProvidedException(getClass(), commandContext));
        }
        final String world = inputQueue.poll();
        final HyperWorld hyperWorld = this.worldManager.getWorld(world);
        if (hyperWorld == null) {
            return ArgumentParseResult.failure(new IllegalArgumentException(Messages.messageNoSuchWorld.withoutColorCodes()));
        }
        if (!this.filterSameWorld && this.allowedWorldState == WorldState.ANY) {
            return ArgumentParseResult.success(hyperWorld);
        }
        if (this.allowedWorldState == WorldState.LOADED && !hyperWorld.isLoaded()) {
            return ArgumentParseResult.failure(new IllegalArgumentException(Messages.messageWorldNotLoaded.withoutColorCodes()));
        } else if (this.allowedWorldState == WorldState.UNLOADED && hyperWorld.isLoaded()) {
            return ArgumentParseResult.failure(new IllegalArgumentException(Messages.messageWorldAlreadyLoaded.withoutColorCodes()));
        }
        // Must guard calls to commandContext behind filterSameWorld check otherwise we are violating
        // the contextFree contract below in #isContextFree
        if (filterSameWorld && (commandContext.getSender() instanceof Entity entity)) {
            HyperWorld currentWorld = this.worldManager.getWorld(entity.getWorld());
            if (currentWorld != null && currentWorld.equals(hyperWorld)) {
                return ArgumentParseResult.failure(new IllegalArgumentException(Messages.messagePlayerAlreadyInWorld.withoutColorCodes()));
            }
        }
        return ArgumentParseResult.success(hyperWorld);
    }

    @Override
    public @NonNull List<@NonNull String> suggestions(
            @NonNull final CommandContext<C> commandContext,
            @NonNull final String input
    ) {
        Stream<HyperWorld> stream = this.worldManager.getWorlds().stream();
        if (this.allowedWorldState == WorldState.LOADED) {
            stream = stream.filter(HyperWorld::isLoaded);
        } else if (this.allowedWorldState == WorldState.UNLOADED) {
            stream = stream.filter(Predicate.not(HyperWorld::isLoaded));
        }
        return stream.map(HyperWorld::getDisplayName)
                .filter(name -> name.startsWith(input))
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    @Override
    public boolean isContextFree() {
        return !this.filterSameWorld;
    }

}
