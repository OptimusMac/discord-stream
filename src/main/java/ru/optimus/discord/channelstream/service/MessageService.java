package ru.optimus.discord.channelstream.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.optimus.discord.channelstream.model.MessageDiscord;
import ru.optimus.discord.channelstream.repository.CassandraRepository;

import java.util.Date;

@Service
@AllArgsConstructor
public class MessageService {

    private final CassandraRepository repository;


    public boolean exists(DiscordService.Message message){
        return repository.existsByContentID(message.getId());
    }

    public MessageDiscord save(DiscordService.Message message){
        MessageDiscord messageDiscord = new MessageDiscord();
        messageDiscord.setContent(message.getContent());
        messageDiscord.setCreateAt(new Date());
        messageDiscord.setContentID(message.getId());

        return repository.save(messageDiscord);
    }

    public MessageDiscord saveByDiscordMessageResponse(DiscordService.DiscordMessageResponse messageResponse){
        MessageDiscord messageDiscord = new MessageDiscord();
        messageDiscord.setContent(messageResponse.getContent());
        messageDiscord.setContentID(messageResponse.getId());
        messageDiscord.setCreateAt(new Date());

        return repository.save(messageDiscord);
    }
}
