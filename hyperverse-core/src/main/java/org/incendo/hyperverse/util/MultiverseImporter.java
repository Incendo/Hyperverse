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

package org.incendo.hyperverse.util;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.flags.implementation.AliasFlag;
import org.incendo.hyperverse.flags.implementation.DifficultyFlag;
import org.incendo.hyperverse.flags.implementation.GamemodeFlag;
import org.incendo.hyperverse.flags.implementation.PvpFlag;
import org.incendo.hyperverse.flags.implementation.RespawnWorldFlag;
import org.incendo.hyperverse.flags.implementation.WorldPermissionFlag;
import org.incendo.hyperverse.modules.HyperWorldFactory;
import org.incendo.hyperverse.modules.WorldConfigurationFactory;
import org.incendo.hyperverse.world.HyperWorld;
import org.incendo.hyperverse.world.WorldConfiguration;
import org.incendo.hyperverse.world.WorldManager;

import java.util.Collection;
import java.util.UUID;

@SuppressWarnings("GuiceAssistedInjectScoping")
public final class MultiverseImporter {

    private final WorldManager worldManager;
    private final HyperWorldFactory hyperWorldFactory;
    private final WorldConfigurationFactory worldConfigurationFactory;

    @Inject
    public MultiverseImporter(
            final @NonNull WorldManager worldManager,
            final @NonNull WorldConfigurationFactory worldConfigurationFactory,
            @Assisted final @NonNull HyperWorldFactory hyperWorldFactory
    ) {
        this.worldManager = worldManager;
        this.worldConfigurationFactory = worldConfigurationFactory;
        this.hyperWorldFactory = hyperWorldFactory;
    }

    public void performImport(
            final @NonNull CommandSender commandSender
    ) {
        final MultiverseCore multiverseCore = MultiverseCore.getPlugin(MultiverseCore.class);
        final MVWorldManager worldManager = multiverseCore.getMVWorldManager();
        final Collection<MultiverseWorld> worlds = worldManager.getMVWorlds();

        MessageUtil.sendMessage(commandSender, Messages.messageImportPluginInitializing,
                "%worlds%", Integer.toString(worlds.size()), "%plugin%", "Multiverse"
        );

        for (final MultiverseWorld multiverseWorld : worlds) {
            HyperWorld hyperWorld = this.worldManager.getWorld(multiverseWorld.getName());
            if (hyperWorld == null) {
                MessageUtil.sendMessage(commandSender, Messages.messageImportPluginCreating,
                        "%world%", multiverseWorld.getName(), "%plugin%", "Multiverse"
                );
                final WorldConfiguration worldConfiguration = this.worldConfigurationFactory.fromWorld(multiverseWorld.getCBWorld());
                final UUID uuid;
                if (multiverseWorld.getCBWorld() != null) {
                    uuid = multiverseWorld.getCBWorld().getUID();
                } else {
                    uuid = UUID.randomUUID();
                }
                hyperWorld = this.hyperWorldFactory.create(uuid, worldConfiguration);
                this.worldManager.addWorld(hyperWorld);
            }
            hyperWorld.setBukkitWorld(multiverseWorld.getCBWorld());
            String generator = multiverseWorld.getGenerator();
            if (generator == null || generator.equalsIgnoreCase("null")) {
                generator = "vanilla";
            }
            hyperWorld.getConfiguration().setGenerator(generator);
            hyperWorld.getConfiguration().setSeed(multiverseWorld.getSeed());
            MessageUtil.sendMessage(commandSender, Messages.messageImportingExternalWorld,
                    "%world%", multiverseWorld.getName(), "%plugin%", "Multiverse"
            );
            hyperWorld.setFlagInstance(DifficultyFlag.DIFFICULTY_FLAG_NORMAL.createFlagInstance(multiverseWorld.getDifficulty()));
            hyperWorld.setFlagInstance(WorldPermissionFlag.WORLD_PERMISSION_FLAG_DEFAULT
                    .createFlagInstance(multiverseWorld.getAccessPermission().getName()));
            hyperWorld.setFlagInstance(PvpFlag.PVP_FLAG_TRUE.createFlagInstance(multiverseWorld.isPVPEnabled()));
            hyperWorld.setFlagInstance(GamemodeFlag.GAMEMODE_SURVIVAL.createFlagInstance(multiverseWorld.getGameMode()));
            final String worldAlias = multiverseWorld.getAlias();
            if (worldAlias != null && !worldAlias.isEmpty()) {
                hyperWorld.setFlagInstance(AliasFlag.ALIAS_NONE.createFlagInstance(worldAlias));
            }
            final World respawnToWorld = multiverseWorld.getRespawnToWorld();
            if (respawnToWorld != null && !respawnToWorld.equals(Bukkit.getWorlds().get(0))) {
                hyperWorld.setFlagInstance(RespawnWorldFlag.RESPAWN_WORLD_FLAG_EMPTY.createFlagInstance(respawnToWorld.getName()));
            }
            hyperWorld.saveConfiguration();
            MessageUtil.sendMessage(commandSender, Messages.messageExternalImportCompleted,
                    "%world%", multiverseWorld.getName(), "%plugin%", "Multiverse"
            );
        }
        MessageUtil.sendMessage(commandSender, Messages.messageImportDone, "%plugin%", "Multiverse");
    }

}
