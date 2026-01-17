package org.phileasfogg3.fabricdiscordbridge.minecraft;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.phileasfogg3.fabricdiscordbridge.config.FabricDiscordBridgeConfig;

import java.awt.*;

public class DeathHandler {

    private final MinecraftServer server;
    private final FabricDiscordBridgeConfig config;
    private final TextChannel channel;

    public DeathHandler(MinecraftServer server, FabricDiscordBridgeConfig config, TextChannel channel) {
        this.server = server;
        this.config = config;
        this.channel = channel;
    }

    public void register() {

        ServerLivingEntityEvents.AFTER_DEATH.register(this::onDeath);

    }

    private void onDeath(net.minecraft.entity.LivingEntity entity, DamageSource source) {

        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (!config.announceDeathsEnabled) return;

        sendDeathMessage(player, source);

    }

    private void sendDeathMessage(ServerPlayerEntity player, DamageSource source) {

        String name = player.getName().getString();
        String uuid = player.getUuidAsString();

        Text deathText = source.getDeathMessage(player);

        String deathMessage = deathText.getString();

        var fmt = config.deathFormatting;

        String title = replace(fmt.messageTitle, name, uuid);
        String description = replace(fmt.messageDescription, name, uuid).replace("%deathMessage%", deathMessage);

        Color color = Color.decode(fmt.embedColour);

        var embed = new net.dv8tion.jda.api.EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(color);

        if (fmt.image != null && fmt.image.enabled) {
            embed.setImage(resolveImage(fmt.image, uuid));
        }

        if (fmt.thumbnail != null && fmt.thumbnail.enabled) {
            embed.setThumbnail(resolveThumbnail(fmt.thumbnail, uuid));
        }

        channel.sendMessageEmbeds(embed.build()).queue();
    }

    private static String replace(String input, String name, String uuid) {
        if (input == null) return "";
        return input.replace("%name%", name).replace("%uuid%", uuid);
    }

    private static String resolveImage(
            FabricDiscordBridgeConfig.Image img, String uuid) {

        if (img.usePlayerSkinAsImage) {
            return "https://mc-heads.net/avatar/" + uuid + ".png";
        }
        return img.imageSettings != null ? img.imageSettings.url : null;
    }

    private static String resolveThumbnail(
            FabricDiscordBridgeConfig.Thumbnail thumb, String uuid) {

        if (thumb.usePlayerSkinAsThumbnail) {
            return "https://mc-heads.net/avatar/" + uuid + ".png";
        }
        return thumb.thumbnailSettings != null ? thumb.thumbnailSettings.url : null;
    }

}
