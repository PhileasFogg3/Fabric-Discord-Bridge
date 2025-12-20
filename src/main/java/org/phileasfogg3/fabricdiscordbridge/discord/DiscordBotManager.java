package org.phileasfogg3.fabricdiscordbridge.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.server.MinecraftServer;
import org.phileasfogg3.fabricdiscordbridge.config.FabricDiscordBridgeConfig;

public final class DiscordBotManager {

    private static JDA jda;
    private static FabricDiscordBridgeConfig config;

    private DiscordBotManager() {}

    /** Start the bot with the given config and server */
    public static void start(FabricDiscordBridgeConfig newConfig, MinecraftServer server) throws Exception {
        config = newConfig;

        if (config.botToken == null || config.botToken.isBlank()) {
            throw new IllegalStateException("Bot token is missing in config!");
        }

        jda = JDABuilder.createDefault(config.botToken)
                .addEventListeners(new DiscordMessageListener(server, config))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();

        jda.awaitReady();
    }

    /** Stop the bot safely */
    public static void stop() {
        if (jda != null) {
            jda.shutdown();
            jda = null;
        }
    }

    /** Reload the bot with new config and server */
    public static void reload(FabricDiscordBridgeConfig newConfig, MinecraftServer server) throws Exception {
        stop();
        start(newConfig, server);
    }

    /** Get current config */
    public static FabricDiscordBridgeConfig getConfig() {
        return config;
    }

    /** Get current JDA instance */
    public static JDA getJDA() {
        return jda;
    }
}
