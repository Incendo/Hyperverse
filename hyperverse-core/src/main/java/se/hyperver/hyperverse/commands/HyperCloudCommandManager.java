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

package se.hyperver.hyperverse.commands;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.taskchain.TaskChainFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import io.leangen.geantyref.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Contract;
import se.hyperver.hyperverse.Hyperverse;
import se.hyperver.hyperverse.commands.parser.EnumParser;
import se.hyperver.hyperverse.commands.parser.GameRuleParser;
import se.hyperver.hyperverse.commands.parser.WorldFlagParser;
import se.hyperver.hyperverse.commands.parser.WorldStructureSettingParser;
import se.hyperver.hyperverse.configuration.FileHyperConfiguration;
import se.hyperver.hyperverse.configuration.Message;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.exception.HyperWorldValidationException;
import se.hyperver.hyperverse.flags.FlagParseException;
import se.hyperver.hyperverse.flags.GlobalWorldFlagContainer;
import se.hyperver.hyperverse.flags.WorldFlag;
import se.hyperver.hyperverse.flags.implementation.EndFlag;
import se.hyperver.hyperverse.flags.implementation.NetherFlag;
import se.hyperver.hyperverse.flags.implementation.ProfileGroupFlag;
import se.hyperver.hyperverse.modules.HyperWorldFactory;
import se.hyperver.hyperverse.modules.WorldConfigurationFactory;
import se.hyperver.hyperverse.modules.WorldImporterFactory;
import se.hyperver.hyperverse.util.IncendoPaster;
import se.hyperver.hyperverse.util.MessageUtil;
import se.hyperver.hyperverse.util.SeedUtil;
import se.hyperver.hyperverse.util.WorldUtil;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.WorldConfiguration;
import se.hyperver.hyperverse.world.WorldConfigurationBuilder;
import se.hyperver.hyperverse.world.WorldFeatures;
import se.hyperver.hyperverse.world.WorldManager;
import se.hyperver.hyperverse.world.WorldStructureSetting;
import se.hyperver.hyperverse.world.WorldType;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandAlias("hyperverse|hv|worlds|world")
@CommandPermission("hyperverse.worlds")
@SuppressWarnings({"unused", "unchecked"})
public final class HyperCloudCommandManager extends BaseCommand {

    private static final int WORLDS_PER_PAGE = 10;

    private final PaperCommandManager<CommandSender> commandManager;
    private final WorldManager worldManager;
    private final FileHyperConfiguration fileHyperConfiguration;
    private final HyperWorldFactory hyperWorldFactory;
    private final WorldImporterFactory worldImporterFactory;
    private final WorldConfigurationFactory worldConfigurationFactory;
    private final GlobalWorldFlagContainer globalFlagContainer;
    private final TaskChainFactory taskChainFactory;

    private final Hyperverse hyperverse;
    private final Server server;

    @Inject
    public HyperCloudCommandManager(
            final Hyperverse hyperverse,
            final WorldManager worldManager,
            final HyperWorldFactory hyperWorldFactory,
            final WorldImporterFactory worldImporterFactory,
            final WorldConfigurationFactory worldConfigurationFactory,
            final GlobalWorldFlagContainer globalFlagContainer,
            final TaskChainFactory taskFactory,
            final FileHyperConfiguration hyperConfiguration,
            final Server server
    ) {
        this.hyperverse = hyperverse;
        this.worldManager = Objects.requireNonNull(worldManager);
        this.hyperWorldFactory = Objects.requireNonNull(hyperWorldFactory);
        this.worldImporterFactory = Objects.requireNonNull(worldImporterFactory);
        this.worldConfigurationFactory = Objects.requireNonNull(worldConfigurationFactory);
        this.globalFlagContainer = Objects.requireNonNull(globalFlagContainer);
        this.taskChainFactory = Objects.requireNonNull(taskFactory);
        this.fileHyperConfiguration = Objects.requireNonNull(hyperConfiguration);
        this.server = server;

        // Create the command manager
        try {
            this.commandManager = new PaperCommandManager<>(
                    hyperverse,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    Function.identity(),
                    Function.identity()
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to initialize the paper command manager", ex);
        }
        if (this.commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            this.commandManager.registerAsynchronousCompletions();
        }
        if (this.commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            this.commandManager.registerBrigadier();
        }
        this.commandManager.parserRegistry().registerParserSupplier(
                TypeToken.get(GameRule.class),
                unused -> new GameRuleParser<>()
        );
        this.commandManager.parserRegistry().registerParserSupplier(
                TypeToken.get(WorldFeatures.class),
                unused -> new EnumParser<>(
                        WorldFeatures.class,
                        WorldFeatures::fromName,
                        WorldFeatures::name,
                        Messages.messageInvalidWorldFeatures.withoutColorCodes()
                )
        );
        this.commandManager.parserRegistry().registerParserSupplier(
                TypeToken.get(WorldStructureSetting.class),
                unused -> new WorldStructureSettingParser<>()
        );
        this.commandManager.parserRegistry().registerParserSupplier(
                TypeToken.get(WorldType.class),
                unused -> new EnumParser<>(
                        WorldType.class,
                        WorldType::fromString,
                        WorldType::name,
                        Messages.messageInvalidWorldType.withoutColorCodes()
                )
        );
        this.commandManager.parserRegistry().registerParserSupplier(
                TypeToken.get(WorldFlag.class),
                unused -> new WorldFlagParser<>(this.globalFlagContainer)
        );
        // Start building the hypervere command
        var builder = this.commandManager.commandBuilder("hyperverse", "hv");
        this.registerCommandCreateWorld(this.commandManager, builder)
                .registerCommandImport(this.commandManager, builder);

    }

    private List<String> suggestGreedyPlayerWorlds(CommandContext<CommandSender> context, String input) {
        String[] splitInput = input.split(" ");
        final int toPop = context.get("pop");
        if (toPop > splitInput.length) {
            throw new IllegalArgumentException("Config to pop is greater than input length!");
        }
        final String inWorld = context.getOrDefault("in_world", "false");
        final boolean checkInWorld = !inWorld.equalsIgnoreCase("false");
        splitInput = Arrays.copyOfRange(splitInput, toPop, splitInput.length - 1);
        for (int index = 0; index < splitInput.length; index++) {
            splitInput[index] = splitInput[index].toLowerCase();
        }
        final List<String> players = new ArrayList<>(Arrays.asList(splitInput));
        Player player = context.getSender() instanceof Player aPlayer ? aPlayer : null;
        if (player != null && !context.getOrDefault("self", "false").equalsIgnoreCase("true")) {
            players.remove(player.getName());
        }
        Stream<? extends Player> stream = this.server.getOnlinePlayers().stream();
        if (checkInWorld) {
            final HyperWorld world = context.getOrDefault("hyperworld", null);
            if (world == null) {
                return Collections.emptyList();
            }
            stream = stream.filter(p -> p.getWorld() != world.getBukkitWorld());
        }
        return stream.map(Player::getName)
                .filter(p -> !players.contains(p.toLowerCase()))
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private List<String> suggestStructures(CommandContext<CommandSender> context, String input) {
        return Arrays.asList("yes", "true", "generate_structures", "structures", "no", "false", "no_structures");
    }

    private List<String> suggestGreedyPlayers(CommandContext<CommandSender> context, String input) {
        String[] splitInput = input.split(" ");
        final int toPop = context.get("pop");
        if (toPop > splitInput.length) {
            throw new IllegalArgumentException("Config to pop is greater than input length!");
        }
        splitInput = Arrays.copyOfRange(splitInput, toPop, splitInput.length - 1);
        for (int index = 0; index < splitInput.length; index++) {
            splitInput[index] = splitInput[index].toLowerCase();
        }
        final List<String> players = new ArrayList<>(Arrays.asList(splitInput));
        Player player = context.getSender() instanceof Player aPlayer ? aPlayer : null;
        if (player != null
                && !context.getOrDefault("self", "false").equalsIgnoreCase("true")
        ) {
            players.remove(player.getName());
        }
        return this.server.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(p -> !players.contains(p.toLowerCase()))
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    private List<String> suggestProfileGroups(CommandContext<CommandSender> context, String input) {
        Stream<String> groups = worldManager
                .getWorlds()
                .stream()
                .map(world -> world.getFlag(ProfileGroupFlag.class))
                .filter(s -> !s.isEmpty());
        String requirePerms = context.getOrDefault("has_perms", "false");
        if (requirePerms.equalsIgnoreCase("true")) {
            groups = groups.filter(profile -> context.getSender().hasPermission("hyperverse.teleportgroup." + profile));
        }
        return groups.collect(Collectors.toList());
    }

    private List<String> suggestGameRule(CommandContext<CommandSender> context, String input) {
        final GameRule<?> gameRule = context.getOrDefault("gamerule", null);
        if (gameRule != null && gameRule.getType() == Boolean.class) {
            return Arrays.asList("true", "false");
        }
        return Collections.emptyList();
    }


    private List<String> suggestGenerators(CommandContext<CommandSender> context, String input) {
        if (input.contains(":")) {
            return Collections.emptyList();
        }
        return Arrays.stream(this.server.getPluginManager().getPlugins())
                .map(Plugin::getName)
                .map(str -> str.toLowerCase(Locale.ENGLISH))
                .toList();
    }

    private List<String> suggestWorldTypes(CommandContext<CommandSender> context, String input) {
        if (input.contains(" ")) {
            return Collections.emptyList();
        }
        return Arrays.stream(WorldType.values())
                .map(WorldType::name)
                .map(String::toLowerCase)
                .toList();
    }

    private @NonNull List<String> suggestImportCandidates(
            @NonNull final CommandContext<CommandSender> context,
            @NonNull final String input
    ) {
        final File baseDirectory = this.server.getWorldContainer();
        try (final Stream<Path> files = Files.list(baseDirectory.toPath())) {
            return files.filter(path -> {
                        final File file = path.toFile();
                        return file.isDirectory() && new File(file, "level.dat").isFile()
                                && this.worldManager.getWorld(file.getName()) == null;
                    }).map(path -> path.toFile().getName()).sorted(Comparator.naturalOrder())
                    .toList();
        } catch (IOException ex) {
            return Collections.emptyList();
        }
    }

    private List<String> suggestHyperWorlds(CommandContext<CommandSender> context, String input) {
        return this.worldManager.getWorlds().stream().filter(hyperWorld -> {
                    final String stateSel = context.getOrDefault("state", "").toLowerCase();
                    final String playerSel = context.getOrDefault("players", "").toLowerCase();
                    boolean ret = true;
                    switch (stateSel) {
                        case "loaded":
                            ret = hyperWorld.isLoaded();
                            break;
                        case "not_loaded":
                            ret = !hyperWorld.isLoaded();
                            break;
                        default:
                            break;
                    }

                    // In here do check if the world is loaded.
                    switch (playerSel) {
                        case "no_players":
                            ret = ret && hyperWorld.isLoaded() && hyperWorld.getBukkitWorld().getPlayers().isEmpty();
                            break;
                        case "has_players":
                            ret = ret && hyperWorld.isLoaded() && !hyperWorld.getBukkitWorld().getPlayers().isEmpty();
                            break;
                        default:
                            break;
                    }
                    return ret;

                }).map(HyperWorld::getConfiguration).map(WorldConfiguration::getName)
                .filter(worldName -> {
                    final String selection = context.getOrDefault("player", "").toLowerCase();
                    final boolean inWorld = context.getSender() instanceof Player player
                            && worldName.equalsIgnoreCase(player.getWorld().getName());
                    return switch (selection) {
                        case "not_in" -> !inWorld;
                        case "in" -> inWorld;
                        default -> true;
                    };
                }).toList();
    }

    @HelpCommand
    public void doHelp(final CommandSender sender, final CommandHelp commandHelp) {
        commandHelp.showHelp();
    }

    @Contract("_,_->this")
    private HyperCloudCommandManager registerCommandCreateWorld(
            final @NonNull CommandManager<CommandSender> commandManager,
            final Command.@NonNull Builder<CommandSender> builder
    ) {
        var commandCreateWorld = builder.literal("create")
                .argument(StringArgument.of("world"))
                .argument(StringArgument.<CommandSender>builder("generator")
                        .withSuggestionsProvider(this::suggestGenerators)
                )
                // WorldType parser already registered in ctor
                .argument(CommandArgument.
                        <CommandSender, WorldType>ofType(WorldType.class, "type")
                        .asOptionalWithDefault("overworld")
                )
                // WorldStructureSetting parser already registered in ctor
                .argument(CommandArgument
                        .<CommandSender, WorldStructureSetting>ofType(WorldStructureSetting.class, "generateStructures")
                        .asOptionalWithDefault("true")
                )
                // WorldFeatures parser already registered in ctor
                .argument(CommandArgument
                        .<CommandSender, WorldFeatures>ofType(WorldFeatures.class, "features")
                        .asOptionalWithDefault("normal"))
                .argument(StringArgument.of("settings", StringArgument.StringMode.GREEDY_FLAG_YIELDING))
                .flag(CommandFlag.builder("specifiedSeed").withArgument(LongArgument.of("specifiedSeed")))
                .handler(this::handleWorldCreation)
                .permission("hyperverse.create")
                .meta(CommandMeta.DESCRIPTION, "{@@command.create}");
        commandManager.command(commandCreateWorld);
        return this;
        // FIXME: command syntax: command syntax
        // old:  @Syntax("<world> [generator: plugin name, vanilla][:[args]] [type: overworld, nether, end] [seed] [generate-structures: true, false] [features: normal, flatland, amplified, bucket] [settings...]")
    }

    private void handleWorldCreation(@NonNull final CommandContext<CommandSender> context) {
        // Command parameters
        final long seed = context.getOrSupplyDefault("specifiedSeed", SeedUtil::randomSeed);
        final String world = context.get("world");
        final String generator = context.get("generator");
        final WorldStructureSetting structureSetting = context.get("generateStructures");
        final String settings = context.get("settings");
        final WorldType worldType = context.get("type");
        final WorldFeatures features = context.get("features");

        final CommandSender sender = context.getSender();

        // Check if the name already exists
        for (final HyperWorld hyperWorld : this.worldManager.getWorlds()) {
            if (hyperWorld.getConfiguration().getName().equalsIgnoreCase(world)) {
                MessageUtil.sendMessage(sender, Messages.messageWorldExists);
                return;
            }
        }
        // Double check that Bukkit doesn't have the world stored
        if (this.server.getWorld(world) != null) {
            MessageUtil.sendMessage(sender, Messages.messageWorldExists);
            return;
        }
        // Now validate the world name
        if (!WorldUtil.validateName(world)) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNameInvalid);
            return;
        }

        String generatorArgs = "";

        final String actualGenerator;
        if (generator.contains(":")) {
            final String[] split = generator.split(":");
            actualGenerator = split[0];
            generatorArgs = split[1];
        } else {
            actualGenerator = generator;
        }

        // Check if the generator is actually valid
        final WorldConfiguration worldConfiguration =
                this.worldConfigurationFactory.builder().setName(world)
                        .setGenerator(actualGenerator)
                        .setType(worldType)
                        .setSeed(seed)
                        .setGenerateStructures(structureSetting)
                        .setSettings(settings)
                        .setWorldFeatures(features)
                        .setGeneratorArg(generatorArgs).createWorldConfiguration();
        final HyperWorld hyperWorld = this.hyperWorldFactory.create(UUID.randomUUID(), worldConfiguration);
        MessageUtil.sendMessage(sender, Messages.messageWorldCreationStarted);
        hyperWorld.sendWorldInfo(sender);

        // Make sure we don't detect the world load
        this.worldManager.ignoreWorld(world);

        try {
            hyperWorld.createBukkitWorld();
            // Register the world
            this.worldManager.addWorld(hyperWorld);
            MessageUtil.sendMessage(sender, Messages.messageWorldCreationFinished);
            if (sender instanceof Player player) {
                // Attempt to teleport them to the world
                hyperWorld.teleportPlayer(player);
            }
        } catch (final HyperWorldValidationException validationException) {
            switch (validationException.getValidationResult()) {
                case UNKNOWN_GENERATOR:
                    MessageUtil.sendMessage(sender, Messages.messageGeneratorInvalid,
                            "%world%", hyperWorld.getConfiguration().getName(),
                            "%generator%", hyperWorld.getConfiguration().getGenerator()
                    );
                    break;
                case SUCCESS:
                    break;
                default:
                    MessageUtil.sendMessage(sender, Messages.messageCreationUnknownFailure);
                    break;
            }
        } catch (final Exception e) {
            MessageUtil.sendMessage(sender, Messages.messageWorldCreationFailed,
                    "%reason%", e.getMessage()
            );
        }
    }

    @Contract("_,_->this")
    private @NonNull HyperCloudCommandManager registerCommandImport(
            @NonNull final CommandManager<CommandSender> commandManager,
            final Command.@NonNull Builder<CommandSender> builder
    ) {
        var commandImport = builder.literal("import")
                .argument(StringArgument.<CommandSender>builder("worldName")
                        .withSuggestionsProvider(this::suggestImportCandidates))
                .argument(StringArgument.<CommandSender>builder("generator")
                        .withSuggestionsProvider(this::suggestGenerators))
                // WorldType parser already registered in ctor
                .argument(CommandArgument
                        .<CommandSender, WorldType>ofType(WorldType.class, "worldType")
                        .asOptionalWithDefault("over_world"))
                .permission("hyperverse.import")
                .meta(CommandMeta.DESCRIPTION, "{{@@command.import}}")
                .build();
        var commandImportProxy = commandManager.commandBuilder("hvi")
                .proxies(commandImport);
        // existing command did not have a command syntax
        commandManager.command(commandImport).command(commandImportProxy);
        return this;
    }

    private void handleWorldImport(@NonNull final CommandContext<CommandSender> context) {
        final CommandSender sender = context.getSender();
        final String worldName = context.get("worldName");
        final String generator = context.get("generator");
        final WorldType worldType = context.get("worldType");
        if (this.worldManager.getWorld(worldName) != null) {
            MessageUtil.sendMessage(sender, Messages.messageWorldAlreadyImported);
            return;
        }
        if (!WorldUtil.isSuitableImportCandidate(worldName, this.worldManager)) {
            MessageUtil.sendMessage(sender, Messages.messageNoSuchWorld);
            return;
        }
        this.worldManager.ignoreWorld(worldName); //Make sure we don't auto register on init
        final HyperWorld hyperWorld = this.hyperWorldFactory.create(
                UUID.randomUUID(),
                new WorldConfigurationBuilder().setName(worldName).setGenerator(generator)
                        .setType(worldType).createWorldConfiguration()
        );
        final World bukkitWorld;
        try {
            hyperWorld.createBukkitWorld();
            bukkitWorld = hyperWorld.getBukkitWorld();
            assert bukkitWorld != null;
        } catch (HyperWorldValidationException e) {
            MessageUtil.sendMessage(sender, Messages.messageWorldImportFailure, "%world%", worldName,
                    "%result%", e.getMessage()
            );
            return;
        }
        this.worldManager.addWorld(hyperWorld);
        MessageUtil.sendMessage(sender, Messages.messageWorldImportFinished);
        if (sender instanceof Player player) {
            //Schedule teleport 1-tick later so the world has a chance to load.
            this.server.getScheduler().runTaskLater(this.hyperverse,
                    () -> this.doTeleport((Player) sender, this.worldManager.getWorld(bukkitWorld)), 1L
            );
        }
    }

    @Contract("_,_->this")
    private HyperCloudCommandManager createCommandList(
            @NonNull final CommandManager<CommandSender> commandManager,
            final Command.@NonNull Builder<CommandSender> builder
    ) {
        var commandList = builder.literal("list", "l", "worlds")
                .argument(IntegerArgument.<CommandSender>builder("page")
                        .asOptionalWithDefault(1))
                .handler(this::handleList)
                .permission("hyperverse.list")
                .meta(CommandMeta.DESCRIPTION, "{@@command.list}")
                .build();
        var commandListProxy = commandManager.commandBuilder("hvl")
                .proxies(commandList)
                .build();
        commandManager.command(commandList).command(commandListProxy);
        return this;
    }

    public void handleList(CommandContext<CommandSender> context) {
        final CommandSender sender = context.getSender();
        final int page = context.get("page");
        final List<HyperWorld> worlds = new ArrayList<>(this.worldManager.getWorlds());
        worlds.sort(Comparator.comparing(world -> world.getConfiguration().getName()));

        final int pages = 1 + (int) ((double) worlds.size() / WORLDS_PER_PAGE);
        final int clampedPage = Math.max(1, Math.min(pages, page));
        final int min = WORLDS_PER_PAGE * (clampedPage - 1);
        final int max = Math.min(worlds.size(), WORLDS_PER_PAGE * clampedPage);

        MessageUtil.sendMessage(
                sender,
                Messages.messageListHeader,
                "%page%",
                Integer.toString(clampedPage),
                "%max%",
                Integer.toString(pages)
        );

        Stream<HyperWorld> stream = worlds.subList(min, max).stream().sorted(Comparator.comparing(world -> world
                .getConfiguration()
                .getName()));
        if (sender instanceof Entity entity) {
            stream = stream.sorted(Comparator
                    .comparing(world -> !entity.getWorld().equals(world.getBukkitWorld())));
        }
        stream.forEachOrdered(hyperWorld -> {
            final WorldConfiguration configuration = hyperWorld.getConfiguration();

            // Format the generator name a little better
            String generator = configuration.getGenerator();
            if (generator.isEmpty()) {
                generator = "vanilla";
            } else {
                generator = generator.toLowerCase();
            }

            final String loadStatus;
            if (hyperWorld.isLoaded()) {
                loadStatus = "<green><hover:show_text:\"<gray>Click to unload</gray>\"><click:run_command:/hyperverse unload "
                        + configuration.getName() + ">loaded</click></hover></green>";
            } else {
                loadStatus = "<red><hover:show_text:\"<gray>Click to load</gray>\"><click:run_command:/hyperverse load "
                        + configuration.getName() + ">unloaded</click></hover></red>";
            }

            final Message message;
            if (sender instanceof Entity entity && entity.getWorld() == hyperWorld.getBukkitWorld()) {
                message = Messages.messageListEntryCurrentWorld;
            } else {
                message = Messages.messageListEntry;
            }

            MessageUtil.sendMessage(sender, message, "%name%", configuration.getName(),
                    "%display-name%", hyperWorld.getDisplayName(), "%generator%", generator, "%type%",
                    configuration.getType().name(), "%load_status%", loadStatus
            );
        });
    }

    @Subcommand("teleport|tp")
    @CommandAlias("hvtp")
    @CommandPermission("hyperverse.teleport")
    @CommandCompletion("@hyperworlds:player=not_in,state=loaded")
    @Description("{@@command.teleport}")
    public void doTeleport(final Player player, final HyperWorld world) {
        if (world == null) {
            MessageUtil.sendMessage(player, Messages.messageNoSuchWorld);
            return;
        }
        if (!world.isLoaded()) {
            MessageUtil.sendMessage(player, Messages.messageWorldNotLoaded);
            return;
        }
        if (world.getBukkitWorld() == player.getWorld()) {
            MessageUtil.sendMessage(player, Messages.messageAlreadyInWorld);
            return;
        }
        MessageUtil.sendMessage(player, Messages.messageTeleporting, "%world%", world.getDisplayName());
        world.teleportPlayer(player);
    }

    @Subcommand("teleport|tp")
    @CommandAlias("hvtp")
    @CommandPermission("hyperverse.teleport")
    @CommandCompletion("@hyperworlds:state=loaded @vararg_player_world:pop=0,in_world=true")
    public void doMassTeleport(final CommandSender sender, final HyperWorld world, final String[] players) {
        if (players.length == 0) {
            if (sender instanceof Player) {
                this.doTeleport((Player) sender, world);
            } else {
                MessageUtil.sendMessage(sender, Messages.messageSpecifyPlayer);
            }
            return;
        }
        if (world == null) {
            MessageUtil.sendMessage(sender, Messages.messageNoSuchWorld);
            return;
        }
        if (!world.isLoaded()) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNotLoaded);
            return;
        }
        final List<Player> playerList = new ArrayList<>(players.length);
        for (final String rawPlayer : players) {
            final Player player = this.server.getPlayer(rawPlayer);
            if (player == null) {
                MessageUtil.sendMessage(sender, Messages.messageNoPlayerFound, "%name%", rawPlayer);
                return;
            }
            if (!playerList.contains(player)) {
                playerList.add(player);
            }
        }
        for (final Player player : playerList) {
            if (world.getBukkitWorld() == player.getWorld()) {
                MessageUtil.sendMessage(sender, Messages.messagePlayerAlreadyInWorld, "%player%",
                        player.getName()
                );
                continue;
            }
            MessageUtil.sendMessage(player, Messages.messageTeleporting, "%world%", world.getDisplayName());
            if (player != sender) {
                MessageUtil.sendMessage(sender, Messages.messageTeleportingPlayer, "%player%",
                        player.getName(), "%world%", world.getDisplayName()
                );
            }
            world.teleportPlayer(player);
        }
    }

    @Subcommand("info|i")
    @CommandAlias("hvi")
    @CommandPermission("hyperverse.info")
    @CommandCompletion("@hyperworlds")
    @Description("View world info")
    public void doInfo(final CommandSender sender, final HyperWorld world) {
        if (world == null) {
            return;
        }
        MessageUtil.sendMessage(sender, Messages.messageInfoHeader);
        world.sendWorldInfo(sender);
    }

    @Subcommand("unload")
    @CommandPermission("hyperverse.unload")
    @CommandCompletion("@hyperworlds:state=loaded true|false")
    @Description("{@@command.unload}")
    public void doUnload(
            final CommandSender sender, final HyperWorld world,
            @Default("true") final boolean saveWorld
    ) {
        if (world == null) {
            return;
        }
        if (!world.isLoaded()) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNotLoaded);
            return;
        }
        final HyperWorld.WorldUnloadResult worldUnloadResult = world.unloadWorld(saveWorld);
        if (worldUnloadResult == HyperWorld.WorldUnloadResult.SUCCESS) {
            MessageUtil.sendMessage(sender, Messages.messageWorldUnloaded);
        } else {
            MessageUtil.sendMessage(sender, Messages.messageWorldUnloadFailed,
                    "%reason%", worldUnloadResult.getDescription()
            );
        }
    }

    @Subcommand("load")
    @CommandPermission("hyperverse.load")
    @CommandCompletion("@hyperworlds:state=not_loaded")
    @Description("{@@command.load}")
    public void doLoad(final CommandSender sender, final HyperWorld world) {
        if (world == null) {
            MessageUtil.sendMessage(sender, Messages.messageNoSuchWorld);
            return;
        }
        if (world.isLoaded()) {
            MessageUtil.sendMessage(sender, Messages.messageWorldAlreadyLoaded);
            return;
        }
        try {
            world.createBukkitWorld();
        } catch (final HyperWorldValidationException e) {
            MessageUtil.sendMessage(sender, Messages.messageWorldImportFailure,
                    "%world%", world.getConfiguration().getName(), "%result%", e.getMessage()
            );
            return;
        }

        world.getConfiguration().setLoaded(true);
        world.saveConfiguration();

        MessageUtil.sendMessage(sender, Messages.messageWorldLoadedSuccessfully);
    }

    @Subcommand("find|where")
    @CommandPermission("hyperverse.find")
    @CommandAlias("hvf|hvfind")
    @CommandCompletion("@players")
    @Description("{@@command.find}")
//public void findPlayer(final CommandSender sender, final String... players) {
    public void findPlayer(final CommandSender sender, final String player) {
        //for (String player : players) {
        final Player bukkitPlayer = this.server.getPlayer(player);
        if (bukkitPlayer == null) {
            MessageUtil.sendMessage(sender, Messages.messageNoPlayerFound, "%name%", player);
            return;
        }
        final Location location = bukkitPlayer.getLocation();
        final DecimalFormat format = Messages.miscCoordinateDecimalFormat;
        MessageUtil
                .sendMessage(sender, Messages.messagePlayerCurrentWorld, "%player%", player, "%world%",
                        bukkitPlayer.getWorld().getName(), "%x%",
                        format.format(location.getX()), "%y%", format.format(location.getY()),
                        "%z%", format.format(location.getZ())
                );
        //}
    }

    @Subcommand("who")
    @CommandPermission("hyperverse.who")
    @CommandAlias("hvwho")
    @CommandCompletion("@hyperworlds")
    @Description("{@@command.who}")
    public void findPlayersPresent(final CommandSender sender, @Optional final String world) {
        if (world != null) {
            final World bukkitWorld = this.server.getWorld(world);
            if (bukkitWorld == null) {
                MessageUtil.sendMessage(sender, Messages.messageNoSuchWorld);
                return;
            }
            final HyperWorld hyperWorld = this.worldManager.getWorld(bukkitWorld);
            if (hyperWorld == null) {
                MessageUtil.sendMessage(sender, Messages.messageNoSuchWorld);
                return;
            }
            final DecimalFormat format = Messages.miscCoordinateDecimalFormat;
            if (bukkitWorld.getPlayers().isEmpty()) {
                MessageUtil.sendMessage(sender, Messages.messageNoPlayersInWorld, "%world%", hyperWorld.getDisplayName());
                return;
            }
            final StringJoiner players = new StringJoiner(", ");
            for (final Player player : bukkitWorld.getPlayers()) {
                final Location location = player.getLocation();
                players.add(MessageUtil
                        .format(Messages.messageListEntryPlayer.toString(), "%player%",
                                ChatColor.stripColor(player.getDisplayName()), "%world%", world, "%x%",
                                format.format(location.getX()), "%y%", format.format(location.getY()),
                                "%z%", format.format(location.getZ())
                        ));
            }
            MessageUtil.sendMessage(sender, Messages.messageListEntryWorld, "%players%",
                    players.toString().trim(), "%world%", hyperWorld.getDisplayName()
            );
        } else {
            for (final World bukkitWorld : this.server.getWorlds()) {
                this.findPlayersPresent(sender, bukkitWorld.getName());
            }
        }
    }

    @Subcommand("flag set")
    @CommandPermission("hyperverse.flag.set")
    @CommandCompletion("@hyperworlds @flags @flag")
    @Description("{@@command.flag.set}")
    public void doFlagSet(
            final CommandSender sender, final HyperWorld hyperWorld,
            final WorldFlag<?, ?> flag, final String value
    ) {
        try {
            hyperWorld.setFlag(flag, value);
        } catch (final FlagParseException e) {
            MessageUtil.sendMessage(
                    sender,
                    Messages.messageFlagParseError,
                    "%flag%",
                    e.getFlag().getName(),
                    "%value%",
                    e.getValue(),
                    "%reason%",
                    e.getErrorMessage()
            );
            return;
        }
        MessageUtil.sendMessage(sender, Messages.messageFlagSet);
    }

    @Subcommand("flag reset")
    @CommandPermission("hyperverse.flag.set")
    @CommandCompletion("@flags|@hyperworlds @flags")
    @SuppressWarnings("unchecked")
    public void doFlagReset(final CommandSender sender, final HyperWorld hyperWorld, final WorldFlag<?, ?> flag) {
        final WorldFlag<?, ?> toSet;
        if (flag.getClass() == EndFlag.class) {
            final World bukkitWorld = hyperWorld.getBukkitWorld();
            if (bukkitWorld == null) {
                toSet = this.globalFlagContainer.getFlag(flag.getClass());
            } else {
                WorldFlag<?, ?> temp;
                try {
                    temp = EndFlag.END_FLAG_DEFAULT.parse(bukkitWorld.getName() + "_the_end");
                } catch (FlagParseException ignored) {
                    temp = this.globalFlagContainer.getFlag(flag.getClass());
                }
                toSet = temp;
            }
        } else if (flag.getClass() == NetherFlag.class) {
            final World bukkitWorld = hyperWorld.getBukkitWorld();
            if (bukkitWorld == null) {
                toSet = this.globalFlagContainer.getFlag(flag.getClass());
            } else {
                WorldFlag<?, ?> temp;
                try {
                    temp = NetherFlag.NETHER_FLAG_DEFAULT.parse(bukkitWorld.getName() + "_nether");
                } catch (FlagParseException ignored) {
                    temp = this.globalFlagContainer.getFlag(flag.getClass());
                }
                toSet = temp;
            }
        } else {
            toSet = this.globalFlagContainer.getFlag(flag.getClass());
        }
        hyperWorld.setFlagInstance(toSet);
        MessageUtil.sendMessage(sender, Messages.messageFlagSet);
    }

    @Subcommand("flag remove")
    @CommandPermission("hyperverse.flag.set")
    @CommandCompletion("@hyperworlds @flags")
    @Description("{@@command.flag.remove}")
    public void doFlagRemove(final CommandSender sender, final HyperWorld hyperWorld, final WorldFlag<?, ?> flag) {
        hyperWorld.removeFlag(flag);
        MessageUtil.sendMessage(sender, Messages.messageFlagRemoved);
    }

    @Subcommand("flag info")
    @CommandPermission("hyperverse.flag.info")
    @CommandCompletion("@flags|@hyperworlds @flags")
    @SuppressWarnings("unchecked")
    public <T> void showFlagStatus(final CommandSender sender, final HyperWorld hyperWorld, final WorldFlag<T, ?> flag) {
        final String value = String.valueOf(hyperWorld.getFlag((Class<? extends WorldFlag<T, ?>>) flag.getClass()));
        final String defaultValue = String.valueOf(this.globalFlagContainer.getFlag(flag.getClass()).getValue());
        MessageUtil.sendMessage(
                sender,
                Messages.messageFlagDisplayInfo,
                "%description%",
                flag.getFlagDescription().toString(),
                "%current%",
                value.isEmpty() ? "Unset" : value,
                "%default%",
                defaultValue.isEmpty() ? "Unset" : defaultValue
        );
    }

    @Subcommand("gamerule set")
    @CommandPermission("hyperverse.gamerule.set")
    @CommandCompletion("@hyperworlds @gamerules @gamerule")
    @Description("{@@command.gamerule.set}")
    @SuppressWarnings("rawtypes")
    public void doGameRuleSet(
            final CommandSender sender, final HyperWorld hyperWorld,
            final GameRule gameRule, final String value
    ) {
        if (!hyperWorld.isLoaded()) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNotLoaded);
            return;
        }

        final Object object;
        if (gameRule.getType() == Boolean.class) {
            try {
                object = Boolean.parseBoolean(value);
            } catch (final Exception e) {
                MessageUtil.sendMessage(sender, Messages.messageGameRuleParseError);
                return;
            }
        } else if (gameRule.getType() == Integer.class) {
            try {
                object = Integer.parseInt(value);
            } catch (final Exception e) {
                MessageUtil.sendMessage(sender, Messages.messageGameRuleParseError);
                return;
            }
        } else {
            // ??
            return;
        }

        hyperWorld.getBukkitWorld().setGameRule(gameRule, object);
        MessageUtil.sendMessage(sender, Messages.messageGameRuleSet);
    }

    @Subcommand("gamerule remove")
    @CommandPermission("hyperverse.gamerule.set")
    @CommandCompletion("@hyperworlds @gamerules")
    @Description("{@@command.gamerule.remove}")
    @SuppressWarnings("rawtypes")
    public void doGameRuleRemove(final CommandSender sender, final HyperWorld hyperWorld, final GameRule gameRule) {
        if (gameRule == null) {
            MessageUtil.sendMessage(sender, Messages.messageGameRuleUnknown);
            return;
        }
        if (hyperWorld == null) {
            MessageUtil.sendMessage(sender, Messages.messageNoSuchWorld);
            return;
        }
        if (!hyperWorld.isLoaded()) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNotLoaded);
            return;
        }
        hyperWorld.getBukkitWorld().setGameRule(
                gameRule,
                hyperWorld.getBukkitWorld().getGameRuleDefault(gameRule)
        );
        MessageUtil.sendMessage(sender, Messages.messageGameRuleRemoved);
    }

    @Subcommand("delete")
    @CommandPermission("hyperverse.delete")
    @CommandCompletion("@hyperworlds true|false")
    @Description("{@@command.delete}")
    public void doDelete(
            final CommandSender sender, final HyperWorld hyperWorld,
            final @Default("false") boolean deleteDirectory
    ) {
        if (hyperWorld == null) {
            MessageUtil.sendMessage(sender, Messages.messageNoSuchWorld);
            return;
        }
        hyperWorld.deleteWorld(worldUnloadResult -> {
            if (worldUnloadResult != HyperWorld.WorldUnloadResult.SUCCESS) {
                MessageUtil.sendMessage(sender, Messages.messageWorldNotRemoved, "%reason%",
                        worldUnloadResult.getDescription()
                );
                return;
            }

            if (deleteDirectory) {
                final Path path = this.server.getWorldContainer().toPath()
                        .resolve(hyperWorld.getConfiguration().getName());
                try {
                    try (Stream<Path> walk = Files.walk(path)) {
                        walk.sorted(Comparator.reverseOrder())
                                .map(Path::toFile)
                                .peek(System.out::println)
                                .forEach(File::delete);
                    }
                } catch (final Exception e) {
                    MessageUtil.sendMessage(sender, Messages.messageWorldNotRemoved, "%reason%",
                            e.getMessage()
                    );
                    e.printStackTrace();
                    return;
                }
            }

            MessageUtil.sendMessage(sender, Messages.messageWorldRemoved);
        });
    }

    @Subcommand("reload")
    @CommandPermission("hyperverse.reload")
    @CommandAlias("hvreload")
    @Description("{@@command.reload}")
    public void doConfigReload(final CommandSender sender) {
        if (!Hyperverse.getPlugin(Hyperverse.class).reloadConfiguration(sender)) {
            throw new CommandException("Failed to reload configuration files");
        }
    }

    @Subcommand("debugpaste")
    @CommandPermission("hyperverse.debugpaste")
    @Description("{@@command.debugpaste}")
    public void doDebugPaste(final CommandSender sender) {
        this.taskChainFactory.newChain().async(() -> {
            try {
                final Hyperverse hyperverse = Hyperverse.getPlugin(Hyperverse.class);

                StringBuilder b = new StringBuilder();
                b.append(
                        "# Welcome to this paste\n# It is meant to provide us at IntellectualSites with better information about your "
                                + "problem\n\n");

                b.append("# Server Information\n");
                b.append("Server Version: ").append(this.server.getVersion()).append("\n");

                b.append("Plugins:");
                for (final Plugin plugin : this.server.getPluginManager().getPlugins()) {
                    b
                            .append("\n  ")
                            .append(plugin.getName())
                            .append(":\n    ")
                            .append("version: '")
                            .append(plugin.getDescription().getVersion())
                            .append('\'')
                            .append("\n    enabled: ")
                            .append(plugin.isEnabled());
                }

                b.append("\n\n# YAY! Now, let's see what we can find in your JVM\n");
                Runtime runtime = Runtime.getRuntime();
                RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
                b.append("Uptime: ").append(
                                TimeUnit.MINUTES.convert(rb.getUptime(), TimeUnit.MILLISECONDS) + " minutes")
                        .append('\n');
                b.append("JVM Flags: ").append(rb.getInputArguments()).append('\n');
                b.append("Free Memory: ").append(runtime.freeMemory() / 1024 / 1024 + " MB")
                        .append('\n');
                b.append("Max Memory: ").append(runtime.maxMemory() / 1024 / 1024 + " MB")
                        .append('\n');
                b.append("Java Name: ").append(rb.getVmName()).append('\n');
                b.append("Java Version: '").append(System.getProperty("java.version"))
                        .append("'\n");
                b.append("Java Vendor: '").append(System.getProperty("java.vendor")).append("'\n");
                b.append("Operating System: '").append(System.getProperty("os.name")).append("'\n");
                b.append("OS Version: ").append(System.getProperty("os.version")).append('\n');
                b.append("OS Arch: ").append(System.getProperty("os.arch")).append('\n');
                b.append("# Okay :D Great. You are now ready to create your bug report!");
                b.append(
                        "\n# You can do so at https://github.com/Sauilitired/Hyperverse/issues");
                b.append("\n# or via our Discord at https://discord.gg/KxkjDVg");

                // We use the PlotSquared profile
                final IncendoPaster incendoPaster = new IncendoPaster("plotsquared");
                incendoPaster.addFile(new IncendoPaster.PasteFile("information", b.toString()));

                try {
                    final File logFile = new File(this.server.getWorldContainer(), "./logs/latest.log");
                    if (Files.size(logFile.toPath()) > 14_000_000) {
                        throw new IOException("Too big...");
                    }
                    incendoPaster.addFile(new IncendoPaster.PasteFile("latest.log", IncendoPaster.readFile(logFile)));
                } catch (IOException ignored) {
                    MessageUtil.sendMessage(sender, Messages.messageLogTooBig);
                }

                try {
                    incendoPaster.addFile(new IncendoPaster.PasteFile(
                            "hyperverse.conf",
                            IncendoPaster.readFile(new File(hyperverse.getDataFolder(), "hyperverse.conf"))
                    ));
                } catch (final IllegalArgumentException | IOException ignored) {
                }


                for (final HyperWorld hyperWorld : this.worldManager.getWorlds()) {
                    incendoPaster.addFile(new IncendoPaster.PasteFile(String.format(
                            "%s.json",
                            hyperWorld.getConfiguration().getName()
                    ), IncendoPaster.readFile(this.worldManager.getWorldDirectory().
                            resolve(String.format("%s.json", hyperWorld.getConfiguration().getName())).toFile())));
                }

                try {
                    final String rawResponse = incendoPaster.upload();
                    final JsonObject jsonObject = JsonParser.parseString(rawResponse).getAsJsonObject();

                    if (jsonObject.has("created")) {
                        final String pasteId = jsonObject.get("paste_id").getAsString();
                        final String link =
                                String.format("https://athion.net/ISPaster/paste/view/%s", pasteId);
                        MessageUtil.sendMessage(sender, Messages.messagePasteUpload, "%paste%", link);
                    } else {
                        final String responseMessage = jsonObject.get("response").getAsString();
                        MessageUtil.sendMessage(sender, Messages.messagePasteFailed, "%reason%", responseMessage);
                    }
                } catch (final Throwable throwable) {
                    throwable.printStackTrace();
                    MessageUtil.sendMessage(sender, Messages.messagePasteFailed, "%reason%", throwable.getMessage());
                }
            } catch (final Exception ignored) {
            }
        }).execute();
    }

    @Subcommand("multiverse")
    @CommandPermission("hyperverse.plugin.import")
    @Description("{@@command.multiverse}")
    public void doMultiverse(final CommandSender sender) {
        if (this.server.getPluginManager().isPluginEnabled("Multiverse-Core")) {
            this.worldImporterFactory.createMultiverseImporter(this.hyperWorldFactory).performImport(sender);
        } else {
            MessageUtil.sendMessage(sender, Messages.messageImportPluginMissing, "%plugin%", "Multiverse");
        }
    }

    @Subcommand("myworlds")
    @CommandPermission("hyperverse.plugin.import")
    public void doMyWorlds(final CommandSender sender) {
        if (this.server.getPluginManager().isPluginEnabled("My_Worlds")) {
            this.worldImporterFactory.createMyWorldsImporter(this.hyperWorldFactory).performImport(sender);
        } else {
            MessageUtil
                    .sendMessage(sender, Messages.messageImportPluginMissing, "%plugin%", "MyWorlds");

        }
    }

    @Subcommand("plugin")
    @Description("{@@command.plugin}")
    public void doPlugin(final CommandSender sender) {
        final Hyperverse plugin = Hyperverse.getPlugin(Hyperverse.class);
        final PluginDescriptionFile description = plugin.getDescription();
        Stream.of(
                "<gold>Plugin Version:</gold> <gray>" + description.getVersion() + "</gray>",
                "<gold>Author(s):</gold> <gray>" + StringUtils.join(description.getAuthors(), ", ") + "</gray>",
                "<gold>Website:</gold> <gray><hover:show_text:\"<gray>Click to open</gray>\"><click:open_url:https://hyperver.se>https://hyperver.se</click></hover></gray>"
        ).forEach(msg ->
                MessageUtil.sendMessage(sender, new Message("plugin.internal", "<dark_gray>[</dark_gray><gold>Hyperverse</gold"
                        + "><dark_gray>]</dark_gray> " + msg)));
    }

    @Subcommand("regenerate|regen")
    @Description("{@@command.regenerate}")
    @CommandPermission("hyperverse.regenerate")
    @CommandCompletion("@hyperworlds true|false")
    public void doRegenerate(final CommandSender sender, final HyperWorld world, @Default("false") final boolean randomiseSeed) {
        if (world == null) {
            MessageUtil.sendMessage(sender, Messages.messageNoSuchWorld);
            return;
        }

        final WorldConfiguration configuration = world.getConfiguration().copy();
        if (randomiseSeed) {
            configuration.setSeed(SeedUtil.randomSeed());
        }

        world.deleteWorld(worldUnloadResult -> {
            if (worldUnloadResult != HyperWorld.WorldUnloadResult.SUCCESS) {
                MessageUtil.sendMessage(sender, Messages.messageWorldNotRemoved, "%reason%",
                        worldUnloadResult.getDescription()
                );
                return;
            }

            final Path path = this.server.getWorldContainer().toPath()
                    .resolve(world.getConfiguration().getName());
            try {
                try (Stream<Path> walk = Files.walk(path)) {
                    walk.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .peek(System.out::println)
                            .forEach(File::delete);
                }
            } catch (final Exception e) {
                MessageUtil.sendMessage(sender, Messages.messageWorldNotRemoved, "%reason%",
                        e.getMessage()
                );
                e.printStackTrace();
                return;
            }
            MessageUtil.sendMessage(sender, Messages.messageWorldRemoved);
            final HyperWorld hyperWorld = this.hyperWorldFactory.create(UUID.randomUUID(), configuration);
            MessageUtil.sendMessage(sender, Messages.messageWorldCreationStarted);
            hyperWorld.sendWorldInfo(sender);

            // Make sure we don't detect the world load
            this.worldManager.ignoreWorld(configuration.getName());

            try {
                hyperWorld.createBukkitWorld();
                // Register the world
                this.worldManager.addWorld(hyperWorld);
                MessageUtil.sendMessage(sender, Messages.messageWorldCreationFinished);
                if (sender instanceof Player) {
                    // Attempt to teleport them to the world
                    hyperWorld.teleportPlayer((Player) sender);
                }
            } catch (final HyperWorldValidationException validationException) {
                switch (validationException.getValidationResult()) {
                    case UNKNOWN_GENERATOR:
                        MessageUtil.sendMessage(sender, Messages.messageGeneratorInvalid,
                                "%world%", hyperWorld.getConfiguration().getName(),
                                "%generator%", hyperWorld.getConfiguration().getGenerator()
                        );
                        break;
                    case SUCCESS:
                        break;
                    default:
                        MessageUtil.sendMessage(sender, Messages.messageCreationUnknownFailure);
                        break;
                }
            } catch (final Exception e) {
                MessageUtil.sendMessage(sender, Messages.messageWorldCreationFailed,
                        "%reason%", e.getMessage()
                );
            }
        });
    }

}
