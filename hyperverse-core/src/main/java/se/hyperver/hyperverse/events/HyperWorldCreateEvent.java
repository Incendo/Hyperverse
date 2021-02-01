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

package se.hyperver.hyperverse.events;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.world.HyperWorld;

/**
 * Called when a new {@link se.hyperver.hyperverse.world.HyperWorld} has been created
 * {@inheritDoc}
 */
public final class HyperWorldCreateEvent extends HyperWorldEvent {

    private static final HandlerList handlers = new HandlerList();

    private HyperWorldCreateEvent(final @NonNull HyperWorld world) {
        super(world);
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static void callFor(final @NonNull HyperWorld world) {
        final HyperWorldCreateEvent hyperWorldCreateEvent = new HyperWorldCreateEvent(world);
        Bukkit.getServer().getPluginManager().callEvent(hyperWorldCreateEvent);
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return handlers;
    }

}
