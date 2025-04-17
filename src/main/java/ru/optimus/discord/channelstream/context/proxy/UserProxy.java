package ru.optimus.discord.channelstream.context.proxy;

public record UserProxy(
        String username,
        String token
) {

    public static UserProxy of(String username, String token){
        return new UserProxy(username, token);
    }

}
