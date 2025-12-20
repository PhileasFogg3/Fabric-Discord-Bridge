package org.phileasfogg3.fabricdiscordbridge;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.MinecraftServer;
import org.phileasfogg3.fabricdiscordbridge.commands.DiscordBridgeReloadCommand;
import org.phileasfogg3.fabricdiscordbridge.discord.DiscordBotManager;
import org.phileasfogg3.fabricdiscordbridge.minecraft.MinecraftChatListener;
import org.phileasfogg3.fabricdiscordbridge.utils.ConfigManager;
import org.phileasfogg3.fabricdiscordbridge.config.FabricDiscordBridgeConfig;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Fabricdiscordbridge implements ModInitializer {

    private static JDA jda;
    private static ScheduledExecutorService jdaExecutor;
    public static FabricDiscordBridgeConfig CONFIG;
    private static MinecraftChatListener chatListener;

    private static int lastPlayerCount = -1;

    @Override
    public void onInitialize() {

        // Scheduled executor for topic updates
        jdaExecutor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "FabricDiscordBridge"));

        // Load config and register listener ONCE
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            CONFIG = ConfigManager.load();

            if ("PUT_YOUR_TOKEN_HERE".equals(CONFIG.botToken)) {
                System.err.println("[DiscordBot] Discord token not set!");
            }

            if (CONFIG.discordChannelToMinecraft == CONFIG.discordChannelDisplayConsole) {
                System.err.println("[DiscordBot] Console and main channel cannot be the same!");
            }

            chatListener = new MinecraftChatListener(CONFIG);
            chatListener.register();
        });

        // Start bot and topic updater when server is fully ready
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                DiscordBotManager.start(CONFIG, server);
                jda = DiscordBotManager.getJDA();

                if (CONFIG.announceServerStartup && jda != null) {
                    sendStartupMessage();
                }

                // Start periodic topic updates
                startUpdateActivity(server);

            } catch (Exception e) {
                throw new RuntimeException("Failed to start Discord bot", e);
            }
        });

        // Clean shutdown
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {

            if (CONFIG.announceServerDisable && jda != null) {
                sendShutdownMessage();
            }

            DiscordBotManager.stop();

            if (jdaExecutor != null) {
                jdaExecutor.shutdownNow();
            }
        });

        // Commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("discordbridge")
                            .requires(source ->
                                    source.getPlayer() == null ||
                                            source.getServer().getPlayerManager()
                                                    .isOperator(source.getPlayer().getPlayerConfigEntry())
                            )
                            .then(CommandManager.literal("reload")
                                    .executes(ctx -> {
                                        // Reload config
                                        FabricDiscordBridgeConfig newConfig = ConfigManager.load();
                                        CONFIG = newConfig;

                                        // Update listener with new config
                                        if (chatListener != null) {
                                            chatListener.updateConfig(newConfig);
                                        }

                                        // Reload bot
                                        try {
                                            DiscordBotManager.reload(newConfig, ctx.getSource().getServer());
                                        } catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }

                                        return DiscordBridgeReloadCommand.execute(
                                                ctx.getSource(),
                                                ctx.getSource().getServer()
                                        );
                                    })
                            )
            );
        });
    }

    private void sendStartupMessage() {
        TextChannel channel = jda.getTextChannelById(CONFIG.discordChannelToMinecraft);
        if (channel != null) {
            channel.sendMessage(CONFIG.serverStartupMessageFormat).queue();
        }
    }

    private void sendShutdownMessage() {
        TextChannel channel = jda.getTextChannelById(CONFIG.discordChannelToMinecraft);
        if (channel != null) {
            channel.sendMessage(CONFIG.serverDisableMessageFormat).queue();
        }
    }

    private void startUpdateActivity(MinecraftServer server) {

        jdaExecutor.scheduleWithFixedDelay(() -> {
            try {
                DiscordBotManager.updateActivity(server, jda); // updates bot activity
            } catch (Exception e) {
                System.err.println("[FDB] Failed to update bot status or topic: " + e.getMessage());
            }
        }, 0, 2, TimeUnit.MINUTES);

    }
}
