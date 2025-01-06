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

package org.incendo.hyperverse.flags.implementation;

import org.bukkit.GameMode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.flags.FlagParseException;
import org.incendo.hyperverse.flags.WorldFlag;

import java.util.Arrays;
import java.util.Collection;

public final class GamemodeFlag extends WorldFlag<GameMode, GamemodeFlag> {

    public static final GamemodeFlag GAMEMODE_SURVIVAL = new GamemodeFlag(GameMode.SURVIVAL);
    public static final GamemodeFlag GAMEMODE_CREATIVE = new GamemodeFlag(GameMode.CREATIVE);
    public static final GamemodeFlag GAMEMODE_ADVENTURE = new GamemodeFlag(GameMode.ADVENTURE);
    public static final GamemodeFlag GAMEMODE_SPECTATOR = new GamemodeFlag(GameMode.SPECTATOR);

    private GamemodeFlag(final @NonNull GameMode value) {
        super(value, Messages.flagDescriptionGamemode);
    }

    @Override
    public GamemodeFlag parse(final @NonNull String input) throws FlagParseException {
        return switch (input.toLowerCase()) {
            case "survival", "0", "s" -> GAMEMODE_SURVIVAL;
            case "creative", "1", "c" -> GAMEMODE_CREATIVE;
            case "adventure", "2", "a" -> GAMEMODE_ADVENTURE;
            case "spectator", "3" -> GAMEMODE_SPECTATOR;
            default -> throw new FlagParseException(this, input, "There is no such game mode");
        };
    }

    @Override
    public GamemodeFlag merge(final @NonNull GameMode newValue) {
        return this.flagOf(newValue);
    }

    @Override
    public String toString() {
        return this.getValue().name();
    }

    @Override
    public String getExample() {
        return "survival";
    }

    @Override
    protected GamemodeFlag flagOf(final @NonNull GameMode value) {
        return switch (value) {
            case SURVIVAL -> GAMEMODE_SURVIVAL;
            case CREATIVE -> GAMEMODE_CREATIVE;
            case ADVENTURE -> GAMEMODE_ADVENTURE;
            case SPECTATOR -> GAMEMODE_SPECTATOR;
            default -> throw new IllegalArgumentException("Unknown gamemode: " + value.name());
        };
    }

    @Override
    public Collection<String> getTabCompletions() {
        return Arrays.asList("survival", "creative", "adventure", "spectator");
    }

}
