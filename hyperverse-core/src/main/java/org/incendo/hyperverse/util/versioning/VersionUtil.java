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

package org.incendo.hyperverse.util.versioning;

import org.jetbrains.annotations.NotNull;

public final class VersionUtil {

    private VersionUtil() {
        throw new IllegalStateException("Cannot instantiate static utility class");
    }

    public static @NotNull Version parseMinecraftVersion(@NotNull final String minecraftVersion) throws IllegalArgumentException {
        // Expecting 1.X.X-R0.1-SNAPSHOT
        int stripLength = "-R0.1-SNAPSHOT".length();
        int length = minecraftVersion.length();
        if (length <= stripLength) {
            throw new IllegalArgumentException("Invalid minecraft version: " + minecraftVersion);
        }
        String strippedVersion = minecraftVersion.substring(0, length - stripLength);
        try {
            return Version.parse(strippedVersion);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid minecraft version: " + minecraftVersion, ex);
        }
    }

}
