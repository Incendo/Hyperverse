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

import se.hyperver.hyperverse.util.SeedUtil;

/**
 * Builder for {@link WorldConfiguration}
 */
public class WorldConfigurationBuilder {

    private String name;
    private WorldType type = WorldType.OVER_WORLD;
    private String settings = "";
    private long seed = SeedUtil.randomSeed();
    private boolean generateStructures = true;
    private String generator = "";
    private String generatorArg = "";

    public WorldConfigurationBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public WorldConfigurationBuilder setType(WorldType type) {
        this.type = type;
        return this;
    }

    public WorldConfigurationBuilder setSettings(String settings) {
        this.settings = settings;
        return this;
    }

    public WorldConfigurationBuilder setSeed(long seed) {
        this.seed = seed;
        return this;
    }

    public WorldConfigurationBuilder setGenerateStructures(boolean generateStructures) {
        this.generateStructures = generateStructures;
        return this;
    }

    public WorldConfigurationBuilder setGenerator(String generator) {
        this.generator = generator;
        return this;
    }

    public WorldConfigurationBuilder setGeneratorArg(String generatorArg) {
        this.generatorArg = generatorArg;
        return this;
    }

    public WorldConfiguration createWorldConfiguration() {
        return new WorldConfiguration(name, type, settings, seed, generateStructures, generator, generatorArg);
    }

}
