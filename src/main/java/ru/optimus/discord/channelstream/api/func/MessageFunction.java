package ru.optimus.discord.channelstream.api.func;

@FunctionalInterface
public interface MessageFunction<Q, W, S> {
    boolean apply(Q q, W w, S s);
}
