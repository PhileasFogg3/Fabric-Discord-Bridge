package org.phileasfogg3.fabricdiscordbridge.discord;

import com.mojang.brigadier.ParseResults;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.phileasfogg3.fabricdiscordbridge.config.FabricDiscordBridgeConfig;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordMessageListener extends ListenerAdapter {

    private final MinecraftServer server;
    private final FabricDiscordBridgeConfig config;

    private static final Pattern CUSTOM_EMOJI = Pattern.compile("<:(\\w+):(\\d+)>");

    private static final Logger LOGGER = LoggerFactory.getLogger("FabricDiscordBridge");

    public DiscordMessageListener(MinecraftServer server, FabricDiscordBridgeConfig config) {
        this.server = server;
        this.config = config;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot() || e.getMember() == null) return;

        long channelId = e.getChannel().getIdLong();

        if (channelId == config.discordChannelToMinecraft) {
            server.execute(() -> handleChatRelay(e));
        }

        if (channelId == config.discordChannelCommandToMinecraft) {
            server.execute(() -> handleCommandRelay(e));
        }
    }

    // ============================
    // Chat relay
    // ============================
    private void handleChatRelay(MessageReceivedEvent e) {
        var fmt = config.minecraftMessageFormatting;
        List<Role> roles = e.getMember().getRoles();
        String topRole = roles.isEmpty() ? "" : roles.get(0).getName();

        boolean blocked = roles.stream()
                .anyMatch(r -> fmt.blockedRoles.contains(r.getName()));
        if (blocked) return;

        String message = e.getMessage().getContentDisplay();

        // Convert custom emojis <:name:id> → :name:
        message = translateCustomEmojis(message);

        // Convert Unicode emojis → :shortcode: if configured
        if (config.translateDiscordEmojisToText) {
            message = unicodeToShortcode(message);
        }

        String format;
        String roleColour = fmt.otherRoleColourCode;

        if (!roles.isEmpty() && !fmt.excludedRoles.contains(topRole)) {
            format = fmt.messageFormatUserWithRole;
            Map<String, String> roleMap = fmt.roles;
            if (roleMap.containsKey(topRole)) {
                roleColour = roleMap.get(topRole);
            }
        } else {
            format = fmt.messageFormatUserWithNoRole;
        }

        String formatted = format
                .replace("%name%", e.getMember().getEffectiveName())
                .replace("%username%", e.getAuthor().getName())
                .replace("%toprole%", topRole)
                .replace("%toprolecolour%", roleColour)
                .replace("%message%", message);

        server.execute(() -> {

            String replyHeader = buildReplyHeader(e, fmt);
            if (replyHeader != null) {
                server.getPlayerManager()
                        .broadcast(Text.literal(colour(replyHeader)), false);
            }

            server.getPlayerManager()
                    .broadcast(Text.literal(colour(formatted)), false);
        });
    }

    // ============================
    // Command relay
    // ============================
    private void handleCommandRelay(MessageReceivedEvent e) {
        var fmt = config.minecraftMessageFormatting;
        List<Role> roles = e.getMember().getRoles();

        boolean blocked = roles.stream()
                .anyMatch(r -> fmt.blockedRoles.contains(r.getName()));
        if (blocked) return;

        boolean allowed = roles.stream()
                .anyMatch(r -> fmt.rolesThatSendCommands.contains(r.getName()));
        if (!allowed) return;

        String raw = e.getMessage().getContentRaw().trim();
        if (raw.isEmpty()
                || !e.getMessage().getAttachments().isEmpty()
                || !e.getMessage().getEmbeds().isEmpty()
                || raw.contains("<@")) {
            return;
        }

        server.execute(() -> {
            try {
                String cmd = raw.startsWith("/") ? raw.substring(1) : raw;

                var dispatcher = server.getCommandManager().getDispatcher();
                var source = server.getCommandSource();

                ParseResults<ServerCommandSource> parse =
                        dispatcher.parse(cmd, source);

                dispatcher.execute(parse);

            } catch (Exception ex) {
                LOGGER.error("Failed to execute Discord command: {}", raw, ex);
            }
        });

    }

    // ============================
    // Helpers
    // ============================
    private static String translateCustomEmojis(String input) {
        Matcher m = CUSTOM_EMOJI.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, ":" + m.group(1) + ":");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String unicodeToShortcode(String input) {
        StringBuilder out = new StringBuilder();

        for (int i = 0; i < input.length(); ) {
            int codePoint = input.codePointAt(i);
            String charAsString = new String(Character.toChars(codePoint));

            Emoji emoji = EmojiManager.getByUnicode(charAsString);
            if (emoji != null && !emoji.getAliases().isEmpty()) {
                out.append(':').append(emoji.getAliases().get(0)).append(':');
            } else {
                out.append(charAsString);
            }

            i += Character.charCount(codePoint);
        }

        return out.toString();
    }

    private String buildReplyHeader(MessageReceivedEvent e, FabricDiscordBridgeConfig.MinecraftMessageFormatting fmt) {

        var referenced = e.getMessage().getReferencedMessage();

        // Do not return if empty message
        if (referenced == null) return null;

        // Case 1: Replying to webhook (Minecraft message)
        if (referenced.isWebhookMessage()) {

            String content = referenced.getContentDisplay();
            if (content.isBlank()) content = config.replies.noTextMessage;
            if (content.length() > config.replies.replyContentCharCutoff) {
                content = content.substring(0, config.replies.replyContentCharCutoff-3) + "...";
            }

            return config.replies.replyPrefix + formatDiscordMessage(fmt.messageFormatUserWithNoRole, referenced.getAuthor().getName(), referenced.getAuthor().getEffectiveName(), "", fmt.otherRoleColourCode, config.replies.replyContentColour + content);
        }

        // Case 2: Normal Discord user
        var member = referenced.getMember();
        if (member == null) return null;

        List<Role> roles = member.getRoles();
        String topRole = roles.isEmpty() ? "" : roles.get(0).getName();

        String roleColour = fmt.otherRoleColourCode;
        String format;

        if (!roles.isEmpty() && !fmt.excludedRoles.contains(topRole)) {
            format = fmt.messageFormatUserWithRole;
            if (fmt.roles.containsKey(topRole)) {
                roleColour = fmt.roles.get(topRole);
            }
        } else {
            format = fmt.messageFormatUserWithNoRole;
        }

        String content = referenced.getContentDisplay();
        if (content.isBlank()) content = config.replies.noTextMessage;
        if (content.length() > config.replies.replyContentCharCutoff) {
            content = content.substring(0, config.replies.replyContentCharCutoff-3) + "...";
        }

        String formatted = formatDiscordMessage(
                format,
                member.getEffectiveName(),
                referenced.getAuthor().getName(),
                topRole,
                roleColour,
                config.replies.replyContentColour + content
        );

        return config.replies.replyPrefix + formatted;
    }


    private String formatDiscordMessage(
            String format,
            String name,
            String username,
            String topRole,
            String roleColour,
            String message
    ) {
        return format
                .replace("%name%", name)
                .replace("%username%", username)
                .replace("%toprole%", topRole)
                .replace("%toprolecolour%", roleColour)
                .replace("%message%", message);
    }


    private static String colour(String input) {
        return input.replace('&', '§');
    }
}