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

package com.intellectualsites.hyperverse.util;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * {@link CommandSender} which does nothing at all
 * Use {@link #getInstance()} to get the singleton instance
 */
public final class NullRouteCommandSender implements CommandSender {

    private static final NullRouteCommandSender instance = new NullRouteCommandSender();

    @NotNull public static NullRouteCommandSender getInstance() {
        return instance;
    }

    private NullRouteCommandSender() {
    }

    @Override public void sendMessage(@NotNull String message) {
    }

    @Override public void sendMessage(@NotNull String[] messages) {
    }

    @Override @NotNull public Server getServer() {
        return Bukkit.getServer();
    }

    @Override @NotNull public String getName() {
        return "null";
    }

    @Override @NotNull public Spigot spigot() {
        return new Spigot();
    }

    @Override public boolean isPermissionSet(@NotNull String name) {
        return true;
    }

    @Override public boolean isPermissionSet(@NotNull Permission perm) {
        return true;
    }

    @Override public boolean hasPermission(@NotNull String name) {
        return true;
    }

    @Override public boolean hasPermission(@NotNull Permission perm) {
        return true;
    }

    @Override @NotNull
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name,
        boolean value) {
        return new PermissionAttachment(plugin, this);
    }

    @Override @NotNull public PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return new PermissionAttachment(plugin, this);
    }

    @Override @NotNull public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name,
        boolean value, int ticks) {
        return new PermissionAttachment(plugin, this);
    }

    @Override @Nullable
    public PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return null;
    }

    @Override public void removeAttachment(@NotNull PermissionAttachment attachment) {
    }

    @Override public void recalculatePermissions() {
    }

    @Override @NotNull public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return Collections.emptySet();
    }

    @Override public boolean isOp() {
        return true;
    }

    @Override public void setOp(boolean value) {
    }

}
