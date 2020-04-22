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

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import se.hyperver.hyperverse.world.WorldManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

/**
 * World utility methods
 */
public final class WorldUtil {

    private static final Pattern worldNamePattern = Pattern.compile("[A-Za-z\\-_0-9]{1,16}");

    private WorldUtil() {
    }

    /**
     * Check whether or not a world name is valid
     *
     * @param worldName World name
     * @return True if the world name is valid, false if not
     */
    public static boolean validateName(@NotNull final String worldName) {
        return worldNamePattern.matcher(worldName).matches();
    }

    /**
     * Check whether or not a world is suitable to be imported.
     *
     * @param worldName World name.
     * @return True of directory containing a level.dat file with the same name is found.
     */
    public static boolean isSuitableImportCandidate(String worldName, final WorldManager manager) {
        if (manager.getWorld(worldName) != null) {
            return false;
        }
        try {
            return Files.list(Bukkit.getWorldContainer().toPath()).anyMatch(path -> {
                final File file = path.toFile();
                return file.getName().equals(worldName) && file.isDirectory() && new File(file,
                    "level.dat").isFile();
            });
        } catch (IOException e) {
            return false;
        }
    }
}
