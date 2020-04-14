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

package com.intellectualsites.hyperverse.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.intellectualsites.hyperverse.Hyperverse;
import com.intellectualsites.hyperverse.configuration.HyperConfiguration;
import com.intellectualsites.hyperverse.configuration.PluginFileHyperConfiguration;
import com.intellectualsites.hyperverse.world.HyperWorld;
import com.intellectualsites.hyperverse.world.HyperWorldCreator;
import com.intellectualsites.hyperverse.world.SimpleWorld;
import com.intellectualsites.hyperverse.world.SimpleWorldManager;
import com.intellectualsites.hyperverse.world.WorldManager;
import org.bukkit.WorldCreator;

public class HyperverseModule extends AbstractModule {

    @Override protected void configure() {
        bind(Hyperverse.class).toInstance(Hyperverse.getPlugin(Hyperverse.class));
        bind(HyperConfiguration.class).to(PluginFileHyperConfiguration.class).in(Singleton.class);
        bind(WorldManager.class).to(SimpleWorldManager.class).in(Singleton.class);
        install(new FactoryModuleBuilder().implement(WorldCreator.class, HyperWorldCreator.class)
            .build(HyperWorldCreatorFactory.class));
        install(new FactoryModuleBuilder().implement(HyperWorld.class, SimpleWorld.class)
            .build(HyperWorldFactory.class));
    }

}
