package ru.optimus.discord.channelstream.modules;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.optimus.discord.channelstream.api.ModulesStreamAPI;
import ru.optimus.discord.channelstream.api.anno.ModuleDiscord;
import ru.optimus.discord.channelstream.api.anno.OnlyChannel;
import ru.optimus.discord.channelstream.api.func.MessageFunction;
import ru.optimus.discord.channelstream.service.DiscordService;
import ru.optimus.discord.channelstream.service.MessageService;
import ru.optimus.discord.channelstream.utils.CityProcess;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@ModuleDiscord
@Slf4j
@OnlyChannel(channelId = "1361659509218476225")
public class CityModule extends ModulesStreamAPI {

    private final MessageService messageService;

    public CityModule(WebClient discordWebClient, MessageService messageService) {
        super(discordWebClient);
        this.messageService = messageService;
    }



    private char findChar(String message, int index){
        return Character.toUpperCase(message.charAt(index));
    }


    @Override
    public MessageFunction<DiscordService.Message, String, String> function() {
        return (message, s, s2) -> {

            String text = message.getContent();

            int backStep = 1;

            char lastChar = findChar(text, text.length() - backStep);

            int retry = 5;

            while((lastChar == 'Ь' || lastChar == 'Ъ') && retry > 0){
                lastChar = findChar(text, text.length() - ++backStep);
                --retry;
            }
            if(retry == 0)
                return false;

            if (!isLastCharCyrillicOrLatin(text))
                return false;

            AtomicBoolean atomicBoolean = new AtomicBoolean(false);

            String word = CityProcess.findWord(lastChar);

            if(word == null) {
                return false;
            }

            if(!CityProcess.validateCity(message.getContent())){

                return false;
            }

            sendMessage(word, message.getChannel_id())
                    .doOnSuccess(discordMessageResponse -> {
                        atomicBoolean.set(true);
                        messageService.saveByDiscordMessageResponse(discordMessageResponse);
                    }).subscribe();

            return atomicBoolean.get();
        };
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
}