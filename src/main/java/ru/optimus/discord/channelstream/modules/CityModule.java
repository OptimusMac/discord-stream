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

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@ModuleDiscord
@Slf4j
@OnlyChannel(channelId = "1361842943542951996")
public class CityModule extends ModulesStreamAPI {

    private final MessageService messageService;
    private final CityProcess cityProcess;
    private final Random random = new Random();

    public CityModule(WebClient discordWebClient, MessageService messageService) {
        super(discordWebClient);
        this.messageService = messageService;
        this.cityProcess = new CityProcess();
    }


    @Override
    public MessageFunction<DiscordService.Message, String, String> function() {
        return (message, s, s2) -> {

            String text = message.getContent();

            char lastChar = text.charAt(text.length() - 1);

            if (!isLastCharCyrillicOrLatin(text))
                return false;

            AtomicBoolean atomicBoolean = new AtomicBoolean(false);

            cityProcess.searchCities(lastChar)
                    .flatMap(cities -> {
                        if (!cities.isEmpty()) {
                            String randomCity = cities.get(random.nextInt(cities.size()));
                            return sendMessage(randomCity, message.getChannel_id())
                                    .doOnSuccess(discordMessageResponse -> {
                                        atomicBoolean.set(true);
                                        messageService.saveByDiscordMessageResponse(discordMessageResponse);
                                    })
                                    .then();
                        }
                        return Mono.empty();
                    })
                    .onErrorResume(e -> {
                        log.error("Ошибка при обработке городов: {}", e.getMessage());
                        return Mono.empty();
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