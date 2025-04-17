package ru.optimus.discord.channelstream.context;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;
import ru.optimus.discord.channelstream.anno.ListenerChannel;
import ru.optimus.discord.channelstream.anno.MessageHook;
import ru.optimus.discord.channelstream.anno.UserContext;
import ru.optimus.discord.channelstream.anno.Username;
import ru.optimus.discord.channelstream.context.functional.ListenerCallback;
import ru.optimus.discord.channelstream.context.proxy.ChannelProxy;
import ru.optimus.discord.channelstream.context.proxy.ProxyListenerContext;
import ru.optimus.discord.channelstream.context.proxy.UserProxy;
import ru.optimus.discord.channelstream.context.utils.ListenContextHelper;
import ru.optimus.discord.channelstream.service.DiscordService;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class FactoryListenerMessageContext {

    private final List<ProxyListenerContext> listenContexts;
    private final ApplicationContext applicationContext;
    private UserProxy userProxy;

    <T extends ProxyListenerContext> void addContext(T context) {
        this.listenContexts.add(context);
    }


    @PostConstruct
    @SneakyThrows
    public void searchListeners() {
        Collection<Object> listeners = searchBeanWithAnnotation(ListenerChannel.class, beans -> {

            beans.forEach(o -> {
                ListenerChannel listenerChannel = o.getClass().getAnnotation(ListenerChannel.class);
                if (listenerChannel.channelId().isEmpty() || listenerChannel.guildId().isEmpty()) {
                    throw new RuntimeException("ListenerChannel must have contains channelId & guildId");
                }
            });
        });

        Optional<Object> userContext = searchBeanWithAnnotation(UserContext.class, beans -> {
            if (beans.size() > 1) {
                throw new RuntimeException("User cannot be contains more 1 example!");
            } else if (beans.isEmpty()) {
                throw new RuntimeException("You need create class with annotation User and setting him!");
            }
        })
                .stream()
                .findFirst();

        if (userContext.isPresent()) {
            Object userConfiguration = userContext.get();

            Method username = findValueObject(userConfiguration, Username.class, methods -> {

                if (methods.size() > 1) {
                    throw new RuntimeException("Username annotation cannot be contains more 1 example!");
                }
            }, Method.class);

            for (Object listener : listeners) {
                Method method = findValueObject(listener, MessageHook.class, methods -> {
                    if (methods.size() > 1) {
                        throw new RuntimeException("Username annotation cannot be contains more 1 example!");
                    }

                    if (methods.isEmpty()) {
                        throw new RuntimeException("Class ListenerChannel not found @MessageHook method!");
                    }
                    Method m = methods.getFirst();

                    if (!m.getReturnType().isAssignableFrom(ListenerCallback.class)) {
                        throw new RuntimeException("@MessageHook must have contains return type ListenerCallback.class");
                    }

                }, Method.class);

                ListenerChannel listenerChannel = listener.getClass().getAnnotation(ListenerChannel.class);

                ChannelProxy channelProxy = ChannelProxy.of(listenerChannel.channelId(), listenerChannel.guildId());
                UserProxy userProxy = Optional.ofNullable(this.userProxy)
                        .orElseGet(() -> {
                            try {
                                return this.userProxy = UserProxy.of((String) username.invoke(userConfiguration), userConfiguration.getClass().getAnnotation(UserContext.class).token());
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        });
                ProxyListenerContext proxyListenerContext = ProxyListenerContext.of(method, listener, channelProxy, userProxy);

                this.addContext(proxyListenerContext);

            }
        }
        this.registerRunnableListenerContexts();
    }

    private List<ProxyListenerContext> searchBy(Predicate<ProxyListenerContext> proxyListenerContextConsumer) {
        return listenContexts
                .stream()
                .filter(proxyListenerContextConsumer)
                .toList();
    }

    @SneakyThrows
    public void tryListen(DiscordService.Message message) {


        List<ProxyListenerContext> proxyListenerContexts = searchBy(proxyListenerContext -> proxyListenerContext.channelProxy().channelId().equals(message.getChannel_id()) &&
                (proxyListenerContext.channelProxy().guildID().equals(message.getGuild_id()) || !proxyListenerContext.channelProxy().guildID().isEmpty() && message.getGuild_id() == null));

        for (ProxyListenerContext proxyListenerContext : proxyListenerContexts) {
            ListenerCallback callback = (ListenerCallback) proxyListenerContext.method().invoke(proxyListenerContext.instance());
            callback.apply(message, proxyListenerContext.userProxy());
        }
    }

    @SneakyThrows()
    <T> T findValueObject(Object o, Class<? extends Annotation> annotation, Consumer<List<Method>> exception, Class<T> returnType) {

        List<Method> methods = findMethods(o, annotation, exception);

        if (methods.isEmpty()) {
            throw new RuntimeException("Error setting UserContext");
        }

        return returnType.cast(methods.getFirst());


    }

    List<Method> findMethods(Object o, Class<? extends Annotation> annotation, Consumer<List<Method>> exception) {
        List<Method> methods = Arrays.stream(o.getClass().getDeclaredMethods())
                .peek(method -> method.setAccessible(true))
                .filter(method -> method.isAnnotationPresent(annotation))
                .toList();

        exception.accept(methods);

        return methods;
    }


    Collection<Object> searchBeanWithAnnotation(Class<? extends Annotation> annotation, Consumer<Collection<Object>> exceptionCheck) {
        Collection<Object> beans = applicationContext.getBeansWithAnnotation(annotation).values();
        exceptionCheck.accept(beans);
        return beans;

    }


    public List<ChannelProxy> registerRunnableListenerContexts() {
      return this.listenContexts.stream().map(ProxyListenerContext::channelProxy).toList();
    }


    @Bean
    public WebClient discordWebClient() {
        return WebClient.builder()
                .baseUrl("https://discord.com/api/v9")
                .defaultHeader("Authorization", userProxy.token())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

}
