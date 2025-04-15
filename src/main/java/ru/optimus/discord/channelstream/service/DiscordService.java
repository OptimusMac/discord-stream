package ru.optimus.discord.channelstream.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.optimus.discord.channelstream.config.DiscordConfig;
import ru.optimus.discord.channelstream.modules.CityProcess;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
@AllArgsConstructor
public class DiscordService {

    private final WebClient discordWebClient;
    private final MessageService messageService;
    private final DiscordConfig discordConfig;
    private final CityProcess cityProcess;

    public Flux<Message> getChannelMessages(String guildId, String channelId, int limit) {
        return validateChannelInGuild(guildId, channelId)
                .thenMany(discordWebClient.get()
                        .uri("/channels/{channelId}/messages?limit={limit}", channelId,limit)
                        .retrieve()
                        .bodyToFlux(Message.class));
    }

    public Flux<Message> streamChannelMessages(String guildId, String channelId, String type) {
        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(tick -> getChannelMessages(guildId, channelId, 1))
                .filter(message -> !messageService.exists(message))
                .filter(message -> !expiredTimestamp(message.timestamp))
                .filter(this::declineForMe)
                .doOnEach(message -> {
                    if (message.hasValue()) {
                        checkAndReply(message.get(), channelId, type).subscribe();
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

        if(message.author == null)
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

    private final Random random = new Random();

    private Mono<Void> checkAndReply(Message message, String channelId, String type) {
        String text = message.content;

        if (isNumber(text) && type.equalsIgnoreCase("NUMERIC")) {
            return sendMessageWithResponse(channelId, incrementString(text))
                    .doOnSuccess(messageService::saveByDiscordMessageResponse)
                    .then()
                    .onErrorResume(e -> {
                        log.error("Ошибка при отправке числового ответа: {}", e.getMessage());
                        return Mono.empty();
                    });
        }

        if (!text.isEmpty() && type.equalsIgnoreCase("WORD")) {

            if(!isLastCharCyrillicOrLatin(text))
                return Mono.empty();

            char lastChar = text.charAt(text.length() - 1);
            return cityProcess.searchCities(lastChar)
                    .flatMap(cities -> {
                        if (!cities.isEmpty()) {
                            String randomCity = cities.get(random.nextInt(cities.size()));
                            return sendMessageWithResponse(channelId, randomCity)
                                    .doOnSuccess(messageService::saveByDiscordMessageResponse)
                                    .then();
                        }
                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        log.error("Ошибка при обработке городов: {}", e.getMessage());
                        return Mono.empty();
                    });
        }

        return Mono.empty();
    }

    public boolean isLastCharCyrillicOrLatin(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        char lastChar = text.charAt(text.length() - 1);
        Character.UnicodeBlock block = Character.UnicodeBlock.of(lastChar);

        return block == Character.UnicodeBlock.CYRILLIC
                || block == Character.UnicodeBlock.BASIC_LATIN && Character.isLetter(lastChar);
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

    private boolean isNumber(String text) {
        return text.matches("-?\\d+(\\.\\d+)?");
    }

    public String incrementString(String text) {
        try {
            String normalized = text.trim().replace(",", ".");
            BigDecimal number = new BigDecimal(normalized);
            BigDecimal incremented = number.add(BigDecimal.ONE);
            if (number.scale() <= 0) {
                return incremented.setScale(0, RoundingMode.UNNECESSARY).toString();
            }
            return incremented.toString();
        } catch (NumberFormatException e) {
            return null;
        }
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