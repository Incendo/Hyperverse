//
// Hyperverse - A minecraft world management plugin
// Copyright © 2020 Alexander Söderberg (sauilitired@gmail.com)
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

package com.intellectualsites.hyperverse.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.google.inject.Inject;
import com.intellectualsites.hyperverse.Hyperverse;
import com.intellectualsites.hyperverse.configuration.Messages;
import com.intellectualsites.hyperverse.exception.HyperWorldValidationException;
import com.intellectualsites.hyperverse.modules.HyperWorldFactory;
import com.intellectualsites.hyperverse.util.MessageUtil;
import com.intellectualsites.hyperverse.util.WorldUtil;
import com.intellectualsites.hyperverse.world.HyperWorld;
import com.intellectualsites.hyperverse.world.WorldConfiguration;
import com.intellectualsites.hyperverse.world.WorldManager;
import com.intellectualsites.hyperverse.world.WorldType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@CommandAlias("worlds|world|hyperverse|hv")
@CommandPermission("hyperverse.worlds")
@SuppressWarnings("unused")
public class HyperCommandManager extends BaseCommand {

    private final WorldManager worldManager;
    private final HyperWorldFactory hyperWorldFactory;

    @Inject public HyperCommandManager(final Hyperverse hyperverse, final WorldManager worldManager,
        final HyperWorldFactory hyperWorldFactory) {
        this.worldManager = Objects.requireNonNull(worldManager);
        this.hyperWorldFactory = Objects.requireNonNull(hyperWorldFactory);
        // Create the command manager
        final BukkitCommandManager bukkitCommandManager = new BukkitCommandManager(hyperverse);
        bukkitCommandManager.getCommandCompletions().registerAsyncCompletion("hyperworlds",
            context -> worldManager.getWorlds().stream().map(HyperWorld::getConfiguration)
                .map(WorldConfiguration::getName).collect(Collectors.toList()));
        bukkitCommandManager.getCommandCompletions().registerCompletion("worldtypes", context -> {
            if (context.getInput().contains(" ")) {
                return Collections.emptyList();
            }
            return Arrays.stream(WorldType.values()).map(WorldType::name).map(String::toLowerCase)
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
        bukkitCommandManager.getCommandContexts().registerContext(WorldType.class, context -> {
            final String arg = context.popFirstArg();
            return WorldType.fromString(arg).orElse(null);
        });
        bukkitCommandManager.getCommandContexts().registerContext(HyperWorld.class, context -> {
            final HyperWorld hyperWorld = worldManager.getWorld(context.popFirstArg());
            if (hyperWorld == null) {
                MessageUtil.sendMessage(context.getSender(), Messages.messageNoSuchWorld);
            }
            return hyperWorld;
        });
        //noinspection deprecation
        bukkitCommandManager.enableUnstableAPI("help");
        bukkitCommandManager.registerCommand(this);
    }

    @HelpCommand public void doHelp(final CommandSender sender, final CommandHelp commandHelp) {
        commandHelp.showHelp();
    }

    @Subcommand("create") @Syntax(
        "<world> [generator: plugin name, vanilla][:[args]] [type: overworld, nether, end] [seed]"
            + " [generate-structures: true, false] [settings...]")
    @CommandPermission("hyperverse.create") @Description("Create a new world")
    @CommandCompletion(" @generators @worldtypes @null")
    public void createWorld(final CommandSender sender, final String world, String generator,
        @Default("overworld") final WorldType type, @Default("0") final long seed,
        @Default("true") final boolean generateStructures, @Default final String settings) {
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
                .setGenerateStructures(generateStructures).setSettings(settings)
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
            if (this.worldManager.addWorld(hyperWorld)) {
                MessageUtil.sendMessage(sender, Messages.messageWorldCreationFinished);
                if (sender instanceof Player) {
                    // Attempt to teleport them to the world
                    hyperWorld.teleportPlayer((Player) sender);
                }
            } else {
                MessageUtil.sendMessage(sender, Messages.messageCreationUnknownFailure);
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

    @Subcommand("list|l|worlds") @CommandPermission("hyperverse.list") @CommandAlias("hvl")
    @Description("List hyperverse worlds") public void doList(final CommandSender sender) {
        MessageUtil.sendMessage(sender, Messages.messageListHeader);
        for (final HyperWorld hyperWorld : this.worldManager.getWorlds()) {
            final WorldConfiguration configuration = hyperWorld.getConfiguration();

            // Format the generator name a little better
            String generator = configuration.getGenerator();
            if (generator.isEmpty()) {
                generator = "vanilla";
            } else {
                generator = generator.toLowerCase();
            }

            MessageUtil.sendMessage(sender, Messages.messageListEntry, "%name%", configuration.getName(),
                "%generator%", generator, "%type%", configuration.getType().name());
        }
    }

    @Subcommand("teleport|tp") @CommandAlias("hvtp") @CommandPermission("hyperverse.teleport")
    @CommandCompletion("@hyperworlds") @Description("Teleport between hyperverse worlds")
    public void doTeleport(final Player player, final HyperWorld world) {
        if (world == null) {
            return;
        }
        MessageUtil.sendMessage(player, Messages.messageTeleporting, "%world%",
            world.getConfiguration().getName());
        world.teleportPlayer(player);
    }

    @Subcommand("info|i") @CommandAlias("hvi") @CommandPermission("hyperverse.info")
    @CommandCompletion("@hyperworlds") @Description("View world info")
    public void doInfo(final CommandSender sender, final HyperWorld world) {
        if (world == null) {
            return;
        }
        MessageUtil.sendMessage(sender, Messages.messageInfoHeader);
        world.sendWorldInfo(sender);
    }

}
