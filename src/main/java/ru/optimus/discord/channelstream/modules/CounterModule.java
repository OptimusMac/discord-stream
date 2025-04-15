package ru.optimus.discord.channelstream.modules;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.optimus.discord.channelstream.api.ModulesStreamAPI;
import ru.optimus.discord.channelstream.api.anno.ModuleDiscord;
import ru.optimus.discord.channelstream.api.func.MessageFunction;
import ru.optimus.discord.channelstream.service.DiscordService;
import ru.optimus.discord.channelstream.service.MessageService;
import ru.optimus.discord.channelstream.utils.NumberExtractor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@ModuleDiscord
public class CounterModule extends ModulesStreamAPI {

    private final MessageService messageService;

    public CounterModule(WebClient discordWebClient, MessageService messageService) {
        super(discordWebClient);
        this.messageService = messageService;
    }


    @Override
    public MessageFunction<DiscordService.Message, String, String> function() {
        return (message, s, s2) -> {

            String text = message.getContent().replace(" ", "");
            String extract = NumberExtractor.extractSplit(text);
            if (extract != null) {
                String numericText = incrementString(extract);
                if (numericText == null) return false;
                sendMessage(numericText, message.getChannel_id())
                        .doOnSuccess(messageService::saveByDiscordMessageResponse)
                        .subscribe();
                return true;
            }
            return false;
        };
    }

    public String incrementString(String text) {
        try {

            String normalized = text.trim().replace(",", ".");
            BigDecimal number = new BigDecimal(normalized);
            BigDecimal incremented = number.add(BigDecimal.ONE);
            if (number.scale() <= 0) {
                return incremented.setScale(0, RoundingMode.UNNECESSARY).toString();
            }
            return incremented.toString();


        } catch (NumberFormatException e) {
            return null;
        }
    }
}
