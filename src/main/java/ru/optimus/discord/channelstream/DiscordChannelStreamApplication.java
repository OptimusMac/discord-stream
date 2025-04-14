package ru.optimus.discord.channelstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.repository.config.EnableReactiveCassandraRepositories;

@SpringBootApplication
public class DiscordChannelStreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscordChannelStreamApplication.class, args);
	}

}
