package org.phileasfogg3.fabricdiscordbridge.minecraft;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.phileasfogg3.fabricdiscordbridge.config.FabricDiscordBridgeConfig;
import org.phileasfogg3.fabricdiscordbridge.utils.LuckPermsUtil;

import java.awt.*;
import java.util.UUID;

public final class JoinLeaveHandler {

    private final MinecraftServer server;
    private final FabricDiscordBridgeConfig config;
    private final TextChannel channel;

    public JoinLeaveHandler(MinecraftServer server, FabricDiscordBridgeConfig config, TextChannel channel) {
        this.server = server;
        this.config = config;
        this.channel = channel;
    }

    public void register() {

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                handleJoin(handler.player)
        );

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                handleLeave(handler.player)
        );
    }

    /* ============================
       Join / Leave handlers
       ============================ */

    private void handleJoin(ServerPlayerEntity player) {
        if (!config.joinLeaveMessageEnabled) return;

        if (LuckPermsUtil.hasPermission(player, config.joinSilentlyPermission)) {
            return; // Silent join
        }

        sendEmbed(player, config.joinMessageFormatting);
    }


    private void handleLeave(ServerPlayerEntity player) {
        if (!config.joinLeaveMessageEnabled) return;

        if (LuckPermsUtil.hasPermission(player, config.leaveSilentlyPermission)) {
            return; // Silent leave
        }

        sendEmbed(player, config.leaveMessageFormatting);
    }


    /* ============================
       Embed builder
       ============================ */

    private void sendEmbed(ServerPlayerEntity player, FabricDiscordBridgeConfig.JoinLeaveFormatting fmt) {

        if (fmt == null || channel == null) return;

        String name = player.getName().getString();
        UUID uuid = player.getUuid();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(format(fmt.messageTitle, name, uuid))
                .setDescription(format(fmt.messageDescription, name, uuid))
                .setColor(parseColor(fmt.embedColour));

        // Image
        if (fmt.image != null && fmt.image.enabled) {
            embed.setImage(resolveImageUrl(fmt.image, uuid));
        }

        // Thumbnail
        if (fmt.thumbnail != null && fmt.thumbnail.enabled) {
            embed.setThumbnail(resolveThumbnailUrl(fmt.thumbnail, uuid));
        }

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    /* ============================
       Helpers
       ============================ */

    private static String format(String input, String name, UUID uuid) {
        if (input == null) return null;
        return input
                .replace("%name%", name)
                .replace("%uuid%", uuid.toString());
    }

    private static Color parseColor(String hex) {
        try {
            return hex != null ? Color.decode(hex) : Color.WHITE;
        } catch (Exception e) {
            return Color.WHITE;
        }
    }

    private static String resolveImageUrl(FabricDiscordBridgeConfig.Image image, UUID uuid) {
        if (image.usePlayerSkinAsImage) {
            return "https://mc-heads.net/avatar/" + uuid + ".png";
        }
        if (image.imageSettings != null) {
            return image.imageSettings.url;
        }
        return null;
    }

    private static String resolveThumbnailUrl(FabricDiscordBridgeConfig.Thumbnail thumbnail, UUID uuid) {
        if (thumbnail.usePlayerSkinAsThumbnail) {
            return "https://mc-heads.net/avatar/" + uuid + ".png";
        }
        if (thumbnail.thumbnailSettings != null) {
            return thumbnail.thumbnailSettings.url;
        }
        return null;
    }
}
