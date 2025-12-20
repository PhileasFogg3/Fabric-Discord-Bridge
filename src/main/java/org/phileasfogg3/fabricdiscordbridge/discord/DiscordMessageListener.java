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

import static com.mojang.text2speech.Narrator.LOGGER;

public class DiscordMessageListener extends ListenerAdapter {

    private final MinecraftServer server;
    private final FabricDiscordBridgeConfig config;

    private static final Pattern CUSTOM_EMOJI =
            Pattern.compile("<:(\\w+):(\\d+)>");

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

        server.execute(() ->
                server.getPlayerManager()
                        .broadcast(Text.literal(colour(formatted)), false)
        );
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
        if (raw.isEmpty() || !e.getMessage().getAttachments().isEmpty() || !e.getMessage().getEmbeds().isEmpty()) return;
        if (raw.contains("<@")) return;

        String command = raw.startsWith("/") ? raw : "/" + raw;

        server.execute(() -> {
            try {
                var dispatcher = server.getCommandManager().getDispatcher();
                var source = server.getCommandSource();
                ParseResults<ServerCommandSource> parse = dispatcher.parse(command, source);
                dispatcher.execute(parse);
            } catch (Exception ex) {
                LOGGER.error("Failed to execute Discord command: {}", command, ex);
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

    private static String colour(String input) {
        return input.replace('&', '§');
    }
}