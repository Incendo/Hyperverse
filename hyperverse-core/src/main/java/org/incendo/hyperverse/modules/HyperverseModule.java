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

package org.incendo.hyperverse.modules;

import cloud.commandframework.services.ServicePipeline;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.internal.ProviderMethodsModule;
import org.bukkit.Server;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.Hyperverse;
import org.incendo.hyperverse.configuration.FileHyperConfiguration;
import org.incendo.hyperverse.configuration.HyperConfiguration;
import org.incendo.hyperverse.database.HyperDatabase;
import org.incendo.hyperverse.database.SQLiteDatabase;
import org.incendo.hyperverse.database.SimpleLocationTransformer;
import org.incendo.hyperverse.events.SimpleHyperEventFactory;
import org.incendo.hyperverse.flags.FlagContainer;
import org.incendo.hyperverse.flags.GlobalWorldFlagContainer;
import org.incendo.hyperverse.flags.WorldFlagContainer;
import org.incendo.hyperverse.platform.PlatformProvider;
import org.incendo.hyperverse.platform.PlatformProvisionException;
import org.incendo.hyperverse.platform.unsupported.NMSImpl;
import org.incendo.hyperverse.teleportation.SimpleTeleportationManager;
import org.incendo.hyperverse.teleportation.TeleportationManager;
import org.incendo.hyperverse.util.HyperConfigShouldGroupProfiles;
import org.incendo.hyperverse.util.NMS;
import org.incendo.hyperverse.world.HyperWorld;
import org.incendo.hyperverse.world.HyperWorldCreator;
import org.incendo.hyperverse.world.SimpleWorld;
import org.incendo.hyperverse.world.SimpleWorldConfigurationFactory;
import org.incendo.hyperverse.world.SimpleWorldManager;
import org.incendo.hyperverse.world.WorldConfiguration;
import org.incendo.hyperverse.world.WorldManager;

import java.util.logging.Logger;

public final class HyperverseModule extends AbstractModule {

    private final Logger logger;
    private final Hyperverse hyperverse;
    private final ServicePipeline servicePipeline;
    private final Server server;

    private final PlatformProvider platformProvider;

    public HyperverseModule(
            final @NonNull Logger logger,
            final @NonNull ServicePipeline servicePipeline,
            final @NonNull Server server,
            final @NonNull PlatformProvider platformProvider,
            final @NonNull Hyperverse hyperverse
    ) {
        this.logger = logger;
        this.hyperverse = hyperverse;
        this.servicePipeline = servicePipeline;
        this.server = server;
        this.platformProvider = platformProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void configure() {
        // Install the bukkit module
        install(new BukkitModule(this.server));
        install(ProviderMethodsModule.forModule(this));
        // Resolve the NMS implementation
        Class<? extends  NMS> nmsAdapter;
        try {
            nmsAdapter = this.platformProvider.providePlatform();
        } catch (final PlatformProvisionException ex) {
            new RuntimeException("Server version unsupported", ex).printStackTrace();
            this.logger.severe(
                    "Could not find a compatible NMS adapter. Some of Hyperverse's functionality will fail exceptionally."
            );
            nmsAdapter = NMSImpl.class;
        }
        bind(NMS.class).to(nmsAdapter).in(Singleton.class);
        bind(Plugin.class).toInstance(this.hyperverse);
        bind(Hyperverse.class).toInstance(this.hyperverse);
        bind(HyperDatabase.class).to(SQLiteDatabase.class).in(Singleton.class);
        bind(PersistentLocationTransformer.class).to(SimpleLocationTransformer.class).in(Singleton.class);
        bind(HyperConfiguration.class).to(FileHyperConfiguration.class).in(Singleton.class);
        bind(WorldManager.class).to(SimpleWorldManager.class).in(Singleton.class);
        bind(GlobalWorldFlagContainer.class).toInstance(new GlobalWorldFlagContainer());
        bind(ServicePipeline.class).toInstance(this.servicePipeline);
        bind(HyperEventFactory.class).to(SimpleHyperEventFactory.class).in(Singleton.class);
        bind(WorldConfigurationFactory.class).to(SimpleWorldConfigurationFactory.class).in(Singleton.class);
        install(new FactoryModuleBuilder().implement(WorldCreator.class, HyperWorldCreator.class)
                .build(HyperWorldCreatorFactory.class));
        install(new FactoryModuleBuilder().implement(HyperWorld.class, SimpleWorld.class)
                .build(HyperWorldFactory.class));
        install(new FactoryModuleBuilder().build(WorldImporterFactory.class));
        install(new FactoryModuleBuilder().implement(FlagContainer.class, WorldFlagContainer.class)
                .build(FlagContainerFactory.class));
        install(new FactoryModuleBuilder().implement(
                TeleportationManager.class,
                SimpleTeleportationManager.class
        ).build(TeleportationManagerFactory.class));

        requestStaticInjection(WorldConfiguration.class);
    }

    @Provides
    @HyperConfigShouldGroupProfiles
    boolean shouldGroupProfiles(final @NonNull HyperConfiguration configuration) {
        return configuration.shouldGroupProfiles();
    }

}
