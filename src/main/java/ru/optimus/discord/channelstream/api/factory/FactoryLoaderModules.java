package ru.optimus.discord.channelstream.api.factory;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.optimus.discord.channelstream.api.DiscordStreamAPI;
import ru.optimus.discord.channelstream.api.ModulesStreamAPI;
import ru.optimus.discord.channelstream.api.anno.ModuleDiscord;
import ru.optimus.discord.channelstream.api.anno.OnlyChannel;
import ru.optimus.discord.channelstream.api.anno.Priority;
import ru.optimus.discord.channelstream.service.DiscordService;
import ru.optimus.discord.channelstream.service.MessageService;

import java.util.*;
import java.util.function.Consumer;

@AllArgsConstructor
@Component
@Slf4j
public class FactoryLoaderModules {

    public static final Collection<Class<? extends DiscordStreamAPI>> loadModules = new ArrayList<>();
    private final WebClient discordWebClient;
    private final ApplicationContext applicationContext;
    private final MessageService messageService;

    public Collection<Class<? extends DiscordStreamAPI>> findModules() {
        return sortedByIndex(loadModules);
    }


    List<Class<? extends DiscordStreamAPI>> sortedByIndex
            (Collection<Class<? extends DiscordStreamAPI>> loadModules) {


        List<Class<? extends DiscordStreamAPI>> withoutPriority =
                loadModules.stream().filter(aClass -> !aClass.isAnnotationPresent(Priority.class))
                        .toList();

        List<Class<? extends DiscordStreamAPI>> withPriority = sortedByPresentPriority(loadModules);

        withPriority.addAll(withoutPriority);

        return withPriority;

    }

    List<Class<? extends DiscordStreamAPI>> sortedByPresentPriority(Collection<Class<? extends DiscordStreamAPI>> loadModules) {

        List<Class<? extends DiscordStreamAPI>> sorted = new ArrayList<>(loadModules.stream().filter(aClass -> aClass.isAnnotationPresent(Priority.class))
                .toList());

        sorted.sort(Comparator.comparing(aClass -> {
            Priority priority = aClass.getAnnotation(Priority.class);
            return priority.index();
        }));

        return sorted;
    }


    @SneakyThrows
    public void applyFirst(DiscordService.Message message, Consumer<DiscordService.Message> consumer) {
        for (Class<? extends DiscordStreamAPI> loadModule : findModules()) {
            Object instance = loadModule.getDeclaredConstructor(WebClient.class, MessageService.class).newInstance(discordWebClient, messageService);

            if (loadModule.isAnnotationPresent(OnlyChannel.class)) {
                OnlyChannel onlyChannel = loadModule.getAnnotation(OnlyChannel.class);

                if (!message.getChannel_id().equals(onlyChannel.channelId())) {
                    continue;
                }
            }

            DiscordStreamAPI discordStreamAPI = (ModulesStreamAPI) instance;
            boolean callback = discordStreamAPI.apply(message, message.getChannel_id(), message.getGuild_id());
            if (callback) {
                consumer.accept(message);
                break;
            }

        }
    }


    @PostConstruct
    public void init() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ModuleDiscord.class);

        for (Object bean : beans.values()) {
            Class<?> beanClass = bean.getClass();

            if (DiscordStreamAPI.class.isAssignableFrom(beanClass)) {
                @SuppressWarnings("unchecked")
                Class<? extends DiscordStreamAPI> moduleClass = (Class<? extends DiscordStreamAPI>) beanClass;
                loadModules.add(moduleClass);
            }
        }
    }

}
