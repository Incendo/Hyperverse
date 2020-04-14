//
// Hyperverse - A minecraft world management plugin
// Copyright © 2020 Alexander Söderberg (sauilitired@gmail.com)
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

package com.intellectualsites.hyperverse.commands;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import co.aikar.commands.InvalidCommandArgument;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class GeneratorCompleter implements CommandCompletions.AsyncCommandCompletionHandler<BukkitCommandCompletionContext> {

    @Override public Collection<String> getCompletions(final BukkitCommandCompletionContext context)
        throws InvalidCommandArgument {
        final List<String> generators = new ArrayList<>();
        generators.add("vanilla");
        for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            generators.add(plugin.getName().toLowerCase());
        }
        return generators;
    }

}
