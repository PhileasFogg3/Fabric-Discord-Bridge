package org.phileasfogg3.fabricdiscordbridge.minecraft;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.phileasfogg3.fabricdiscordbridge.config.FabricDiscordBridgeConfig;
import org.phileasfogg3.fabricdiscordbridge.discord.DiscordBotManager;
import org.phileasfogg3.fabricdiscordbridge.discord.DiscordWebhookSender;

public class MinecraftChatListener {

    private FabricDiscordBridgeConfig config;

    public MinecraftChatListener(FabricDiscordBridgeConfig config) {
        this.config = config;
    }

    /** Update the config on hot reload */
    public void updateConfig(FabricDiscordBridgeConfig newConfig) {
        this.config = newConfig;
    }

    public void register() {
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            handleChat(sender, message.getContent().getString());
        });
    }

    private void handleChat(ServerPlayerEntity player, String message) {
        if (config == null) return;

        String playerName = player.getName().getString();
        String uuid = player.getUuidAsString();

        // Use the current config
        String format = config.discordMessageFormatting.messageFormat;
        String formatted = format
                .replace("%name%", playerName)
                .replace("%uuid%", uuid)
                .replace("%message%", message);

        if (config.discordMessageFormatting.webhooks) {
            sendViaWebhook(player, message);
        } else {
            sendViaBot(formatted);
        }
    }

    private void sendViaWebhook(ServerPlayerEntity player, String message) {
        String avatarUrl = "https://mc-heads.net/avatar/" + player.getUuidAsString() + ".png";

        DiscordWebhookSender.send(
                config.discordMessageFormatting.discord_webhook_url,
                player.getName().getString(),
                avatarUrl,
                message
        );
    }

    private void sendViaBot(String message) {
        var jda = DiscordBotManager.getJDA();
        if (jda == null) return;

        var channel = jda.getTextChannelById(config.discordChannelToMinecraft);
        if (channel == null) return;

        channel.sendMessage(message).queue();
    }
}
