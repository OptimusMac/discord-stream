package ru.optimus.discord.channelstream.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;

import java.net.URI;

@Service
@AllArgsConstructor
public class DiscordGatewayService {

    private final WebClient discordWebClient;


    public Flux<Void> connectToGateway() {
        return discordWebClient.get()
                .uri("/gateway/bot")
                .retrieve()
                .bodyToMono(GatewayResponse.class)
                .flatMapMany(response -> {
                    ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
                    return client.execute(
                            URI.create(response.url + "?v=9&encoding=json"),
                            session -> session.receive()
                                    .map(WebSocketMessage::getPayloadAsText).then()
                    );
                });
    }

    @Data
    private static class GatewayResponse {
        private String url;

    }
}