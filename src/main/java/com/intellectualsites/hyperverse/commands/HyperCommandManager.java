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
import co.aikar.commands.annotation.Values;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@CommandAlias("worlds|world|hyperverse|hv")
@CommandPermission("hyperverse.worlds")
public class HyperCommandManager extends BaseCommand {

    private final WorldManager worldManager;
    private final BukkitCommandManager bukkitCommandManager;
    private final HyperWorldFactory hyperWorldFactory;

    @Inject public HyperCommandManager(final Hyperverse hyperverse, final WorldManager worldManager,
        final HyperWorldFactory hyperWorldFactory) {
        this.worldManager = Objects.requireNonNull(worldManager);
        this.hyperWorldFactory = Objects.requireNonNull(hyperWorldFactory);
        // Create the command manager
        bukkitCommandManager = new BukkitCommandManager(hyperverse);
        bukkitCommandManager.registerCommand(this);
        bukkitCommandManager.getCommandCompletions().registerAsyncCompletion("hyperworlds",
            context -> worldManager.getWorlds().stream().map(HyperWorld::getConfiguration)
                    .map(WorldConfiguration::getName).collect(Collectors.toList()));
        bukkitCommandManager.getCommandCompletions().registerAsyncCompletion("generators",
            context -> {
                final List<String> generators = new ArrayList<>();
                generators.add("vanilla");
                for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    generators.add(plugin.getName().toLowerCase());
                }
                return generators;
            });
        bukkitCommandManager.getCommandContexts().registerContext(WorldType.class, context -> {
            final String arg = context.getFirstArg();
            return WorldType.fromString(arg).orElse(null);
        });
    }

    @HelpCommand
    public void doHelp(final CommandSender sender, final CommandHelp commandHelp) {
        commandHelp.showHelp();
    }

    @Subcommand("create")
    @Syntax("<world> [generator: plugin name, vanilla] [type: overworld, nether, end] [seed]"
        + " [generate-structures: true, false] [settings...]")
    @CommandPermission("hyperverse.create")
    @Description("Create a new world")
    public void createWorld(final CommandSender sender, final String world,
        @Values("@generators") @Default("vanilla") final String generator,
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
        // Check if the generator is actually valid
        final WorldConfiguration worldConfiguration = WorldConfiguration.builder()
            .name(world).generator(generator).type(type).seed(seed)
            .generateStructures(generateStructures).settings(settings).build();
        final HyperWorld hyperWorld = hyperWorldFactory.create(UUID.randomUUID(), worldConfiguration);
        MessageUtil.sendMessage(sender, Messages.messageWorldCreationStarted);
        MessageUtil.sendMessage(sender, Messages.messageWorldProperty, "%key%", "name", "%value%", world);
        MessageUtil.sendMessage(sender, Messages.messageWorldProperty, "%key%", "type", "%value%", type.name());
        MessageUtil.sendMessage(sender, Messages.messageWorldProperty, "%key%", "seed", "%value%", Long.toString(seed));
        MessageUtil.sendMessage(sender, Messages.messageWorldProperty, "%key%", "structures", "%value%", Boolean.toString(generateStructures));
        MessageUtil.sendMessage(sender, Messages.messageWorldProperty, "%key%", "settings", "%value%", settings);
        try {
            hyperWorld.createBukkitWorld();
            MessageUtil.sendMessage(sender, Messages.messageWorldCreationFinished);
            if (sender instanceof Player) {
                // Attempt to teleport them to the world
                hyperWorld.teleportPlayer((Player) sender);
            }
            // Register the world
            this.worldManager.addWorld(hyperWorld);
        } catch (final HyperWorldValidationException validationException) {
            switch (validationException.getValidationResult()) {
                case UNKNOWN_GENERATOR:
                    MessageUtil.sendMessage(sender, Messages.messageGeneratorInvalid);
                    break;
                case SUCCESS:
                    break;
                default:
                    MessageUtil.sendMessage(sender, Messages.messageCreationUnknownFailure);
                    break;
            }
        }
    }

    @Subcommand("list|l|worlds")
    @CommandPermission("hyperverse.list")
    @Description("List hyperverse worlds")
    public void doList(final CommandSender sender) {
    }

    @Subcommand("teleport|tp")
    @CommandAlias("hvtp")
    @CommandPermission("hyperverse.teleport")
    @CommandCompletion("@hyperworlds")
    @Description("Teleport between hyperverse worlds")
    public void doTeleport(final CommandSender sender, final HyperWorld world) {
    }

}
