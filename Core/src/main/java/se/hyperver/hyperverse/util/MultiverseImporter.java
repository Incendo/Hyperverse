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

package se.hyperver.hyperverse.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.flags.implementation.DifficultyFlag;
import se.hyperver.hyperverse.flags.implementation.GamemodeFlag;
import se.hyperver.hyperverse.flags.implementation.PvpFlag;
import se.hyperver.hyperverse.flags.implementation.WorldPermissionFlag;
import se.hyperver.hyperverse.modules.HyperWorldFactory;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.WorldConfiguration;
import se.hyperver.hyperverse.world.WorldManager;

import java.util.Collection;
import java.util.UUID;

@Singleton public final class MultiverseImporter {

    private final WorldManager worldManager;
    private final HyperWorldFactory hyperWorldFactory;

    @Inject public MultiverseImporter(final WorldManager worldManager,
        @Assisted final HyperWorldFactory hyperWorldFactory) {
        this.worldManager = worldManager;
        this.hyperWorldFactory = hyperWorldFactory;
    }

    public void performImport(@NotNull final CommandSender commandSender) {
        final MultiverseCore multiverseCore = MultiverseCore.getPlugin(MultiverseCore.class);
        final MVWorldManager worldManager = multiverseCore.getMVWorldManager();
        final Collection<MultiverseWorld> worlds = worldManager.getMVWorlds();

        MessageUtil.sendMessage(commandSender, Messages.messageMultiverseInitializing,
            "%worlds%", Integer.toString(worlds.size()));

        for (final MultiverseWorld multiverseWorld : worlds) {
            HyperWorld hyperWorld = this.worldManager.getWorld(multiverseWorld.getName());
            if (hyperWorld == null) {
                MessageUtil.sendMessage(commandSender, Messages.messageMultiverseCreating,
                    "%world%", multiverseWorld.getName());
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
            hyperWorld.getConfiguration().setGenerator(multiverseWorld.getGenerator());
            hyperWorld.getConfiguration().setSeed(multiverseWorld.getSeed());
            MessageUtil.sendMessage(commandSender, Messages.messageMultiverseImporting,
                "%world%", multiverseWorld.getName());
            hyperWorld.setFlagInstance(DifficultyFlag.DIFFICULTY_FLAG_NORMAL.createFlagInstance(multiverseWorld.getDifficulty()));
            hyperWorld.setFlagInstance(WorldPermissionFlag.WORLD_PERMISSION_FLAG_DEFAULT
                .createFlagInstance(multiverseWorld.getAccessPermission().getName()));
            hyperWorld.setFlagInstance(PvpFlag.PVP_FLAG_TRUE.createFlagInstance(multiverseWorld.isPVPEnabled()));
            hyperWorld.setFlagInstance(GamemodeFlag.GAMEMODE_SURVIVAL.createFlagInstance(multiverseWorld.getGameMode()));
            hyperWorld.saveConfiguration();
            MessageUtil.sendMessage(commandSender, Messages.messageMultiverseImported,
            "%world%", multiverseWorld.getName());
        }
        MessageUtil.sendMessage(commandSender, Messages.messageMultiverseDone);
    }

}
