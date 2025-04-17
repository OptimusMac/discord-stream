package ru.optimus.discord.channelstream.context.proxy;

public record ChannelProxy(
        String channelId,
        String guildID
) {

    public static ChannelProxy of(String channelId, String guildID){
        return new ChannelProxy(channelId, guildID);
    }

}
