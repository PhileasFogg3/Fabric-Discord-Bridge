package org.phileasfogg3.fabricdiscordbridge.discord;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.phileasfogg3.fabricdiscordbridge.Fabricdiscordbridge;
import org.phileasfogg3.fabricdiscordbridge.discord.DiscordBotManager;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class DiscordLogAppender extends AbstractAppender {

    private static final Pattern ANSI_REGEX = Pattern.compile("\u001B\\[[;\\d]*m");
    private static final int DISCORD_MAX = 2000;

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Discord-Log-Batcher");
                t.setDaemon(true);
                return t;
            });

    private final AtomicBoolean running = new AtomicBoolean(false);

    public DiscordLogAppender() {
        super(
                "DiscordLogAppender",
                null,
                PatternLayout.createDefaultLayout(),
                false,
                null
        );
        start();
        startBatcher();
    }

    @Override
    public void append(LogEvent event) {
        if (Fabricdiscordbridge.INSTANCE == null) return;
        if (DiscordBotManager.getJDA() == null) return;

        String raw = event.getMessage().getFormattedMessage();
        if (raw == null || raw.trim().isEmpty()) return;

        String message = "[" + event.getThreadName() + "/" + event.getLevel() + "]: " + raw;
        message = ANSI_REGEX.matcher(message).replaceAll("").trim();
        if (message.isEmpty()) return;

        messageQueue.offer(message);
    }

    private void startBatcher() {
        if (running.getAndSet(true)) return;

        scheduler.scheduleAtFixedRate(() -> {
            if (Fabricdiscordbridge.INSTANCE == null) return;
            if (DiscordBotManager.getJDA() == null) return;

            String channelId = String.valueOf(Fabricdiscordbridge.CONFIG.discordChannelDisplayConsole);
            if (channelId == null || channelId.isEmpty()) return;

            TextChannel channel =
                    DiscordBotManager.getJDA().getTextChannelById(channelId);
            if (channel == null) return;

            StringBuilder batch = new StringBuilder();
            String line;

            while ((line = messageQueue.poll()) != null) {
                if (batch.length() + line.length() + 1 > DISCORD_MAX - 6) {
                    messageQueue.offer(line);
                    break;
                }
                batch.append(line).append("\n");
            }

            if (batch.length() == 0) return;

            String formatted = "```" + batch.toString().trim() + "```";
            channel.sendMessage(formatted).queue();

        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        super.stop();
        running.set(false);
        scheduler.shutdownNow();
    }

    // Registration helper

    public static void register() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();

        DiscordLogAppender appender = new DiscordLogAppender();
        appender.start();

        config.addAppender(appender);

        for (LoggerConfig loggerConfig : config.getLoggers().values()) {
            loggerConfig.addAppender(appender, Level.INFO, null);
        }

        config.getRootLogger().addAppender(appender, Level.INFO, null);
        ctx.updateLoggers();
    }
}
