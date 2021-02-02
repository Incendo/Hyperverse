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

    /**
     * Set the world name
     *
     * @param name New world name
     * @return {@code this}
     */
    public final @NonNull WorldConfigurationBuilder setName(final @NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the world type
     *
     * @param type New world type
     * @return {@code this}
     */
    public final @NonNull WorldConfigurationBuilder setType(final @NonNull WorldType type) {
        this.type = type;
        return this;
    }

    /**
     * Set the world settings
     *
     * @param settings New world settings
     * @return {@code this}
     */
    public final @NonNull WorldConfigurationBuilder setSettings(final @NonNull String settings) {
        this.settings = settings;
        return this;
    }

    /**
     * Set the world seed
     *
     * @param seed New seed
     * @return {@code this}
     */
    public final @NonNull WorldConfigurationBuilder setSeed(final long seed) {
        this.seed = seed;
        return this;
    }

    /**
     * Set whether structures should be generated
     *
     * @param generateStructures Whether structures should be generated
     * @return {@code this}
     */
    public @NonNull WorldConfigurationBuilder setGenerateStructures(final @NonNull WorldStructureSetting generateStructures) {
        this.generateStructures = generateStructures;
        return this;
    }

    /**
     * Set the generator
     *
     * @param generator New generator
     * @return {@code this}
     */
    public @NonNull WorldConfigurationBuilder setGenerator(final @NonNull String generator) {
        this.generator = generator;
        return this;
    }

    /**
     * Set the generator arguments
     *
     * @param generatorArg New generator arguments
     * @return {@code this}
     */
    public @NonNull WorldConfigurationBuilder setGeneratorArg(final @NonNull String generatorArg) {
        this.generatorArg = generatorArg;
        return this;
    }

    /**
     * Set the {@link WorldFeatures}
     *
     * @param worldFeatures New world features
     * @return {@code this}
     */
    public @NonNull WorldConfigurationBuilder setWorldFeatures(final @NonNull WorldFeatures worldFeatures) {
        this.worldFeatures = worldFeatures;
        return this;
    }

    /**
     * Create the {@link WorldConfiguration} instance from this builder
     *
     * @return Created instance
     */
    public @NonNull WorldConfiguration createWorldConfiguration() {
        return new WorldConfiguration(this.name, this.type, this.worldFeatures, this.settings, this.seed,
                this.generateStructures == WorldStructureSetting.GENERATE_STRUCTURES,
                this.generator, this.generatorArg
        );
    }

}
