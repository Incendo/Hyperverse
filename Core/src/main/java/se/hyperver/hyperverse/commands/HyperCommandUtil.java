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

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.BukkitMessageFormatter;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.flags.GlobalWorldFlagContainer;
import se.hyperver.hyperverse.flags.WorldFlag;
import se.hyperver.hyperverse.flags.implementation.ProfileGroupFlag;
import se.hyperver.hyperverse.world.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class HyperCommandUtil {



    public static void setupCommandManager(final BukkitCommandManager bukkitCommandManager,
        final WorldManager worldManager, final GlobalWorldFlagContainer globalFlagContainer) {

        bukkitCommandManager.usePerIssuerLocale(true, true);
        bukkitCommandManager.getLocales().addMessages(Locale.ENGLISH, Messages.getMessages());

        setupLocales(bukkitCommandManager);
        setFormats(bukkitCommandManager);
        registerCompletions(bukkitCommandManager, worldManager);
        registerAsyncCompletions(bukkitCommandManager, worldManager, globalFlagContainer);
        registerContexts(bukkitCommandManager, worldManager, globalFlagContainer);
    }

    private static void setupLocales(final BukkitCommandManager bukkitCommandManager) {
        bukkitCommandManager.usePerIssuerLocale(true, true);
        bukkitCommandManager.getLocales().addMessages(Locale.ENGLISH, Messages.getMessages());
    }


    private static void setFormats(final BukkitCommandManager bukkitCommandManager) {
        bukkitCommandManager.setDefaultFormatter(new BukkitMessageFormatter(ChatColor.GRAY) {
            @Override public String format(@NotNull final String message) {
                return ChatColor.translateAlternateColorCodes('&', Messages.messagePrefix.toString())
                    + super.format(message);
            }
        });
        bukkitCommandManager.setFormat(
            MessageType.ERROR, new BukkitMessageFormatter(ChatColor.RED, ChatColor.GOLD, ChatColor.WHITE) {
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
    }

    private static void registerContexts(final BukkitCommandManager bukkitCommandManager,
        final WorldManager worldManager, final GlobalWorldFlagContainer globalFlagContainer) {
         /*bukkitCommandManager.getCommandContexts().registerContext(Player[].class, context -> {
            final List<String> args = context.getArgs();
            final Player[] arr = new Player[args.size()];
            for (int index = 0; index < args.size(); index++) {
                final Player player = Bukkit.getPlayer(args.get(index));
                if (player == null) {
                    throw new InvalidCommandArgument(MessageUtil.format(Messages.messageNoPlayerFound.toString(), "%name%", args.get(index)));
                }
                arr[index] = player;
            }
            args.clear();
            return arr;
        });*/
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
        bukkitCommandManager.getCommandContexts().registerIssuerAwareContext(HyperWorld.class, context -> {
            HyperWorld hyperWorld = worldManager.getWorld(context.getFirstArg());
            if (hyperWorld == null) {
                if (context.getPlayer() != null) {
                    hyperWorld = worldManager.getWorld(context.getPlayer().getWorld());
                }
                if (hyperWorld == null) {
                    throw new InvalidCommandArgument(Messages.messageNoSuchWorld.withoutColorCodes());
                }
            } else {
                context.popFirstArg(); // remove the world argument as it's a valid world
            }
            return hyperWorld;
        });
        bukkitCommandManager.getCommandContexts().registerContext(GameRule.class, context ->
            java.util.Optional.ofNullable(GameRule.getByName(context.popFirstArg()))
                .orElseThrow(() -> new InvalidCommandArgument(Messages.messageInvalidGameRule.withoutColorCodes())));
        bukkitCommandManager.getCommandContexts().registerContext(WorldFlag.class, context -> {
            final WorldFlag<?, ?> flag = globalFlagContainer.getFlagFromString(context.popFirstArg().toLowerCase());
            if (flag == null) {
                throw new InvalidCommandArgument(Messages.messageFlagUnknown.withoutColorCodes());
            }
            return flag;
        });
        //noinspection deprecation
        bukkitCommandManager.enableUnstableAPI("help");
    }

    private static void registerCompletions(final BukkitCommandManager bukkitCommandManager,
        final WorldManager worldManager) {
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
        bukkitCommandManager.getCommandCompletions().registerCompletion("profile_groups", context -> {
            Stream<String> groups = worldManager.getWorlds().stream().map(world -> world.getFlag(
                ProfileGroupFlag.class)).filter(s -> !s.isEmpty());
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
    }

    private static void registerAsyncCompletions(final BukkitCommandManager bukkitCommandManager,
        final WorldManager worldManager, final GlobalWorldFlagContainer globalFlagContainer) {
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
                        ret = hyperWorld.isLoaded();
                        break;
                    case "not_loaded":
                        ret = !hyperWorld.isLoaded();
                        break;
                    default:
                        break;
                }
                switch (playerSel) {
                    case "no_players":
                        ret = ret && hyperWorld.getBukkitWorld().getPlayers().isEmpty();
                        break;
                    case "has_players":
                        ret = ret && !hyperWorld.getBukkitWorld().getPlayers().isEmpty();
                        break;
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
                            && worldManager.getWorld(file.getName()) == null;
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
    }


}
