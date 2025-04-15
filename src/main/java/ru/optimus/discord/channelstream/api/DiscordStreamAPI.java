package ru.optimus.discord.channelstream.api;

import reactor.core.publisher.Mono;
import ru.optimus.discord.channelstream.service.DiscordService;

public interface DiscordStreamAPI {


    Mono<DiscordService.DiscordMessageResponse> sendMessage(String message, String channelId);

    boolean apply(DiscordService.Message message, String channelId, String guildId);

}
