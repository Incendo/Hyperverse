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

package se.hyperver.hyperverse.flags.implementation;

import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.flags.FlagParseException;
import se.hyperver.hyperverse.flags.WorldFlag;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

public class GamemodeFlag extends WorldFlag<GameMode, GamemodeFlag> {

    public static final GamemodeFlag GAMEMODE_SURVIVAL  = new GamemodeFlag(GameMode.SURVIVAL);
    public static final GamemodeFlag GAMEMODE_CREATIVE  = new GamemodeFlag(GameMode.CREATIVE);
    public static final GamemodeFlag GAMEMODE_ADVENTURE = new GamemodeFlag(GameMode.ADVENTURE);
    public static final GamemodeFlag GAMEMODE_SPECTATOR = new GamemodeFlag(GameMode.SPECTATOR);

    private GamemodeFlag(@NotNull final GameMode value) {
        super(value, Messages.flagDescriptionGamemode);
    }

    @Override public GamemodeFlag parse(@NotNull final String input) throws FlagParseException {
        switch (input.toLowerCase()) {
            case "survival":
            case "0":
            case "s":
                return GAMEMODE_SURVIVAL;
            case "creative":
            case "1":
            case "c":
                return GAMEMODE_CREATIVE;
            case "adventure":
            case "2":
            case "a":
                return GAMEMODE_ADVENTURE;
            case "spectator":
            case "3":
                return GAMEMODE_SPECTATOR;
            default:
                throw new FlagParseException(this, input, "There is no such game mode");
        }
    }

    @Override public GamemodeFlag merge(@NotNull final GameMode newValue) {
        return flagOf(newValue);
    }

    @Override public String toString() {
        return this.getValue().name();
    }

    @Override public String getExample() {
        return "survival";
    }

    @Override protected GamemodeFlag flagOf(@NotNull final GameMode value) {
        switch (value) {
            case SURVIVAL:
                return GAMEMODE_SURVIVAL;
            case CREATIVE:
                return GAMEMODE_CREATIVE;
            case ADVENTURE:
                return GAMEMODE_ADVENTURE;
            case SPECTATOR:
                return GAMEMODE_SPECTATOR;
            default:
                throw new IllegalArgumentException("Unknown gamemode: " + value.name());
        }
    }

    @Override public Collection<String> getTabCompletions() {
        return Arrays.asList("survival", "creative", "adventure", "spectator");
    }

}
