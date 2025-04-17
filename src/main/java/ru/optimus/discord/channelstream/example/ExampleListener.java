package ru.optimus.discord.channelstream.example;

import org.springframework.context.annotation.Lazy;
import ru.optimus.discord.channelstream.anno.ListenerChannel;
import ru.optimus.discord.channelstream.anno.MessageHook;
import ru.optimus.discord.channelstream.context.functional.ListenerCallback;
import ru.optimus.discord.channelstream.context.utils.ListenContextHelper;

@ListenerChannel(channelId = "1361659509218476225", guildId = "1361659509218476222")
public class ExampleListener {

    private ListenContextHelper listenContextHelper;

    @Lazy
    public ExampleListener(ListenContextHelper listenContextHelper) {
        this.listenContextHelper = listenContextHelper;
    }

    @MessageHook
    public ListenerCallback listenerCallback() {
        return (message, userProxy) -> {
            listenContextHelper.defaultMessageSender(message.getChannel_id(), "SOSI");
        };
    }
}
