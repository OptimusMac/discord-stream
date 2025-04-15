package ru.optimus.discord.channelstream.config;


import com.datastax.oss.driver.shaded.guava.common.collect.ImmutableList;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Configuration
@Data
public class DiscordConfig {

    @Value("${discord.token}")
    private String token;

    @Value("${discord.token-username}")
    private String username;

    @Value("${discord.reply-for-me}")
    private boolean replyForMe;

    @Value("${discord.auto-runner}")
    private boolean runner;



    @Bean
    public WebClient discordWebClient() {
        return WebClient.builder()
                .baseUrl("https://discord.com/api/v9")
                .defaultHeader("Authorization", token)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

}

