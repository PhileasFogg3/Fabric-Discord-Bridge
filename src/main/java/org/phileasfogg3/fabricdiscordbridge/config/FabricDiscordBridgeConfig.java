package org.phileasfogg3.fabricdiscordbridge.config;

import java.util.List;
import java.util.Map;

public class FabricDiscordBridgeConfig {

    // --- Bot ---
    public String botToken = "";
    public String botActivity = "watching";
    public String botStreamingUrl = "";
    public String botDoing = "";

    // --- Channels ---
    public long discordChannelToMinecraft;
    public long discordChannelDisplayConsole;
    public long discordChannelCommandToMinecraft;

    // --- Formatting ---
    public MinecraftMessageFormatting minecraftMessageFormatting;
    public DiscordMessageFormatting discordMessageFormatting;

    // --- Join / Leave ---
    public boolean joinLeaveMessageEnabled = true;
    public JoinLeaveFormatting joinMessageFormatting;
    public JoinLeaveFormatting leaveMessageFormatting;
    public String joinSilentlyPermission;
    public String leaveSilentlyPermission;

    // --- Advancements ---
    public boolean announceAdvancementsEnabled = true;
    public EventFormatting advancementFormatting;

    // --- Deaths ---
    public boolean announceDeathsEnabled = true;
    public EventFormatting deathFormatting;

    // --- AFK ---
    public boolean announceAfkEnabled = true;
    public AfkFormatting afkFormatting;

    // --- Server lifecycle ---
    public boolean announceServerStartup = true;
    public String serverStartupMessageFormat;

    public boolean announceServerDisable = true;
    public String serverDisableMessageFormat;

    // --- Misc ---
    public boolean translateDiscordEmojisToText = true;

    // =====================
    // Nested config classes
    // =====================

    public static class MinecraftMessageFormatting {
        public List<String> rolesThatSendCommands;
        public String messageFormatUserWithRole;
        public String messageFormatUserWithNoRole;
        public Map<String, String> roles;
        public List<String> excludedRoles;
        public List<String> blockedRoles;
        public String otherRoleColourCode;
    }

    public static class DiscordMessageFormatting {
        public String messageFormat;
        public boolean webhooks;
        public String discord_webhook_url;
    }

    public static class JoinLeaveFormatting {
        public String messageTitle;
        public String messageDescription;
        public String embedColour;
        public Image image;
        public Thumbnail thumbnail;
    }

    public static class EventFormatting {
        public String messageTitle;
        public String messageDescription;
        public String embedColour;
        public Image image;
        public Thumbnail thumbnail;
    }

    public static class AfkFormatting {
        public String afkMessageTitle;
        public String returnMessageTitle;
        public String afkMessageDescription;
        public String returnMessageDescription;
        public String afkEmbedColour;
        public String returnEmbedColour;
        public Image image;
        public Thumbnail thumbnail;
    }

    public static class Image {
        public boolean enabled;
        public boolean usePlayerSkinAsImage;
        public boolean useOtherImage;
        public ImageSettings imageSettings;
    }

    public static class Thumbnail {
        public boolean enabled;
        public boolean usePlayerSkinAsThumbnail;
        public boolean useOtherThumbnail;
        public ImageSettings thumbnailSettings;
    }

    public static class ImageSettings {
        public String url;
    }

}
