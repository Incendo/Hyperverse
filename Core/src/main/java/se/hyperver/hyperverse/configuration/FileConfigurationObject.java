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

package se.hyperver.hyperverse.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

/**
 * Data holder class that is instantiated in
 * {@link FileHyperConfiguration}
 */
@ConfigSerializable
public class FileConfigurationObject {

    @Setting(value = "import-automatically")
    @Comment(value = "Whether or not worlds should be automatically imported into Hyperverse on load")
    private boolean importAutomatically = true;
    @Setting(value = "persist-locations")
    @Comment(value = "Whether or not player locations should be saved to the database,"
                + " and be used when a player teleports between worlds")
    private boolean persistLocations = true;
    @Setting(value = "keep-loaded")
    @Comment(value = "Whether or not world spawn chunks"
        + " should be kept in memory")
    private boolean keepSpawnLoaded = true;
    @Setting(value = "grouped-inventories")
    @Comment(value = "Whether or player profile groups are enabled")
    private boolean groupedProfiles = false;
    @Setting(value = "language-code")
    @Comment(value = "Language code used to resolve translations. Currently supported: en, sv, de, cn")
    private String languageCode = "en";
    @Setting(value = "safe-teleport")
    @Comment(value = "Whether or not safe teleportation should be enforced")
    private boolean safeTeleport = true;
    @Setting(value = "hook-essentials")
    @Comment(value = "Whether or not Hyperverse should attempt to utilize Essentials' specific features.")
    private boolean hookEssentials = true;
    @Setting(value = "debug")
    @Comment(value = "Whether or not Hyperverse should print verbose debugging messages")
    private boolean debug = false;

    public boolean isImportAutomatically() {
        return this.importAutomatically;
    }

    public boolean isPersistLocations() {
        return this.persistLocations;
    }

    public boolean isKeepSpawnLoaded() {
        return this.keepSpawnLoaded;
    }

    public boolean useGroupedProfiles() {
        return this.groupedProfiles;
    }

    public String getLanguageCode() {
        return this.languageCode;
    }

    public boolean shouldSafeTeleport() {
        return this.safeTeleport;
    }

    public boolean shouldHookEssentials() {
        return this.hookEssentials;
    }

    public boolean shouldPrintDebug() {
        return this.debug;
    }

}
