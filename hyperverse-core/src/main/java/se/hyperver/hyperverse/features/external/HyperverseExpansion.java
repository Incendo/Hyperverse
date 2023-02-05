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

package se.hyperver.hyperverse.features.external;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import se.hyperver.hyperverse.Hyperverse;
import se.hyperver.hyperverse.flags.implementation.DifficultyFlag;
import se.hyperver.hyperverse.flags.implementation.PveFlag;
import se.hyperver.hyperverse.flags.implementation.PvpFlag;
import se.hyperver.hyperverse.world.HyperWorld;
import se.hyperver.hyperverse.world.WorldConfiguration;
import se.hyperver.hyperverse.world.WorldManager;

/**
 * PlaceholderAPI expansion
 */
final class HyperverseExpansion extends PlaceholderExpansion {

    private final Hyperverse plugin;

    private final String author;
    private final String version;

    public HyperverseExpansion() {
        this.plugin = JavaPlugin.getPlugin(Hyperverse.class);
        final PluginDescriptionFile descriptionFile = this.plugin.getDescription();
        this.author = descriptionFile.getAuthors().toString();
        this.version = descriptionFile.getVersion();
    }

    @Override
    public @NonNull String getIdentifier() {
        return "hyperverse";
    }

    @Override
    public @NonNull String getAuthor() {
        return this.author;
    }

    @Override
    public @NonNull String getVersion() {
        return this.version;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(
            final @Nullable Player player,
            final @NonNull String identifier
    ) {
        if (player == null) {
            return "";
        }

        final WorldManager worldManager = this.plugin.getWorldManager();

        final HyperWorld hyperWorld = worldManager.getWorld(player.getWorld());
        if (hyperWorld == null) {
            return "";
        }

        final WorldConfiguration worldConfiguration = hyperWorld.getConfiguration();

        switch (identifier.toLowerCase()) {
            case "world_display_name":
                return MiniMessage.miniMessage().escapeTags(hyperWorld.getDisplayName());
            case "world_name":
                return worldConfiguration.getName();
            case "world_generator":
                return worldConfiguration.getGenerator();
            case "world_difficulty":
                return hyperWorld.getFlag(DifficultyFlag.class).name();
            case "world_pvp":
                return hyperWorld.getFlag(PvpFlag.class).toString();
            case "world_pve":
                return hyperWorld.getFlag(PveFlag.class).toString();
            default:
                return null;
        }
    }

}
