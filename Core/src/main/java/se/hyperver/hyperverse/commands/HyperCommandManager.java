//
// Hyperverse - A Minecraft world management plugin
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

package se.hyperver.hyperverse.commands;

import cloud.commandframework.Command;
import cloud.commandframework.Description;
import cloud.commandframework.MinecraftHelp;
import cloud.commandframework.arguments.standard.BooleanArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import co.aikar.taskchain.TaskChainFactory;
import com.google.inject.Inject;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import se.hyperver.hyperverse.Hyperverse;
import se.hyperver.hyperverse.commands.arguments.HyperWorldArgument;
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
import se.hyperver.hyperverse.util.*;
import se.hyperver.hyperverse.world.WorldType;
import se.hyperver.hyperverse.world.*;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class HyperCommandManager {

    private static final int WORLDS_PER_PAGE = 10;

    private final WorldManager worldManager;
    private final FileHyperConfiguration fileHyperConfiguration;
    private final HyperWorldFactory hyperWorldFactory;
    private final GlobalWorldFlagContainer globalFlagContainer;
    private final TaskChainFactory taskChainFactory;

    @Inject
    public HyperCommandManager(final Hyperverse hyperverse, final WorldManager worldManager,
                               final HyperWorldFactory hyperWorldFactory, final GlobalWorldFlagContainer globalFlagContainer,
                               final TaskChainFactory taskChainFactory, final FileHyperConfiguration hyperConfiguration) throws Exception {
        this.worldManager = Objects.requireNonNull(worldManager);
        this.hyperWorldFactory = Objects.requireNonNull(hyperWorldFactory);
        this.globalFlagContainer = Objects.requireNonNull(globalFlagContainer);
        this.taskChainFactory = Objects.requireNonNull(taskChainFactory);
        this.fileHyperConfiguration = Objects.requireNonNull(hyperConfiguration);

        /* Create the command manager */
        final PaperCommandManager<CommandSender> manager = new PaperCommandManager<>(
                hyperverse,
                CommandExecutionCoordinator.simpleCoordinator(),
                Function.identity(),
                Function.identity()
        );

        /* Register Brigadier support */
        try {
            manager.registerBrigadier();
        } catch (final Exception e) {
            hyperverse.getLogger().warning("Failed to enable Brigadier: " + e.getMessage());
        }

        /* Register asynchronous completions */
        try {
            manager.registerAsynchronousCompletions();
        } catch (final Exception e) {
            hyperverse.getLogger().warning("Failed to enable asynchronous completions: " + e.getMessage());
        }

        /* Create the help manager */
        final BukkitAudiences bukkitAudiences = BukkitAudiences.create(hyperverse);
        final MinecraftHelp<CommandSender> minecraftHelp = new MinecraftHelp<>(
                "/hyperverse",
                bukkitAudiences::sender,
                manager
        );

        /* Register the help command */
        manager.command(manager.commandBuilder("hyperverse")
                .meta("description", "Hyperverse help")
                .literal("help", Description.of("Hyperverse help"))
                .argument(
                        StringArgument.optional("query", StringArgument.StringMode.GREEDY),
                        Description.of("Search query")
                )
                .handler(context -> {
                    final String query = context.getOrDefault("query", "");
                    minecraftHelp.queryCommands(query, context.getSender());
                }).build()
        );

        /* Base command builder */
        final Command.Builder<CommandSender> builder = manager.commandBuilder("hyperverse");

        /* Construct the commands */
        manager.command(this.buildPlugin(builder))
               .command(this.buildRegenerate(builder));

        bukkitCommandManager.getCommandCompletions()
                .registerAsyncCompletion("import-candidates", context -> {
                    final File baseDirectory = Bukkit.getWorldContainer();
                    try {
                        return Files.list(baseDirectory.toPath()).filter(path -> {
                            final File file = path.toFile();
                            return file.isDirectory() && new File(file, "level.dat").isFile()
                                    && this.worldManager.getWorld(file.getName()) == null;
                        }).map(path -> path.toFile().getName()).sorted(Comparator.naturalOrder())
                                .collect(Collectors.toList());
                    } catch (IOException ex) {
                        return Collections.emptyList();
                    }
                });
        bukkitCommandManager.getCommandCompletions().registerAsyncCompletion("worldtypes", context -> {
            if (context.getInput().contains(" ")) {
                return Collections.emptyList();
            }
            return Arrays.stream(WorldType.values()).map(WorldType::name).map(String::toLowerCase)
                    .collect(Collectors.toList());
        });
        bukkitCommandManager.getCommandCompletions().registerAsyncCompletion("worldfeatures", context -> {
            if (context.getInput().contains(" ")) {
                return Collections.emptyList();
            }
            return Arrays.stream(WorldFeatures.values()).map(WorldFeatures::name).map(String::toLowerCase)
                    .collect(Collectors.toList());
        });
        bukkitCommandManager.getCommandCompletions().registerCompletion("null", context ->
                Collections.emptyList());
        bukkitCommandManager.getCommandCompletions()
                .registerAsyncCompletion("generators", context -> {
                    final String arg = context.getInput();
                    if (arg.contains(":")) {
                        return Collections.emptyList();
                    }
                    final List<String> generators = new ArrayList<>();
                    generators.add("vanilla");
                    for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                        generators.add(plugin.getName().toLowerCase());
                    }
                    return generators;
                });
        bukkitCommandManager.getCommandCompletions().registerAsyncCompletion("flags", context ->
                globalFlagContainer.getFlagMap().values().stream().map(WorldFlag::getName).collect(
                        Collectors.toList()));
        bukkitCommandManager.getCommandCompletions().registerAsyncCompletion("gamerules", context ->
                Arrays.stream(GameRule.values()).map(GameRule::getName).collect(Collectors.toList()));
        bukkitCommandManager.getCommandCompletions().registerCompletion("flag", context -> {
            final WorldFlag<?, ?> flag = context.getContextValue(WorldFlag.class);
            if (flag != null) {
                return flag.getTabCompletions();
            }
            return Collections.emptyList();
        });
        bukkitCommandManager.getCommandCompletions().registerAsyncCompletion("gamerule", context -> {
            final GameRule<?> gameRule = context.getContextValue(GameRule.class);
            if (gameRule != null) {
                if (gameRule.getType() == Boolean.class) {
                    return Arrays.asList("true", "false");
                }
            }
            return Collections.emptyList();
        });
        bukkitCommandManager.getCommandCompletions().registerCompletion("profile_groups", context -> {
            Stream<String> groups = worldManager.getWorlds().stream().map(world -> world.getFlag(ProfileGroupFlag.class)).filter(s -> !s.isEmpty());
            String requirePerms = context.getConfig("has_perms", "false");
            if (requirePerms.equalsIgnoreCase("true")) {
                groups = groups.filter(profile -> context.getSender().hasPermission("hyperverse.teleportgroup." + profile));
            }
            return groups.collect(Collectors.toList());
        });
        bukkitCommandManager.getCommandCompletions().registerAsyncCompletion("structures", context ->
                Arrays.asList("yes", "true", "generate_structures", "structures", "no", "false", "no_structures"));
        bukkitCommandManager.getCommandContexts().registerContext(WorldStructureSetting.class, context -> {
            switch (context.popFirstArg().toLowerCase()) {
                case "yes":
                case "true":
                case "generate_structures":
                case "structures":
                    return WorldStructureSetting.GENERATE_STRUCTURES;
                case "no":
                case "false":
                case "no_structures":
                    return WorldStructureSetting.NO_STRUCTURES;
                default:
                    throw new InvalidCommandArgument(Messages.messageInvalidStructureSetting.withoutColorCodes());
            }
        });
        bukkitCommandManager.getCommandCompletions()
                .registerCompletion("vararg_players", context -> {
                    String[] input = context.getInput().split(" ");
                    final int toPop;
                    try {
                        toPop = Integer.parseInt(context.getConfig("pop"));
                    } catch (final NumberFormatException ex) {
                        ex.printStackTrace();
                        return Collections.emptyList();
                    }
                    if (toPop > input.length) {
                        throw new IllegalArgumentException(
                                "Config to pop is greater than input length!");
                    }
                    input = Arrays.copyOfRange(input, toPop, input.length - 1);
                    for (int index = 0; index < input.length; index++) {
                        input[index] = input[index].toLowerCase();
                    }
                    final List<String> players = new ArrayList<>(Arrays.asList(input));
                    if (context.getPlayer() != null && !context.getConfig("self", "false")
                            .equalsIgnoreCase("true")) {
                        players.remove(context.getPlayer().getName());
                    }
                    return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                            .filter(player -> !players.contains(player.toLowerCase()))
                            .sorted(Comparator.naturalOrder()).collect(Collectors.toList());

                });
        bukkitCommandManager.getCommandCompletions().registerCompletion("vararg_player_world", context -> {
            String[] input = context.getInput().split(" ");
            final int toPop;
            try {
                toPop = Integer.parseInt(context.getConfig("pop"));
            } catch (final NumberFormatException ex) {
                ex.printStackTrace();
                return Collections.emptyList();
            }
            if (toPop > input.length) {
                throw new IllegalArgumentException(
                        "Config to pop is greater than input length!");
            }
            final String inWorld = context.getConfig("in_world", "false");
            final boolean checkInWorld = !inWorld.equalsIgnoreCase("false");
            input = Arrays.copyOfRange(input, toPop, input.length - 1);
            for (int index = 0; index < input.length; index++) {
                input[index] = input[index].toLowerCase();
            }
            final List<String> players = new ArrayList<>(Arrays.asList(input));
            if (context.getPlayer() != null && !context.getConfig("self", "false")
                    .equalsIgnoreCase("true")) {
                players.remove(context.getPlayer().getName());
            }
            Stream<? extends Player> stream = Bukkit.getOnlinePlayers().stream();
            if (checkInWorld) {
                final HyperWorld world = context.getContextValue(HyperWorld.class);
                if (world == null) {
                    return Collections.emptyList();
                }
                stream = stream.filter(player -> player.getWorld() != world.getBukkitWorld());
            }
            return stream.map(Player::getName)
                    .filter(player -> !players.contains(player.toLowerCase()))
                    .sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        });
        bukkitCommandManager.getCommandContexts().registerContext(WorldType.class, context -> {
            final String arg = context.popFirstArg();
            return WorldType.fromString(arg).orElseThrow(() ->
                    new InvalidCommandArgument(Messages.messageInvalidWorldType.withoutColorCodes()));
        });
        bukkitCommandManager.getCommandContexts().registerContext(WorldFeatures.class, context -> {
            final String arg = context.popFirstArg();
            return WorldFeatures.fromName(arg).orElseThrow(() ->
                    new InvalidCommandArgument(Messages.messageInvalidWorldFeatures.withoutColorCodes()));
        });
        bukkitCommandManager.getCommandContexts().registerContext(GameRule.class, context ->
                java.util.Optional.ofNullable(GameRule.getByName(context.popFirstArg()))
                        .orElseThrow(() -> new InvalidCommandArgument(Messages.messageInvalidGameRule.withoutColorCodes())));
        bukkitCommandManager.getCommandContexts().registerContext(WorldFlag.class, context -> {
            final WorldFlag<?, ?> flag = this.globalFlagContainer.getFlagFromString(context.popFirstArg().toLowerCase());
            if (flag == null) {
                throw new InvalidCommandArgument(Messages.messageFlagUnknown.withoutColorCodes());
            }
            return flag;
        });
    }

    @Category("Management")
    @Subcommand("create")
    @Syntax("<world> [generator: plugin name, vanilla][:[args]] [type: overworld, nether, end] [seed] [generate-structures: true, false] [features: normal, flatland, amplified, bucket] [settings...]")
    @CommandPermission("hyperverse.create")
    @Description("{@@command.create}")
    @CommandCompletion("@null @generators @worldtypes @null @structures @worldfeatures @null")
    public void createWorld(final CommandSender sender, final String world, String generator,
                            @Default("overworld") final WorldType type, @Optional final Long specifiedSeed,
                            @Default("true") final WorldStructureSetting generateStructures, @Default("normal") final WorldFeatures features,
                            @Default final String settings) {
        final long seed = specifiedSeed == null ? SeedUtil.randomSeed() : specifiedSeed;
        // Check if the name already exists
        for (final HyperWorld hyperWorld : this.worldManager.getWorlds()) {
            if (hyperWorld.getConfiguration().getName().equalsIgnoreCase(world)) {
                MessageUtil.sendMessage(sender, Messages.messageWorldExists);
                return;
            }
        }
        // Double check that Bukkit doesn't have the world stored
        if (Bukkit.getWorld(world) != null) {
            MessageUtil.sendMessage(sender, Messages.messageWorldExists);
            return;
        }
        // Now validate the world name
        if (!WorldUtil.validateName(world)) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNameInvalid);
            return;
        }

        String generatorArgs = "";
        if (generator.contains(":")) {
            final String[] split = generator.split(":");
            generator = split[0];
            generatorArgs = split[1];
        }

        // Check if the generator is actually valid
        final WorldConfiguration worldConfiguration =
                WorldConfiguration.builder().setName(world).setGenerator(generator).setType(type).setSeed(seed)
                        .setGenerateStructures(generateStructures).setSettings(settings).setWorldFeatures(features)
                        .setGeneratorArg(generatorArgs).createWorldConfiguration();
        final HyperWorld hyperWorld =
                hyperWorldFactory.create(UUID.randomUUID(), worldConfiguration);
        MessageUtil.sendMessage(sender, Messages.messageWorldCreationStarted);
        hyperWorld.sendWorldInfo(sender);

        // Make sure we don't detect the world load
        this.worldManager.ignoreWorld(world);

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
                            "%generator%", hyperWorld.getConfiguration().getGenerator());
                    break;
                case SUCCESS:
                    break;
                default:
                    MessageUtil.sendMessage(sender, Messages.messageCreationUnknownFailure);
                    break;
            }
        } catch (final Exception e) {
            MessageUtil.sendMessage(sender, Messages.messageWorldCreationFailed,
                    "%reason%", e.getMessage());
        }
    }

    @Category("Management")
    @Subcommand("import")
    @CommandPermission("hyperverse.import")
    @CommandAlias("hvimport")
    @CommandCompletion("@import-candidates @generators @worldtypes")
    @Description("{@@command.import}")
    public void importWorld(final CommandSender sender, final String worldName, final String generator, @Default("over_world") final WorldType worldType) {
        if (worldManager.getWorld(worldName) != null) {
            MessageUtil.sendMessage(sender, Messages.messageWorldAlreadyImported);
            return;
        }
        if (!WorldUtil.isSuitableImportCandidate(worldName, worldManager)) {
            MessageUtil.sendMessage(sender, Messages.messageNoSuchWorld);
            return;
        }
        worldManager.ignoreWorld(worldName); //Make sure we don't auto register on init
        final HyperWorld hyperWorld = hyperWorldFactory.create(UUID.randomUUID(),
                new WorldConfigurationBuilder().setName(worldName).setGenerator(generator)
                        .setType(worldType).createWorldConfiguration());
        final World bukkitWorld;
        try {
            hyperWorld.createBukkitWorld();
            bukkitWorld = hyperWorld.getBukkitWorld();
            assert bukkitWorld != null;
        } catch (HyperWorldValidationException e) {
            MessageUtil.sendMessage(sender, Messages.messageWorldImportFailure, "%world%", worldName,
                    "%result%", e.getMessage());
            return;
        }
        worldManager.addWorld(hyperWorld);
        MessageUtil.sendMessage(sender, Messages.messageWorldImportFinished);
        if (sender instanceof Player) {
            //Schedule teleport 1-tick later so the world has a chance to load.
            Bukkit.getScheduler().runTaskLater(Hyperverse.getPlugin(Hyperverse.class),
                    () -> doTeleport((Player) sender, worldManager.getWorld(bukkitWorld)), 1L);
        }
    }

    @Category("Informational")
    @Subcommand("list|l|worlds")
    @CommandPermission("hyperverse.list")
    @CommandAlias("hvl")
    @CommandCompletion("@range:1-10")
    @Description("{@@command.list}")
    public void doList(final CommandSender sender, @Default("1") final int page) {
        final List<HyperWorld> worlds = new ArrayList<>(this.worldManager.getWorlds());
        worlds.sort(Comparator.comparing(world -> world.getConfiguration().getName()));

        final int pages = 1 + (int) ((double) worlds.size() / WORLDS_PER_PAGE);
        final int clampedPage = Math.max(1, Math.min(pages, page));
        final int min = WORLDS_PER_PAGE * (clampedPage - 1);
        final int max = Math.min(worlds.size(), WORLDS_PER_PAGE * clampedPage);

        MessageUtil.sendMessage(sender, Messages.messageListHeader, "%page%", Integer.toString(clampedPage), "%max%", Integer.toString(pages));

        Stream<HyperWorld> stream = worlds.subList(min, max).stream().sorted(Comparator.comparing(world -> world.getConfiguration().getName()));
        if (sender instanceof Entity) {
            stream = stream.sorted(Comparator
                    .comparing(world -> !((Entity) sender).getWorld().equals(world.getBukkitWorld())));
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
            if (sender instanceof Entity && ((Entity) sender).getWorld() == hyperWorld.getBukkitWorld()) {
                message = Messages.messageListEntryCurrentWorld;
            } else {
                message = Messages.messageListEntry;
            }

            MessageUtil.sendMessage(sender, message, "%name%", configuration.getName(),
                    "%display-name%", hyperWorld.getDisplayName(), "%generator%", generator, "%type%",
                    configuration.getType().name(), "%load_status%", loadStatus);
        });
    }

    @Category("Misc")
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

    @Category("Misc")
    @Subcommand("teleport|tp")
    @CommandAlias("hvtp")
    @CommandPermission("hyperverse.teleport")
    @CommandCompletion("@hyperworlds:state=loaded @vararg_player_world:pop=0,in_world=true")
    public void doMassTeleport(final CommandSender sender, final HyperWorld world, String[] players) {
        if (players.length == 0) {
            if (sender instanceof Player) {
                doTeleport((Player) sender, world);
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
            final Player player = Bukkit.getPlayer(rawPlayer);
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
                        player.getName());
                continue;
            }
            MessageUtil.sendMessage(player, Messages.messageTeleporting, "%world%", world.getDisplayName());
            if (player != sender) {
                MessageUtil.sendMessage(sender, Messages.messageTeleportingPlayer, "%player%",
                        player.getName(), "%world%", world.getDisplayName());
            }
            world.teleportPlayer(player);
        }
    }

    @Category("Informational")
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

    @Category("Management")
    @Subcommand("unload")
    @CommandPermission("hyperverse.unload")
    @CommandCompletion("@hyperworlds:state=loaded true|false")
    @Description("{@@command.unload}")
    public void doUnload(final CommandSender sender, final HyperWorld world,
                         @Default("true") final boolean saveWorld) {
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
                    "%reason%", worldUnloadResult.getDescription());
        }
    }

    @Category("Management")
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
                    "%world%", world.getConfiguration().getName(), "%result%", e.getMessage());
            return;
        }

        world.getConfiguration().setLoaded(true);
        world.saveConfiguration();

        MessageUtil.sendMessage(sender, Messages.messageWorldLoadedSuccessfully);
    }

    @Category("Misc")
    @Subcommand("find|where")
    @CommandPermission("hyperverse.find")
    @CommandAlias("hvf|hvfind")
    @CommandCompletion("@players")
    @Description("{@@command.find}")
    //public void findPlayer(final CommandSender sender, final String... players) {
    public void findPlayer(final CommandSender sender, final String player) {
        //for (String player : players) {
        final Player bukkitPlayer = Bukkit.getPlayer(player);
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
                        "%z%", format.format(location.getZ()));
        //}
    }

    @Category("Misc")
    @Subcommand("who")
    @CommandPermission("hyperverse.who")
    @CommandAlias("hvwho")
    @CommandCompletion("@hyperworlds")
    @Description("{@@command.who}")
    public void findPlayersPresent(final CommandSender sender, @Optional final String world) {
        if (world != null) {
            final World bukkitWorld = Bukkit.getWorld(world);
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
                                "%z%", format.format(location.getZ())));
            }
            MessageUtil.sendMessage(sender, Messages.messageListEntryWorld, "%players%",
                    players.toString().trim(), "%world%", hyperWorld.getDisplayName());
        } else {
            for (final World bukkitWorld : Bukkit.getWorlds()) {
                findPlayersPresent(sender, bukkitWorld.getName());
            }
        }
    }

    @Category("Management")
    @Subcommand("flag set")
    @CommandPermission("hyperverse.flag.set")
    @CommandCompletion("@hyperworlds @flags @flag")
    @Description("{@@command.flag.set}")
    public void doFlagSet(final CommandSender sender, final HyperWorld hyperWorld,
                          final WorldFlag<?, ?> flag, final String value) {
        try {
            hyperWorld.setFlag(flag, value);
        } catch (final FlagParseException e) {
            MessageUtil.sendMessage(sender, Messages.messageFlagParseError, "%flag%", e.getFlag().getName(), "%value%", e.getValue(), "%reason%", e.getErrorMessage());
            return;
        }
        MessageUtil.sendMessage(sender, Messages.messageFlagSet);
    }

    @Category("Management")
    @Subcommand("flag reset")
    @CommandPermission("hyperverse.flag.set")
    @CommandCompletion("@flags|@hyperworlds @flags")
    @SuppressWarnings("unchecked")
    public void doFlagReset(final CommandSender sender, final HyperWorld hyperWorld, final WorldFlag<?, ?> flag) {
        final WorldFlag<?, ?> toSet;
        if (flag.getClass() == EndFlag.class) {
            final World bukkitWorld = hyperWorld.getBukkitWorld();
            if (bukkitWorld == null) {
                toSet = globalFlagContainer.getFlag(flag.getClass());
            } else {
                WorldFlag<?, ?> temp;
                try {
                    temp = EndFlag.END_FLAG_DEFAULT.parse(bukkitWorld.getName() + "_the_end");
                } catch (FlagParseException ignored) {
                    temp = globalFlagContainer.getFlag(flag.getClass());
                }
                toSet = temp;
            }
        } else if (flag.getClass() == NetherFlag.class) {
            final World bukkitWorld = hyperWorld.getBukkitWorld();
            if (bukkitWorld == null) {
                toSet = globalFlagContainer.getFlag(flag.getClass());
            } else {
                WorldFlag<?, ?> temp;
                try {
                    temp = NetherFlag.NETHER_FLAG_DEFAULT.parse(bukkitWorld.getName() + "_nether");
                } catch (FlagParseException ignored) {
                    temp = globalFlagContainer.getFlag(flag.getClass());
                }
                toSet = temp;
            }
        } else {
            toSet = globalFlagContainer.getFlag(flag.getClass());
        }
        hyperWorld.setFlagInstance(toSet);
        MessageUtil.sendMessage(sender, Messages.messageFlagSet);
    }

    @Category("Management")
    @Subcommand("flag remove")
    @CommandPermission("hyperverse.flag.set")
    @CommandCompletion("@hyperworlds @flags")
    @Description("{@@command.flag.remove}")
    public void doFlagRemove(final CommandSender sender, final HyperWorld hyperWorld, final WorldFlag<?, ?> flag) {
        hyperWorld.removeFlag(flag);
        MessageUtil.sendMessage(sender, Messages.messageFlagRemoved);
    }

    @Category("Management")
    @Subcommand("flag info")
    @CommandPermission("hyperverse.flag.info")
    @CommandCompletion("@flags|@hyperworlds @flags")
    @SuppressWarnings("unchecked")
    public <T> void showFlagStatus(final CommandSender sender, final HyperWorld hyperWorld, final WorldFlag<T, ?> flag) {
        final String value = String.valueOf(hyperWorld.getFlag((Class<? extends WorldFlag<T, ?>>) flag.getClass())), defaultValue;
        defaultValue = String.valueOf((globalFlagContainer.getFlag(flag.getClass()).getValue()));
        MessageUtil.sendMessage(sender, Messages.messageFlagDisplayInfo, "%description%", flag.getFlagDescription().toString(), "%current%", value.isEmpty() ? "Unset" : value, "%default%", defaultValue.isEmpty() ? "Unset" : defaultValue);
    }

    @Category("Management")
    @Subcommand("gamerule set")
    @CommandPermission("hyperverse.gamerule.set")
    @CommandCompletion("@hyperworlds @gamerules @gamerule")
    @Description("{@@command.gamerule.set}")
    public void doGameRuleSet(final CommandSender sender, final HyperWorld hyperWorld,
                              final GameRule gameRule, final String value) {
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

    @Category("Management")
    @Subcommand("gamerule remove")
    @CommandPermission("hyperverse.gamerule.set")
    @CommandCompletion("@hyperworlds @gamerules")
    @Description("{@@command.gamerule.remove}")
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
        hyperWorld.getBukkitWorld().setGameRule(gameRule,
                hyperWorld.getBukkitWorld().getGameRuleDefault(gameRule));
        MessageUtil.sendMessage(sender, Messages.messageGameRuleRemoved);
    }

    @Category("Management")
    @Subcommand("delete")
    @CommandPermission("hyperverse.delete")
    @CommandCompletion("@hyperworlds true|false")
    @Description("{@@command.delete}")
    public void doDelete(final CommandSender sender, final HyperWorld hyperWorld,
                         @Default("false") boolean deleteDirectory) {
        if (hyperWorld == null) {
            MessageUtil.sendMessage(sender, Messages.messageNoSuchWorld);
            return;
        }
        hyperWorld.deleteWorld(worldUnloadResult -> {
            if (worldUnloadResult != HyperWorld.WorldUnloadResult.SUCCESS) {
                MessageUtil.sendMessage(sender, Messages.messageWorldNotRemoved, "%reason%",
                        worldUnloadResult.getDescription());
                return;
            }

            if (deleteDirectory) {
                final Path path = Bukkit.getWorldContainer().toPath()
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
                            e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }

            MessageUtil.sendMessage(sender, Messages.messageWorldRemoved);
        });
    }

    @Category("Misc")
    @Subcommand("reload")
    @CommandPermission("hyperverse.reload")
    @CommandAlias("hvreload")
    @Description("{@@command.reload}")
    public void doConfigReload(final CommandSender sender) {
        if (!Hyperverse.getPlugin(Hyperverse.class).reloadConfiguration(sender)) {
            throw new CommandException("Failed to reload configuration files");
        }
    }

    @Category("Misc")
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
                b.append("Server Version: ").append(Bukkit.getVersion()).append("\n");

                b.append("Plugins:");
                for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    b.append("\n  ").append(plugin.getName()).append(":\n    ").append("version: '")
                            .append(plugin.getDescription().getVersion()).append('\'').append("\n    enabled: ").append(plugin.isEnabled());
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
                    final File logFile = new File(Bukkit.getWorldContainer(), "./logs/latest.log");
                    if (Files.size(logFile.toPath()) > 14_000_000) {
                        throw new IOException("Too big...");
                    }
                    incendoPaster.addFile(new IncendoPaster.PasteFile("latest.log", IncendoPaster.readFile(logFile)));
                } catch (IOException ignored) {
                    MessageUtil.sendMessage(sender, Messages.messageLogTooBig);
                }

                try {
                    incendoPaster.addFile(new IncendoPaster.PasteFile("hyperverse.conf",
                            IncendoPaster.readFile(new File(hyperverse.getDataFolder(), "hyperverse.conf"))));
                } catch (final IllegalArgumentException | IOException ignored) {
                }


                for (final HyperWorld hyperWorld : worldManager.getWorlds()) {
                    incendoPaster.addFile(new IncendoPaster.PasteFile(String.format("%s.json",
                            hyperWorld.getConfiguration().getName()), IncendoPaster.readFile(this.worldManager.getWorldDirectory().
                            resolve(String.format("%s.json", hyperWorld.getConfiguration().getName())).toFile())));
                }

                try {
                    final String rawResponse = incendoPaster.upload();
                    final JsonObject jsonObject = new JsonParser().parse(rawResponse).getAsJsonObject();

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

    @Category("Misc")
    @Subcommand("multiverse")
    @CommandPermission("hyperverse.plugin.import")
    @Description("{@@command.multiverse}")
    public void doMultiverse(final CommandSender sender) {
        if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core")) {
            new MultiverseImporter(this.worldManager, this.hyperWorldFactory)
                    .performImport(sender);
        } else {
            MessageUtil.sendMessage(sender, Messages.messageImportPluginMissing, "%plugin%", "Multiverse");
        }
    }

    @Category("Misc")
    @Subcommand("myworlds")
    @CommandPermission("hyperverse.plugin.import")
    public void doMyWorlds(final CommandSender sender) {
        if (Bukkit.getPluginManager().isPluginEnabled("My_Worlds")) {
            new MyWorldsImporter(this.worldManager, this.hyperWorldFactory).performImport(sender);
        } else {
            MessageUtil
                    .sendMessage(sender, Messages.messageImportPluginMissing, "%plugin%", "MyWorlds");

        }
    }

    @NotNull
    private Command<CommandSender> buildPlugin(@NotNull final Command.Builder<CommandSender> builder) {
        return builder
                .literal("plugin", Description.of(Messages.commandDescriptionPlugin.toString()))
                .handler(context -> {
                    final Hyperverse plugin = Hyperverse.getPlugin(Hyperverse.class);
                    final PluginDescriptionFile description = plugin.getDescription();
                    Stream.of("<gold>Plugin Version:</gold> <gray>" + description.getVersion() + "</gray>",
                            "<gold>Author(s):</gold> <gray>" + StringUtils.join(description.getAuthors(), ", ") + "</gray>",
                            "<gold>Website:</gold> <gray><hover:show_text:\"<gray>Click to open</gray>\"><click:open_url:https://hyperver.se>https://hyperver.se</click></hover></gray>").forEach(msg ->
                            context.getSender().spigot().sendMessage(MiniMessage.get().parse(
                                    "<dark_gray>[</dark_gray><gold>Hyperverse</gold><dark_gray>]</dark_gray> " + msg)));
                }).build();
    }

    @NotNull
    private Command<CommandSender> buildRegenerate(@NotNull final Command.Builder<CommandSender> builder) {
        return builder
                .literal("regenerate", Description.of(Messages.commandDescriptionRegenerate.toString()), "regen")
                .argument(new HyperWorldArgument(this.worldManager, true, "world"), Description.of("World to regenerate"))
                .argument(
                        BooleanArgument.optional("randomise", false),
                        Description.of("Whether or not to randomise the seed")
                )
                .withPermission("hyperverse.regenerate")
                .handler(context -> {
                    final HyperWorld world = context.get("world");
                    final boolean randomiseSeed = context.getOrDefault("regenerate", false);
                    final CommandSender sender = context.getSender();

                    final WorldConfiguration configuration = world.getConfiguration().copy();
                    if (randomiseSeed) {
                        configuration.setSeed(SeedUtil.randomSeed());
                    }

                    world.deleteWorld(worldUnloadResult -> {
                        if (worldUnloadResult != HyperWorld.WorldUnloadResult.SUCCESS) {
                            MessageUtil.sendMessage(sender, Messages.messageWorldNotRemoved, "%reason%",
                                    worldUnloadResult.getDescription());
                            return;
                        }

                        final Path path = Bukkit.getWorldContainer().toPath()
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
                                    e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                        MessageUtil.sendMessage(sender, Messages.messageWorldRemoved);
                        final HyperWorld hyperWorld = hyperWorldFactory.create(UUID.randomUUID(), configuration);
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
                                            "%generator%", hyperWorld.getConfiguration().getGenerator());
                                    break;
                                case SUCCESS:
                                    break;
                                default:
                                    MessageUtil.sendMessage(sender, Messages.messageCreationUnknownFailure);
                                    break;
                            }
                        } catch (final Exception e) {
                            MessageUtil.sendMessage(sender, Messages.messageWorldCreationFailed,
                                    "%reason%", e.getMessage());
                        }
                    });
                }).build();
    }

}
