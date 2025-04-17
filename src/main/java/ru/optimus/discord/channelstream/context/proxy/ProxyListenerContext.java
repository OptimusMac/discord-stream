package ru.optimus.discord.channelstream.context.proxy;

import ru.optimus.discord.channelstream.context.functional.ListenerCallback;

import java.lang.reflect.Method;

public record ProxyListenerContext(
        Method method,
        Object instance,
        ChannelProxy channelProxy,
        UserProxy userProxy
) {


    public static ProxyListenerContext of(Method method, Object instance, ChannelProxy channelProxy, UserProxy userProxy){
        return new ProxyListenerContext(method, instance, channelProxy, userProxy);

    }

    @Override
    public String toString() {
        return "ProxyListenerContext{" +
                "method=" + method +
                ", instance=" + instance +
                ", channelProxy=" + channelProxy +
                ", userProxy=" + userProxy +
                '}';
    }
}
