package ru.optimus.discord.channelstream.context.functional;

import ru.optimus.discord.channelstream.context.proxy.UserProxy;
import ru.optimus.discord.channelstream.service.DiscordService;

@FunctionalInterface
public interface ListenerCallback {

    void apply(DiscordService.Message message, UserProxy userProxy);
}
