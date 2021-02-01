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

package se.hyperver.hyperverse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.flags.implementation.AliasFlag;
import se.hyperver.hyperverse.flags.implementation.DifficultyFlag;
import se.hyperver.hyperverse.flags.implementation.GamemodeFlag;
import se.hyperver.hyperverse.flags.implementation.PvpFlag;
import se.hyperver.hyperverse.flags.implementation.RespawnWorldFlag;
import se.hyperver.hyperverse.flags.implementation.WorldPermissionFlag;
import se.hyperver.hyperverse.modules.HyperWorldFactory;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.WorldConfiguration;
import se.hyperver.hyperverse.world.WorldManager;

import java.util.Collection;
import java.util.UUID;

@Singleton
@SuppressWarnings("GuiceAssistedInjectScoping")
public final class MultiverseImporter {

    private final WorldManager worldManager;
    private final HyperWorldFactory hyperWorldFactory;

    @Inject
    public MultiverseImporter(
            final @NonNull WorldManager worldManager,
            @Assisted final @NonNull HyperWorldFactory hyperWorldFactory
    ) {
        this.worldManager = worldManager;
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
                final WorldConfiguration worldConfiguration = WorldConfiguration.fromWorld(multiverseWorld.getCBWorld());
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
