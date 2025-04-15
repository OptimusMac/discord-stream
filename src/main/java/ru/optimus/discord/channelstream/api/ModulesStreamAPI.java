package ru.optimus.discord.channelstream.api;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.optimus.discord.channelstream.api.func.MessageFunction;
import ru.optimus.discord.channelstream.service.DiscordService;

import java.util.Map;

@AllArgsConstructor
public abstract class ModulesStreamAPI implements DiscordStreamAPI {


    private final WebClient discordWebClient;


    public Mono<DiscordService.DiscordMessageResponse> sendMessage(String message, String channelId) {
        return discordWebClient.post()
                .uri("/channels/{channelId}/messages", channelId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("content", message))
                .retrieve()
                .bodyToMono(DiscordService.DiscordMessageResponse.class)
                .doOnNext(response -> System.out.println("Сообщение отправлено: " + response))
                .doOnError(e -> System.err.println("Ошибка отправки: " + e.getMessage()));
    }

    @Override
    public boolean apply(DiscordService.Message message, String channelId, String guildId){
        return function().apply(message, channelId, guildId);
    }


    public MessageFunction<DiscordService.Message, String, String> function() {
        throw new UnsupportedOperationException();
    }
}
