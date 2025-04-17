package ru.optimus.discord.channelstream.example;

import ru.optimus.discord.channelstream.anno.UserContext;
import ru.optimus.discord.channelstream.anno.Username;

@UserContext(token = "USER_AUTHORIZE_TOKEN")

public class ExampleUser {

    @Username
    public String username() {
        return "OptimusMac";
    }
}
