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

package org.incendo.hyperverse.platform;

import org.incendo.hyperverse.util.NMS;
import org.incendo.hyperverse.util.versioning.Version;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public final class ReflectionPlatformProvider implements PlatformProvider {

    private final Version minecraftVersion;
    public ReflectionPlatformProvider(@NotNull final Version version) {
        this.minecraftVersion = version;
    }

    @Override
    public @NotNull Class<? extends NMS> providePlatform() throws PlatformProvisionException {
        String expectedPackage = this.minecraftVersion.original().toLowerCase(Locale.ENGLISH).replace('.', '_');
        String packageName = "org.incendo.hyperverse.platform.v" + expectedPackage;
        try {
            Class<?> clazz = Class.forName(packageName + ".NMSImpl");
            return clazz.asSubclass(NMS.class);
        } catch (ReflectiveOperationException ex) {
            throw new PlatformProvisionException("Could not provide platform for version: " + this.minecraftVersion + "!", ex);
        }
    }

}
