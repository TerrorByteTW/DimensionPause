package org.reprogle.dimensionpause;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.reprogle.bytelib.ByteLibPlugin;
import org.reprogle.bytelib.db.sqlite.SqliteModule;
import org.reprogle.bytelib.boot.wiring.PluginWiring;

import java.nio.file.Path;
import java.util.List;

public final class DimensionPausePlugin extends ByteLibPlugin {

    @Inject
    public DimensionPausePlugin(Injector injector, PluginMeta meta, Path dataDir, ComponentLogger logger) {
        super(injector, meta, dataDir, logger);
    }

    /*
     * Modules are loaded in one of three ways by ByteLib:
     * 1. Checking the main class and finding a class with "Wiring" appended to it (DimensionPausePlugin.class > DimensionPausePluginWiring.class)
     * 2. Checking if the main class has a nested Wiring class in it (DimensionPausePlugin$Wiring.class)
     * 3. Using the ServiceLoader to check for classes implementing PluginWiring
     *
     * DimensionPause's Guice modules are wired via method 2 for cleanliness
     * Unused warnings are suppressed since these classes are always referenced via reflection
     */
    @SuppressWarnings("unused")
    public static class Wiring implements PluginWiring {
        @Override
        public List<Module> modules(PluginMeta meta, Path dataDir, ComponentLogger logger) {
            return List.of(
                    new DimensionPauseModule(),
                    new SqliteModule("dimensionpause.db")
            );
        }
    }
}
