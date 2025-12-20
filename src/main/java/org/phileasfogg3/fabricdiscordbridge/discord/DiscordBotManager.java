package org.phileasfogg3.fabricdiscordbridge.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.minecraft.server.MinecraftServer;
import org.phileasfogg3.fabricdiscordbridge.config.FabricDiscordBridgeConfig;

import java.util.Objects;

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

        JDABuilder builder = JDABuilder.createDefault(config.botToken)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new DiscordMessageListener(server, config));


        // Set initial activity
        updateActivity(server, builder);

        jda = builder.build();
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

    /** Update bot activity, replacing %playercount% if present */
    public static void updateActivity(MinecraftServer server, Object target) {
        if (config == null) return;

        String doing = config.botDoing != null ? config.botDoing : "";
        if (doing.contains("%playercount%") && server != null) {
            int online = server.getPlayerManager().getCurrentPlayerCount();
            doing = doing.replace("%playercount%", String.valueOf(online));
        }

        Activity activity = switch (config.botActivity) {
            case "watching" -> Activity.watching(Objects.requireNonNull(doing));
            case "competing" -> Activity.competing(Objects.requireNonNull(doing));
            case "listening" -> Activity.listening(Objects.requireNonNull(doing));
            case "playing" -> Activity.playing(Objects.requireNonNull(doing));
            case "streaming" -> Activity.streaming(doing, Objects.requireNonNull(config.botStreamingUrl));
            default -> Activity.playing("playnexia.net");
        };

        if (target instanceof JDABuilder builder) {
            builder.setActivity(activity);
        } else if (target instanceof JDA jdaInstance) {
            jdaInstance.getPresence().setActivity(activity);
        }
    }
}