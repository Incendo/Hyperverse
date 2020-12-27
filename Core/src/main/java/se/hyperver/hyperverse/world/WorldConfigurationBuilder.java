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

import org.checkerframework.checker.nullness.qual.NonNull;
import se.hyperver.hyperverse.util.SeedUtil;

/**
 * Builder for {@link WorldConfiguration}
 */
public class WorldConfigurationBuilder {

    private String name;
    private WorldType type = WorldType.OVER_WORLD;
    private WorldFeatures worldFeatures = WorldFeatures.NORMAL;
    private String settings = "";
    private long seed = SeedUtil.randomSeed();
    private WorldStructureSetting generateStructures = WorldStructureSetting.GENERATE_STRUCTURES;
    private String generator = "";
    private String generatorArg = "";

    public final @NonNull WorldConfigurationBuilder setName(final @NonNull String name) {
        this.name = name;
        return this;
    }

    public final @NonNull WorldConfigurationBuilder setType(final @NonNull WorldType type) {
        this.type = type;
        return this;
    }

    public final @NonNull WorldConfigurationBuilder setSettings(final @NonNull String settings) {
        this.settings = settings;
        return this;
    }

    public final @NonNull WorldConfigurationBuilder setSeed(final long seed) {
        this.seed = seed;
        return this;
    }

    public @NonNull WorldConfigurationBuilder setGenerateStructures(final @NonNull WorldStructureSetting generateStructures) {
        this.generateStructures = generateStructures;
        return this;
    }

    public @NonNull WorldConfigurationBuilder setGenerator(final @NonNull String generator) {
        this.generator = generator;
        return this;
    }

    public @NonNull WorldConfigurationBuilder setGeneratorArg(final @NonNull String generatorArg) {
        this.generatorArg = generatorArg;
        return this;
    }

    public @NonNull WorldConfigurationBuilder setWorldFeatures(final @NonNull WorldFeatures worldFeatures) {
        this.worldFeatures = worldFeatures;
        return this;
    }

    public @NonNull WorldConfiguration createWorldConfiguration() {
        return new WorldConfiguration(name, type, worldFeatures, settings, seed,
                generateStructures == WorldStructureSetting.GENERATE_STRUCTURES,
                generator, generatorArg
        );
    }

}
