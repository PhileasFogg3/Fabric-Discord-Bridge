package org.phileasfogg3.fabricdiscordbridge;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Fabricdiscordbridge implements ModInitializer {

    private static JDA jda;
    private static ExecutorService jdaExecutor;

    public static FabricDiscordBridgeConfig CONFIG;

    private TextChannel discordChannelMinecraft;

    @Override
    public void onInitialize() {

        jdaExecutor = Executors.newSingleThreadExecutor(r ->
                new Thread(r, "FabricDiscordBridge"));

        // Load config and warn if token missing
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            CONFIG = ConfigManager.load();

            if ("PUT_YOUR_TOKEN_HERE".equals(CONFIG.botToken)) {
                System.err.println("[DiscordBot] Discord token not set!");
            }

            if (CONFIG.discordChannelToMinecraft == CONFIG.discordChannelDisplayConsole) {
                System.err.println("[DiscordBot] The console and main channel are set to be the same discord channel!");
                DiscordBotManager.stop();
            }
        });

        // Start Discord bot once the server is ready
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            try {
                DiscordBotManager.start(CONFIG, server);
                jda = DiscordBotManager.getJDA();
            } catch (Exception e) {
                throw new RuntimeException("Failed to start Discord bot", e);
            }

            if (CONFIG.announceServerStartup && (jda != null)) {
                enableMessage();
            }

            registerEvents();

        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {

            if (CONFIG.announceServerDisable && jda != null) {
                disableMessage();
            }

        });

        // Stop Discord bot when server stops
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            DiscordBotManager.stop();
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("discordbridge")
                            .requires(source -> {
                                if (source.getPlayer() == null) return true; // console allowed
                                return source.getServer().getPlayerManager().isOperator(
                                        source.getPlayer().getPlayerConfigEntry()
                                );
                            })
                            .then(CommandManager.literal("reload")
                                    .executes(ctx -> {
                                        // Pass server to reload for listener
                                        return DiscordBridgeReloadCommand.execute(ctx.getSource(), ctx.getSource().getServer());
                                    })
                            )
            );
        });

    }

    public void enableMessage() {

        TextChannel channel = jda.getTextChannelById(CONFIG.discordChannelToMinecraft);
        if (channel != null) {
            channel.sendMessage(CONFIG.serverStartupMessageFormat).queue();
        }

    }

    public void disableMessage() {

        TextChannel channel = jda.getTextChannelById(CONFIG.discordChannelToMinecraft);
        if (channel != null) {
            channel.sendMessage(CONFIG.serverDisableMessageFormat).queue();
        }

    }

    public void registerEvents() {

        new MinecraftChatListener().register();

    }
}
