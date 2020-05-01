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

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.BukkitMessageFormatter;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageType;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import co.aikar.taskchain.TaskChainFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import me.minidigger.minimessage.text.MiniMessageParser;
import net.kyori.text.adapter.bukkit.TextAdapter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import se.hyperver.hyperverse.Hyperverse;
import se.hyperver.hyperverse.configuration.FileHyperConfiguration;
import se.hyperver.hyperverse.configuration.Message;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.exception.HyperWorldValidationException;
import se.hyperver.hyperverse.flags.FlagParseException;
import se.hyperver.hyperverse.flags.GlobalWorldFlagContainer;
import se.hyperver.hyperverse.flags.WorldFlag;
import se.hyperver.hyperverse.modules.HyperWorldFactory;
import se.hyperver.hyperverse.util.IncendoPaster;
import se.hyperver.hyperverse.util.MessageUtil;
import se.hyperver.hyperverse.util.MultiverseImporter;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandAlias("hyperverse|hv|worlds|world")
@CommandPermission("hyperverse.worlds")
@SuppressWarnings("unused")
public class HyperCommandManager extends BaseCommand {

    private final BukkitCommandManager bukkitCommandManager;
    private final WorldManager worldManager;
    private final FileHyperConfiguration fileHyperConfiguration;
    private final HyperWorldFactory hyperWorldFactory;
    private final GlobalWorldFlagContainer globalFlagContainer;
    private final TaskChainFactory taskChainFactory;

    @Inject public HyperCommandManager(final Hyperverse hyperverse, final WorldManager worldManager,
        final HyperWorldFactory hyperWorldFactory, final GlobalWorldFlagContainer globalFlagContainer,
        final TaskChainFactory taskChainFactory, final FileHyperConfiguration hyperConfiguration) {
        this.worldManager = Objects.requireNonNull(worldManager);
        this.hyperWorldFactory = Objects.requireNonNull(hyperWorldFactory);
        this.globalFlagContainer = Objects.requireNonNull(globalFlagContainer);
        this.taskChainFactory = Objects.requireNonNull(taskChainFactory);
        this.fileHyperConfiguration = Objects.requireNonNull(hyperConfiguration);

        // Create the command manager
        bukkitCommandManager = new BukkitCommandManager(hyperverse);
        bukkitCommandManager.usePerIssuerLocale(true, true);
        bukkitCommandManager.getLocales().addMessages(Locale.ENGLISH, Messages.getMessages());
        bukkitCommandManager.setDefaultFormatter(new BukkitMessageFormatter(ChatColor.GRAY) {
            @Override public String format(@NotNull final String message) {
                return ChatColor.translateAlternateColorCodes('&', Messages.messagePrefix.toString())
                    + super.format(message);
            }
        });
        bukkitCommandManager.setFormat(MessageType.ERROR, new BukkitMessageFormatter(ChatColor.RED, ChatColor.GOLD, ChatColor.WHITE) {
            @Override public String format(final String message) {
                return ChatColor.translateAlternateColorCodes('&', Messages.messagePrefix.toString())
                    + ChatColor.RED + super.format(message);
            }
        });
        bukkitCommandManager.setFormat(MessageType.SYNTAX, new BukkitMessageFormatter(ChatColor.GRAY, ChatColor.GOLD, ChatColor.WHITE) {
            @Override public String format(final String message) {
                return ChatColor.translateAlternateColorCodes('&', Messages.messagePrefix.toString())
                    + ChatColor.GRAY + super.format(message);
            }
        });
        bukkitCommandManager.setFormat(MessageType.HELP, new BukkitMessageFormatter(ChatColor.GRAY, ChatColor.GOLD, ChatColor.WHITE) {
            @Override public String format(final String message) {
                return ChatColor.translateAlternateColorCodes('&', Messages.messagePrefix.toString())
                    + ChatColor.GRAY + super.format(message);
            }
        });
        bukkitCommandManager.getCommandCompletions().registerAsyncCompletion("hyperworlds",
            context -> worldManager.getWorlds().stream().filter(hyperWorld -> {
                final String stateSel = context.getConfig("state", "").toLowerCase();
                final String playerSel = context.getConfig("players", "").toLowerCase();
                if (!hyperWorld.isLoaded()) {
                    return false;
                }
                assert hyperWorld.getBukkitWorld() != null;
                boolean ret = true;
                switch (stateSel) {
                    case "loaded":
                        ret = hyperWorld.isLoaded(); break;
                    case "not_loaded":
                        ret = !hyperWorld.isLoaded(); break;
                    default:
                        break;
                }
                switch (playerSel) {
                    case "no_players":
                        ret = ret && hyperWorld.getBukkitWorld().getPlayers().isEmpty(); break;
                    case "has_players":
                        ret = ret && !hyperWorld.getBukkitWorld().getPlayers().isEmpty(); break;
                    default:
                        break;
                }
                return ret;

            }).map(HyperWorld::getConfiguration).map(WorldConfiguration::getName)
                .filter(worldName -> {
                    final String selection = context.getConfig("player", "").toLowerCase();
                    final boolean inWorld = context.getIssuer().isPlayer() &&
                        worldName.equalsIgnoreCase(context.getPlayer().getWorld().getName());
                    switch (selection) {
                        case "not_in":
                            return !inWorld;
                        case "in":
                            return inWorld;
                        default:
                            return true;
                    }
                }).collect(Collectors.toList()));
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
        bukkitCommandManager.getCommandContexts().registerContext(HyperWorld.class, context -> {
            final HyperWorld hyperWorld = worldManager.getWorld(context.popFirstArg());
            if (hyperWorld == null) {
                throw new InvalidCommandArgument(Messages.messageNoSuchWorld.withoutColorCodes());
            }
            return hyperWorld;
        });
        bukkitCommandManager.getCommandContexts().registerContext(GameRule.class, context ->
            java.util.Optional.ofNullable(GameRule.getByName(context.popFirstArg()))
                .orElseThrow(() -> new InvalidCommandArgument(Messages.messageInvalidGameRule.withoutColorCodes())));
        bukkitCommandManager.getCommandContexts().registerContext(WorldFlag.class, context ->
            this.globalFlagContainer.getFlagFromString(context.popFirstArg().toLowerCase()));
        //noinspection deprecation
        bukkitCommandManager.enableUnstableAPI("help");
        bukkitCommandManager.registerCommand(this);
    }

    @HelpCommand public void doHelp(final CommandSender sender, final CommandHelp commandHelp) {
        commandHelp.showHelp();
    }

    @Category("Management")
    @Subcommand("create") @Syntax("<world> [generator: plugin name, vanilla][:[args]] [type: overworld, nether, end] [seed] [generate-structures: true, false] [features: normal, flat, amplified, bucket] [settings...]")
    @CommandPermission("hyperverse.create") @Description("{@@command.create}")
    @CommandCompletion("@null @generators @worldtypes @null @null @structures @worldfeatures @null")
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

    @Category("Management") @Subcommand("import") @CommandPermission("hyperverse.import")
    @CommandAlias("hvimport") @CommandCompletion("@import-candidates @generators @worldtypes") @Description("{@@command.import}")
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
            MessageUtil.sendMessage(sender, Messages.messageWorldImportFailure, "%reason%",
                e.getMessage());
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

    @Category("Informational") @Subcommand("list|l|worlds") @CommandPermission("hyperverse.list")
    @CommandAlias("hvl") @Description("{@@command.list}") public void doList(final CommandSender sender) {
        MessageUtil.sendMessage(sender, Messages.messageListHeader);
        Stream<HyperWorld> stream = this.worldManager.getWorlds().stream().sorted(Comparator.comparing(world -> world.getConfiguration().getName()));
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
                "%generator%", generator, "%type%", configuration.getType().name(), "%load_status%", loadStatus);
        });
    }

    @Category("Misc") @Subcommand("teleport|tp") @CommandAlias("hvtp") @CommandPermission("hyperverse.teleport")
    @CommandCompletion("@hyperworlds:player=not_in,state=loaded") @Description("{@@command.teleport}")
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
        MessageUtil.sendMessage(player, Messages.messageTeleporting, "%world%",
            world.getConfiguration().getName());
        world.teleportPlayer(player);
    }

    @Category("Informational") @Subcommand("info|i") @CommandAlias("hvi")
    @CommandPermission("hyperverse.info") @CommandCompletion("@hyperworlds") @Description("View world info")
    public void doInfo(final CommandSender sender, final HyperWorld world) {
        if (world == null) {
            return;
        }
        MessageUtil.sendMessage(sender, Messages.messageInfoHeader);
        world.sendWorldInfo(sender);
    }

    @Category("Management") @Subcommand("unload") @CommandPermission("hyperverse.unload")
    @CommandCompletion("@hyperworlds:state=loaded") @Description("{@@command.unload}")
    public void doUnload(final CommandSender sender, final HyperWorld world) {
        if (world == null) {
            return;
        }
        if (!world.isLoaded()) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNotLoaded);
            return;
        }
        final HyperWorld.WorldUnloadResult worldUnloadResult = world.unloadWorld();
        if (worldUnloadResult == HyperWorld.WorldUnloadResult.SUCCESS) {
            MessageUtil.sendMessage(sender, Messages.messageWorldUnloaded);
        } else {
            MessageUtil.sendMessage(sender, Messages.messageWorldUnloadFailed,
                "%reason%", worldUnloadResult.getDescription());
        }
    }

    @Category("Management") @Subcommand("load") @CommandPermission("hyperverse.load")
    @CommandCompletion("@hyperworlds:state=unloaded") @Description("{@@command.load}")
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

    @Category("Misc") @Subcommand("find|where") @CommandPermission("hyperverse.find") @CommandAlias("hvf|hvfind")
    @CommandCompletion("@players") @Description("{@@command.find}")
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

    @Category("Misc") @Subcommand("who") @CommandPermission("hyperverse.who") @CommandAlias("hvwho")
    @CommandCompletion("@hyperworlds") @Description("{@@command.who}")
    public void findPlayersPresent(final CommandSender sender, @Optional final String world) {
        if (world != null) {
            final World bukkitWorld = Bukkit.getWorld(world);
            if (bukkitWorld == null) {
                MessageUtil.sendMessage(sender, Messages.messageNoSuchWorld);
                return;
            }
            final DecimalFormat format = Messages.miscCoordinateDecimalFormat;
            if (bukkitWorld.getPlayers().isEmpty()) {
                MessageUtil.sendMessage(sender, Messages.messageNoPlayersInWorld, "%world%", world);
                return;
            }
            final StringBuilder players = new StringBuilder();
            for (final Player player : bukkitWorld.getPlayers()) {
                final Location location = player.getLocation();
                players.append(MessageUtil.format(Messages.messageListEntryPlayer.toString(), "%player%",
                    player.getDisplayName(), "%world%", world, "%x%",
                    format.format(location.getX()), "%y%",
                    format.format(location.getY()), "%z%",
                    format.format(location.getZ())));
            }
            MessageUtil.sendMessage(sender, Messages.messageListEntryWorld, "%players%", players.toString(),
                "%world%", bukkitWorld.getName());
        }
        else {
            for (final World bukkitWorld : Bukkit.getWorlds()) {
                findPlayersPresent(sender, bukkitWorld.getName());
            }
        }
    }

    @Category("Management") @Subcommand("flag set") @CommandPermission("hyperverse.flag.set")
    @CommandCompletion("@hyperworlds @flags @flag") @Description("{@@command.flag.set}")
    public void doFlagSet(final CommandSender sender, final HyperWorld hyperWorld,
        final WorldFlag<?, ?> flag, final String value) {
        if (flag == null) {
            MessageUtil.sendMessage(sender, Messages.messageFlagUnknown);
            return;
        }
        if (hyperWorld == null) {
            MessageUtil.sendMessage(sender, Messages.messageNoSuchWorld);
            return;
        }
        try {
            hyperWorld.setFlag(flag, value);
        } catch (final FlagParseException e) {
            MessageUtil.sendMessage(sender, Messages.messageFlagParseError,
                "%flag%", e.getFlag().getName(), "%value%", e.getValue(), "%reason%", e.getErrorMessage());
            return;
        }
        MessageUtil.sendMessage(sender, Messages.messageFlagSet);
    }

    @Category("Mangement") @Subcommand("flag remove") @CommandPermission("hyperverse.flag.set")
    @CommandCompletion("@hyperworlds @flags") @Description("{@@command.flag.remove}")
    public void doFlagRemove(final CommandSender sender, final HyperWorld hyperWorld, final WorldFlag<?, ?> flag) {
        if (flag == null) {
            MessageUtil.sendMessage(sender, Messages.messageFlagUnknown);
            return;
        }
        if (hyperWorld == null) {
            MessageUtil.sendMessage(sender, Messages.messageNoSuchWorld);
            return;
        }
        hyperWorld.removeFlag(flag);
        MessageUtil.sendMessage(sender, Messages.messageFlagRemoved);
    }

    @Category("Management") @Subcommand("gamerule set") @CommandPermission("hyperverse.gamerule.set")
    @CommandCompletion("@hyperworlds @gamerules @gamerule") @Description("{@@command.gamerule.set}")
    public void doGameRuleSet(final CommandSender sender, final HyperWorld hyperWorld,
        final GameRule gameRule, final String value) {
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

    @Category("Management") @Subcommand("gamerule remove") @CommandPermission("hyperverse.gamerule.set")
    @CommandCompletion("@hyperworlds @gamerules") @Description("{@@command.gamerule.remove}")
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

    @Category("Management") @Subcommand("delete") @CommandPermission("hyperverse.delete")
    @CommandCompletion("@hyperworlds true|false") @Description("{@@command.delete}")
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

    @Category("Misc") @Subcommand("reload") @CommandPermission("hyperverse.reload") @CommandAlias("hvreload")
    @Description("{@@command.reload}")
    public void doConfigReload(final CommandSender sender) {
        if (Hyperverse.getPlugin(Hyperverse.class).reloadConfiguration(sender)) {
            MessageUtil.sendMessage(sender, Messages.messageConfigReloaded);
        } else {
            throw new CommandException("Failed to reload configuration files");
        }
    }

    @Category("Misc") @Subcommand("debugpaste") @CommandPermission("hyperverse.debugpaste")
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
                    final JsonObject jsonObject =
                        new JsonParser().parse(rawResponse).getAsJsonObject();

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

    @Category("Misc") @Subcommand("multiverse") @CommandPermission("hyperverse.multiverse")
    @Description("{@@command.multiverse}")
    public void doMultiverse(final CommandSender sender) {
        if (Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core")) {
            new MultiverseImporter(this.worldManager, this.hyperWorldFactory)
                .performImport(sender);
        } else {
            MessageUtil.sendMessage(sender, Messages.messageMultiverseMissing);
        }
    }

    @Category("Misc") @Subcommand("plugin")
    @Description("{@@command.plugin}")
    public void doPlugin(final CommandSender sender) {
        final Hyperverse plugin = Hyperverse.getPlugin(Hyperverse.class);
        final PluginDescriptionFile description = plugin.getDescription();
        Stream.of("<gold>Plugin Version:</gold> <gray>" + description.getVersion() + "</gray>",
                  "<gold>Author(s):</gold> <gray>" + StringUtils.join(description.getAuthors(), ", ") + "</gray>",
                  "<gold>Website:</gold> <gray><hover:show_text:\"<gray>Click to open</gray>\"><click:open_url:https://hyperver.se>https://hyperver.se</click></hover></gray>").forEach(msg ->
            TextAdapter.sendComponent(sender, MiniMessageParser.parseFormat(
                "<dark_gray>[</dark_gray><gold>Hyperverse</gold><dark_gray>]</dark_gray> " + msg)));
    }

    @Category("Management") @Subcommand("regenerate|regen") @Description("{@@command.regenerate}")
    @CommandPermission("hyperverse.regenerate") @CommandCompletion("@hyperworlds true|false")
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
    }

}
