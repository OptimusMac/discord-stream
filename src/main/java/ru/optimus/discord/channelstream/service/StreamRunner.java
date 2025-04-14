package ru.optimus.discord.channelstream.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.optimus.discord.channelstream.config.DiscordConfig;
import ru.optimus.discord.channelstream.config.DiscordStreamConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamRunner {
    private final DiscordStreamConfig config;

    private DiscordConfig discordConfig;

    @PostConstruct
    public void init() {
        if(discordConfig.isRunner()) {
            config.getStreams().forEach(stream -> {
                new Thread(() -> runStream(stream)).start();
            });
        }
    }

    private void runStream(DiscordStreamConfig.StreamConfig stream) {
        String[] command = {
                "curl",
                "-s",
                "-N",
                "http://localhost:8080/api/discord/guilds/" + stream.getGuildId() +
                        "/channels/" + stream.getChannelId() + "/messages"
        };

        while (true) {
            try {
                Process process = new ProcessBuilder(command).start();
                log.info("Started stream for guild: {}, channel: {}",
                        stream.getGuildId(), stream.getChannelId());

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("Received: {}", line);
                    }
                }

                log.warn("Stream closed for guild: {}, channel: {}",
                        stream.getGuildId(), stream.getChannelId());
                Thread.sleep(5000);

            } catch (Exception e) {
                log.error("Stream error for guild: {}, channel: {}: {}",
                        stream.getGuildId(), stream.getChannelId(), e.getMessage());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}