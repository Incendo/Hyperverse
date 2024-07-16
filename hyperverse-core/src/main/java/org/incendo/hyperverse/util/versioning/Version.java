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

import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Version(@NotNull String original, @NotNull VersionData versionData) {

    /**
     * Semver pattern, cg1 = major, cg2 = minor, cg3 = patch, cg4 = prerelease and cg5 = buildmetadata
     * Taken from https://semver.org/ and https://regex101.com/r/vkijKf/1/
     */
    public static final Pattern SEMVER_PATTERN = Pattern.compile(
            "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$");

    public static @NotNull Version parseMinecraft(@NotNull final String version) throws IllegalArgumentException {
        String[] split = version.split("\\.");
        if (split.length < 2) {
            throw new IllegalArgumentException("Invalid minecraft version: " + version);
        }
        if (split.length == 2) {
            StringJoiner joiner = new StringJoiner(".");
            // insert a .0 to make it correctly formatted
            joiner.add(split[0]);
            joiner.add("0");
            joiner.add(split[1]);
            Version formatted = parseSemVer(joiner.toString());
            return new Version(version, formatted.versionData());
        }
        return parseSemVer(version);
    }

    public static @NotNull Version parseSemVer(@NotNull final String version) throws IllegalArgumentException {
        Matcher matcher = SEMVER_PATTERN.matcher(version);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid version: " + version);
        }
        String majorRaw = matcher.group(1);
        String minorRaw = matcher.group(2);
        String patchRaw = matcher.group(3);
        String preReleaseRaw = matcher.group(4);
        int major;
        int minor;
        try {
            major = Integer.parseInt(majorRaw);
            minor = Integer.parseInt(minorRaw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid version: " + version);
        }
        if (patchRaw == null || patchRaw.isEmpty()) {
            return new Version(version, new VersionData(major, minor, 0, null));
        }
        int patch;
        try {
            patch = Integer.parseInt(patchRaw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid version: " + version);
        }
        if (preReleaseRaw == null || preReleaseRaw.isEmpty()) {
            return new Version(version, new VersionData(major, minor, patch, null));
        }
        PreReleaseType releaseType = PreReleaseType.parse(preReleaseRaw);
        return new Version(version, new VersionData(major, minor, patch, releaseType));
    }

    @Override
    public @NotNull String toString() {
        return this.original;
    }

}
