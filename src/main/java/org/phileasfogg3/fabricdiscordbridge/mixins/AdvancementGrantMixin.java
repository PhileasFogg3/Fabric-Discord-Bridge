package org.phileasfogg3.fabricdiscordbridge.mixins;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.phileasfogg3.fabricdiscordbridge.Fabricdiscordbridge;
import org.phileasfogg3.fabricdiscordbridge.config.FabricDiscordBridgeConfig;
import org.phileasfogg3.fabricdiscordbridge.discord.DiscordBotManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(PlayerAdvancementTracker.class)
public abstract class AdvancementGrantMixin {

    @Shadow private ServerPlayerEntity owner;

    @Inject(
            method = "grantCriterion",
            at = @At("TAIL")
    )
    private void onAdvancementGranted(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        // Only trigger once (not per criterion)
        if (!cir.getReturnValue()) return;


        if (advancement.value().display().isEmpty()) return;

        var displayOpt = advancement.value().display().get();
        if (!displayOpt.shouldAnnounceToChat()) return;

        sendAdvancementMessage(owner, advancement);

    }

    private void sendAdvancementMessage(ServerPlayerEntity player, AdvancementEntry advancement) {

        String name = player.getName().getString();
        String uuid = player.getUuid().toString();

        String advancementTitle = advancement.value().display().get().getTitle().getString();
        System.out.println("Advancement Title: " + advancementTitle);

        var config = Fabricdiscordbridge.CONFIG;
        if (config == null || !config.announceAdvancementsEnabled) return;

        var fmt = config.advancementFormatting;
        if (fmt == null) return;

        var jda = DiscordBotManager.getJDA();
        if (jda == null) return;

        TextChannel channel = jda.getTextChannelById(config.discordChannelToMinecraft);
        if (channel == null) return;

        String title = replace(fmt.messageTitle, name, uuid);
        String description = replace(fmt.messageDescription, name, uuid).replace("%advancement%", advancementTitle);

        Color color = Color.decode(fmt.embedColour);

        var embed = new EmbedBuilder()
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

