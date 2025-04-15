package ru.optimus.discord.channelstream.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "discord")
@Data
public class DiscordStreamConfig {
    private List<StreamConfig> streams;

    @Data
    public static class StreamConfig {
        private String guildId;
        private String channelId;
        private String type;
    }
}