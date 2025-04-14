package ru.optimus.discord.channelstream.controller;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import ru.optimus.discord.channelstream.service.DiscordService;

@RestController
@RequestMapping("/api/discord/guilds/{guildId}/channels")
@AllArgsConstructor
public class DiscordController {

    private final DiscordService discordService;


    @GetMapping(value = "/{channelId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DiscordService.Message> streamMessages(
            @PathVariable String guildId,
            @PathVariable String channelId) {
        return discordService.streamChannelMessages(guildId, channelId);
    }


    @Bean
    public WebSocketHandler discordWebSocketHandler() {
        return session -> {
            Flux<WebSocketMessage> messages = session.receive()
                    .flatMap(message -> {
                        String[] ids = message.getPayloadAsText().split("/");
                        if (ids.length != 2) {
                            return Flux.error(new IllegalArgumentException("Invalid format. Send guildId/channelId"));
                        }
                        return discordService.streamChannelMessages(ids[0], ids[1])
                                .map(msg -> session.textMessage(msg.toString()));
                    });
            return session.send(messages);
        };
    }
}