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

public record VersionData(int major, int minor, int patch, PreReleaseType preReleaseType)
        implements Comparable<VersionData> {

    public VersionData(final int major, final int minor, final int patch) {
        this(major, minor, patch, null);
    }

    public VersionData(final int major, final int minor) {
        this(major, minor, 0);
    }

    @Override
    public @NotNull String toString() {
        return String.format("%d.%d.%d", this.major, this.minor, this.patch);
    }

    public boolean isNewerThan(@NotNull final VersionData other) {
        return this.compareTo(other) > 0;
    }

    public boolean isOlderThan(@NotNull final VersionData other) {
        return this.compareTo(other) < 0;
    }


    @Override
    public int compareTo(@NotNull final VersionData o) {
        int majorComp = Integer.compare(this.major, o.major);
        if (majorComp != 0) {
            return majorComp;
        }
        int minorComp = Integer.compare(this.minor, o.minor);
        if (minorComp != 0) {
            return minorComp;
        }
        int patchComp = Integer.compare(this.patch, o.patch);
        if (patchComp != 0) {
            return patchComp;
        }
        if (this.preReleaseType == null && o.preReleaseType == null) {
            return 0;
        }
        if (this.preReleaseType == null) {
            return 1;
        }
        return this.preReleaseType.compareTo(o.preReleaseType);
    }
}
