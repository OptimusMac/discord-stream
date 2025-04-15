package ru.optimus.discord.channelstream.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.optimus.discord.channelstream.api.factory.FactoryLoaderModules;
import ru.optimus.discord.channelstream.config.DiscordConfig;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class DiscordService {

    private final WebClient discordWebClient;
    private final MessageService messageService;
    private final DiscordConfig discordConfig;
    private final FactoryLoaderModules factoryLoaderModules;

    public Flux<Message> getChannelMessages(String guildId, String channelId, int limit) {
        return validateChannelInGuild(guildId, channelId)
                .thenMany(discordWebClient.get()
                        .uri("/channels/{channelId}/messages?limit={limit}", channelId, limit)
                        .retrieve()
                        .bodyToFlux(Message.class));
    }

    public Flux<Message> streamChannelMessages(String guildId, String channelId) {
        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(tick -> getChannelMessages(guildId, channelId, 1))
                .filter(message -> !messageService.exists(message))
                .filter(message -> !expiredTimestamp(message.timestamp))
                .filter(this::declineForMe)
                .doOnEach(message -> {
                    if (message.hasValue()) {
                        checkAndReply(message.get()).subscribe();
                    }
                })
                .distinct(Message::getId)
                .doOnEach(messageSignal -> {
                    if (messageSignal.hasValue()) {
                        messageService.save(messageSignal.get());
                    }
                });
    }

    public boolean declineForMe(Message message) {

        if (message.author == null)
            return false;

        return discordConfig.isReplyForMe() || !message.author.username.equals(discordConfig.getUsername());
    }

    private Mono<Void> validateChannelInGuild(String guildId, String channelId) {
        return discordWebClient.get()
                .uri("/channels/{channelId}", channelId)
                .retrieve()
                .bodyToMono(ChannelInfo.class)
                .flatMap(channel -> {
                    if (!guildId.equals(channel.guild_id)) {
                        return Mono.error(new IllegalArgumentException("Channel does not belong to the specified guild"));
                    }

                    return Mono.empty();
                });
    }

    private Mono<Void> checkAndReply(Message message) {
        return Mono.fromCallable(() -> {
            factoryLoaderModules.applyFirst(message, messageService::save);
            return null;
        });

    }


    public boolean expiredTimestamp(String timestamp) {
        try {
            Instant messageTime = Instant.parse(timestamp);

            Instant now = Instant.now();
            Duration duration = Duration.between(messageTime, now);

            return duration.toMinutes() >= 1;

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid timestamp format: " + timestamp, e);
        }
    }

    public Mono<DiscordMessageResponse> sendMessageWithResponse(String channelId, String message) {
        return discordWebClient.post()
                .uri("/channels/{channelId}/messages", channelId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("content", message))
                .retrieve()
                .bodyToMono(DiscordMessageResponse.class)
                .doOnNext(response -> System.out.println("Сообщение отправлено: " + response))
                .doOnError(e -> System.err.println("Ошибка отправки: " + e.getMessage()));
    }


    @Data
    public static class Message {
        private String id;
        private String content;
        private String channel_id;
        private String guild_id;
        private Author author;
        private String timestamp;

        // геттеры и сеттеры
    }

    // DTO для информации о канале
    @Data
    public static class ChannelInfo {
        private String id;
        private String guild_id;

        // геттеры и сеттеры
    }

    @Data
    public static class Author {
        private String id;
        private String username;

        // геттеры и сеттеры
    }

    @Data
    public static class DiscordMessageResponse {
        private String id;
        private String content;

        @JsonProperty("channel_id")
        private String channelId;
        private String timestamp;


    }
}