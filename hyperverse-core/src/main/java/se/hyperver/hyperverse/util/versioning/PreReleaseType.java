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

package se.hyperver.hyperverse.util.versioning;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Locale;

public enum PreReleaseType {

    UNKNOWN("unknown"),

    ALPHA("alpha"),
    BETA("beta"),
    SNAPSHOT("snapshot"),
    RELEASE_CANDIDATE("rc");

    private static final PreReleaseType[] VALUES = values();

    private final String asString;

    PreReleaseType(@NotNull final String asString) {
        this.asString = asString;
    }

    @Nullable
    public static PreReleaseType parse(@NotNull final String release) {
        if (release.isEmpty()) {
            return null;
        }
        String sanitized = release.toLowerCase(Locale.ENGLISH);
        for (PreReleaseType preReleaseType : VALUES) {
            if (preReleaseType.asString.equals(sanitized)) {
                return preReleaseType;
            }
        }
        return UNKNOWN;
    }

    public @NotNull String asString() {
        return this.asString;
    }
}
