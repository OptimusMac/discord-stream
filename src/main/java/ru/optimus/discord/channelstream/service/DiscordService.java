package ru.optimus.discord.channelstream.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.optimus.discord.channelstream.context.FactoryListenerMessageContext;
import ru.optimus.discord.channelstream.context.proxy.ChannelProxy;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@Service
@Slf4j
@AllArgsConstructor
public class DiscordService {

    private final WebClient discordWebClient;
    private final MessageService messageService;
    private FactoryListenerMessageContext listenerMessageContext;

    public Flux<Message> getChannelMessages(String guildId, String channelId, int limit) {
        return validateChannelInGuild(guildId, channelId)
                .thenMany(discordWebClient.get()
                        .uri("/channels/{channelId}/messages?limit={limit}", channelId, limit)
                        .retrieve()
                        .bodyToFlux(Message.class));
    }



    @PostConstruct
    public void initRunnable(){
        for (ChannelProxy channelProxy : listenerMessageContext.registerRunnableListenerContexts()) {
            this.streamChannelMessages(channelProxy.guildID(), channelProxy.channelId()).subscribe();
        }
    }

    private Flux<Message> streamChannelMessages(String guildId, String channelId) {
        return Flux.interval(Duration.ofSeconds(1))
                .flatMap(tick -> getChannelMessages(guildId, channelId, 1))
                .filter(message -> !messageService.exists(message))
                .filter(message -> !expiredTimestamp(message.timestamp))
                .doOnEach(message -> {
                    if (message.hasValue()) {
                        checkAndReply(message.get()).subscribe();
                    }
                })
                .distinct(Message::getId)
                .doOnEach(messageSignal -> {
                    if (messageSignal.hasValue()) {

                        messageService.save(Objects.requireNonNull(messageSignal.get()));
                    }
                });
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
            listenerMessageContext.tryListen(message);
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



    @Data
    public static class Message {
        private String id;
        private String content;
        private String channel_id;
        private String guild_id;
        private Author author;
        private String timestamp;

    }

    @Data
    public static class ChannelInfo {
        private String id;
        private String guild_id;

    }

    @Data
    public static class Author {
        private String id;
        private String username;

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
