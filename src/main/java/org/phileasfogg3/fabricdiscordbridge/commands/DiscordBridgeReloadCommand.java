package org.phileasfogg3.fabricdiscordbridge.commands;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.phileasfogg3.fabricdiscordbridge.utils.ConfigManager;
import org.phileasfogg3.fabricdiscordbridge.discord.DiscordBotManager;

public final class DiscordBridgeReloadCommand {

    private DiscordBridgeReloadCommand() {}

    public static int execute(ServerCommandSource source, MinecraftServer server) {
        source.sendFeedback(
                () -> Text.literal("§e[DiscordBridge] Reloading configuration..."),
                false
        );

        try {
            var newConfig = ConfigManager.load();
            DiscordBotManager.reload(newConfig, server);

            source.sendFeedback(
                    () -> Text.literal("§a[DiscordBridge] Reload complete."),
                    false
            );

            return 1;

        } catch (Exception e) {
            source.sendError(
                    Text.literal("§c[DiscordBridge] Reload failed. Check server log.")
            );
            e.printStackTrace();
            return 0;
        }
    }
}
