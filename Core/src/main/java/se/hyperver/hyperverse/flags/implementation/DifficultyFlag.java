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

import org.bukkit.Difficulty;
import org.jetbrains.annotations.NotNull;
import se.hyperver.hyperverse.configuration.Messages;
import se.hyperver.hyperverse.flags.FlagParseException;
import se.hyperver.hyperverse.flags.WorldFlag;

import java.util.Objects;

public final class DifficultyFlag extends WorldFlag<Difficulty, DifficultyFlag> {

    public static final DifficultyFlag DIFFICULTY_FLAG_PEACEFUL = new DifficultyFlag(Difficulty.PEACEFUL);
    public static final DifficultyFlag DIFFICULTY_FLAG_EASY     = new DifficultyFlag(Difficulty.EASY);
    public static final DifficultyFlag DIFFICULTY_FLAG_NORMAL   = new DifficultyFlag(Difficulty.NORMAL);
    public static final DifficultyFlag DIFFICULTY_FLAG_HARD     = new DifficultyFlag(Difficulty.HARD);

    private DifficultyFlag(@NotNull final Difficulty difficulty) {
        super(difficulty, Messages.flagDescriptionDifficulty);
    }

    @Override public DifficultyFlag parse(@NotNull final String input) throws FlagParseException {
        switch (input.toLowerCase()) {
            case "peaceful":
                return flagOf(Difficulty.PEACEFUL);
            case "easy":
                return flagOf(Difficulty.EASY);
            case "normal":
                return flagOf(Difficulty.NORMAL);
            case "hard":
                return flagOf(Difficulty.HARD);
            default:
                throw new FlagParseException(this, input,
                    "Invalid difficulty. Available values are: peaceful, easy, normal and hard");
        }
    }

    @Override public DifficultyFlag merge(@NotNull final Difficulty newValue) {
        return flagOf(Objects.requireNonNull(Difficulty.getByValue(Math.max(this.getValue().getValue(),
            newValue.getValue()))));
    }

    @Override public String toString() {
        return this.getValue().name().toLowerCase();
    }

    @Override public String getExample() {
        return "peaceful";
    }

    @Override protected DifficultyFlag flagOf(@NotNull Difficulty value) {
        switch (value) {
            case PEACEFUL:
                return DIFFICULTY_FLAG_PEACEFUL;
            case EASY:
                return DIFFICULTY_FLAG_EASY;
            case HARD:
                return DIFFICULTY_FLAG_HARD;
            default:
                return DIFFICULTY_FLAG_NORMAL;
        }
    }

}
