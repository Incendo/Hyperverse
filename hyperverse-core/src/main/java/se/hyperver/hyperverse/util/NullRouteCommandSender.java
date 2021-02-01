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

package se.hyperver.hyperverse.util;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * {@link CommandSender} which does nothing at all
 * Use {@link #getInstance()} to get the singleton instance
 */
public final class NullRouteCommandSender implements CommandSender {

    private static final NullRouteCommandSender INSTANCE = new NullRouteCommandSender();

    private NullRouteCommandSender() {
    }

    @NonNull
    public static NullRouteCommandSender getInstance() {
        return INSTANCE;
    }

    @Override
    public void sendMessage(final @NonNull String message) {
    }

    @Override
    public void sendMessage(final @NonNull String[] messages) {
    }

    @Override
    public void sendMessage(final @Nullable UUID sender, final @NonNull String message) {
    }

    @Override
    public void sendMessage(final @Nullable UUID sender, final @NonNull String[] messages) {
    }

    @Override
    @NonNull
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    @NonNull
    public String getName() {
        return "null";
    }

    @Override
    @NonNull
    public Spigot spigot() {
        return new Spigot();
    }

    @Override
    public boolean isPermissionSet(@NonNull final String name) {
        return true;
    }

    @Override
    public boolean isPermissionSet(@NonNull final Permission perm) {
        return true;
    }

    @Override
    public boolean hasPermission(@NonNull final String name) {
        return true;
    }

    @Override
    public boolean hasPermission(@NonNull final Permission perm) {
        return true;
    }

    @Override
    public @NonNull PermissionAttachment addAttachment(
            final @NonNull Plugin plugin,
            final @NonNull String name,
            final boolean value
    ) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public @NonNull PermissionAttachment addAttachment(final @NonNull Plugin plugin) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public @NonNull PermissionAttachment addAttachment(
            final @NonNull Plugin plugin,
            final @NonNull String name,
            final boolean value,
            final int ticks
    ) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(
            final @NonNull Plugin plugin,
            final int ticks
    ) {
        return null;
    }

    @Override
    public void removeAttachment(final @NonNull PermissionAttachment attachment) {
    }

    @Override
    public void recalculatePermissions() {
    }

    @Override
    public @NonNull Set<@NonNull PermissionAttachmentInfo> getEffectivePermissions() {
        return Collections.emptySet();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(final boolean value) {
    }

}
