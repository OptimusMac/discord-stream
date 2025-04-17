package ru.optimus.discord.channelstream.context.utils;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.optimus.discord.channelstream.service.DiscordService;
import ru.optimus.discord.channelstream.service.MessageService;

import java.util.Map;

@AllArgsConstructor
@Component
public class ListenContextHelper {

    private final WebClient discordWebClient;
    private final MessageService messageService;


    public Mono<DiscordService.DiscordMessageResponse> sendMessageWithResponse(String channelId, String message) {
        return discordWebClient.post()
                .uri("/channels/{channelId}/messages", channelId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("content", message))
                .retrieve()
                .bodyToMono(DiscordService.DiscordMessageResponse.class)
                .doOnNext(response -> System.out.println("Сообщение отправлено: " + response))
                .doOnSuccess(messageService::saveByDiscordMessageResponse)
                .doOnError(e -> System.err.println("Ошибка отправки: " + e.getMessage()));
    }

    public void defaultMessageSender(String channelId, String message) {
        this.sendMessageWithResponse(channelId, message).subscribe();
    }
}
