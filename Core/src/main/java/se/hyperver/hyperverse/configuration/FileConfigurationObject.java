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

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

/**
 * Data holder class that is instantiated in
 * {@link FileHyperConfiguration}
 */
@ConfigSerializable
public class FileConfigurationObject {

    @Setting(value = "import-automatically",
        comment = "Whether or not worlds should be automatically imported into Hyperverse on load")
    private boolean importAutomatically = true;
    @Setting(value = "persist-locations",
        comment = "Whether or not player locations should be saved to the database,"
                + " and be used when a player teleports between worlds")
    private boolean persistLocations = true;
    @Setting(value = "keep-loaded", comment = "Whether or not world spawn chunks"
        + " should be kept in memory")
    private boolean keepSpawnLoaded = true;
    @Setting(value = "grouped-inventories", comment = "Whether or player profile groups are enabled")
    private boolean groupedProfiles = false;

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

}
