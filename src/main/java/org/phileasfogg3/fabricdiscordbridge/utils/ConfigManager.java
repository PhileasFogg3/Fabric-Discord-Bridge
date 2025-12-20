package org.phileasfogg3.fabricdiscordbridge.utils;

import net.fabricmc.loader.api.FabricLoader;
import org.phileasfogg3.fabricdiscordbridge.config.FabricDiscordBridgeConfig;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {

    private static final String CONFIG_FILE = "fabric_discord_bridge.yml";
    private static final String DEFAULT_RESOURCE = "default-fabric_discord_bridge.yml";
    private static final Yaml YAML;

    static {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(50);

        Constructor constructor =
                new Constructor(FabricDiscordBridgeConfig.class, options);

        YAML = new Yaml(constructor);
    }

    private ConfigManager() {
    }

    public static FabricDiscordBridgeConfig load() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            Path configPath = configDir.resolve(CONFIG_FILE);

            if (Files.notExists(configPath)) {
                copyDefaultConfig(configPath);
            }

            try (InputStream in = Files.newInputStream(configPath)) {
                FabricDiscordBridgeConfig config = YAML.load(in);
                return config != null ? config : new FabricDiscordBridgeConfig();
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load " + CONFIG_FILE +
                            ". Fix the file or delete it to regenerate.", e);
        }
    }

    private static void copyDefaultConfig(Path path) throws Exception {
        Files.createDirectories(path.getParent());

        try (InputStream in =
                     ConfigManager.class
                             .getClassLoader()
                             .getResourceAsStream(DEFAULT_RESOURCE)) {

            if (in == null) {
                throw new IllegalStateException(
                        "Missing resource: " + DEFAULT_RESOURCE);
            }

            Files.copy(in, path);
        }
    }
}