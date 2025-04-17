package ru.optimus.discord.channelstream.context.functional;

@FunctionalInterface
public interface FunctionalContextApplicationResolver<T> {

    T find(Class<T> tClass);
}
