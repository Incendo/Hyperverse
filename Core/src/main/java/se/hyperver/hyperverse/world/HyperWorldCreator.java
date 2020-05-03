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

package se.hyperver.hyperverse.world;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import se.hyperver.hyperverse.util.NullRouteCommandSender;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * World creator instance used to create a {@link HyperWorld}
 */
public final class HyperWorldCreator extends WorldCreator {

    private final HyperWorld hyperWorld;

    @Inject public HyperWorldCreator(@Assisted final HyperWorld hyperWorld) {
        super(Objects.requireNonNull(hyperWorld).getConfiguration().getName());
        this.hyperWorld = hyperWorld;
    }

    /**
     * Validate the world configuration
     *
     * @return Result of the validation
     */
    @NotNull public ValidationResult validate() {
        final WorldConfiguration worldConfiguration = this.hyperWorld.getConfiguration();
        if (!worldConfiguration.getGenerator().isEmpty() &&
            !worldConfiguration.getGenerator().equalsIgnoreCase("vanilla")) {
            final ChunkGenerator chunkGenerator =
                getGeneratorForName(worldConfiguration.getName(), getJoinedName(),
                    NullRouteCommandSender.getInstance());
            if (chunkGenerator == null) {
                return ValidationResult.UNKNOWN_GENERATOR;
            }
        }
        return ValidationResult.SUCCESS;
    }

    /**
     * Configure the world creator
     */
    public void configure() {
        final WorldConfiguration worldConfiguration = this.hyperWorld.getConfiguration();
        if (worldConfiguration.getWorldFeatures() != null) {
            this.type(worldConfiguration.getWorldFeatures().getBukkitType());
        } else {
            this.type(WorldType.NORMAL);
        }
        this.environment(worldConfiguration.getType().getBukkitType());
        this.generatorSettings(worldConfiguration.getSettings());
        this.seed(worldConfiguration.getSeed());
        this.generateStructures(worldConfiguration.isGenerateStructures());
        this.generator(getJoinedName(), NullRouteCommandSender.getInstance());
    }

    private String getJoinedName() {
        if (this.hyperWorld.getConfiguration().getGeneratorArg().isEmpty()) {
            return this.hyperWorld.getConfiguration().getGenerator();
        }
        return String.format("%s:%s", this.hyperWorld.getConfiguration().getGenerator(),
            this.hyperWorld.getConfiguration().getGeneratorArg());
    }

    /**
     * Result of configuration validation
     */
    public enum ValidationResult {
        SUCCESS, UNKNOWN_GENERATOR, NAME_TAKEN, UNKNOWN_ERROR
    }

}
